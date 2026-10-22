package ru.stickoroch.mineshop.bot.routing.routes.callback;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.BotContext;
import ru.stickoroch.mineshop.bot.routing.routes.form.BotFormSender;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;
import ru.stickoroch.mineshop.bot.update.UpdateType;
import ru.stickoroch.mineshop.core.PlatformType;
import ru.stickoroch.mineshop.core.PlatformUserId;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShopSearchFlowTest {

    private final BotContext context = mock(BotContext.class);
    private final BotSessionService sessions = mock(BotSessionService.class);
    private final BotFormSender sender = mock(BotFormSender.class);
    private final LocalizationService localizationService = mock(LocalizationService.class);

    private final PlatformUserId userId = new PlatformUserId(1L, PlatformType.TELEGRAM);
    private BotSessionModel session;

    @BeforeEach
    void setUp() {
        session = new BotSessionModel();
        session.setUserId(userId);
        session.setState(BotState.SHOP);

        when(localizationService.getText(any(LocalizationKey.class))).thenReturn("text");

        doAnswer(invocation -> {
            Consumer<BotSessionModel> modifier = invocation.getArgument(1);
            modifier.accept(session);
            return session;
        }).when(sessions).update(eq(userId), any());
    }

    @Test
    void textQueryIsSavedAndSearchResultsAreShown() {
        ShopSearchCallback callback = new ShopSearchCallback(sessions, sender, localizationService);

        callback.handle(textUpdate("  Алмазный   меч "), session);

        assertThat(session.getState()).isEqualTo(BotState.SHOP_SEARCH);
        assertThat(session.getSearchQuery()).isEqualTo("Алмазный меч");
        assertThat(session.getPage()).isZero();
        verify(sender).sendShopSearchResults(userId, "Алмазный меч", 0);
    }

    @Test
    void tooShortQueryDoesNotStartSearch() {
        ShopSearchCallback callback = new ShopSearchCallback(sessions, sender, localizationService);

        callback.handle(textUpdate("м"), session);

        verify(context).sendMessage(1L, "text");
        verify(sender).sendShopSearchMessage(userId);
        verify(sender, never()).sendShopSearchResults(any(), any(), anyInt());
        assertThat(session.getSearchQuery()).isNull();
    }

    @Test
    void searchCallbackOpensPromptAndClearsPreviousQuery() {
        ShopSearchCallback callback = new ShopSearchCallback(sessions, sender, localizationService);
        session.setSearchQuery("старый запрос");
        session.setPage(3);

        callback.handle(callbackUpdate("shop_search"), session);

        assertThat(session.getState()).isEqualTo(BotState.SHOP_SEARCH);
        assertThat(session.getSearchQuery()).isNull();
        assertThat(session.getPage()).isZero();
        verify(sender).sendShopSearchMessage(userId);
    }

    @Test
    void paginationKeepsSearchQuery() {
        PageableCallback callback = new PageableCallback(sessions, sender);
        session.setState(BotState.SHOP_SEARCH);
        session.setSearchQuery("меч");
        session.setPage(0);

        callback.handle(callbackUpdate("page_forward"), session);

        assertThat(session.getPage()).isEqualTo(1);
        verify(sender).sendShopSearchResults(userId, "меч", 1);
    }

    @Test
    void paginationDoesNotGoBeforeFirstPage() {
        PageableCallback callback = new PageableCallback(sessions, sender);
        session.setState(BotState.SHOP_POPULAR);
        session.setPage(0);

        callback.handle(callbackUpdate("page_back"), session);

        assertThat(session.getPage()).isZero();
        verify(sender).sendPopularShopMenu(userId, 0);
    }

    private BotUpdate textUpdate(String text) {
        return new BotUpdate(
                PlatformType.TELEGRAM, 1L, 1L, 10, "user",
                UpdateType.TEXT, text, null, null, context
        );
    }

    private BotUpdate callbackUpdate(String data) {
        return new BotUpdate(
                PlatformType.TELEGRAM, 1L, 1L, 10, "user",
                UpdateType.CALLBACK, null, "callback-id", data, context
        );
    }
}
