package com.cleevio.vexl.module.contact.event;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

public record GroupJoinedEvent(

        @NotBlank
        String groupUuid,

        @NotEmpty
        Set<@NotBlank String> membersFirebaseTokens

) {
}
