package lissa.trading.tg.bot.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lissa.trading.tg.bot.model.UserEntity;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final UUID externalId;
    private final String firstName;
    private final String lastName;
    private final String telegramNickname;
    private final String tinkoffToken;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(UserEntity userEntity) {
        this.id = userEntity.getId();
        this.externalId = userEntity.getExternalId();
        this.firstName = userEntity.getFirstName();
        this.lastName = userEntity.getLastName();
        this.telegramNickname = userEntity.getTelegramNickname();
        this.tinkoffToken = userEntity.getTinkoffToken();
        this.password = userEntity.getPassword();
        this.authorities = userEntity.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getUserRole().name())).toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return telegramNickname;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}