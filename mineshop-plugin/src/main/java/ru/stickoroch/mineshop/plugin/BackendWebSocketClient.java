package ru.stickoroch.mineshop.plugin;

import com.google.gson.Gson;
import lombok.Setter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class BackendWebSocketClient {

    private final MineShopPlugin plugin;
    private final PluginSettings settings;
    private final Gson gson = new Gson();
    private final HttpClient httpClient;
    private final ScheduledExecutorService executor;

    private final AtomicBoolean connecting = new AtomicBoolean();
    private final AtomicBoolean reconnectScheduled = new AtomicBoolean();
    private final StringBuilder textBuffer = new StringBuilder();
    private final Queue<PluginMessage> pendingResults = new ConcurrentLinkedQueue<>();

    private volatile boolean running;
    private volatile WebSocket webSocket;
    @Setter
    private volatile DeliveryExecutor deliveryExecutor;

    public BackendWebSocketClient(MineShopPlugin plugin, PluginSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "MineShop-WebSocket");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        running = true;
        connect();
    }

    public void stop() {
        running = false;
        executor.shutdownNow();

        WebSocket socket = webSocket;
        webSocket = null;

        if (socket != null) {
            try {
                socket.sendClose(WebSocket.NORMAL_CLOSURE, "Plugin disabled").join();
            } catch (Exception ignored) {
            }
        }
    }

    public void playerOnline(String playerName) {
        send(PluginMessage.playerOnline(playerName));
    }

    public void sendResult(java.util.UUID orderId, boolean success, String error) {
        PluginMessage message = PluginMessage.deliveryResult(orderId, success, error);
        if (!send(message) && pendingResults.size() < 1000) {
            pendingResults.add(message);
        }
    }

    public boolean isConnected() {
        WebSocket socket = webSocket;
        return socket != null && !socket.isOutputClosed();
    }

    private void connect() {
        if (!running || !connecting.compareAndSet(false, true)) {
            return;
        }

        plugin.getLogger().info("Connecting to backend " + settings.backendUrl() + " ...");

        try {
            httpClient.newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .buildAsync(URI.create(settings.backendUrl()), new Listener())
                    .whenComplete((socket, error) -> {
                        connecting.set(false);

                        if (!running) {
                            if (socket != null) {
                                socket.abort();
                            }
                            return;
                        }

                        if (error != null || socket == null) {
                            plugin.getLogger().warning("Backend connection failed: " + error);
                            scheduleReconnect();
                            return;
                        }

                        webSocket = socket;
                        plugin.getLogger().info("Connected to backend");
                        send(PluginMessage.hello(settings.token()));
                    });
        } catch (Exception e) {
            connecting.set(false);
            plugin.getLogger().warning("Backend connection failed: " + e.getMessage());
            scheduleReconnect();
        }
    }

    private void scheduleReconnect() {
        if (!running || !reconnectScheduled.compareAndSet(false, true)) {
            return;
        }

        executor.schedule(() -> {
            reconnectScheduled.set(false);
            connect();
        }, settings.reconnectSeconds(), TimeUnit.SECONDS);
    }

    private boolean send(PluginMessage message) {
        WebSocket socket = webSocket;

        if (socket == null || socket.isOutputClosed()) {
            return false;
        }

        try {
            socket.sendText(message.toJson(gson), true)
                    .exceptionally(error -> {
                        plugin.getLogger().warning("WebSocket send failed: " + error.getMessage());
                        return null;
                    });
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("WebSocket send failed: " + e.getMessage());
            return false;
        }
    }

    private void flushPendingResults() {
        PluginMessage message;

        while ((message = pendingResults.poll()) != null) {
            send(message);
        }
    }

    private void handleMessage(String json) {
        PluginMessage message;

        try {
            message = gson.fromJson(json, PluginMessage.class);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid backend message: " + e.getMessage());
            return;
        }

        if (message == null || message.type() == null) {
            return;
        }

        switch (message.type()) {
            case "hello_ok" -> {
                plugin.getLogger().info("Backend handshake complete");
                flushPendingResults();
                send(PluginMessage.sync());
            }
            case "pending" -> {
                if (message.orders() != null && deliveryExecutor != null) {
                    message.orders().forEach(deliveryExecutor::execute);
                }
            }
            case "deliver" -> {
                if (deliveryExecutor != null) {
                    deliveryExecutor.execute(new DeliveryTask(
                            message.orderId(),
                            message.player(),
                            message.command(),
                            message.amount() == null ? 1 : message.amount()
                    ));
                }
            }
            case "ping" -> send(PluginMessage.pong());
            case "error" -> plugin.getLogger().warning("Backend error: " + message.error());
            default -> plugin.getLogger().warning("Unknown backend message: " + message.type());
        }
    }

    private class Listener implements WebSocket.Listener {

        @Override
        public CompletionStage<?> onText(WebSocket socket, CharSequence data, boolean last) {
            synchronized (textBuffer) {
                textBuffer.append(data);

                if (last) {
                    String json = textBuffer.toString();
                    textBuffer.setLength(0);

                    try {
                        handleMessage(json);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Could not handle backend message: " + e.getMessage());
                    }
                }
            }

            socket.request(1);
            return null;
        }

        @Override
        public CompletionStage<?> onClose(WebSocket socket, int statusCode, String reason) {
            plugin.getLogger().warning("Backend connection closed: " + statusCode + " " + reason);
            handleDisconnect(socket);
            return null;
        }

        @Override
        public void onError(WebSocket socket, Throwable error) {
            plugin.getLogger().warning("Backend WebSocket error: " + error.getMessage());
            socket.abort();
            handleDisconnect(socket);
        }
    }

    private void handleDisconnect(WebSocket socket) {
        if (webSocket == socket) {
            webSocket = null;
        }

        scheduleReconnect();
    }
}
