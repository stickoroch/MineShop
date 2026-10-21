package ru.stickoroch.mineshop.bot.routing.routes.callback;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.stickoroch.mineshop.bot.localization.LocalizationService;
import ru.stickoroch.mineshop.bot.routing.BotHandler;
import ru.stickoroch.mineshop.bot.routing.routes.form.BotFormSender;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.session.BotState;
import ru.stickoroch.mineshop.bot.update.BotUpdate;
import ru.stickoroch.mineshop.core.model.ProductModel;
import ru.stickoroch.mineshop.web.service.ProductService;

@Component
@AllArgsConstructor
public class ProductCallback implements BotHandler {

    private final BotSessionService sessions;
    private final BotFormSender botFormSender;
    private final ProductService productService;

    @Override
    public boolean supports(BotUpdate update, BotSessionModel session) {
        return update.isCallback() && update.callbackData().startsWith("to_product:");
    }

    @Override
    public void handle(BotUpdate update, BotSessionModel session) {
        long productId = Long.parseLong(update.callbackData().split(":")[1]);

        sessions.update(session.getUserId(), sess->{
           sess.setState(BotState.PRODUCT);
           sess.setProductId(productId);
        });

        ProductModel productModel = productService.getProduct(productId);
        botFormSender.sendProductMessage(session.getUserId(), productModel);
    }
}