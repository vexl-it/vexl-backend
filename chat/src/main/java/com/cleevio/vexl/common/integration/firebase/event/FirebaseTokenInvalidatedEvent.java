package com.cleevio.vexl.common.integration.firebase.event;

import javax.validation.constraints.NotBlank;

public record FirebaseTokenInvalidatedEvent(

        @NotBlank
        String inboxPublicKey,

        @NotBlank
        String firebaseToken

) {
}
