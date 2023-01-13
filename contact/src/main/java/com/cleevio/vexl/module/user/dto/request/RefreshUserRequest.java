package com.cleevio.vexl.module.user.dto.request;

import javax.validation.constraints.NotNull;

public record RefreshUserRequest(

        @NotNull
        Boolean offersAlive

) {
}
