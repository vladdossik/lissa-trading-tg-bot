package lissa.trading.tg.bot.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignupRequest {
    private UUID externalId;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @NotNull
    private String telegramNickname;
    @NotNull
    private String tinkoffToken;
    private Set<String> role;
    @Size(min = 3, message = "Password must be at least 3 characters")
    private String password;
}
