package com.cleevio.vexl.module.message.service;

import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.challenge.service.ChallengeService;
import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.inbox.service.InboxService;
import com.cleevio.vexl.module.inbox.service.WhitelistService;
import com.cleevio.vexl.module.message.entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MessageServiceTest {

    private static final Inbox INBOX;
    private static final Message MESSAGE_1;
    private static final Message MESSAGE_2;
    private static final Message MESSAGE_3_PULLED;
    private static final String PUBLIC_KEY_HASH;
    private static final String SENDER_PUBLIC_KEY;

    private final MessageRepository messageRepository = mock(MessageRepository.class);
    private final WhitelistService whitelistService = mock(WhitelistService.class);
    private final InboxService inboxService = mock(InboxService.class);
    private final AdvisoryLockService advisoryLockService = mock(AdvisoryLockService.class);
    private final ChallengeService challengeService = mock(ChallengeService.class);
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);

    private final MessageService messageService = new MessageService(
            messageRepository,
            whitelistService,
            inboxService,
            challengeService,
            applicationEventPublisher,
            advisoryLockService
    );

    static {
        PUBLIC_KEY_HASH = "3Qf0lOb0FaWDXU59xGNa/4pv0s9kKxbbnNIYXBNxUUQ=";
        SENDER_PUBLIC_KEY = "dummy_key";

        INBOX = new Inbox();
        INBOX.setPublicKey(PUBLIC_KEY_HASH);

        MESSAGE_1 = new Message();
        MESSAGE_1.setId(1L);
        MESSAGE_1.setMessage("dummy_message_1");
        MESSAGE_1.setPulled(false);
        MESSAGE_1.setSenderPublicKey(SENDER_PUBLIC_KEY);
        MESSAGE_1.setInbox(INBOX);

        MESSAGE_2 = new Message();
        MESSAGE_2.setId(2L);
        MESSAGE_2.setMessage("dummy_message_2");
        MESSAGE_2.setPulled(false);
        MESSAGE_2.setSenderPublicKey(SENDER_PUBLIC_KEY);
        MESSAGE_2.setInbox(INBOX);

        MESSAGE_3_PULLED = new Message();
        MESSAGE_3_PULLED.setId(3L);
        MESSAGE_3_PULLED.setMessage("dummy_message_3");
        MESSAGE_3_PULLED.setPulled(true);
        MESSAGE_3_PULLED.setSenderPublicKey(SENDER_PUBLIC_KEY);
        MESSAGE_3_PULLED.setInbox(INBOX);


        INBOX.setMessages(List.of(MESSAGE_3_PULLED, MESSAGE_1, MESSAGE_2));

    }

    @Test
    void retrieveMessages_shouldReceiveMessages() {
        List<Message> messages = this.messageService.retrieveMessages(INBOX);
        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).isPulled()).isEqualTo(true);
        assertThat(messages.get(1).isPulled()).isEqualTo(true);
        assertThat(messages.get(2).isPulled()).isEqualTo(true);
        assertThat(messages.get(0).getId()).isEqualTo(1L);
        assertThat(messages.get(1).getId()).isEqualTo(2L);
        assertThat(messages.get(2).getId()).isEqualTo(3L);
    }
}
