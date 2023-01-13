package com.cleevio.vexl.module.user.service;

import com.cleevio.vexl.module.user.config.SecretKeyConfig;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.user.entity.UserVerification;
import com.cleevio.vexl.module.user.exception.VerificationExpiredException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignatureServiceTest {

    private static final String PUBLIC_KEY = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUU0d0VBWUhLb1pJemowQ0FRWUZLNEVFQUNFRE9nQUVZMzcrNGxQYWtVd213aFMxWlJsVFd3enlYRFFRbFBZNgpkdDhhRHFIRzBzTFVrYk5FMjlEWDNiWWpSWTd1bitNRmhoZ281RCtCY3lBPQotLS0tLUVORCBQVUJMSUMgS0VZLS0tLS0K";
    private static final String PUBLIC_KEY_MS = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUU0d0VBWUhLb1pJemowQ0FRWUZLNEVFQUNFRE9nQUV1VGUxV3EwazBFK0MyendURnZHOGtUa01iOWZ3cW14RwppTndJTk4vSG1UcUEvRFR1Ny9XWEZERWZXUlp0aWlvY1ZjUTZ5MkYwUFI4PQotLS0tLUVORCBQVUJMSUMgS0VZLS0tLS0K";
    private static final String PRIVATE_KEY_MS = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1IZ0NBUUF3RUFZSEtvWkl6ajBDQVFZRks0RUVBQ0VFWVRCZkFnRUJCQndaU1VVNWtEZDFUcVg5M1hVUUZsSGsKTGxsaENPSmRSMDNybDNacm9Ud0RPZ0FFdVRlMVdxMGswRStDMnp3VEZ2RzhrVGtNYjlmd3FteEdpTndJTk4vSAptVHFBL0RUdTcvV1hGREVmV1JadGlpb2NWY1E2eTJGMFBSOD0KLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQo=";
    private static final String HMAC_KEY = "test";
    private static final String AES_KEY = "test";
    private static final String PHONE_HASH = "dummy_phone_hash";

    private static final SecretKeyConfig secretKey = new SecretKeyConfig(PUBLIC_KEY_MS, PRIVATE_KEY_MS, HMAC_KEY, AES_KEY);
    private static final SignatureService signatureService = new SignatureService(secretKey);

    private static final User USER;
    private static final UserVerification USER_VERIFICATION;

    static {
        USER = new User();
        USER.setPublicKey(PUBLIC_KEY);

        USER_VERIFICATION = new UserVerification();
        USER_VERIFICATION.setPhoneNumber(PHONE_HASH);
    }

    @Test
    void createSignatureAndVerify_shouldBeVerified() throws VerificationExpiredException {
        USER.setUserVerification(USER_VERIFICATION);
        final var signature = signatureService.createSignature(PUBLIC_KEY, PHONE_HASH, false, 1);

        final boolean result = signatureService.isSignatureValid(PUBLIC_KEY, signature.hash(), signature.signature(), 1);
        assertThat(result).isTrue();
    }

    @Test
    void createSignatureAndVerify_shouldNotBeVerified() throws VerificationExpiredException {
        USER.setUserVerification(USER_VERIFICATION);
        final var signature = signatureService.createSignature(PUBLIC_KEY, PHONE_HASH, false, 1);

        final boolean result = signatureService.isSignatureValid(PUBLIC_KEY_MS, signature.hash(), signature.signature(), 1);
        assertThat(result).isFalse();
    }



    @Test
    void createSignatureAndVerify_v2_shouldBeVerified() throws VerificationExpiredException {
        USER.setUserVerification(USER_VERIFICATION);
        final var signature = signatureService.createSignature(PUBLIC_KEY, PHONE_HASH, false, 2);

        final boolean result = signatureService.isSignatureValid(PUBLIC_KEY, signature.hash(), signature.signature(), 2);
        assertThat(result).isTrue();
    }

    @Test
    void createSignatureAndVerify_v2_shouldNotBeVerified() throws VerificationExpiredException {
        USER.setUserVerification(USER_VERIFICATION);
        final var signature = signatureService.createSignature(PUBLIC_KEY, PHONE_HASH, false, 2);

        final boolean result = signatureService.isSignatureValid(PUBLIC_KEY_MS, signature.hash(), signature.signature(), 2);
        assertThat(result).isFalse();
    }
}
