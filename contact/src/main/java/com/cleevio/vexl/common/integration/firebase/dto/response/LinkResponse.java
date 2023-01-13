package com.cleevio.vexl.common.integration.firebase.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LinkResponse(

        @JsonProperty(value = "shortLink")
        String link,

        @JsonProperty(value = "previewLink")
        String preview

) {
}
