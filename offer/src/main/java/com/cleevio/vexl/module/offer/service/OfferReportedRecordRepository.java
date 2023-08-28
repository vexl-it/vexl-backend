package com.cleevio.vexl.module.offer.service;

import com.cleevio.vexl.module.offer.entity.OfferReportedRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;

import java.time.Instant;

interface OfferReportedRecordRepository extends JpaRepository<OfferReportedRecord, JpaSpecificationExecutor<OfferReportedRecord>> {

    int countByUserPublicKeyEqualsAndReportedAtAfter(String userPublicKey, Instant reportedAt);

    @Modifying
    void deleteByReportedAtBefore(Instant reportedAt);
}
