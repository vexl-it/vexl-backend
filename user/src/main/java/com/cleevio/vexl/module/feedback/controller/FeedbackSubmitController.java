package com.cleevio.vexl.module.feedback.controller;

import com.cleevio.vexl.module.feedback.dto.FeedbackSubmitRequest;
import com.cleevio.vexl.module.feedback.service.FeedbackSubmitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Feedback")
@RestController
@RequestMapping(value = "/api/v1/feedback")
@RequiredArgsConstructor
public class FeedbackSubmitController {
    private final FeedbackSubmitService feedbackSubmitService;

    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Submit feedback", description = "Upsert feedback for formId")
    public void submitFeedback(@RequestBody FeedbackSubmitRequest request){
        feedbackSubmitService.submitFeedback(request);
    }
}
