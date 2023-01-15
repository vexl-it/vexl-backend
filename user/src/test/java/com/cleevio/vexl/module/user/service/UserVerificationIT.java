package com.cleevio.vexl.module.user.service;

import com.cleevio.vexl.common.IntegrationTest;
import it.vexl.common.crypto.CryptoLibrary;
import com.cleevio.vexl.module.user.dto.request.CodeConfirmRequest;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.user.entity.UserVerification;
import com.cleevio.vexl.module.user.exception.InvalidPhoneNumberException;
import com.cleevio.vexl.module.user.exception.PreviousVerificationCodeNotExpiredException;
import com.cleevio.vexl.module.user.exception.UserAlreadyExistsException;
import com.cleevio.vexl.module.user.exception.VerificationExpiredException;
import com.cleevio.vexl.util.CreateRequestUtilTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserVerificationIT {

    private static final String PHONE_NUMBER = "+420731958956";
    private static final String PHONE_NUMBER_INVALID = "invalid_format";
    private static final String DEV_PASSWORD = "test";
    private static final String DEV_CODE = "111111";
    private static final String PUBLIC_KEY = "dummy_public_key";
    private final UserVerificationService userVerificationService;
    private final UserVerificationRepository userVerificationRepository;

    @Autowired
    public UserVerificationIT(UserVerificationService userVerificationService, UserVerificationRepository userVerificationRepository) {
        this.userVerificationService = userVerificationService;
        this.userVerificationRepository = userVerificationRepository;
    }

    @Test
    void testRequestConfirmPhone_shouldBeCreated() {
        userVerificationService.requestConfirmPhone(CreateRequestUtilTest.createPhoneConfirmRequest(PHONE_NUMBER));
        final String hashedPhoneNumber = CryptoLibrary.instance.hmacDigest(DEV_PASSWORD, PHONE_NUMBER);

        final UserVerification userVerification = userVerificationRepository.findAll().get(0);
        assertThat(userVerification.getPhoneNumber()).isEqualTo(hashedPhoneNumber);
        assertThat(userVerification.getVerificationCode()).isEqualTo(DEV_CODE);
    }

    @Test
    void testRequestConfirmPhoneTwice_shouldReturnsPreviousVerificationCodeNotExpiredException() {
        userVerificationService.requestConfirmPhone(CreateRequestUtilTest.createPhoneConfirmRequest(PHONE_NUMBER));
        final String hashedPhoneNumber = CryptoLibrary.instance.hmacDigest(DEV_PASSWORD, PHONE_NUMBER);

        final UserVerification userVerification = userVerificationRepository.findAll().get(0);
        assertThat(userVerification.getPhoneNumber()).isEqualTo(hashedPhoneNumber);
        assertThat(userVerification.getVerificationCode()).isEqualTo(DEV_CODE);

        assertThrows(
                PreviousVerificationCodeNotExpiredException.class,
                () -> userVerificationService.requestConfirmPhone(CreateRequestUtilTest.createPhoneConfirmRequest(PHONE_NUMBER))
        );
    }

    @Test
    void testRequestConfirmPhone_invalidInput_shouldReturnException() {
        assertThrows(
                InvalidPhoneNumberException.class,
                () -> userVerificationService.requestConfirmPhone(CreateRequestUtilTest.createPhoneConfirmRequest(PHONE_NUMBER_INVALID))
        );
    }

    @Test
    void testRequestConfirmCodeAndGenerateCodeChallenge_shouldGenerateChallenge() {
        final UserVerification userVerification = userVerificationService.requestConfirmPhone(CreateRequestUtilTest.createPhoneConfirmRequest(PHONE_NUMBER));

        final CodeConfirmRequest codeConfirmRequest = CreateRequestUtilTest.createCodeConfirmRequest(userVerification.getId(), userVerification.getVerificationCode(), PUBLIC_KEY);

        userVerificationService.requestConfirmCodeAndGenerateCodeChallenge(codeConfirmRequest);

        final UserVerification result = userVerificationRepository.findAll().get(0);
        assertThat(result.getId()).isEqualTo(result.getId());
        assertThat(result.getChallenge()).isNotNull();
        assertThat(result.getChallenge()).isNotEmpty();
        assertThat(result.isPhoneVerified()).isTrue();

        final User user = result.getUser();
        assertThat(user.getPublicKey()).isEqualTo(PUBLIC_KEY);
    }

    @Test
    void testRequestConfirmCodeAndGenerateCodeChallenge_creatingUserWithSamePublicKey_notPossible_shouldReturnsUserAlreadyExistsException() {
        final UserVerification userVerification = userVerificationService.requestConfirmPhone(CreateRequestUtilTest.createPhoneConfirmRequest(PHONE_NUMBER));

        final CodeConfirmRequest codeConfirmRequest = CreateRequestUtilTest.createCodeConfirmRequest(userVerification.getId(), userVerification.getVerificationCode(), PUBLIC_KEY);

        userVerificationService.requestConfirmCodeAndGenerateCodeChallenge(codeConfirmRequest);

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userVerificationService.requestConfirmCodeAndGenerateCodeChallenge(codeConfirmRequest)
        );
    }

    @Test
    void testRequestConfirmCodeAndGenerateCodeChallenge_invalidId_shouldReturnsVerificationExpiredException() {
        final UserVerification userVerification = userVerificationService.requestConfirmPhone(CreateRequestUtilTest.createPhoneConfirmRequest(PHONE_NUMBER));

        final CodeConfirmRequest codeConfirmRequest = CreateRequestUtilTest.createCodeConfirmRequest(0L, userVerification.getVerificationCode(), PUBLIC_KEY);

        assertThrows(
                VerificationExpiredException.class,
                () -> userVerificationService.requestConfirmCodeAndGenerateCodeChallenge(codeConfirmRequest)
        );
    }

    @Test
    void testRequestConfirmCodeAndGenerateCodeChallenge_invalidCode_shouldReturnsVerificationExpiredException() {
        final UserVerification userVerification = userVerificationService.requestConfirmPhone(CreateRequestUtilTest.createPhoneConfirmRequest(PHONE_NUMBER));

        final CodeConfirmRequest codeConfirmRequest = CreateRequestUtilTest.createCodeConfirmRequest(userVerification.getId(), "random_code", PUBLIC_KEY);

        assertThrows(
                VerificationExpiredException.class,
                () -> userVerificationService.requestConfirmCodeAndGenerateCodeChallenge(codeConfirmRequest)
        );
    }

    @Test
    void testRequestConfirmCodeAndGenerateCodeChallenge_expiredCode_shouldReturnsVerificationExpiredException() {
        final UserVerification userVerification = userVerificationService.requestConfirmPhone(CreateRequestUtilTest.createPhoneConfirmRequest(PHONE_NUMBER));

        assertThrows(
                VerificationExpiredException.class,
                () -> this.userVerificationRepository.findValidUserVerificationByIdAndCode(userVerification.getId(), userVerification.getVerificationCode(), ZonedDateTime.now().plusSeconds(100))
                        .orElseThrow(VerificationExpiredException::new)
        );
    }

}
