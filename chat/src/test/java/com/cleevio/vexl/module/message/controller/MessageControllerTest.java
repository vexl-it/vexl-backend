package com.cleevio.vexl.module.message.controller;

import com.cleevio.vexl.common.BaseControllerTest;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.module.challenge.service.query.VerifySignedChallengeQuery;
import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import com.cleevio.vexl.module.message.dto.request.MessageRequest;
import com.cleevio.vexl.module.message.dto.response.MessagesResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MessageControllerTest extends BaseControllerTest {

    private static final String DEFAULT_EP = "/api/v1/inboxes/messages";
    private static final String X_PLATFORM = "IOS";
    private static final MessageRequest MESSAGE_REQUEST;
    private static final SignedChallenge SIGNED_CHALLENGE;
    private static final VerifySignedChallengeQuery VERIFY_SIGNED_CHALLENGE_QUERY;
    private static final MessagesResponse.MessageResponse MESSAGE_RESPONSE;


    private static final String POST_MESSAGE = String.format("""
            {
                "senderPublicKey": "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUU0d0VBWUhLb1pJemowQ0FRWUZLNEVFQUNFRE9nQUVqT2xDSnhwVHFFZ1k2T0FER2lTdXdUbjBJZWFIZHZEawo0NkZYeDM5Yk5memY0Ry9zcFZXb1NibTIvODVhbmNodDE1c2hzSmdONnVBPQotLS0tLUVORCBQVUJMSUMgS0VZLS0tLS0K",
                "receiverPublicKey": "%s",
                "message": "last",
                "messageType": "MESSAGE",
                "signedChallenge": {
                            "challenge": "dummy_challenge",
                            "signature": "dummy_signature"
                            }
            }
                        """, INBOX_PUBLIC_KEY);

    static {
        SIGNED_CHALLENGE = new SignedChallenge(CHALLENGE, CHALLENGE_SIGNATURE);

        MESSAGE_REQUEST = new MessageRequest(INBOX_PUBLIC_KEY, SIGNED_CHALLENGE);

        MESSAGE_RESPONSE = new MessagesResponse.MessageResponse(MESSAGE.getId(), MESSAGE.getMessage(), MESSAGE.getSenderPublicKey(), MESSAGE.getType());

        VERIFY_SIGNED_CHALLENGE_QUERY = new VerifySignedChallengeQuery(INBOX_PUBLIC_KEY, SIGNED_CHALLENGE);
    }

    @BeforeEach
    void beforeEach() {
        when(challengeService.isSignedChallengeValid(eq(VERIFY_SIGNED_CHALLENGE_QUERY), anyInt())).thenReturn(true);
    }

    @Test
    @SneakyThrows
    void testPostMessage_validInput_shouldReturn200() {
        when(inboxService.findInbox(INBOX_PUBLIC_KEY)).thenReturn(INBOX);
        when(messageService.sendMessageToInbox(any())).thenReturn(MESSAGE);
        when(messageMapper.mapSingle(MESSAGE)).thenReturn(MESSAGE_RESPONSE);

        mvc.perform(post(DEFAULT_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY_HEADER)
                        .header(SecurityFilter.HEADER_HASH, HASH_HEADER)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE_HEADER)
                        .header(SecurityFilter.X_PLATFORM, X_PLATFORM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(POST_MESSAGE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(MESSAGE.getId().intValue())))
                .andExpect(jsonPath("$.message", is(MESSAGE.getMessage())))
                .andExpect(jsonPath("$.senderPublicKey", is(MESSAGE.getSenderPublicKey())))
                .andExpect(jsonPath("$.messageType", is(MESSAGE.getType().name())));
    }

    @Test
    @SneakyThrows
    void testRetrieveMessages_validInput_shouldReturn200() {
        final var messages = List.of(MESSAGE);
        when(inboxService.findInbox(INBOX_PUBLIC_KEY)).thenReturn(INBOX);
        when(messageService.retrieveMessages(INBOX, 0)).thenReturn(messages);
        when(messageMapper.mapList(messages)).thenReturn(List.of(MESSAGE_RESPONSE));

        mvc.perform(put(DEFAULT_EP)
                        .header(SecurityFilter.HEADER_PUBLIC_KEY, PUBLIC_KEY_HEADER)
                        .header(SecurityFilter.HEADER_HASH, HASH_HEADER)
                        .header(SecurityFilter.HEADER_SIGNATURE, SIGNATURE_HEADER)
                        .header(SecurityFilter.X_PLATFORM, X_PLATFORM)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(MESSAGE_REQUEST)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].id", is(MESSAGE.getId().intValue())))
                .andExpect(jsonPath("$.messages[0].message", is(MESSAGE.getMessage())))
                .andExpect(jsonPath("$.messages[0].senderPublicKey", is(MESSAGE.getSenderPublicKey())))
                .andExpect(jsonPath("$.messages[0].messageType", is(MESSAGE.getType().name())));
    }
}
