package lissa.trading.tg.bot.details;

import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    @Cacheable(value = "users", key = "#telegramNickname")
    public CustomUserDetails loadUserByUsername(String telegramNickname) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByTelegramNickname(telegramNickname)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with telegram nickname: " + telegramNickname));

        return new CustomUserDetails(userEntity);
    }
}
