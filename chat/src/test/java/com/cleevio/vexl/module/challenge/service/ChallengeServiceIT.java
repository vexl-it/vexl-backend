package com.cleevio.vexl.module.challenge.service;

import com.cleevio.vexl.common.IntegrationTest;
import it.vexl.common.crypto.CryptoLibrary;
import com.cleevio.vexl.module.challenge.dto.request.CreateBatchChallengeRequest;
import com.cleevio.vexl.module.challenge.entity.Challenge;
import com.cleevio.vexl.module.challenge.exception.ChallengeExpiredException;
import com.cleevio.vexl.module.challenge.service.query.VerifySignedChallengeQuery;
import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import com.cleevio.vexl.utils.CryptographyTestKeysUtil;
import com.cleevio.vexl.utils.RequestCreatorTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChallengeServiceIT {

    private final ChallengeService challengeService;

    @Autowired
    public ChallengeServiceIT(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    private static final String PUBLIC_KEY_USER_A = CryptographyTestKeysUtil.PUBLIC_KEY_USER_A;
    private static final String PUBLIC_KEY_USER_B = CryptographyTestKeysUtil.PUBLIC_KEY_USER_B;
    private static final String PRIVATE_KEY_USER_A = CryptographyTestKeysUtil.PRIVATE_KEY_USER_A;
    private static final String PRIVATE_KEY_USER_B = CryptographyTestKeysUtil.PRIVATE_KEY_USER_B;

    @Test
    public void testSigningChallenge_shouldSignAndBeSuccessfullyVerified() {
        final String challenge = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String signature = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_A, challenge);
        final boolean signedChallengeValid = this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(challenge, signature)), 1);

        assertThat(signedChallengeValid).isTrue();
    }

    @Test
    void testSigningOldChallenge_shouldNotBeValid() {
        final String oldChallenge = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String newChallenge = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String signature = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_A, oldChallenge);
        final boolean signedChallengeValid = this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(newChallenge, signature)), 1);

        assertThat(signedChallengeValid).isFalse();
    }

    @Test
    void testVerifyAlreadyUsedChallenge_shouldReturnsChallengeExpiredException() {
        final String oldChallenge = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String signature = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_A, oldChallenge);

        //First use
        this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(oldChallenge, signature)), 1);

        //Second use
        assertThrows(
                ChallengeExpiredException.class,
                () -> this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(oldChallenge, signature)), 1)
        );
    }

    @Test
    void testSigningNewChallenge_shouldBeValid() {
        final String oldChallenge = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String newChallenge1 = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String newChallenge2 = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String signature = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_A, newChallenge2);
        final boolean signedChallengeValid = this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(newChallenge2, signature)), 1);

        assertThat(signedChallengeValid).isTrue();
    }

    @Test
    void testSigningChallengeForDifferentPublicKey_shouldReturnException() {
        final String challenge = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PRIVATE_KEY_USER_B));
        final String signature = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_A, challenge);

        assertThrows(
                ChallengeExpiredException.class,
                () -> this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(challenge, signature)), 1)
        );
    }

    @Test
    void testCreatingBatchChallengesSignThemAndVerifyThem_shouldBeCreatedSignedAndVerified() {
        final List<Challenge> batchChallenge = this.challengeService.createBatchChallenge(new CreateBatchChallengeRequest(Set.of(PUBLIC_KEY_USER_A, PUBLIC_KEY_USER_B)));

        assertThat(batchChallenge).hasSize(2);

        final String challengeForA = batchChallenge.stream().filter(c -> c.getPublicKey().equals(PUBLIC_KEY_USER_A)).toList().get(0).getChallenge();
        final String challengeForB = batchChallenge.stream().filter(c -> c.getPublicKey().equals(PUBLIC_KEY_USER_B)).toList().get(0).getChallenge();

        final String signatureA = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_A, challengeForA);
        final String signatureB = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_B, challengeForB);

        final boolean resultA = this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(challengeForA, signatureA)), 1);
        final boolean resultB = this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_B, new SignedChallenge(challengeForB, signatureB)), 1);

        assertThat(resultA).isTrue();
        assertThat(resultB).isTrue();
    }


    @Test
    void testSigningChallenge_v2_shouldSignAndBeSuccessfullyVerified() {
        final String challenge = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String signature = CryptoLibrary.instance.ecdsaSignV2(PRIVATE_KEY_USER_A, challenge);
        final boolean signedChallengeValid = this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(challenge, signature)), 2);

        assertThat(signedChallengeValid).isTrue();
    }

    @Test
    void testSigningOldChallenge_v2_shouldNotBeValid() {
        final String oldChallenge = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String newChallenge = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String signature = CryptoLibrary.instance.ecdsaSignV2(PRIVATE_KEY_USER_A, oldChallenge);
        final boolean signedChallengeValid = this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(newChallenge, signature)), 2);

        assertThat(signedChallengeValid).isFalse();
    }

    @Test
    void testVerifyAlreadyUsedChallenge_v2_shouldReturnsChallengeExpiredException() {
        final String oldChallenge = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String signature = CryptoLibrary.instance.ecdsaSignV2(PRIVATE_KEY_USER_A, oldChallenge);

        //First use
        this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(oldChallenge, signature)), 2);

        //Second use
        assertThrows(
                ChallengeExpiredException.class,
                () -> this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(oldChallenge, signature)), 2)
        );
    }

    @Test
    void testSigningNewChallenge_v2_shouldBeValid() {
        final String oldChallenge = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String newChallenge1 = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String newChallenge2 = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PUBLIC_KEY_USER_A));
        final String signature = CryptoLibrary.instance.ecdsaSignV2(PRIVATE_KEY_USER_A, newChallenge2);
        final boolean signedChallengeValid = this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(newChallenge2, signature)), 2);

        assertThat(signedChallengeValid).isTrue();
    }

    @Test
    void testSigningChallengeForDifferentPublicKey_v2_shouldReturnException() {
        final String challenge = this.challengeService.createChallenge(RequestCreatorTestUtil.createChallengeRequest(PRIVATE_KEY_USER_B));
        final String signature = CryptoLibrary.instance.ecdsaSignV2(PRIVATE_KEY_USER_A, challenge);

        assertThrows(
                ChallengeExpiredException.class,
                () -> this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(challenge, signature)), 2)
        );
    }

    @Test
    void testCreatingBatchChallengesSignThemAndVerifyThem_v2_shouldBeCreatedSignedAndVerified() {
        final List<Challenge> batchChallenge = this.challengeService.createBatchChallenge(new CreateBatchChallengeRequest(Set.of(PUBLIC_KEY_USER_A, PUBLIC_KEY_USER_B)));

        assertThat(batchChallenge).hasSize(2);

        final String challengeForA = batchChallenge.stream().filter(c -> c.getPublicKey().equals(PUBLIC_KEY_USER_A)).toList().get(0).getChallenge();
        final String challengeForB = batchChallenge.stream().filter(c -> c.getPublicKey().equals(PUBLIC_KEY_USER_B)).toList().get(0).getChallenge();

        final String signatureA = CryptoLibrary.instance.ecdsaSignV2(PRIVATE_KEY_USER_A, challengeForA);
        final String signatureB = CryptoLibrary.instance.ecdsaSignV2(PRIVATE_KEY_USER_B, challengeForB);

        final boolean resultA = this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_A, new SignedChallenge(challengeForA, signatureA)), 2);
        final boolean resultB = this.challengeService.isSignedChallengeValid(new VerifySignedChallengeQuery(PUBLIC_KEY_USER_B, new SignedChallenge(challengeForB, signatureB)), 2);

        assertThat(resultA).isTrue();
        assertThat(resultB).isTrue();
    }
}
