package lissa.trading.tg.bot.service;

import lissa.trading.lissa.auth.lib.dto.UserInfoDto;
import lissa.trading.tg.bot.dto.notification.OperationEnum;
import lissa.trading.tg.bot.dto.tinkoff.stock.TickersDto;
import lissa.trading.tg.bot.dto.user.UserPatchDto;
import lissa.trading.tg.bot.mapper.FavouriteStockMapper;
import lissa.trading.tg.bot.service.consumer.NotificationContext;
import lissa.trading.tg.bot.exception.UserNotFoundException;
import lissa.trading.tg.bot.feign.UserServiceClient;
import lissa.trading.tg.bot.mapper.UserMapper;
import lissa.trading.tg.bot.model.FavouriteStock;
import lissa.trading.tg.bot.model.Role;
import lissa.trading.tg.bot.model.Roles;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.payload.response.UserRegistrationResponse;
import lissa.trading.tg.bot.repository.RoleRepository;
import lissa.trading.tg.bot.repository.UserRepository;
import lissa.trading.tg.bot.service.publisher.UserUpdatesPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final FavouriteStockMapper favouriteStockMapper;
    private final UserUpdatesPublisher userUpdatesPublisher;
    private final UserServiceClient userServiceClient;
    private final UserMapper userMapper;
    private final NotificationContext notificationContext;

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#signupRequest.telegramNickname")
    public UserRegistrationResponse registerUser(SignupRequest signupRequest) {
        if (isTelegramNicknameInUse(signupRequest.getTelegramNickname())) {
            return new UserRegistrationResponse("Error: Nickname already in use!", false);
        }
        UserEntity newUser = createUserEntity(signupRequest);
        try {
            userRepository.save(newUser);
            log.info("User {} registered successfully", newUser.getTelegramNickname());
            userUpdatesPublisher.publishUserUpdateNotification(newUser, OperationEnum.REGISTER);
            notificationContext.clear();
            return new UserRegistrationResponse("User registered successfully!", true);
        } catch (Exception e) {
            log.error("Failed to register user {}: {}", newUser.getTelegramNickname(), e.getMessage(), e);
            return new UserRegistrationResponse("Registration failed due to an error.", false);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#telegramNickname")
    public Optional<UserInfoDto> getUserByTelegramNickname(String telegramNickname) {
        return userRepository.findByTelegramNickname(telegramNickname)
                .map(this::mapUserEntityToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserEntity> getUserByChatId(Long chatId) {
        return userRepository.findByTelegramChatId(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#telegramNickname")
    public void updateUserToken(String telegramNickname, String newToken) {
        userRepository.findByTelegramNickname(telegramNickname).ifPresent(user -> {
            user.setTinkoffToken(newToken);
            userRepository.save(user);
            userUpdatesPublisher.publishUserUpdateNotification(user, OperationEnum.UPDATE);
            notificationContext.clear();
            log.info("Updated Tinkoff token for user: {}", telegramNickname);
        });
    }

    @Override
    @Transactional
    public void updateUserChatId(String telegramNickname, Long chatId) {
        log.info("Updating chatId {} for user: {}", chatId, telegramNickname);
        userRepository.findByTelegramNickname(telegramNickname).ifPresent(user -> {
            if (user.getTelegramChatId() == null) {
                user.setTelegramChatId(chatId);
                userRepository.save(user);
                log.info("ChatId set for user: {}", telegramNickname);
            } else {
                log.debug("ChatId already set for user: {}", telegramNickname);
            }
        });
    }

    @Override
    @Transactional
    public void deleteUser(UUID externalId) {
        userRepository.delete(findByExternalId(externalId));
    }

    @Override
    @Transactional
    public void updateUserInformation(UUID externalId, UserPatchDto userPatchDto) {
        UserEntity user = findByExternalId(externalId);
        userMapper.updateUserFromDto(userPatchDto, user);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void setUpdatedFavouriteStocksToUser(UUID externalId, List<FavouriteStock> favouriteStocks) {
        UserEntity user = findByExternalId(externalId);
        try {
            List<FavouriteStock> existingStocks = new ArrayList<>(user.getFavouriteStocks());

            Map<String, FavouriteStock> existingStocksMap = existingStocks.stream()
                    .collect(Collectors.toMap(FavouriteStock::getTicker, stock -> stock));

            List<FavouriteStock> updatedStocks = favouriteStocks.stream()
                    .map(newStock -> {
                        FavouriteStock existingStock = existingStocksMap.get(newStock.getTicker());
                        if (existingStock != null) {
                            favouriteStockMapper.updateFavoriteStocksFromFavoriteStock(existingStock, newStock);
                            return existingStock;
                        } else {
                            newStock.setUser(user);
                            return newStock;
                        }
                    })
                    .toList();

            user.clearAndSetFavouriteStocks(updatedStocks);

            userRepository.save(user);

            log.info("User {} updated with {} favourite stocks",
                     user.getTelegramNickname(), favouriteStocks.size());
        } catch (Exception e) {
            log.error("Failed to update user {} with new favourite stocks: {}",
                      user.getTelegramNickname(), e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteUserFavouriteStocks(String telegramNickname, List<String> tickers) {
        UserEntity user = findByTelegramNickname(telegramNickname);
        user.getFavouriteStocks().removeIf(ticker -> tickers.contains(ticker.getTicker()));
        userRepository.save(user);
        userUpdatesPublisher.publishUserFavoriteStocksUpdateNotification(user);
        notificationContext.clear();
    }

    @Override
    public void addUserFavouriteStocks(String telegramNickname, List<String> tickers) {
        UserEntity user = findByTelegramNickname(telegramNickname);
        userServiceClient.updateUserFavoriteStocks(
                user.getExternalId(), new TickersDto(tickers));
    }

    private boolean isTelegramNicknameInUse(String telegramNickname) {
        return Boolean.TRUE.equals(userRepository.existsByTelegramNickname(telegramNickname));
    }

    private UserEntity createUserEntity(SignupRequest signupRequest) {
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName(signupRequest.getFirstName());
        userEntity.setLastName(signupRequest.getLastName());
        userEntity.setTelegramNickname(signupRequest.getTelegramNickname());
        userEntity.setTinkoffToken(signupRequest.getTinkoffToken());
        userEntity.setRoles(resolveRoles(signupRequest.getRole()));
        userEntity.setPassword(encoder.encode(signupRequest.getPassword()));
        UUID externalId = signupRequest.getExternalId();
        if (externalId == null || externalId.toString().isEmpty()) {
            externalId = UUID.randomUUID();
        }
        userEntity.setExternalId(externalId);
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

    private UserEntity findByExternalId(UUID externalId) {
        return userRepository.findByExternalId(externalId)
                .orElseThrow(() -> new UserNotFoundException("User with external id " + externalId + " not found"));
    }

    private UserEntity findByTelegramNickname(String telegramNickname) {
        return userRepository.findByTelegramNickname(telegramNickname)
                .orElseThrow(() -> new UserNotFoundException("User with telegramNickname "
                                                                     + telegramNickname + " not found"));
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
