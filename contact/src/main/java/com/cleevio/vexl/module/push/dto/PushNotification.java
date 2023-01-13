package com.cleevio.vexl.module.push.dto;

import com.cleevio.vexl.common.annotation.NullOrNotBlank;
import com.cleevio.vexl.module.push.constant.NotificationType;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

public record PushNotification(

        @NotNull
        NotificationType type,

        @Nullable
        @NullOrNotBlank
        String groupUuid,

        @Nullable
        @NullOrNotBlank
        String newUserPublicKey,

        @NotEmpty
        Set<@NotBlank String> membersFirebaseTokens,

        Set<@NotBlank String> secondDegreeMembersFirebaseTokens

) {
}
