package ru.stickoroch.mineshop.bot.session;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.stickoroch.mineshop.core.PlatformUserId;

import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Transactional
public class BotSessionService {

    private final BotSessionRepository repository;

    public BotSessionModel get(PlatformUserId platformAccountId) {

        return repository.findById(platformAccountId)
                .orElseGet(() -> create(platformAccountId));
    }

    public BotSessionModel setState(
            PlatformUserId platformAccountId,
            BotState state
    ) {
        BotSessionModel session = get(platformAccountId);

        session.setState(state);

        return session;
    }

    public BotSessionModel save(BotSessionModel session) {
        return repository.save(session);
    }

    public void clear(PlatformUserId platformAccountId) {
        repository.deleteById(platformAccountId);
    }

    @Transactional
    public BotSessionModel update(
            PlatformUserId platformAccountId,
            Consumer<BotSessionModel> modifier
    ) {
        BotSessionModel session = get(platformAccountId);

        modifier.accept(session);

        return repository.save(session);
    }

    private BotSessionModel create(PlatformUserId platformAccountId) {

        BotSessionModel session = new BotSessionModel();

        session.setUserId(platformAccountId);
        session.setState(BotState.EMPTY);

        return repository.save(session);
    }
}