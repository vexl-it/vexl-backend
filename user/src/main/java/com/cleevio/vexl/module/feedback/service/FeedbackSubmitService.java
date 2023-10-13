package com.cleevio.vexl.module.feedback.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.feedback.dto.FeedbackSubmitRequest;
import com.cleevio.vexl.module.feedback.entity.FeedbackSubmit;
import com.cleevio.vexl.module.user.constant.UserAdvisoryLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;

@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class FeedbackSubmitService {

    private FeedbackSubmitRepository feedbackSubmitRepository;
    private final AdvisoryLockService advisoryLockService;

    @Transactional
    public void submitFeedback(final FeedbackSubmitRequest request) {
        advisoryLockService.lock(
                ModuleLockNamespace.FEEDBACK_SUBMIT,
                UserAdvisoryLock.SUBMITTING_FEEDBACK.name(),
                request.formId()
        );

        final FeedbackSubmit feedbackSubmit = feedbackSubmitRepository.findByFormId(request.formId())
                .orElse(FeedbackSubmit.builder()
                        .formId(request.formId())
                        .build());

        feedbackSubmit.setLastUpdate(LocalDate.now());
        feedbackSubmit.setType(request.type());

        if(request.stars() != null)
            feedbackSubmit.setStars(request.stars());
        if(request.objections() != null)
            feedbackSubmit.setObjections(request.objections());
        if(request.textComment() != null)
            feedbackSubmit.setTextComment(request.textComment());

        log.info("Feedback with formId {} is submitted.", request.formId());

        feedbackSubmitRepository.save(feedbackSubmit);
    }
}
