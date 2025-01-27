package lissa.trading.tg.bot.mapper;

import lissa.trading.tg.bot.dto.notification.UserUpdateNotificationDto;
import lissa.trading.tg.bot.dto.user.UserPatchDto;
import lissa.trading.tg.bot.model.UserEntity;
import lissa.trading.tg.bot.payload.request.SignupRequest;
import lissa.trading.tg.bot.utils.TokenUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Optional;
import java.util.function.Consumer;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "operation", ignore = true)
    UserUpdateNotificationDto toUserUpdateNotificationDto(UserEntity user);

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", ignore = true)
    SignupRequest toSignupRequest(UserUpdateNotificationDto dto);

    UserPatchDto toUserPatchDto(UserUpdateNotificationDto dto);

    @AfterMapping
    default UserEntity updateUserFromDto(UserPatchDto userPatchDto, @MappingTarget UserEntity user) {
        mapOptionalValue(userPatchDto.getFirstName(), user::setFirstName);
        mapOptionalValue(userPatchDto.getLastName(), user::setLastName);
        mapOptionalValue(userPatchDto.getTelegramNickname(), user::setTelegramNickname);
        mapOptionalValue(userPatchDto.getTinkoffToken(), user::setTinkoffToken);
        mapOptionalValue(userPatchDto.getExternalId(), user::setExternalId);
        user.setTinkoffToken(TokenUtils.encryptToken(user.getTinkoffToken()));
        return user;
    }

    default <T> void mapOptionalValue(Optional<T> optional, Consumer<T> setter) {
        optional.ifPresent(value -> setter.accept(
                value instanceof String string && string.isEmpty() ? null : value
        ));
    }
}
