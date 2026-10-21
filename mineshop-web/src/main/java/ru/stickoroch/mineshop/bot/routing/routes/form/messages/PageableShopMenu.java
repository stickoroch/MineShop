package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardButton;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.bot.routing.routes.form.AbstractFormMessage;
import ru.stickoroch.mineshop.core.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class PageableShopMenu extends AbstractFormMessage {
    public PageableShopMenu(LocalizationService localizationService,
                            String title,
                            int page,
                            Function<Integer, List<ProductRepository.ProductPopularityView>> getProducts) {
        super(localizationService);

        this.inlineKeyboard = new BotInlineKeyboardMarkup();

        List<ProductRepository.ProductPopularityView> products = getProducts.apply(page);

        if (products == null || products.isEmpty()) {
            this.text = localizationService.getText(LocalizationKey.PRODUCTS_EMPTY);
        } else {
            this.text = title;

            for (ProductRepository.ProductPopularityView productModel : products) {
                this.inlineKeyboard.addLine(new BotInlineKeyboardButton(productModel.getTitle(), "to_product:" + productModel.getId()));
            }

            List<BotInlineKeyboardButton> pageButtons = new ArrayList<>();
            if (page > 0) {
                pageButtons.add(new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.PAGE_BACK), "page_back"));
            }

            pageButtons.add(new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.PAGE_FORWARD), "page_forward"));
            this.inlineKeyboard.addLine(
                    pageButtons
            );
        }
        this.inlineKeyboard.addLine(new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.MENU_BUTTON), "mainMenu"));
    }
}
