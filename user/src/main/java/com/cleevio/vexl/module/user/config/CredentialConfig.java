package com.cleevio.vexl.module.user.config;

import com.cleevio.vexl.common.annotation.NullOrNotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "credential")
public record CredentialConfig(

        @Nullable
        List<@NotBlank String> phones,

        @Nullable
        @NullOrNotBlank
        String code,

        @NotNull
        Boolean active,

        @NotNull
        Boolean dummyCodeForAll
) {
}
