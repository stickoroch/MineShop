package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.web.service.ProductService;

public class ProductsShopMenu extends PageableShopMenu {

    public ProductsShopMenu(LocalizationService localizationService, int page,
                            ProductService productService) {
        super(localizationService,
                localizationService.getText(LocalizationKey.SHOP_PRODUCTS_MENU),
                localizationService.getText(LocalizationKey.PRODUCTS_EMPTY),
                page,
                productService.getProductsPage(page));
    }
}
