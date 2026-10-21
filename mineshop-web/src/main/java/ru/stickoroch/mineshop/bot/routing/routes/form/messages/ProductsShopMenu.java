package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.core.repository.ProductRepository;
import ru.stickoroch.mineshop.web.service.ProductService;

public class ProductsShopMenu extends PageableShopMenu{

    public ProductsShopMenu(LocalizationService localizationService, int page,
                           ProductService productService) {
        super(localizationService, localizationService.getText(LocalizationKey.POPULAR_SHOP_MENU), page,
                (p)-> productService.getProducts(p).stream().map(x-> (ProductRepository.ProductPopularityView)x).toList());
    }
}
