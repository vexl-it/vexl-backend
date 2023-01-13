package com.cleevio.vexl.module.user.controller;

import com.cleevio.vexl.common.BaseControllerTest;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.user.dto.SignatureData;
import com.cleevio.vexl.module.user.dto.UserData;
import com.cleevio.vexl.module.user.dto.request.ChallengeRequest;
import com.cleevio.vexl.module.user.dto.request.CodeConfirmRequest;
import com.cleevio.vexl.module.user.dto.request.PhoneConfirmRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest extends BaseControllerTest {

    private static final String DEFAULT_EP = "/api/v1/user";
    private static final String REQUEST_SMS_EP = DEFAULT_EP + "/confirmation/phone";
    private static final String CONFIRM_SMS_AND_GENERATE_CHALLENGE = DEFAULT_EP + "/confirmation/code";
    private static final String CONFIRM_CHALLENGE = DEFAULT_EP + "/confirmation/challenge";
    private static final String FACEBOOK_ID = "dummy_facebook_id";
    private static final String FB_SIGNATURE_EP = DEFAULT_EP + "/signature/" + FACEBOOK_ID;
    private static final PhoneConfirmRequest PHONE_CONFIRM_REQUEST;
    private static final CodeConfirmRequest CODE_CONFIRM_REQUEST;
    private static final ChallengeRequest CHALLENGE_REQUEST;
    private static final UserData USER_DATA;
    private static final SignatureData SIGNATURE_DATA;
    private static final String USER_PHONE = "+420856856856";

    static {
        PHONE_CONFIRM_REQUEST = new PhoneConfirmRequest(USER_PHONE);

        CODE_CONFIRM_REQUEST = new CodeConfirmRequest(1L, VERIFICATION_CODE, USER_PUBLIC_KEY);

        CHALLENGE_REQUEST = new ChallengeRequest(USER_PUBLIC_KEY, SIGNATURE_CHALLENGE);

        USER_DATA = new UserData(USER_PUBLIC_KEY, USER_PHONE, SIGNATURE_CHALLENGE, SIGNATURE);

        SIGNATURE_DATA = new SignatureData(PHONE_HASH, SIGNATURE, true);
    }

    @Test
    void testRequestConfirmPhone_validInput_shouldReturn200() throws Exception {
        when(userVerificationService.requestConfirmPhone(PHONE_CONFIRM_REQUEST)).thenReturn(USER_VERIFICATION);

        mvc.perform(post(REQUEST_SMS_EP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(PHONE_CONFIRM_REQUEST)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationId", is(USER_VERIFICATION.getId())));
    }

    @Test
    void testConfirmCodeAndGenerateCodeChallenge_validInput_shouldReturn200() throws Exception {
        when(userVerificationService.requestConfirmCodeAndGenerateCodeChallenge(CODE_CONFIRM_REQUEST)).thenReturn(USER_VERIFICATION);

        mvc.perform(post(CONFIRM_SMS_AND_GENERATE_CHALLENGE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(CODE_CONFIRM_REQUEST)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.challenge", is(USER_VERIFICATION.getChallenge())))
                .andExpect(jsonPath("$.phoneVerified", is(USER_VERIFICATION.isPhoneVerified())));
    }

    @Test
    void testVerifyChallengeAndGenerateSignature_validInput_shouldReturn200() throws Exception {
        when(userService.findValidUserWithChallenge(any())).thenReturn(USER_DATA);
        when(signatureService.createSignature(any(), anyInt())).thenReturn(SIGNATURE_DATA);

        mvc.perform(post(CONFIRM_CHALLENGE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(CHALLENGE_REQUEST)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hash", is(PHONE_HASH)))
                .andExpect(jsonPath("$.signature", is(SIGNATURE)))
                .andExpect(jsonPath("$.challengeVerified", equalTo(true)));
    }

    @Test
    void testGenerateSignature_validInput_shouldReturn200() throws Exception {
        when(signatureService.createSignature(eq(USER.getPublicKey()), eq(FACEBOOK_ID), eq(false), anyInt())).thenReturn(SIGNATURE_DATA);

        mvc.perform(get(FB_SIGNATURE_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY)
                        .header(SecurityFilter.HEADER_HASH, PHONE_HASH)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hash", is(PHONE_HASH)))
                .andExpect(jsonPath("$.signature", is(SIGNATURE)))
                .andExpect(jsonPath("$.challengeVerified", equalTo(true)));
    }
}
