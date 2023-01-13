package com.cleevio.vexl.module.challenge.service;

import com.cleevio.vexl.module.challenge.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.Optional;

interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    Optional<Challenge> findByChallengeAndPublicKey(String challenge, String publicKey);

    @Modifying
    @Query("delete from Challenge c where c.valid = false or c.createdAt < :expiration")
    void removeInvalidAndExpiredChallenges(ZonedDateTime expiration);
}