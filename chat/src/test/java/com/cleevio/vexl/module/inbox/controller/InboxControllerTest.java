package com.cleevio.vexl.module.inbox.controller;

import com.cleevio.vexl.common.BaseControllerTest;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.challenge.service.query.VerifySignedChallengeQuery;
import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import com.cleevio.vexl.module.inbox.dto.request.BlockInboxRequest;
import com.cleevio.vexl.module.inbox.dto.request.UpdateInboxRequest;
import com.cleevio.vexl.module.message.dto.response.MessagesResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(InboxController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InboxControllerTest extends BaseControllerTest {

    private static final String DEFAULT_EP = "/api/v1/inboxes";
    private static final String BLOCK_EP = DEFAULT_EP + "/block";

    private static final String APPROVAL_REQUEST = DEFAULT_EP + "/approval/request";
    private static final String APPROVAL_CONFIRM = DEFAULT_EP + "/approval/confirm";
    private static final String DELETE_MESSAGES = DEFAULT_EP + "/messages";
    private static final String X_PLATFORM = "android";
    private static final UpdateInboxRequest UPDATE_INBOX_REQUEST;
    private static final MessagesResponse.MessageResponse MESSAGE_RESPONSE;
    private static final BlockInboxRequest BLOCK_INBOX_REQUEST;
    private static final SignedChallenge SIGNED_CHALLENGE;
    private static final VerifySignedChallengeQuery VERIFY_SIGNED_CHALLENGE_QUERY;

    private static final String CREATE_INBOX = """
               {
                "publicKey": "dummy_public_key",
                "token": "123"
                }
            """;

    private static final String APPROVAL_REQUEST_PAYLOAD = String.format("""
            {
                "publicKey": "%s",
                "message": "dummy_message"
            }
                                    """, INBOX_PUBLIC_KEY);

    private static final String DELETE_PAYLOAD = String.format("""
            {
                "publicKey": "%s",
                "signedChallenge": {
                            "challenge": "dummy_challenge",
                            "signature": "dummy_signature"
                            }
            }
                                    """, INBOX_PUBLIC_KEY);

    private static final String APPROVAL_CONFIRM_PAYLOAD = String.format("""
            {
                "publicKey": "%s",
                "publicKeyToConfirm": "dummy_pk_to_confirm",
                "message": "Yes, I approve. Let's meet in the most beautiful part of Prague, in Karlin.",
                "approve": true,
                "signedChallenge": {
                            "challenge": "dummy_challenge",
                            "signature": "%s"
                            }
            }
                                                """, INBOX_PUBLIC_KEY, CHALLENGE_SIGNATURE);

    static {
        SIGNED_CHALLENGE = new SignedChallenge(CHALLENGE, CHALLENGE_SIGNATURE);

        UPDATE_INBOX_REQUEST = new UpdateInboxRequest(INBOX_PUBLIC_KEY, FIREBASE_TOKEN, SIGNED_CHALLENGE);

        BLOCK_INBOX_REQUEST = new BlockInboxRequest(INBOX_PUBLIC_KEY, PUBLIC_KEY_HEADER, true, SIGNED_CHALLENGE);

        MESSAGE_RESPONSE = new MessagesResponse.MessageResponse(MESSAGE.getId(), MESSAGE.getMessage(), MESSAGE.getSenderPublicKey(), MESSAGE.getType());

        VERIFY_SIGNED_CHALLENGE_QUERY = new VerifySignedChallengeQuery(INBOX_PUBLIC_KEY, SIGNED_CHALLENGE);
    }

    @BeforeEach
    void beforeEach() {
        when(challengeService.isSignedChallengeValid(eq(VERIFY_SIGNED_CHALLENGE_QUERY), anyInt())).thenReturn(true);
    }

    @Test
    @SneakyThrows
    void testCreateInbox_validInput_shouldReturn204() {
        mvc.perform(post(DEFAULT_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY_HEADER)
                        .header(SecurityFilter.HEADER_HASH, HASH_HEADER)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE_HEADER)
                        .header(SecurityFilter.X_PLATFORM, X_PLATFORM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_INBOX))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    void testUpdateInbox_validInput_shouldReturn202() {
        when(inboxService.updateInbox(UPDATE_INBOX_REQUEST, 0)).thenReturn(INBOX);
        when(inboxService.findInbox(INBOX_PUBLIC_KEY)).thenReturn(INBOX);

        mvc.perform(put(DEFAULT_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY_HEADER)
                        .header(SecurityFilter.HEADER_HASH, HASH_HEADER)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE_HEADER)
                        .header(SecurityFilter.X_PLATFORM, X_PLATFORM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(UPDATE_INBOX_REQUEST)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.firebaseToken", is(FIREBASE_TOKEN)));
    }

    @Test
    @SneakyThrows
    void testBlock_validInput_shouldReturn204() {
        when(inboxService.findInbox(INBOX_PUBLIC_KEY)).thenReturn(INBOX);

        mvc.perform(put(BLOCK_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY_HEADER)
                        .header(SecurityFilter.HEADER_HASH, HASH_HEADER)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE_HEADER)
                        .header(SecurityFilter.X_PLATFORM, X_PLATFORM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(BLOCK_INBOX_REQUEST)))
                .andExpect(status().isNoContent());
    }

    @Test
    @SneakyThrows
    void testApprovalRequest_validInput_shouldReturn200() {
        when(inboxService.findInbox(INBOX_PUBLIC_KEY)).thenReturn(INBOX);
        when(messageService.sendRequestToPermission(any())).thenReturn(MESSAGE);
        when(messageMapper.mapSingle(MESSAGE)).thenReturn(MESSAGE_RESPONSE);

        mvc.perform(post(APPROVAL_REQUEST)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY_HEADER)
                        .header(SecurityFilter.HEADER_HASH, HASH_HEADER)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE_HEADER)
                        .header(SecurityFilter.X_PLATFORM, X_PLATFORM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPROVAL_REQUEST_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(MESSAGE.getId().intValue())))
                .andExpect(jsonPath("$.message", is(MESSAGE.getMessage())))
                .andExpect(jsonPath("$.senderPublicKey", is(MESSAGE.getSenderPublicKey())))
                .andExpect(jsonPath("$.messageType", is(MESSAGE.getType().name())));
    }

    @Test
    @SneakyThrows
    void testApprovalConfirm_validInput_shouldReturn200() {
        when(inboxService.findInbox(any())).thenReturn(INBOX);
        when(messageService.sendMessageToInbox(any())).thenReturn(MESSAGE);
        when(messageMapper.mapSingle(MESSAGE)).thenReturn(MESSAGE_RESPONSE);

        mvc.perform(post(APPROVAL_CONFIRM)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY_HEADER)
                        .header(SecurityFilter.HEADER_HASH, HASH_HEADER)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE_HEADER)
                        .header(SecurityFilter.X_PLATFORM, X_PLATFORM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(APPROVAL_CONFIRM_PAYLOAD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(MESSAGE.getId().intValue())))
                .andExpect(jsonPath("$.message", is(MESSAGE.getMessage())))
                .andExpect(jsonPath("$.senderPublicKey", is(MESSAGE.getSenderPublicKey())))
                .andExpect(jsonPath("$.messageType", is(MESSAGE.getType().name())));
    }

    @Test
    @SneakyThrows
    void testDeleteMessages_validInput_shouldReturn200() {
        when(inboxService.findInbox(any())).thenReturn(INBOX);

        mvc.perform(delete(DELETE_MESSAGES)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY_HEADER)
                        .header(SecurityFilter.HEADER_HASH, HASH_HEADER)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE_HEADER)
                        .header(SecurityFilter.X_PLATFORM, X_PLATFORM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DELETE_PAYLOAD))
                .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void testDeleteInboxAndMessages_validInput_shouldReturn200() {
        when(inboxService.findInbox(any())).thenReturn(INBOX);

        mvc.perform(delete(DEFAULT_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY_HEADER)
                        .header(SecurityFilter.HEADER_HASH, HASH_HEADER)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE_HEADER)
                        .header(SecurityFilter.X_PLATFORM, X_PLATFORM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(DELETE_PAYLOAD))
                .andExpect(status().isOk());
    }
}
