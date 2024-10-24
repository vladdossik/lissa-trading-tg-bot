package lissa.trading.tg.bot;

import lissa.trading.lissa.auth.lib.security.EncryptionService;
import lissa.trading.tg.bot.model.Role;
import lissa.trading.tg.bot.model.Roles;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.payload.response.UserRegistrationResponse;
import lissa.trading.tg.bot.service.UserServiceImpl;
import lissa.trading.tg.bot.tinkoff.dto.Currency;
import lissa.trading.tg.bot.tinkoff.dto.Stock;
import lissa.trading.tg.bot.tinkoff.dto.account.FavouriteStocksDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceImplTest extends BaseTest {

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        userService = new UserServiceImpl(
                userRepository,
                roleRepository,
                passwordEncoder,
                tinkoffAccountClient,
                favouriteStockRepository
        );
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setFirstName("John");
        signupRequest.setLastName("Doe");
        signupRequest.setTelegramNickname("john_doe");
        signupRequest.setTinkoffToken("encryptedToken");
        signupRequest.setPassword("password123");
        signupRequest.setRole(Set.of("user"));

        when(userRepository.existsByTelegramNickname("john_doe")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        Role userRole = new Role();
        userRole.setUserRole(Roles.ROLE_USER);
        when(roleRepository.findByUserRole(Roles.ROLE_USER)).thenReturn(Optional.of(userRole));

        FavouriteStocksDto favouriteStocksDto = new FavouriteStocksDto(List.of("TICKER1"));
        when(tinkoffAccountClient.getFavouriteStocks()).thenReturn(favouriteStocksDto);

        Stock stock = new Stock("TICKER1", "figi1", "Name1", "share", Currency.RUB, "someSource");
        when(tinkoffAccountClient.getStockByTicker("TICKER1")).thenReturn(stock);

        try (MockedStatic<EncryptionService> mockedEncryptionService = mockStatic(EncryptionService.class)) {
            mockedEncryptionService.when(() -> EncryptionService.decrypt("encryptedToken")).thenReturn("decryptedToken");

            // Act
            UserRegistrationResponse response = userService.registerUser(signupRequest);

            // Assert
            assertTrue(response.isSuccess());
            assertEquals("User registered successfully!", response.getMessage());
            // Adjust the verify statement to account for multiple invocations
            verify(userRepository, atLeastOnce()).save(any(UserEntity.class));
        }
    }

    @Test
    void testRegisterUser_TelegramNicknameInUse() {
        // Arrange
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setTelegramNickname("john_doe");

        when(userRepository.existsByTelegramNickname("john_doe")).thenReturn(true);

        // Act
        UserRegistrationResponse response = userService.registerUser(signupRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Error: Nickname already in use!", response.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}