package com.cleevio.vexl.module.user.service;

import com.cleevio.vexl.module.user.entity.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.Optional;

interface UserVerificationRepository extends JpaRepository<UserVerification, Long>, JpaSpecificationExecutor<UserVerification> {

    @Query("select uv from UserVerification uv where uv.expirationAt > :now AND uv.id = :id AND uv.verificationCode = :code ")
    Optional<UserVerification> findValidUserVerificationByIdAndCode(Long id, String code, ZonedDateTime now);

    @Query("select uv from UserVerification uv where uv.expirationAt > :now AND uv.id = :id")
    Optional<UserVerification> findValidUserVerificationById(Long id, ZonedDateTime now);

    @Modifying
    @Query("delete from UserVerification uv where uv.expirationAt < :now ")
    void deleteExpiredVerifications(ZonedDateTime now);

    @Query("""
            select case when (count(uv) > 0) then true else false end from UserVerification uv 
            where uv.phoneNumber = :formattedNumber 
            and uv.expirationAt > :now and uv.phoneVerified = false
            """)
    boolean doesPreviousVerificationExist(String formattedNumber, ZonedDateTime now);
}
