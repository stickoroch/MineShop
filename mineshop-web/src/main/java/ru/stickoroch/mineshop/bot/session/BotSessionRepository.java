package ru.stickoroch.mineshop.bot.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BotSessionRepository extends JpaRepository<BotSessionModel, Long> {
}
