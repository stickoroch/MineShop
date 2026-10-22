## Установка

1. Положите jar в `plugins/`.
2. Запустите сервер — создастся `plugins/MineShop/config.yml`.
3. Укажите адрес бэкенда и общий секрет:

```yaml
backend-url: "ws://127.0.0.1:8080/ws/plugin"
token: "change-me"       # тот же, что app.plugin.token на бэкенде
reconnect-seconds: 5
```

4. На бэкенде задайте тот же секрет:

```yaml
app:
  plugin:
    token: ${PLUGIN_TOKEN:change-me}
```

5. Перезапустите сервер или выполните `/reload confirm` (полный перезапуск надёжнее).

## Как это работает

- Плагин подключается к `ws://<backend>/ws/plugin` и отправляет `hello`.
- После `hello_ok` отправляет `sync` и получает все невыданные заказы.
- При оплате бэкенд сам присылает `deliver`; плагин выполняет команду и отвечает
  `delivery_result` (`success=true/false`).
- Если игрок офлайн, плагин отвечает `PLAYER_OFFLINE`, заказ остаётся в очереди и
  будет выдан при входе игрока (`player_online`) или следующей синхронизации.
- Команда берётся из товара и поддерживает `%player%` и `%amount%`.
- Выдача выполняется на главном потоке сервера через `Bukkit.dispatchCommand`.

## Протокол

```text
plugin  -> backend : hello {token}
backend -> plugin  : hello_ok
plugin  -> backend : sync
backend -> plugin  : pending {orders:[{orderId, player, command, amount}]}
plugin  -> backend : player_online {player}
backend -> plugin  : pending {orders:[...]}
backend -> plugin  : deliver {orderId, player, command, amount}
plugin  -> backend : delivery_result {orderId, success, error}
backend -> plugin  : ping
plugin  -> backend : pong
```

Очередь хранится на бэкенде в таблице `order_deliveries`, поэтому выключение
Minecraft-сервера не приводит к потере оплаченного заказа.
