package ru.stickoroch.mineshop.bot.routing.routes.form;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import ru.stickoroch.mineshop.bot.routing.routes.form.messages.*;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.BotContext;
import ru.stickoroch.mineshop.bot.pipeline.BotRegistry;
import ru.stickoroch.mineshop.core.PlatformUserId;
import ru.stickoroch.mineshop.core.model.ProductModel;
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

    public void sendProductMessage(PlatformUserId userId, ProductModel productModel) {
        BotContext context = getContextForUser(userId);

        new ProductMessage(localizationService, productModel).sendMenu(userId.getPlatformId(), context);
    }
}
