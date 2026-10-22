package ru.stickoroch.mineshop.bot.routing.routes.form;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.stickoroch.mineshop.bot.routing.routes.form.messages.*;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.BotContext;
import ru.stickoroch.mineshop.bot.pipeline.BotRegistry;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.core.model.OrderModel;
import ru.stickoroch.mineshop.core.model.ProductModel;
import ru.stickoroch.mineshop.web.media.CoverFileStorage;
import ru.stickoroch.mineshop.web.service.OrderService;
import ru.stickoroch.mineshop.web.service.ProductService;
import ru.stickoroch.mineshop.web.service.UserService;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BotFormSender {

    BotRegistry botRegistry;
    LocalizationService localizationService;
    UserService userService;
    ProductService productService;
    OrderService orderService;
    CoverFileStorage coverFileStorage;

    private BotContext getContextForUser(PlatformUserId user){
        return botRegistry.getBotContext(user.getPlatform());
    }

    public void sendMainMenu(PlatformUserId userId){
        BotContext context = getContextForUser(userId);

        new MainMenu(localizationService, userService.getUser(userId)).sendMenu(userId.getPlatformId(), context);
    }

    public void sendShopMenu(PlatformUserId user){
        BotContext context = getContextForUser(user);

        new ShopMenu(localizationService).sendMenu(user.getPlatformId(), context);
    }

    public void sendPopularShopMenu(PlatformUserId userId, int page) {
        BotContext context = getContextForUser(userId);

        new PopularShopMenu(localizationService, page, productService).sendMenu(userId.getPlatformId(), context);
    }

    public void sendProductsShopMenu(PlatformUserId userId, int page){
        BotContext context = getContextForUser(userId);

        new ProductsShopMenu(localizationService, page, productService).sendMenu(userId.getPlatformId(), context);
    }

    public void sendShopSearchMessage(PlatformUserId userId){
        BotContext context = getContextForUser(userId);

        new ShopSearchMessage(localizationService).sendMenu(userId.getPlatformId(), context);
    }

    public void sendShopSearchResults(PlatformUserId userId, String query, int page){
        BotContext context = getContextForUser(userId);

        new ShopSearchResultsMenu(localizationService, query, page, productService)
                .sendMenu(userId.getPlatformId(), context);
    }

    public void sendProductMessage(PlatformUserId userId, ProductModel productModel) {
        BotContext context = getContextForUser(userId);

        new ProductMessage(
                localizationService,
                productModel,
                coverFileStorage.getCoverAsFileOrNull(productModel)
        ).sendMenu(userId.getPlatformId(), context);
    }

    public void sendOrderMessage(PlatformUserId userId, OrderModel orderModel) {
        BotContext context = getContextForUser(userId);

        new OrderMessage(
                localizationService,
                orderModel,
                coverFileStorage.getCoverAsFileOrNull(orderModel.getProduct())
        ).sendMenu(userId.getPlatformId(), context);
    }

    public void sendHistoryMenu(PlatformUserId userId, int page) {
        BotContext context = getContextForUser(userId);

        Page<OrderModel> orders = orderService.getHistoryPage(userId, page);

        new OrdersMenu(
                localizationService,
                localizationService.getText(LocalizationKey.HISTORY_TITLE),
                localizationService.getText(LocalizationKey.HISTORY_EMPTY),
                page,
                orders
        ).sendMenu(userId.getPlatformId(), context);
    }

    public void sendActiveOrdersMenu(PlatformUserId userId, int page) {
        BotContext context = getContextForUser(userId);

        Page<OrderModel> orders = orderService.getActiveOrdersPage(userId, page);

        new OrdersMenu(
                localizationService,
                localizationService.getText(LocalizationKey.ACTIVE_ORDERS_TITLE),
                localizationService.getText(LocalizationKey.ACTIVE_ORDERS_EMPTY),
                page,
                orders
        ).sendMenu(userId.getPlatformId(), context);
    }
}
