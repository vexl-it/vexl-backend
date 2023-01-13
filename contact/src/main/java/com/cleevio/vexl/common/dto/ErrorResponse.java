package com.cleevio.vexl.common.dto;


import java.util.Collection;

public record ErrorResponse(

        Collection<String> message,

        String code

) {
}
