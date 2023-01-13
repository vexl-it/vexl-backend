package com.cleevio.vexl.module.inbox.service;

import com.cleevio.vexl.common.IntegrationTest;
import com.cleevio.vexl.common.cryptolib.CryptoLibrary;
import com.cleevio.vexl.module.challenge.dto.request.CreateChallengeRequest;
import com.cleevio.vexl.module.challenge.exception.InvalidChallengeSignature;
import com.cleevio.vexl.module.challenge.service.ChallengeService;
import com.cleevio.vexl.module.inbox.constant.Platform;
import com.cleevio.vexl.module.inbox.dto.SignedChallenge;
import com.cleevio.vexl.module.inbox.dto.request.BatchDeletionRequest;
import com.cleevio.vexl.module.inbox.dto.request.DeletionRequest;
import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.message.entity.Message;
import com.cleevio.vexl.module.inbox.entity.Whitelist;
import com.cleevio.vexl.module.message.constant.MessageType;
import com.cleevio.vexl.module.inbox.constant.WhitelistState;
import com.cleevio.vexl.module.inbox.exception.AlreadyApprovedException;
import com.cleevio.vexl.module.inbox.exception.RequestMessagingNotAllowedException;
import com.cleevio.vexl.module.message.service.MessageService;
import com.cleevio.vexl.module.message.service.query.SendMessageToInboxQuery;
import com.cleevio.vexl.utils.CryptographyTestKeysUtil;
import com.cleevio.vexl.utils.RequestCreatorTestUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.cleevio.vexl.utils.CryptographyTestKeysUtil.PRIVATE_KEY_USER_A;
import static com.cleevio.vexl.utils.CryptographyTestKeysUtil.PRIVATE_KEY_USER_B;
import static com.cleevio.vexl.utils.CryptographyTestKeysUtil.PRIVATE_KEY_USER_C;
import static com.cleevio.vexl.utils.CryptographyTestKeysUtil.PRIVATE_KEY_USER_D;
import static com.cleevio.vexl.utils.CryptographyTestKeysUtil.PUBLIC_KEY_USER_C;
import static com.cleevio.vexl.utils.CryptographyTestKeysUtil.PUBLIC_KEY_USER_D;
import static com.cleevio.vexl.utils.RequestCreatorTestUtil.MESSAGE_TO_SENDER_1;
import static com.cleevio.vexl.utils.RequestCreatorTestUtil.MESSAGE_TO_SENDER_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InboxMessagingIT {

    private final InboxService inboxService;
    private final WhitelistService whitelistService;
    private final MessageService messageService;
    private final InboxRepository inboxRepository;
    private final WhitelistRepository whitelistRepository;
    private final ChallengeService challengeService;

    @Autowired
    public InboxMessagingIT(InboxService inboxService, WhitelistService whitelistService,
                            MessageService messageService, InboxRepository inboxRepository,
                            WhitelistRepository whitelistRepository, ChallengeService challengeService) {
        this.inboxService = inboxService;
        this.whitelistService = whitelistService;
        this.messageService = messageService;
        this.inboxRepository = inboxRepository;
        this.whitelistRepository = whitelistRepository;
        this.challengeService = challengeService;
    }

    private static final String REQUEST_APPROVAL_MESSAGE = "dummy_approval_request";
    private static final String CONFIRMATION_MESSAGE = "dummy_confirmation";

    private static final String PUBLIC_KEY_USER_A = CryptographyTestKeysUtil.PUBLIC_KEY_USER_A;
    private static final String PUBLIC_KEY_USER_B = CryptographyTestKeysUtil.PUBLIC_KEY_USER_B;


    @Test
    void testCreateNewInboxes_shouldBeCreated() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        final var requestUserB = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_B);

        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        this.inboxService.createInbox(requestUserB, Platform.ANDROID);

        final Inbox inboxA = this.inboxRepository.findByPublicKey(PUBLIC_KEY_USER_A).get();
        final Inbox inboxB = this.inboxRepository.findByPublicKey(PUBLIC_KEY_USER_B).get();

        assertThat(inboxA.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_A);
        assertThat(inboxA.getToken()).isNull();
        assertThat(inboxB.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_B);
        assertThat(inboxB.getToken()).isNull();
    }

    @Test
    void testCreateDuplicatedInbox_shouldNotCreateInbox() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        assertThat(this.inboxRepository.findAll()).hasSize(1);

        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        assertThat(this.inboxRepository.findAll()).hasSize(1);
    }

    @Test
    void testCreateApprovalRequest_shouldCreateApprovalRequest() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        final var requestUserB = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_B);

        //creating new inboxes
        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        this.inboxService.createInbox(requestUserB, Platform.ANDROID);

        final Inbox inboxToWhichRequestIsSent = this.inboxRepository.findByPublicKey(PUBLIC_KEY_USER_B).get();

        //sending approval request
        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_A, PUBLIC_KEY_USER_B, inboxToWhichRequestIsSent, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));

        List<Message> messages = this.messageService.findAll();

        //verifying message
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getMessage()).isEqualTo(REQUEST_APPROVAL_MESSAGE);
        assertThat(messages.get(0).isPulled()).isEqualTo(false);
        assertThat(messages.get(0).getSenderPublicKey()).isEqualTo(PUBLIC_KEY_USER_A);
        assertThat(messages.get(0).getType()).isEqualTo(MessageType.REQUEST_MESSAGING);

        //verifying record on whitelist
        List<Whitelist> whitelists = this.whitelistRepository.findAll();
        assertThat(whitelists).hasSize(1);
        whitelists.forEach(w -> {
            assertThat(w.getPublicKey()).isEqualTo(PUBLIC_KEY_USER_A);
            assertThat(w.getState()).isEqualTo(WhitelistState.WAITING);
            assertThat(w.getInbox()).isEqualTo(inboxToWhichRequestIsSent);
        });
    }

    @Test
    void testSendDuplicatedApprovalRequest_shouldReturnException() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        final var requestUserB = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_B);

        //creating new inboxes
        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        this.inboxService.createInbox(requestUserB, Platform.ANDROID);

        final Inbox inboxToWhichRequestIsSent = this.inboxRepository.findByPublicKey(PUBLIC_KEY_USER_B).get();

        //sending approval request
        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_A, PUBLIC_KEY_USER_B, inboxToWhichRequestIsSent, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));

        assertThrows(
                RequestMessagingNotAllowedException.class,
                () -> messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_A, PUBLIC_KEY_USER_B, inboxToWhichRequestIsSent, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING))
        );
    }

    @Test
    void testConfirmApprovalRequest_shouldConfirmAndCreateConfirmationMessage() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        final var requestUserB = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_B);

        //creating new inboxes
        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        this.inboxService.createInbox(requestUserB, Platform.ANDROID);

        final Inbox confirmer = this.inboxRepository.findByPublicKey(PUBLIC_KEY_USER_A).get();
        final Inbox requester = this.inboxRepository.findByPublicKey(PUBLIC_KEY_USER_B).get();

        //sending approval request
        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_B, PUBLIC_KEY_USER_A, confirmer, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));

        //get sender public key - sender = requester in this case
        List<Message> messages = this.messageService.findAll();
        String publicKeyToConfirm = messages.get(0).getSenderPublicKey();

        this.whitelistService.connectRequesterAndReceiver(confirmer, requester, PUBLIC_KEY_USER_A, publicKeyToConfirm);
        this.messageService.sendMessageToInbox(new SendMessageToInboxQuery(confirmer.getPublicKey(), PUBLIC_KEY_USER_B, requester, CONFIRMATION_MESSAGE, MessageType.APPROVE_MESSAGING));

        List<Whitelist> whitelistAll = this.whitelistRepository.findAll().stream()
                .sorted(Comparator.comparing(Whitelist::getId))
                .collect(Collectors.toList());

        List<Message> messagesAll = this.messageService.findAll().stream()
                .sorted(Comparator.comparing(Message::getId))
                .collect(Collectors.toList());

        assertThat(whitelistAll).hasSize(2);
        assertThat(messagesAll).hasSize(2);

        assertThat(messagesAll.get(0).getMessage()).isEqualTo(REQUEST_APPROVAL_MESSAGE);
        assertThat(messagesAll.get(0).getSenderPublicKey()).isEqualTo(publicKeyToConfirm);
        assertThat(messagesAll.get(1).getMessage()).isEqualTo(CONFIRMATION_MESSAGE);
        assertThat(messagesAll.get(1).getType()).isEqualTo(MessageType.APPROVE_MESSAGING);
        assertThat(messagesAll.get(1).getSenderPublicKey()).isEqualTo(PUBLIC_KEY_USER_A);

        assertThat(whitelistAll.get(0).getPublicKey()).isEqualTo(PUBLIC_KEY_USER_B);
        assertThat(whitelistAll.get(0).getState()).isEqualTo(WhitelistState.APPROVED);
        assertThat(whitelistAll.get(1).getPublicKey()).isEqualTo(PUBLIC_KEY_USER_A);
        assertThat(whitelistAll.get(1).getState()).isEqualTo(WhitelistState.APPROVED);
    }

    @Test
    void testConfirmApprovalRequestTwice_shouldReturnException() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        final var requestUserB = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_B);

        //creating new inboxes
        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        this.inboxService.createInbox(requestUserB, Platform.ANDROID);

        final Inbox confirmer = this.inboxRepository.findByPublicKey(PUBLIC_KEY_USER_A).get();
        final Inbox requester = this.inboxRepository.findByPublicKey(PUBLIC_KEY_USER_B).get();

        //sending approval request
        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_B, PUBLIC_KEY_USER_A, confirmer, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));

        //get sender public key - sender = requester in this case
        List<Message> messages = this.messageService.findAll();
        String publicKeyToConfirm = messages.get(0).getSenderPublicKey();

        this.whitelistService.connectRequesterAndReceiver(confirmer, requester, PUBLIC_KEY_USER_A, publicKeyToConfirm);
        this.messageService.sendMessageToInbox(new SendMessageToInboxQuery(confirmer.getPublicKey(), PUBLIC_KEY_USER_B, requester, CONFIRMATION_MESSAGE, MessageType.APPROVE_MESSAGING));

        assertThrows(
                AlreadyApprovedException.class,
                () -> this.whitelistService.connectRequesterAndReceiver(confirmer, requester, PUBLIC_KEY_USER_A, publicKeyToConfirm)
        );
    }

    @Test
    void testBatchMessage_shouldBeSentAllMessages() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        final var requestUserB = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_B);
        final var requestUserC = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_C);
        final var requestUserD = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_D);

        //creating new inboxes
        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        this.inboxService.createInbox(requestUserB, Platform.IOS);
        this.inboxService.createInbox(requestUserC, Platform.IOS);
        this.inboxService.createInbox(requestUserD, Platform.IOS);

        final Inbox inboxA = this.inboxService.findInbox(PUBLIC_KEY_USER_A);
        final Inbox inboxB = this.inboxService.findInbox(PUBLIC_KEY_USER_B);
        final Inbox inboxC = this.inboxService.findInbox(PUBLIC_KEY_USER_C);
        final Inbox inboxD = this.inboxService.findInbox(PUBLIC_KEY_USER_D);

        //connect A with C and B with D
        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_A, PUBLIC_KEY_USER_C, inboxC, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));
        this.whitelistService.connectRequesterAndReceiver(inboxC, inboxA, PUBLIC_KEY_USER_C, PUBLIC_KEY_USER_A);

        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_B, PUBLIC_KEY_USER_D, inboxD, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));
        this.whitelistService.connectRequesterAndReceiver(inboxD, inboxB, PUBLIC_KEY_USER_D, PUBLIC_KEY_USER_B);

        // create and sign challenges for senders
        final String challengeForUserA = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_A));
        final String challengeForUserB = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_B));

        final String signatureForUserA = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_A, challengeForUserA);
        final String signatureForUserB = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_B, challengeForUserB);

        final var sendMessageBatchRequest = RequestCreatorTestUtil.createSendMessageBatchRequest(
                PUBLIC_KEY_USER_A,
                PUBLIC_KEY_USER_C,
                new SignedChallenge(challengeForUserA, signatureForUserA),

                PUBLIC_KEY_USER_B,
                PUBLIC_KEY_USER_D,
                new SignedChallenge(challengeForUserB, signatureForUserB)
        );

        this.messageService.sendMessagesBatch(sendMessageBatchRequest, 1);

        final var messageList = this.messageService.findAll();

        final var messagesByUserA = messageList
                .stream()
                .filter(it -> it.getSenderPublicKey().equals(PUBLIC_KEY_USER_A))
                .filter(it -> it.getType().equals(MessageType.MESSAGE))
                .toList();

        final var messagesByUserB = messageList
                .stream()
                .filter(it -> it.getSenderPublicKey().equals(PUBLIC_KEY_USER_B))
                .filter(it -> it.getType().equals(MessageType.MESSAGE))
                .toList();

        assertThat(messagesByUserA).hasSize(1);
        assertThat(messagesByUserB).hasSize(1);

        assertThat(messagesByUserA.get(0).getMessage()).isEqualTo(MESSAGE_TO_SENDER_1);
        assertThat(messagesByUserA.get(0).getSenderPublicKey()).isEqualTo(PUBLIC_KEY_USER_A);
        assertThat(messagesByUserA.get(0).getInbox().getPublicKey()).isEqualTo(PUBLIC_KEY_USER_C);

        assertThat(messagesByUserB.get(0).getMessage()).isEqualTo(MESSAGE_TO_SENDER_2);
        assertThat(messagesByUserB.get(0).getSenderPublicKey()).isEqualTo(PUBLIC_KEY_USER_B);
        assertThat(messagesByUserB.get(0).getInbox().getPublicKey()).isEqualTo(PUBLIC_KEY_USER_D);
    }


    @Test
    void testBatchMessage_v2_shouldBeSentAllMessages() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        final var requestUserB = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_B);
        final var requestUserC = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_C);
        final var requestUserD = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_D);

        //creating new inboxes
        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        this.inboxService.createInbox(requestUserB, Platform.IOS);
        this.inboxService.createInbox(requestUserC, Platform.IOS);
        this.inboxService.createInbox(requestUserD, Platform.IOS);

        final Inbox inboxA = this.inboxService.findInbox(PUBLIC_KEY_USER_A);
        final Inbox inboxB = this.inboxService.findInbox(PUBLIC_KEY_USER_B);
        final Inbox inboxC = this.inboxService.findInbox(PUBLIC_KEY_USER_C);
        final Inbox inboxD = this.inboxService.findInbox(PUBLIC_KEY_USER_D);

        //connect A with C and B with D
        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_A, PUBLIC_KEY_USER_C, inboxC, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));
        this.whitelistService.connectRequesterAndReceiver(inboxC, inboxA, PUBLIC_KEY_USER_C, PUBLIC_KEY_USER_A);

        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_B, PUBLIC_KEY_USER_D, inboxD, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));
        this.whitelistService.connectRequesterAndReceiver(inboxD, inboxB, PUBLIC_KEY_USER_D, PUBLIC_KEY_USER_B);

        // create and sign challenges for senders
        final String challengeForUserA = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_A));
        final String challengeForUserB = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_B));

        final String signatureForUserA = CryptoLibrary.instance.ecdsaSignV2(PRIVATE_KEY_USER_A, challengeForUserA);
        final String signatureForUserB = CryptoLibrary.instance.ecdsaSignV2(PRIVATE_KEY_USER_B, challengeForUserB);

        final var sendMessageBatchRequest = RequestCreatorTestUtil.createSendMessageBatchRequest(
                PUBLIC_KEY_USER_A,
                PUBLIC_KEY_USER_C,
                new SignedChallenge(challengeForUserA, signatureForUserA),

                PUBLIC_KEY_USER_B,
                PUBLIC_KEY_USER_D,
                new SignedChallenge(challengeForUserB, signatureForUserB)
        );

        this.messageService.sendMessagesBatch(sendMessageBatchRequest, 2);

        final var messageList = this.messageService.findAll();

        final var messagesByUserA = messageList
                .stream()
                .filter(it -> it.getSenderPublicKey().equals(PUBLIC_KEY_USER_A))
                .filter(it -> it.getType().equals(MessageType.MESSAGE))
                .toList();

        final var messagesByUserB = messageList
                .stream()
                .filter(it -> it.getSenderPublicKey().equals(PUBLIC_KEY_USER_B))
                .filter(it -> it.getType().equals(MessageType.MESSAGE))
                .toList();

        assertThat(messagesByUserA).hasSize(1);
        assertThat(messagesByUserB).hasSize(1);

        assertThat(messagesByUserA.get(0).getMessage()).isEqualTo(MESSAGE_TO_SENDER_1);
        assertThat(messagesByUserA.get(0).getSenderPublicKey()).isEqualTo(PUBLIC_KEY_USER_A);
        assertThat(messagesByUserA.get(0).getInbox().getPublicKey()).isEqualTo(PUBLIC_KEY_USER_C);

        assertThat(messagesByUserB.get(0).getMessage()).isEqualTo(MESSAGE_TO_SENDER_2);
        assertThat(messagesByUserB.get(0).getSenderPublicKey()).isEqualTo(PUBLIC_KEY_USER_B);
        assertThat(messagesByUserB.get(0).getInbox().getPublicKey()).isEqualTo(PUBLIC_KEY_USER_D);
    }

    @Test
    void testBatchMessage_oneMessageShouldBeExcluded() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        final var requestUserB = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_B);
        final var requestUserC = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_C);
        final var requestUserD = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_D);

        //creating new inboxes
        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        this.inboxService.createInbox(requestUserB, Platform.IOS);
        this.inboxService.createInbox(requestUserC, Platform.IOS);
        this.inboxService.createInbox(requestUserD, Platform.IOS);

        final Inbox inboxA = this.inboxService.findInbox(PUBLIC_KEY_USER_A);
        final Inbox inboxC = this.inboxService.findInbox(PUBLIC_KEY_USER_C);

        //connect A with C
        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_A, PUBLIC_KEY_USER_C, inboxC, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));
        this.whitelistService.connectRequesterAndReceiver(inboxC, inboxA, PUBLIC_KEY_USER_C, PUBLIC_KEY_USER_A);

        // create and sign challenges for senders
        final String challengeForUserA = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_A));
        final String challengeForUserB = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_B));

        final String signatureForUserA = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_A, challengeForUserA);
        final String signatureForUserB = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_B, challengeForUserB);

        final var sendMessageBatchRequest = RequestCreatorTestUtil.createSendMessageBatchRequest(
                PUBLIC_KEY_USER_A,
                PUBLIC_KEY_USER_C,
                new SignedChallenge(challengeForUserA, signatureForUserA),

                PUBLIC_KEY_USER_B,
                PUBLIC_KEY_USER_D,
                new SignedChallenge(challengeForUserB, signatureForUserB)
        );

        this.messageService.sendMessagesBatch(sendMessageBatchRequest, 1);

        final var messageList = this.messageService.findAll();

        final var messagesByUserA = messageList
                .stream()
                .filter(it -> it.getSenderPublicKey().equals(PUBLIC_KEY_USER_A))
                .filter(it -> it.getType().equals(MessageType.MESSAGE))
                .toList();

        final var messagesByUserB = messageList
                .stream()
                .filter(it -> it.getSenderPublicKey().equals(PUBLIC_KEY_USER_B))
                .filter(it -> it.getType().equals(MessageType.MESSAGE))
                .toList();

        assertThat(messagesByUserA).hasSize(1);
        assertThat(messagesByUserB).hasSize(0);

        assertThat(messagesByUserA.get(0).getMessage()).isEqualTo(MESSAGE_TO_SENDER_1);
        assertThat(messagesByUserA.get(0).getSenderPublicKey()).isEqualTo(PUBLIC_KEY_USER_A);
        assertThat(messagesByUserA.get(0).getInbox().getPublicKey()).isEqualTo(PUBLIC_KEY_USER_C);
    }

    @Test
    void testBatchMessage_allMessagesShouldBeExcluded() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        final var requestUserB = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_B);
        final var requestUserC = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_C);
        final var requestUserD = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_D);

        //creating new inboxes
        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        this.inboxService.createInbox(requestUserB, Platform.IOS);
        this.inboxService.createInbox(requestUserC, Platform.IOS);
        this.inboxService.createInbox(requestUserD, Platform.IOS);

        //not connect anyone. Noone is on whitelist.

        // create and sign challenges for senders
        final String challengeForUserA = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_A));
        final String challengeForUserB = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_B));

        final String signatureForUserA = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_A, challengeForUserA);
        final String signatureForUserB = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_B, challengeForUserB);

        final var sendMessageBatchRequest = RequestCreatorTestUtil.createSendMessageBatchRequest(
                PUBLIC_KEY_USER_A,
                PUBLIC_KEY_USER_C,
                new SignedChallenge(challengeForUserA, signatureForUserA),

                PUBLIC_KEY_USER_B,
                PUBLIC_KEY_USER_D,
                new SignedChallenge(challengeForUserB, signatureForUserB)
        );

        this.messageService.sendMessagesBatch(sendMessageBatchRequest, 1);

        final var messageList = this.messageService.findAll();

        assertThat(messageList).hasSize(0);
    }

    @Test
    void testBatchMessage_invalidSignature_shouldReturnInvalidChallengeException() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        final var requestUserB = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_B);
        final var requestUserC = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_C);
        final var requestUserD = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_D);

        //creating new inboxes
        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        this.inboxService.createInbox(requestUserB, Platform.IOS);
        this.inboxService.createInbox(requestUserC, Platform.IOS);
        this.inboxService.createInbox(requestUserD, Platform.IOS);

        final Inbox inboxA = this.inboxService.findInbox(PUBLIC_KEY_USER_A);
        final Inbox inboxB = this.inboxService.findInbox(PUBLIC_KEY_USER_B);
        final Inbox inboxC = this.inboxService.findInbox(PUBLIC_KEY_USER_C);
        final Inbox inboxD = this.inboxService.findInbox(PUBLIC_KEY_USER_D);

        //connect A with C and B with D
        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_A, PUBLIC_KEY_USER_C, inboxC, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));
        this.whitelistService.connectRequesterAndReceiver(inboxC, inboxA, PUBLIC_KEY_USER_C, PUBLIC_KEY_USER_A);

        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_B, PUBLIC_KEY_USER_D, inboxD, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));
        this.whitelistService.connectRequesterAndReceiver(inboxD, inboxB, PUBLIC_KEY_USER_D, PUBLIC_KEY_USER_B);

        // create and sign challenges for senders
        final String challengeForUserA = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_A));
        final String challengeForUserB = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_B));

        final String signatureForUserA = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_A, challengeForUserA);

        // create request with wrong signature in case of use B

        final var sendMessageBatchRequest = RequestCreatorTestUtil.createSendMessageBatchRequest(
                PUBLIC_KEY_USER_A,
                PUBLIC_KEY_USER_C,
                new SignedChallenge(challengeForUserA, signatureForUserA),

                PUBLIC_KEY_USER_B,
                PUBLIC_KEY_USER_D,
                new SignedChallenge(challengeForUserB, "dummy_signature")
        );

        assertThrows(
                InvalidChallengeSignature.class,
                () -> this.messageService.sendMessagesBatch(sendMessageBatchRequest, 1)
        );
    }


    @Test
    void testBatchMessage_invalidSignature_v2_shouldReturnInvalidChallengeException() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        final var requestUserB = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_B);
        final var requestUserC = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_C);
        final var requestUserD = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_D);

        //creating new inboxes
        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        this.inboxService.createInbox(requestUserB, Platform.IOS);
        this.inboxService.createInbox(requestUserC, Platform.IOS);
        this.inboxService.createInbox(requestUserD, Platform.IOS);

        final Inbox inboxA = this.inboxService.findInbox(PUBLIC_KEY_USER_A);
        final Inbox inboxB = this.inboxService.findInbox(PUBLIC_KEY_USER_B);
        final Inbox inboxC = this.inboxService.findInbox(PUBLIC_KEY_USER_C);
        final Inbox inboxD = this.inboxService.findInbox(PUBLIC_KEY_USER_D);

        //connect A with C and B with D
        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_A, PUBLIC_KEY_USER_C, inboxC, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));
        this.whitelistService.connectRequesterAndReceiver(inboxC, inboxA, PUBLIC_KEY_USER_C, PUBLIC_KEY_USER_A);

        this.messageService.sendRequestToPermission(new SendMessageToInboxQuery(PUBLIC_KEY_USER_B, PUBLIC_KEY_USER_D, inboxD, REQUEST_APPROVAL_MESSAGE, MessageType.REQUEST_MESSAGING));
        this.whitelistService.connectRequesterAndReceiver(inboxD, inboxB, PUBLIC_KEY_USER_D, PUBLIC_KEY_USER_B);

        // create and sign challenges for senders
        final String challengeForUserA = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_A));
        final String challengeForUserB = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_B));

        final String signatureForUserA = CryptoLibrary.instance.ecdsaSignV2(PRIVATE_KEY_USER_A, challengeForUserA);

        // create request with wrong signature in case of use B

        final var sendMessageBatchRequest = RequestCreatorTestUtil.createSendMessageBatchRequest(
                PUBLIC_KEY_USER_A,
                PUBLIC_KEY_USER_C,
                new SignedChallenge(challengeForUserA, signatureForUserA),

                PUBLIC_KEY_USER_B,
                PUBLIC_KEY_USER_D,
                new SignedChallenge(challengeForUserB, "dummy_signature")
        );

        assertThrows(
                InvalidChallengeSignature.class,
                () -> this.messageService.sendMessagesBatch(sendMessageBatchRequest, 2)
        );
    }

    @Test
    void testDeleteBatchInboxes_shouldBeDeleted() {
        final var requestUserA = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_A);
        final var requestUserB = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_B);
        final var requestUserC = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_C);
        final var requestUserD = RequestCreatorTestUtil.createInboxRequest(PUBLIC_KEY_USER_D);

        //creating new inboxes
        this.inboxService.createInbox(requestUserA, Platform.ANDROID);
        this.inboxService.createInbox(requestUserB, Platform.IOS);
        this.inboxService.createInbox(requestUserC, Platform.IOS);
        this.inboxService.createInbox(requestUserD, Platform.IOS);

        final List<Inbox> allInboxes = this.inboxRepository.findAll();
        assertThat(allInboxes.size()).isEqualTo(4);

        // create and sign challenges for senders
        final String challengeForUserA = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_A));
        final String challengeForUserB = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_B));
        final String challengeForUserC = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_C));
        final String challengeForUserD = this.challengeService.createChallenge(new CreateChallengeRequest(PUBLIC_KEY_USER_D));

        final String signatureForUserA = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_A, challengeForUserA);
        final String signatureForUserB = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_B, challengeForUserB);
        final String signatureForUserC = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_C, challengeForUserC);
        final String signatureForUserD = CryptoLibrary.instance.ecdsaSignV1(PRIVATE_KEY_USER_D, challengeForUserD);

        final List<DeletionRequest> deletionRequests = List.of(
                new DeletionRequest(PUBLIC_KEY_USER_A, new SignedChallenge(challengeForUserA, signatureForUserA)),
                new DeletionRequest(PUBLIC_KEY_USER_B, new SignedChallenge(challengeForUserB, signatureForUserB)),
                new DeletionRequest(PUBLIC_KEY_USER_C, new SignedChallenge(challengeForUserC, signatureForUserC)),
                new DeletionRequest(PUBLIC_KEY_USER_D, new SignedChallenge(challengeForUserD, signatureForUserD))
        );

        final BatchDeletionRequest batchDeletionRequest = new BatchDeletionRequest(deletionRequests);

        this.inboxService.deleteInboxBatch(batchDeletionRequest);

        final List<Inbox> allInboxes_shouldBeNone = this.inboxRepository.findAll();
        assertThat(allInboxes_shouldBeNone.size()).isEqualTo(0);
    }
}
