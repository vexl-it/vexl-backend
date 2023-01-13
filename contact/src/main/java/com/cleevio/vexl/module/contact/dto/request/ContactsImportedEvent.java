package com.cleevio.vexl.module.contact.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

public record ContactsImportedEvent(

        @NotEmpty
        Set<@NotBlank String> firebaseTokens,

        Set<@NotBlank String> firebaseTokensSecondDegree,

        @NotBlank
        String newUserPublicKey
) {
}
