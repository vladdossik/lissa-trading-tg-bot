package lissa.trading.tg.bot.repository;

import lissa.trading.tg.bot.model.Role;
import lissa.trading.tg.bot.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByUserRole(Roles role);
}