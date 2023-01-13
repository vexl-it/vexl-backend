package com.cleevio.vexl.module.challenge.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.cryptolib.CryptoLibrary;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.challenge.config.ChallengeConfig;
import com.cleevio.vexl.module.challenge.constant.ChallengeAdvisoryLock;
import com.cleevio.vexl.module.challenge.dto.request.CreateBatchChallengeRequest;
import com.cleevio.vexl.module.challenge.dto.request.CreateChallengeRequest;
import com.cleevio.vexl.module.challenge.entity.Challenge;
import com.cleevio.vexl.module.challenge.exception.ChallengeCreateException;
import com.cleevio.vexl.module.challenge.exception.ChallengeExpiredException;
import com.cleevio.vexl.module.challenge.exception.InvalidChallengeSignature;
import com.cleevio.vexl.module.challenge.service.query.VerifySignedChallengeQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
@Validated
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final AdvisoryLockService advisoryLockService;
    private final ChallengeConfig config;
    private static final String SHA256 = "SHA-256";

    @Transactional
    public String createChallenge(@Valid CreateChallengeRequest request) {
        advisoryLockService.lock(
                ModuleLockNamespace.CHALLENGE,
                ChallengeAdvisoryLock.CREATE_CHALLENGE.name(),
                request.publicKey()
        );

        log.info("Creating new challenge for public key [{}]", request.publicKey());

        final String challenge = generateRandomChallenge();
        createNewChallenge(challenge, request.publicKey());
        return challenge;
    }

    @Transactional
    public List<Challenge> createBatchChallenge(@Valid CreateBatchChallengeRequest request) {
        final List<Challenge> challengeList = new ArrayList<>();
        request.publicKeys().forEach(key -> challengeList.add(createActiveChallengeEntity(generateRandomChallenge(), key)));

        return this.challengeRepository.saveAll(challengeList);
    }

    @Transactional
    public boolean isSignedChallengeValid(@Valid VerifySignedChallengeQuery query, final int cryptoVersion) {
        Challenge challenge = this.challengeRepository.findByChallengeAndPublicKey(
                query.signedChallenge().challenge(),
                query.publicKey()
        ).orElseThrow(ChallengeExpiredException::new);

        invalidateChallenge(challenge);

        if (ZonedDateTime.now().isAfter(challenge.getCreatedAt().plusMinutes(config.expiration()))) {
            log.info("Challenge [{}] is expired. Returning exception.", challenge);
            throw new ChallengeExpiredException();
        }

        if (cryptoVersion >= 2) {
            return CryptoLibrary.instance.ecdsaVerifyV2(
                    challenge.getPublicKey(),
                    challenge.getChallenge(),
                    query.signedChallenge().signature()
            );
        }
        return CryptoLibrary.instance.ecdsaVerifyV1(
                challenge.getPublicKey(),
                challenge.getChallenge(),
                query.signedChallenge().signature()
        );
    }

    @Transactional
    public void removeInvalidAndExpiredChallenges() {
        advisoryLockService.lock(
                ModuleLockNamespace.CHALLENGE,
                ChallengeAdvisoryLock.REMOVE_CHALLENGE_TASK.name()
        );

        this.challengeRepository.removeInvalidAndExpiredChallenges(ZonedDateTime.now().minusMinutes(config.expiration()));
    }

    @Transactional
    public void verifySignedChallenge(@Valid VerifySignedChallengeQuery query, final int cryptoVersion) {
        advisoryLockService.lock(
                ModuleLockNamespace.CHALLENGE,
                ChallengeAdvisoryLock.VERIFYING_CHALLENGE.name(),
                query.publicKey()
        );

        if (!isSignedChallengeValid(query, cryptoVersion)) {
            throw new InvalidChallengeSignature();
        }
    }

    @Transactional
    public void verifySignedChallengeForBatch(List<@Valid VerifySignedChallengeQuery> query, final int cryptoVersion) {
        query.forEach(verifySignedChallengeQuery -> verifySignedChallenge(verifySignedChallengeQuery, cryptoVersion));
    }

    private void invalidateChallenge(Challenge challenge) {
        challenge.setValid(false);
        this.challengeRepository.save(challenge);
    }

    private void createNewChallenge(String challenge, String publicKey) {
        this.challengeRepository.save(
                createActiveChallengeEntity(challenge, publicKey)
        );
    }

    private Challenge createActiveChallengeEntity(String challenge, String publicKey) {
        return Challenge.builder()
                .challenge(challenge)
                .publicKey(publicKey)
                .valid(true)
                .build();
    }

    private String generateRandomChallenge() {
        try {
            byte[] bytes = generateCodeVerifier().getBytes(StandardCharsets.US_ASCII);
            MessageDigest messageDigest = MessageDigest.getInstance(SHA256);
            messageDigest.update(bytes, 0, bytes.length);
            byte[] digest = messageDigest.digest();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error while getting instance of algorithm: " + e.getMessage(), e);
            throw new ChallengeCreateException();
        } catch (Exception e) {
            log.error("Error while creating the challenge: " + e.getMessage(), e);
            throw new ChallengeCreateException();
        }
    }

    private String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }
}
