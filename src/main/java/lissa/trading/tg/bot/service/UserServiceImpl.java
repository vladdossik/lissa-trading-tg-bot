package lissa.trading.tg.bot.service;

import lissa.trading.lissa.auth.lib.dto.UserInfoDto;
import lissa.trading.tg.bot.model.Role;
import lissa.trading.tg.bot.model.Roles;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.payload.response.UserRegistrationResponse;
import lissa.trading.tg.bot.repository.RoleRepository;
import lissa.trading.tg.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#signupRequest.getTelegramNickname()")
    public UserRegistrationResponse registerUser(SignupRequest signupRequest) {

        if (Boolean.TRUE.equals(userRepository.existsByTelegramNickname(signupRequest.getTelegramNickname()))) {
            return new UserRegistrationResponse("Error: Nickname already in use!", false);
        }

        if (Boolean.TRUE.equals(userRepository.existsByFirstName(signupRequest.getFirstName()))) {
            return new UserRegistrationResponse("Error: Username already taken!", false);
        }

        userRepository.save(setUserInfo(signupRequest));
        return new UserRegistrationResponse("User registered successfully!", true);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#telegramNickname")
    public Optional<UserInfoDto> getUserByTelegramNickname(String telegramNickname) {
        return userRepository.findByTelegramNickname(telegramNickname)
                .map(this::mapUserEntityToDto);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#telegramNickname")
    public void updateUserToken(String telegramNickname, String newToken) {
        UserEntity userEntity = userRepository.findByTelegramNickname(telegramNickname)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        userEntity.setTinkoffToken(newToken);
        userRepository.save(userEntity);
        log.info("Updated Tinkoff token for user: {}", telegramNickname);
    }

    private UserEntity setUserInfo(SignupRequest signupRequest) {
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName(signupRequest.getFirstName());
        userEntity.setLastName(signupRequest.getLastName());
        userEntity.setTelegramNickname(signupRequest.getTelegramNickname());
        userEntity.setTinkoffToken(signupRequest.getTinkoffToken());
        userEntity.setRoles(resolveRoles(signupRequest.getRole()));
        userEntity.setPassword(encoder.encode(signupRequest.getPassword()));
        return userEntity;
    }

    private UserInfoDto mapUserEntityToDto(UserEntity userEntity) {
        return UserInfoDto.builder()
                .externalId(userEntity.getExternalId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .telegramNickname(userEntity.getTelegramNickname())
                .tinkoffToken(userEntity.getTinkoffToken())
                .roles(userEntity.getRoles().stream()
                        .map(role -> role.getUserRole().name())
                        .toList())
                .build();
    }

    private Set<Role> resolveRoles(Set<String> strRoles) {
        Set<Role> roles = CollectionUtils.isEmpty(strRoles) ? new HashSet<>() : strRoles.stream()
                .map(this::getRoleEnum)
                .map(this::getRole)
                .collect(Collectors.toSet());
        roles.add(getRole(Roles.ROLE_USER));
        return roles;
    }

    private Role getRole(Roles role) {
        return roleRepository.findByUserRole(role)
                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
    }

    private Roles getRoleEnum(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> Roles.ROLE_ADMIN;
            case "vip" -> Roles.ROLE_VIP;
            default -> Roles.ROLE_USER;
        };
    }
}