package com.cleevio.vexl.common;

import com.cleevio.vexl.module.export.service.ExportService;
import com.cleevio.vexl.module.user.entity.User;
import com.cleevio.vexl.module.user.entity.UserVerification;
import com.cleevio.vexl.module.user.service.SignatureService;
import com.cleevio.vexl.module.user.service.UserService;
import com.cleevio.vexl.module.user.service.UserVerificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class BaseControllerTest {

    protected static final String PUBLIC_KEY = "dummy_public_key";
    protected static final String PHONE_HASH = "dummy_hash";
    protected static final String SIGNATURE = "dummy_signature";
    protected static final String SIGNATURE_CHALLENGE = "dummy_signature_challenge";
    protected static final String USER_NAME = "David Nejneobhospodářovávatelnější";
    protected static final String USER_AVATAR = "dummy_avatar";
    protected static final String USER_PUBLIC_KEY = "dummy_user_public_key";
    protected static final String VERIFICATION_CODE = "dummy_verification_code";
    protected static final String CHALLENGE = "dummy_challenge";


    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected UserService userService;

    @MockBean
    protected SignatureService signatureService;

    @MockBean
    protected ExportService exportService;

    @MockBean
    protected UserVerificationService userVerificationService;

    protected static final User USER;
    protected static final UserVerification USER_VERIFICATION;

    @Autowired
    protected ObjectMapper objectMapper;

    static {
        USER = new User();
        USER.setId(1L);
        USER.setPublicKey(USER_PUBLIC_KEY);

        USER_VERIFICATION = new UserVerification();
        USER_VERIFICATION.setVerificationCode(VERIFICATION_CODE);
        USER_VERIFICATION.setExpirationAt(ZonedDateTime.now());
        USER_VERIFICATION.setChallenge(CHALLENGE);
        USER_VERIFICATION.setPhoneVerified(true);
    }

    @BeforeEach
    @SneakyThrows
    public void setup() {
        when(userService.findByPublicKey(any())).thenReturn(Optional.of(USER));

        when(signatureService.isSignatureValid(eq(PUBLIC_KEY), eq(PHONE_HASH), eq(SIGNATURE), anyInt())).thenReturn(true);
    }

    /**
     * Entity to json string body helper
     *
     * @param obj Entity
     * @return JSON string
     */
    protected String asJsonString(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
