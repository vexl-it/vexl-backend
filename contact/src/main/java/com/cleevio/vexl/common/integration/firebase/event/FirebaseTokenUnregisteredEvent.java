package com.cleevio.vexl.common.integration.firebase.event;

import javax.validation.constraints.NotBlank;

public record FirebaseTokenUnregisteredEvent(

        @NotBlank
        String firebaseToken

) {
}
