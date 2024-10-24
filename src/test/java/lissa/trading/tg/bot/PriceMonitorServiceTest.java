package lissa.trading.tg.bot;

import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.service.PriceMonitorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class PriceMonitorServiceTest extends BaseTest {

    private PriceMonitorService priceMonitorService;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        priceMonitorService = new PriceMonitorService(userService, userProcessingService, notificationProperties);
    }

    @Test
    void testCheckPriceChanges() {
        UserEntity user1 = new UserEntity();
        user1.setId(1L);
        user1.setTelegramChatId(12345L);
        user1.setTelegramNickname("user1");

        UserEntity user2 = new UserEntity();
        user2.setId(2L);
        user2.setTelegramChatId(null);

        List<UserEntity> users = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(users);
        when(userProcessingService.processUserAsync(ArgumentMatchers.anyLong())).thenReturn(CompletableFuture.completedFuture(null));

        priceMonitorService.checkPriceChanges();

        verify(userService, times(1)).getAllUsers();
        verify(userProcessingService, times(1)).processUserAsync(1L);
        verify(userProcessingService, never()).processUserAsync(2L);
    }
}