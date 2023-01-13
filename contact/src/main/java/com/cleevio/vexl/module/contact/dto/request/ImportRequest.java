package com.cleevio.vexl.module.contact.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import java.util.List;

public record ImportRequest(

        @Schema(required = true, description = "Contacts in String. Must be hashed with HMAC-SHA256!!!.")
        List<@NotBlank String> contacts

) {
}
