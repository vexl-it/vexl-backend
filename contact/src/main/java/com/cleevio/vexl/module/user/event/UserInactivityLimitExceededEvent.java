package com.cleevio.vexl.module.user.event;

import com.cleevio.vexl.module.user.dto.InactivityNotificationDto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public record UserInactivityLimitExceededEvent(

        @NotEmpty
        List<@NotNull InactivityNotificationDto> inactivityNotificationDtos

) {
}
