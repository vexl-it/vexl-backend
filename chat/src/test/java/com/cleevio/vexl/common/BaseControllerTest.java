package com.cleevio.vexl.common;

import com.cleevio.vexl.common.service.SignatureService;
import com.cleevio.vexl.common.service.query.CheckSignatureValidityQuery;
import com.cleevio.vexl.module.challenge.service.ChallengeService;
import com.cleevio.vexl.module.export.service.ExportService;
import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.message.entity.Message;
import com.cleevio.vexl.module.message.constant.MessageType;
import com.cleevio.vexl.module.message.mapper.MessageMapper;
import com.cleevio.vexl.module.inbox.service.InboxService;
import com.cleevio.vexl.module.message.service.MessageService;
import com.cleevio.vexl.module.inbox.service.WhitelistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

public class BaseControllerTest {

    protected static final Inbox INBOX;
    protected static final Message MESSAGE;
    protected static final String MESSAGE_TEST = "dummy_message_text";
    protected static final String PUBLIC_KEY_HEADER = "dummy_public_key_header";
    protected static final String HASH_HEADER = "dummy_hash_header";
    protected static final String SIGNATURE_HEADER = "dummy_signature_header";
    protected static final String INBOX_PUBLIC_KEY = "dummy_inbox_public_key";
    protected static final String FIREBASE_TOKEN = "dummy_firebase_token";
    protected static final String CHALLENGE_SIGNATURE = "dummy_challenge_signature";
    protected static final String CHALLENGE = "dummy_challenge";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected InboxService inboxService;

    @MockBean
    protected ExportService exportService;

    @MockBean
    protected SignatureService signatureService;

    @MockBean
    protected ChallengeService challengeService;

    @MockBean
    protected MessageService messageService;

    @MockBean
    protected WhitelistService whitelistService;

    @MockBean
    protected MessageMapper messageMapper;

    @BeforeEach
    @SneakyThrows
    public void setup() {
        when(signatureService.isSignatureValid(any(CheckSignatureValidityQuery.class), anyInt())).thenReturn(true);
    }

    static {
        INBOX = new Inbox();
        INBOX.setToken(FIREBASE_TOKEN);
        INBOX.setPublicKey(INBOX_PUBLIC_KEY);

        MESSAGE = new Message();
        MESSAGE.setId(1L);
        MESSAGE.setMessage(MESSAGE_TEST);
        MESSAGE.setPulled(false);
        MESSAGE.setSenderPublicKey(INBOX_PUBLIC_KEY);
        MESSAGE.setType(MessageType.MESSAGE);
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
