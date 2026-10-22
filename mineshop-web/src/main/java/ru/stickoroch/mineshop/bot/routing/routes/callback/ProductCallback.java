package ru.stickoroch.mineshop.bot.routing.routes.callback;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.localization.LocalizationKey;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.routing.BotHandler;
import ru.stickoroch.mineshop.bot.routing.routes.form.BotFormSender;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;
import ru.stickoroch.mineshop.core.model.ProductModel;
import ru.stickoroch.mineshop.web.service.ProductService;

import java.util.Optional;

@Component
@AllArgsConstructor
public class ProductCallback implements BotHandler {

    private static final String PRODUCT_CALLBACK_PREFIX = "to_product:";

    private final BotSessionService sessions;
    private final BotFormSender botFormSender;
    private final ProductService productService;
    private final LocalizationService localizationService;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback()
                && update.callbackData() != null
                && update.callbackData().startsWith(PRODUCT_CALLBACK_PREFIX);
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        Long productId = parseProductId(update.callbackData());

        Optional<ProductModel> productModel = productId == null
                ? Optional.empty()
                : productService.findActiveProduct(productId);

        if (productModel.isEmpty()) {
            update.context().sendMessage(
                    update.chatId(),
                    localizationService.getText(LocalizationKey.PRODUCT_NOT_FOUND)
            );
            return;
        }

        sessions.update(session.getUserId(), sess -> {
            sess.setState(BotState.PRODUCT);
            sess.setProductId(productModel.get().getId());
        });

        botFormSender.sendProductMessage(session.getUserId(), productModel.get());
    }

    private Long parseProductId(String callbackData) {
        try {
            return Long.parseLong(callbackData.substring(PRODUCT_CALLBACK_PREFIX.length()));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
