package com.cleevio.vexl.module.feedback.service;

import com.cleevio.vexl.module.feedback.entity.FeedbackSubmit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

interface FeedbackSubmitRepository extends JpaRepository<FeedbackSubmit, Long>, JpaSpecificationExecutor<FeedbackSubmit> {
    Optional<FeedbackSubmit> findByFormId(String formId);
}
