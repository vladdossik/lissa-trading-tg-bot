package lissa.trading.tg.bot.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;
import java.util.UUID;

@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPatchDto {
    private UUID externalId;
    private String firstName;
    private String lastName;
    private String telegramNickname;
    private String tinkoffToken;

    public Optional<String> getFirstName() {
        return Optional.ofNullable(firstName);
    }

    public Optional<String> getLastName() {
        return Optional.ofNullable(lastName);
    }

    public Optional<String> getTelegramNickname() {
        return Optional.ofNullable(telegramNickname);
    }

    public Optional<String> getTinkoffToken() {
        return Optional.ofNullable(tinkoffToken);
    }

    public Optional<UUID> getExternalId() { return Optional.ofNullable(externalId); }
}