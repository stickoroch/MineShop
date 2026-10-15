package ru.stickoroch.mineshop.bot.pipeline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stickoroch.mineshop.bot.routing.BotRouter;
import ru.stickoroch.mineshop.bot.session.BotSessionModel;
import ru.stickoroch.mineshop.bot.session.BotSessionService;
import ru.stickoroch.mineshop.bot.update.BotUpdate;
import ru.stickoroch.mineshop.web.service.UserService;

@Service
@RequiredArgsConstructor
@Transactional
public class BotPipeline {

    private final UserService userService;
    private final BotSessionService sessions;
    private final BotRouter router;

    public void process(BotUpdate update) {
        userService.createIfNotExists(
                update.platformUserId(),
                update.username()
        );

        BotSessionModel session =
                sessions.get(update.platformUserId());

        router.route(
                update,
                session
        );
    }
}