package com.cleevio.vexl.module.inbox.service;

import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.inbox.constant.Platform;
import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import com.cleevio.vexl.module.inbox.dto.request.CreateInboxRequest;
import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.inbox.exception.InboxNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InboxServiceTest {

    private static final CreateInboxRequest CREATE_INBOX_REQUEST;
    private static final SignedChallenge SIGNED_CHALLENGE;
    private static final String PUBLIC_KEY;
    private static final String PUBLIC_KEY_HASH;
    private static final String TOKEN;
    private static final Inbox INBOX;
    private static final String CHALLENGE = "dummy_challenge";
    private static final String SIGNATURE = "dummy_signature";

    private final InboxRepository inboxRepository = mock(InboxRepository.class);
    private final ApplicationEventPublisher applicationEventPublisher = mock(ApplicationEventPublisher.class);
    private final AdvisoryLockService advisoryLockService = mock(AdvisoryLockService.class);

    private final InboxService inboxService = new InboxService(
            inboxRepository,
            advisoryLockService,
            applicationEventPublisher
    );

    static {
        SIGNED_CHALLENGE = new SignedChallenge(CHALLENGE, SIGNATURE);

        PUBLIC_KEY_HASH = "3Qf0lOb0FaWDXU59xGNa/4pv0s9kKxbbnNIYXBNxUUQ=";
        PUBLIC_KEY = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUU0d0VBWUhLb1pJemowQ0FRWUZLNEVFQUNFRE9nQUVqT2xDSnhwVHFFZ1k2T0FER2lTdXdUbjBJZWFIZHZEawo0NkZYeDM5Yk5memY0Ry9zcFZXb1NibTIvODVhbmNodDE1c2hzSmdONnVBPQotLS0tLUVORCBQVUJMSUMgS0VZLS0tLS0K";
        TOKEN = "dummy_token";
        CREATE_INBOX_REQUEST = new CreateInboxRequest(PUBLIC_KEY, TOKEN, SIGNED_CHALLENGE);

        INBOX = new Inbox();
        INBOX.setPublicKey(PUBLIC_KEY_HASH);
        INBOX.setToken(TOKEN);
    }

    @Test
    void createInbox_inboxShouldBeCreated() {
        final var inboxEvent = ArgumentCaptor.forClass(Inbox.class);
        final var publicHash = ArgumentCaptor.forClass(String.class);
        this.inboxService.createInbox(CREATE_INBOX_REQUEST, Platform.ANDROID, 0);
        verify(this.inboxRepository).existsByPublicKey(publicHash.capture());
        verify(this.inboxRepository).save(inboxEvent.capture());
        final var inboxResult = inboxEvent.getValue();
        final var publicHashResult = publicHash.getValue();
        assertThat(inboxResult.getPublicKey()).isEqualTo(PUBLIC_KEY);
        assertThat(inboxResult.getToken()).isEqualTo(TOKEN);
        assertThat(publicHashResult).isEqualTo(PUBLIC_KEY);
    }

    @Test
    void createDuplicateInbox_shouldNotCreateInbox() {
        when(this.inboxRepository.existsByPublicKey(PUBLIC_KEY)).thenReturn(true);

        this.inboxService.createInbox(CREATE_INBOX_REQUEST, Platform.ANDROID, 0);
        verify(inboxRepository, never()).save(any());
    }

    @Test
    void findInbox_shouldFindInbox() {
        when(this.inboxRepository.findByPublicKey(PUBLIC_KEY)).thenReturn(Optional.of(INBOX));

        Inbox inbox = this.inboxService.findInbox(PUBLIC_KEY);
        assertThat(inbox.getPublicKey()).isEqualTo(PUBLIC_KEY_HASH);
        assertThat(inbox.getToken()).isEqualTo(TOKEN);
    }

    @Test
    void findInbox_shouldThrowException() {
        when(this.inboxRepository.findByPublicKey(PUBLIC_KEY)).thenReturn(Optional.empty());

        assertThrows(
                InboxNotFoundException.class,
                () -> this.inboxService.findInbox(PUBLIC_KEY)
        );
    }

}
