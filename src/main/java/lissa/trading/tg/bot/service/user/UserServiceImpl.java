package lissa.trading.tg.bot.service.user;

import lissa.trading.lissa.auth.lib.dto.UserInfoDto;
import lissa.trading.lissa.auth.lib.security.EncryptionService;
import lissa.trading.tg.bot.details.CustomUserDetails;
import lissa.trading.tg.bot.exception.ErrorGettingApplicationContextException;
import lissa.trading.tg.bot.model.Role;
import lissa.trading.tg.bot.model.Roles;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.payload.response.UserRegistrationResponse;
import lissa.trading.tg.bot.repository.RoleRepository;
import lissa.trading.tg.bot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public UserRegistrationResponse registerUser(SignupRequest signupRequest) {

        if (Boolean.TRUE.equals(userRepository.existsByTelegramNickname(signupRequest.getTelegramNickname()))) {
            return new UserRegistrationResponse("Error: Nickname already in use!");
        }

        if (Boolean.TRUE.equals(userRepository.existsByFirstName(signupRequest.getFirstName()))) {
            return new UserRegistrationResponse("Error: Username already taken!");
        }

        userRepository.save(setUserInfo(signupRequest));

        return new UserRegistrationResponse("User registered successfully!");
    }

    @Override
    public UserInfoDto getUserInfoFromContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ErrorGettingApplicationContextException("User is not authenticated");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return UserInfoDto.builder()
                .externalId(userDetails.getExternalId())
                .firstName(userDetails.getFirstName())
                .lastName(userDetails.getLastName())
                .telegramNickname(userDetails.getTelegramNickname())
                .tinkoffToken(EncryptionService.encrypt(userDetails.getTinkoffToken()))
                .roles(userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .build();
    }

    @Override
    @Transactional
    public UserInfoDto getUserByTelegramNickname(String telegramNickname) {
        log.info("Telegram nickname: {}", telegramNickname);

        UserEntity userEntity = userRepository.findByTelegramNickname(telegramNickname)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        log.info("User: {}", userEntity);

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

    @Override
    public boolean userExistsByTelegramNickname(String telegramNickname) {
        return userRepository.existsByTelegramNickname(telegramNickname);
    }

    @Override
    public boolean userExistsByFirstName(String firstName) {
        return userRepository.existsByFirstName(firstName);
    }

    @Override
    @Transactional
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
        userEntity.setTinkoffToken(EncryptionService.encrypt(signupRequest.getTinkoffToken()));
        userEntity.setRoles(resolveRoles(signupRequest.getRole()));
        userEntity.setPassword(encoder.encode(signupRequest.getPassword()));
        return userEntity;
    }

    private Set<Role> resolveRoles(Set<String> strRoles) {
        Set<Role> roles = new HashSet<>();
        if (strRoles != null) {
            strRoles.forEach(role -> roles.add(getRole(getRoleEnum(role))));
        }
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