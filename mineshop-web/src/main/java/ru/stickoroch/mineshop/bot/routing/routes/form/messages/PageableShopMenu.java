package ru.stickoroch.mineshop.bot.routing.routes.form.messages;

import org.springframework.data.domain.Page;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardButton;
import ru.stickoroch.mineshop.bot.pipeline.callback.BotInlineKeyboardMarkup;
import ru.stickoroch.mineshop.bot.routing.routes.form.AbstractFormMessage;
import ru.stickoroch.mineshop.core.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

public class PageableShopMenu extends AbstractFormMessage {

    private static final int MAX_BUTTON_TEXT_LENGTH = 48;

    public PageableShopMenu(LocalizationService localizationService,
                            String title,
                            String emptyText,
                            int page,
                            Page<? extends ProductRepository.ProductPopularityView> productsPage) {
        super(localizationService);

        this.inlineKeyboard = new BotInlineKeyboardMarkup();

        List<? extends ProductRepository.ProductPopularityView> products = productsPage.getContent();

        if (products.isEmpty()) {
            this.text = emptyText;
        } else {
            this.text = title;

            for (ProductRepository.ProductPopularityView product : products) {
                this.inlineKeyboard.addLine(new BotInlineKeyboardButton(
                        shorten(product.getTitle()),
                        "to_product:" + product.getId()
                ));
            }
        }

        List<BotInlineKeyboardButton> pageButtons = new ArrayList<>();
        if (page > 0) {
            pageButtons.add(new BotInlineKeyboardButton(
                    localizationService.getText(LocalizationKey.PAGE_BACK),
                    "page_back"
            ));
        }
        if (productsPage.hasNext()) {
            pageButtons.add(new BotInlineKeyboardButton(
                    localizationService.getText(LocalizationKey.PAGE_FORWARD),
                    "page_forward"
            ));
        }
        if (!pageButtons.isEmpty()) {
            this.inlineKeyboard.addLine(pageButtons);
        }

        this.inlineKeyboard.addLine(
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.SHOP_BUTTON), "shop"),
                new BotInlineKeyboardButton(localizationService.getText(LocalizationKey.MENU_BUTTON), "mainMenu")
        );
    }

    private String shorten(String title) {
        if (title == null) {
            return "";
        }

        if (title.length() <= MAX_BUTTON_TEXT_LENGTH) {
            return title;
        }

        return title.substring(0, MAX_BUTTON_TEXT_LENGTH - 1) + "…";
    }
}
