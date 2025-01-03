package lissa.trading.tg.bot.service.dataInitializer;

import lissa.trading.tg.bot.model.Role;
import lissa.trading.tg.bot.model.Roles;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.repository.RoleRepository;
import lissa.trading.tg.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.EasyRandom;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Order(1)
public class UserInitializerService implements DataInitializerService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EasyRandom easyRandom;

    @Override
    public void createData() {
        if (userRepository.count() == 0) {
            for (int i = 0; i < 10; i++) {
                UserEntity user = easyRandom.nextObject(UserEntity.class);
                if (user.getRoles().isEmpty()) {
                    user.setRoles(createRoles());
                }
                userRepository.save(user);
            }
            log.info("Users successfully initialized");
        } else {
            log.info("Users already initialized");
        }
    }

    private Set<Role> createRoles() {
        Roles randomRole = Roles.values()[new Random().nextInt(3)];
        Role role = new Role();
        role.setUserRole(randomRole);
        role.setId(roleRepository.findByUserRole(randomRole).get().getId());
        return Set.of(role);
    }
}