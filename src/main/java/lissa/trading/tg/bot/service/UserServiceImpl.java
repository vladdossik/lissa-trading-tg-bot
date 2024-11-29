package lissa.trading.tg.bot.service;

import lissa.trading.lissa.auth.lib.dto.UserInfoDto;
import lissa.trading.lissa.auth.lib.security.EncryptionService;
import lissa.trading.tg.bot.model.FavouriteStock;
import lissa.trading.tg.bot.model.Role;
import lissa.trading.tg.bot.model.Roles;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.payload.response.UserRegistrationResponse;
import lissa.trading.tg.bot.repository.FavouriteStockRepository;
import lissa.trading.tg.bot.repository.RoleRepository;
import lissa.trading.tg.bot.repository.UserRepository;
import lissa.trading.tg.bot.tinkoff.dto.Stock;
import lissa.trading.tg.bot.tinkoff.dto.account.FavouriteStocksDto;
import lissa.trading.tg.bot.tinkoff.dto.account.TinkoffTokenDto;
import lissa.trading.tg.bot.tinkoff.feign.TinkoffAccountClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final TinkoffAccountClient tinkoffAccountClient;
    private final FavouriteStockRepository favouriteStockRepository;

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#signupRequest.telegramNickname")
    public UserRegistrationResponse registerUser(SignupRequest signupRequest) {
        if (isTelegramNicknameInUse(signupRequest.getTelegramNickname())) {
            return new UserRegistrationResponse("Error: Nickname already in use!", false);
        }

        UserEntity newUser = createUserEntity(signupRequest);
        setTinkoffTokenForClient(newUser.getTinkoffToken());

        try {
            updateUserFromTinkoffData(newUser);
            userRepository.save(newUser);
            log.info("User {} registered successfully", newUser.getTelegramNickname());
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

    // --- Обновление данных пользователя ---
    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#telegramNickname")
    public void updateUserToken(String telegramNickname, String newToken) {
        userRepository.findByTelegramNickname(telegramNickname).ifPresent(user -> {
            user.setTinkoffToken(newToken);
            userRepository.save(user);
            updateUserFromTinkoffData(user);
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
    public void updateUserFromTinkoffData(UserEntity user) {
        log.info("Updating user {} with Tinkoff data", user.getTelegramNickname());
        userRepository.save(user);
        updateUserFavouriteStocks(user);
    }

    private void updateUserFavouriteStocks(UserEntity user) {
        try {
            FavouriteStocksDto favouriteStocksDto = tinkoffAccountClient.getFavouriteStocks();
            log.info("Received {} favourite stocks from Tinkoff for user {}", favouriteStocksDto.getFavouriteStocks().size(), user.getTelegramNickname());
            Set<FavouriteStock> updatedFavouriteStocks = fetchFavouriteStocksFromDto(favouriteStocksDto, user);

            syncFavouriteStocks(user, updatedFavouriteStocks);
            userRepository.save(user);
            log.info("User {} updated with {} favourite stocks", user.getTelegramNickname(), updatedFavouriteStocks.size());
        } catch (Exception e) {
            log.error("Failed to update user {} from Tinkoff data: {}", user.getTelegramNickname(), e.getMessage(), e);
        }
    }

    private Set<FavouriteStock> fetchFavouriteStocksFromDto(FavouriteStocksDto favouriteStocksDto, UserEntity user) {
        return favouriteStocksDto.getFavouriteStocks().stream()
                .map(ticker -> fetchFavouriteStock(ticker, user))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private FavouriteStock fetchFavouriteStock(String ticker, UserEntity user) {
        try {
            Stock stock = tinkoffAccountClient.getStockByTicker(ticker);
            return new FavouriteStock(
                    null,
                    user,
                    ticker,
                    stock.getFigi(),
                    stock.getTicker(),
                    stock.getName(),
                    stock.getType(),
                    stock.getCurrency()
            );
        } catch (Exception e) {
            log.warn("Failed to get stock for ticker {}: {}", ticker, e.getMessage());
            return null;
        }
    }

    private void syncFavouriteStocks(UserEntity user, Set<FavouriteStock> updatedFavouriteStocks) {
        Set<FavouriteStock> existingFavouriteStocks = user.getFavouriteStocks();

        existingFavouriteStocks.removeIf(fs -> updatedFavouriteStocks.stream()
                .noneMatch(ufs -> ufs.getFigi().equals(fs.getFigi())));

        for (FavouriteStock updatedStock : updatedFavouriteStocks) {
            existingFavouriteStocks.stream()
                    .filter(fs -> fs.getFigi().equals(updatedStock.getFigi()))
                    .findFirst()
                    .ifPresentOrElse(
                            existingStock -> updateExistingStock(existingStock, updatedStock),
                            () -> existingFavouriteStocks.add(updatedStock)
                    );
        }
    }

    private void updateExistingStock(FavouriteStock existingStock, FavouriteStock updatedStock) {
        existingStock.setTicker(updatedStock.getTicker());
        existingStock.setServiceTicker(updatedStock.getServiceTicker());
        existingStock.setName(updatedStock.getName());
        existingStock.setInstrumentType(updatedStock.getInstrumentType());
        existingStock.setCurrency(updatedStock.getCurrency());
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
        return userEntity;
    }

    private void setTinkoffTokenForClient(String encryptedToken) {
        String decryptedToken = EncryptionService.decrypt(encryptedToken);
        tinkoffAccountClient.setTinkoffToken(new TinkoffTokenDto(decryptedToken));
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
