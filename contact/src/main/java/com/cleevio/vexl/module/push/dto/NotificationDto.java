package com.cleevio.vexl.module.push.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

public record NotificationDto(

        @NotBlank
        String groupUuid,

        @NotEmpty
        Set<@NotBlank String> membersFirebaseTokens

) {
}
