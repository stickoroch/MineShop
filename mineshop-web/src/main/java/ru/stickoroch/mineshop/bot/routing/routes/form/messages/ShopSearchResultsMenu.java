package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardButton;
import ru.stickoroch.mineshop.web.service.ProductService;

public class ShopSearchResultsMenu extends PageableShopMenu {

    public ShopSearchResultsMenu(LocalizationService localizationService,
                                 String query,
                                 int page,
                                 ProductService productService) {
        super(localizationService,
                localizationService.getText(
                        LocalizationKey.SHOP_SEARCH_RESULTS,
                        localizationService.escapeMarkdown(query)
                ),
                localizationService.getText(
                        LocalizationKey.SHOP_SEARCH_EMPTY,
                        localizationService.escapeMarkdown(query)
                ),
                page,
                productService.searchProducts(query, page));

        this.inlineKeyboard.addLine(new BotInlineKeyboardButton(
                localizationService.getText(LocalizationKey.SHOP_SEARCH_BUTTON),
                "shop_search"
        ));
    }
}
