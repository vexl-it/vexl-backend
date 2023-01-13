package com.cleevio.vexl.module.user.event;

import com.cleevio.vexl.module.user.entity.User;

import javax.validation.constraints.NotNull;

public record UserRemovedEvent(

        @NotNull
        User user

) {
}
