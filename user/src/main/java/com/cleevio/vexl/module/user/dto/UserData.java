package com.cleevio.vexl.module.user.dto;

import javax.validation.constraints.NotBlank;

public record UserData(

        @NotBlank
        String publicKey,

        @NotBlank
        String phoneNumber,

        @NotBlank
        String challenge,

        @NotBlank
        String signature,

        int countryPrefix

) {
}
