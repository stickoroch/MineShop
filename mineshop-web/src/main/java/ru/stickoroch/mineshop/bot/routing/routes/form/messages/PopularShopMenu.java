package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.web.service.ProductService;

public class PopularShopMenu extends PageableShopMenu {
    public PopularShopMenu(LocalizationService localizationService, int page,
                           ProductService productService) {
        super(localizationService, localizationService.getText(LocalizationKey.POPULAR_SHOP_MENU), page, productService::getPopularProducts);
    }
}
