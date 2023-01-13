package com.cleevio.vexl.common.integration.firebase.event;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

public record InactivityNotificationSuccessfullySentEvent(

        @NotEmpty
        List<@NotBlank String> firebaseTokens

) {
}
