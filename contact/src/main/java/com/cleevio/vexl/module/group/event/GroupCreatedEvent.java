package com.cleevio.vexl.module.group.event;

import com.cleevio.vexl.module.user.entity.User;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record GroupCreatedEvent(

        @NotBlank
        String groupUuid,

        @NotNull
        User user

) {
}
