package com.cleevio.vexl.module.contact.dto.request;

import javax.validation.constraints.NotBlank;
import java.util.Set;

public record CommonContactsRequest(

    Set<@NotBlank String> publicKeys

) {
}
