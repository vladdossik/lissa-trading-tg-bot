package lissa.trading.tg.bot.repository;

import lissa.trading.tg.bot.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByTelegramNickname(String telegramNickname);

    Boolean existsByFirstName(String firstName);

    Boolean existsByTelegramNickname(String telegramNickname);
}