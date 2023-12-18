package com.cleevio.vexl.module.feedback.dto;

import com.cleevio.vexl.module.user.serializer.TrimStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotBlank;

public record FeedbackSubmitRequest(
        @NotBlank
        @Schema(required = true, description = "Form UUID")
        String formId,

        @NotBlank
        @Schema(required = true, description = "Type of feedback. Possible values: [\"create\" | \"trade\"]")
        String type,

        @Nullable
        @Schema(description = "Number of stars. Will be merged with other feedbacks with same formId.")
        Integer stars,

        @Nullable
        @Schema(description = "Objections divided by comma")
        String objections,

        @Nullable
        @Schema(description = "Country code")
        String countryCode,

        @Nullable
        @Schema(description = "Text comment")
        @JsonDeserialize(using = TrimStringDeserializer.class)
        String textComment
){}
