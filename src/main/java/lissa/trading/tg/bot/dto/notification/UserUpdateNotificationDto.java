package lissa.trading.tg.bot.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateNotificationDto {
    private UUID externalId;
    private String firstName;
    private String lastName;
    private String telegramNickname;
    private String tinkoffToken;
    private OperationEnum operation;
}
