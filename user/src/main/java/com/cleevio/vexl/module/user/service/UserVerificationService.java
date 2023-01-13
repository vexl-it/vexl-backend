package com.cleevio.vexl.module.user.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.cryptolib.CryptoLibrary;
import com.cleevio.vexl.common.integration.twilio.service.SmsService;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.common.util.PhoneUtils;
import com.cleevio.vexl.module.user.config.CredentialConfig;
import com.cleevio.vexl.module.user.config.SecretKeyConfig;
import com.cleevio.vexl.module.user.constant.VerificationAdvisoryLock;
import com.cleevio.vexl.module.user.dto.request.CodeConfirmRequest;
import com.cleevio.vexl.module.user.dto.request.PhoneConfirmRequest;
import com.cleevio.vexl.module.user.entity.UserVerification;
import com.cleevio.vexl.module.user.exception.ChallengeGenerationException;
import com.cleevio.vexl.module.user.exception.InvalidPhoneNumberException;
import com.cleevio.vexl.module.user.exception.PreviousVerificationCodeNotExpiredException;
import com.cleevio.vexl.module.user.exception.VerificationExpiredException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;

import static com.cleevio.vexl.module.user.util.ChallengeUtil.generateChallenge;

/**
 * Service for processing of request to phone number verification. Phone must be valid according industry-standard
 * notation pattern specified by ITU-T E.123. This service is also responsible for processing code verification and
 * generating challenge for user.
 */
@Service
@Validated
@Slf4j
@RequiredArgsConstructor
public class UserVerificationService {

    private final SmsService smsService;
    private final UserVerificationRepository userVerificationRepository;
    private final AdvisoryLockService advisoryLockService;
    private final UserService userService;
    private final SecretKeyConfig secretKey;
    private final CredentialConfig credentialConfig;

    @Value("${verification.phone.expiration.time}")
    private final Integer expirationTime;

    @Value("${environment.devel}")
    private final boolean isDevel;

    private static final String DEVEL_CODE = "111111";

    /**
     * Generate a random code for phone verification. Store the unencrypted code and the phone encrypted with HMAC-SHA256 in the database
     * and create an entry in the USER_VERIFICATION table.
     */
    @Transactional
    public UserVerification requestConfirmPhone(@Valid PhoneConfirmRequest phoneConfirmRequest) {
        advisoryLockService.lock(
                ModuleLockNamespace.VERIFICATION,
                VerificationAdvisoryLock.REQUEST_VERIFICATION_CODE.name(),
                phoneConfirmRequest.phoneNumber()
        );

        final String formattedNumber = PhoneUtils.trimAndDeleteSpacesFromPhoneNumber(phoneConfirmRequest.phoneNumber());

        if (!PhoneUtils.isValidPhoneNumber(formattedNumber)) {
            throw new InvalidPhoneNumberException();
        }

        if (isPreviousVerificationValid(formattedNumber)) {
            throw new PreviousVerificationCodeNotExpiredException();
        }

        String codeToSend = null;
        String verificationSid = null;
        if (isDevel) {
            codeToSend = DEVEL_CODE;
        } else if (areCredentialsActiveAndDoesNumberMatch(formattedNumber)) {
            codeToSend = credentialConfig.code();
        } else {
            // TODO rename column
            verificationSid = smsService.sendMessage(formattedNumber);
        }

        log.info("Creating user verification for new request for phone number verification.");
        final UserVerification userVerification =
                createUserVerification(
                        codeToSend,
                        verificationSid,
                        CryptoLibrary.instance.hmacDigest(
                                this.secretKey.hmacKey(),
                                formattedNumber
                        )
                );

        final UserVerification savedVerification = this.userVerificationRepository.save(userVerification);

        log.info("Created verification and code sent for verification id {}",
                savedVerification.getId());

        return savedVerification;
    }

    /**
     * Check if there is a valid verification for the ID and code. If there is, we create a challenge that verifies
     * that the user is giving us a public key to which he owns a private key.
     * We store the public key and the challenge, thus creating the basis for the USER entity.
     */
    @Transactional
    public UserVerification requestConfirmCodeAndGenerateCodeChallenge(@Valid CodeConfirmRequest codeConfirmRequest) {
        advisoryLockService.lock(
                ModuleLockNamespace.VERIFICATION,
                VerificationAdvisoryLock.CONFIRM_VERIFICATION_CODE.name(),
                codeConfirmRequest.userPublicKey()
        );

        UserVerification userVerification =
                this.userVerificationRepository.findValidUserVerificationById(
                        codeConfirmRequest.id(),
                        ZonedDateTime.now()
                ).orElseThrow(VerificationExpiredException::new);

        if(!verifySmsCode(codeConfirmRequest, userVerification)) {
            throw new VerificationExpiredException();
        }


        log.info("Code is verified for verification: {}", userVerification.getId());

        try {
            final String challenge = generateChallenge();
            log.info("Challenge is created.");

            userVerification.setChallenge(challenge);
            userVerification.setPhoneVerified(true);
            userVerification.setUser(
                    this.userService.prepareUser(codeConfirmRequest.userPublicKey()));

            return this.userVerificationRepository.save(userVerification);
        } catch (NoSuchAlgorithmException exception) {
            log.error("Challenge generation failed.", exception);
            throw new ChallengeGenerationException();
        }
    }

    @Transactional
    public void deleteExpiredVerifications() {
        this.userVerificationRepository.deleteExpiredVerifications(ZonedDateTime.now().minusSeconds(10));
    }

    private UserVerification createUserVerification(String codeToSend, String verificationSid, String phoneNumber) {
        return UserVerification.builder()
                .verificationCode(codeToSend)
                .verificationSid(verificationSid)
                .expirationAt(ZonedDateTime.now().plusSeconds(this.expirationTime))
                .phoneNumber(phoneNumber)
                .build();
    }

    private boolean verifySmsCode(CodeConfirmRequest codeConfirmRequest, UserVerification userVerification) {
        if(userVerification.getVerificationCode() != null) {
            // if verification code is in the database, verify against it.
            return codeConfirmRequest.code().equals(userVerification.getVerificationCode());
        }
        // Otherwise verify against Twilio
        return smsService.verifyMessage(userVerification.getVerificationSid(), codeConfirmRequest.code());
    }

    private boolean isPreviousVerificationValid(String formattedNumber) {
        final String hmacNumber = CryptoLibrary.instance.hmacDigest(
                this.secretKey.hmacKey(),
                formattedNumber
        );
        return this.userVerificationRepository.doesPreviousVerificationExist(hmacNumber, ZonedDateTime.now());
    }

    private boolean areCredentialsActiveAndDoesNumberMatch(final String formattedNumber) {
        return credentialConfig.active() && credentialConfig.phones() != null &&
                credentialConfig.code() != null && credentialConfig.phones().contains(formattedNumber);
    }
}
