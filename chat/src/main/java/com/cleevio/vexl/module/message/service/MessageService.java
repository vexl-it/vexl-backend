package com.cleevio.vexl.module.message.service;

import com.cleevio.vexl.common.constant.ModuleLockNamespace;
import com.cleevio.vexl.common.service.AdvisoryLockService;
import com.cleevio.vexl.module.challenge.service.ChallengeService;
import com.cleevio.vexl.module.challenge.service.query.VerifySignedChallengeQuery;
import com.cleevio.vexl.module.inbox.dto.request.LeaveChatRequest;
import com.cleevio.vexl.module.inbox.exception.WhitelistMissingException;
import com.cleevio.vexl.module.inbox.service.InboxService;
import com.cleevio.vexl.module.message.constant.MessageAdvisoryLock;
import com.cleevio.vexl.module.message.dto.request.SendMessageBatchRequest;
import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.inbox.service.WhitelistService;
import com.cleevio.vexl.module.message.entity.Message;
import com.cleevio.vexl.module.message.constant.MessageType;
import com.cleevio.vexl.module.inbox.constant.WhitelistState;
import com.cleevio.vexl.module.inbox.event.NewMessageReceivedEvent;
import com.cleevio.vexl.module.inbox.exception.RequestMessagingNotAllowedException;
import com.cleevio.vexl.module.inbox.exception.WhiteListException;
import com.cleevio.vexl.module.message.service.query.SendMessageToInboxQuery;
import com.cleevio.vexl.module.stats.constant.StatsKey;
import com.cleevio.vexl.module.stats.dto.StatsDto;
import it.vexl.common.constants.ClientVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.cleevio.vexl.module.stats.constant.StatsKey.MESSAGES_ABSOLUTE_SUM;
import static com.cleevio.vexl.module.stats.constant.StatsKey.MESSAGES_NOT_PULLED_SUM;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final WhitelistService whitelistService;
    private final InboxService inboxService;
    private final ChallengeService challengeService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AdvisoryLockService advisoryLockService;

    @Transactional
    public List<Message> retrieveMessages(Inbox inbox, int clientVersion) {
        advisoryLockService.lock(
                ModuleLockNamespace.MESSAGE,
                MessageAdvisoryLock.RETRIEVE_MESSAGE.name(),
                inbox.getPublicKey()
        );

        List<Message> messages = inbox.getMessages()
                .stream()
                .sorted(Comparator.comparing(Message::getId))
                .toList();

        messages.forEach(m -> {
            m.setPulled(true);
            this.messageRepository.save(m);
        });

        if(clientVersion < ClientVersion.MIN_CLIENT_VERSION_THAT_UNDERSTANDS_CANCELING) {
            messages = messages.stream()
                    .filter(m -> m.getType() != MessageType.CANCEL_REQUEST_MESSAGING)
                    .toList();
        }

        return messages;
    }

    @Transactional
    public void deletePulledMessages(Inbox inbox) {
        advisoryLockService.lock(
                ModuleLockNamespace.MESSAGE,
                MessageAdvisoryLock.DELETE_MESSAGE.name(),
                inbox.getPublicKey()
        );

        this.messageRepository.deleteAllPulledMessages(inbox);
    }

    @Transactional
    public List<Message> sendMessagesBatch(@Valid SendMessageBatchRequest request, final int cryptoVersion) {
        verifyBatchChallenges(request, cryptoVersion);

        final List<SendMessageToInboxQuery> queryList = new ArrayList<>();

        if (areAllSendersInReceiversWhitelistApproved(request)) {
            request.data().forEach(it -> it.messages().forEach(m -> {
                final Inbox receiverInbox = this.inboxService.findInbox(m.receiverPublicKey());
                queryList.add(new SendMessageToInboxQuery(it.senderPublicKey(), m.receiverPublicKey(), receiverInbox, m.message(), m.messageType()));
            }));

        } else {
            // we have to iterate via each message and check if is whitelisted. Then not whitelisted exclude.
            request.data().forEach(it -> it.messages().forEach(m -> {
                final Inbox receiverInbox = this.inboxService.findInbox(m.receiverPublicKey());
                if (this.whitelistService.isSenderInWhitelistApproved(it.senderPublicKey(), receiverInbox)) {
                    queryList.add(new SendMessageToInboxQuery(it.senderPublicKey(), m.receiverPublicKey(), receiverInbox, m.message(), m.messageType()));
                } else {
                    log.info("Sender [{}] is blocked by receiver [{}] or not approve yet.", it.senderPublicKey(), receiverInbox);
                }
            }));
        }

        return saveBatchMessagesToInboxAndSendNotifications(queryList);
    }

    @Transactional
    public Message sendMessageToInbox(@Valid SendMessageToInboxQuery query) {
        advisoryLockService.lock(
                ModuleLockNamespace.MESSAGE,
                MessageAdvisoryLock.SEND_MESSAGE.name(),
                query.receiverPublicKey(), query.senderPublicKey()
        );

        if (!this.whitelistService.isSenderInWhitelistApproved(query.senderPublicKey(), query.receiverInbox())) {
            log.info("Sender [{}] is blocked by receiver [{}] or not approve yet.", query.senderPublicKey(), query.receiverInbox());
            throw new WhiteListException();
        }

        return this.saveMessageToInboxAndSendNotification(query, true);
    }

    @Transactional
    public Message sendRequestToPermission(@Valid SendMessageToInboxQuery query) {
        advisoryLockService.lock(
                ModuleLockNamespace.MESSAGE,
                MessageAdvisoryLock.SEND_MESSAGE.name(),
                query.receiverPublicKey(), query.senderPublicKey()
        );

        if (this.whitelistService.isSenderInWhitelist(query.senderPublicKey(), query.receiverInbox())) {
            log.warn("Sender [{}] has already sent a request for permission to messaging for inbox [{}]",
                    query.senderPublicKey(),
                    query.receiverInbox());
            throw new RequestMessagingNotAllowedException();
        }

        this.whitelistService.upsertSenderEntity(query.receiverInbox(), query.senderPublicKey(), WhitelistState.WAITING, LocalDate.now());

        return this.saveMessageToInboxAndSendNotification(query, true);
    }
    @Transactional
    public Message sendCancelRequestToPermission(@Valid SendMessageToInboxQuery query) {
        advisoryLockService.lock(
                ModuleLockNamespace.MESSAGE,
                MessageAdvisoryLock.SEND_MESSAGE.name(),
                query.receiverPublicKey(), query.senderPublicKey()
        );

        this.whitelistService.cancelSenderRequest(query.senderPublicKey(), query.receiverInbox());

        return this.saveMessageToInboxAndSendNotification(query, true);
    }


    @Transactional
    public Message sendLeaveChat(@Valid LeaveChatRequest leaveChatRequest) {
        advisoryLockService.lock(
                ModuleLockNamespace.MESSAGE,
                MessageAdvisoryLock.SEND_MESSAGE.name(),
                leaveChatRequest.receiverPublicKey(), leaveChatRequest.senderPublicKey()
        );

        Inbox receiverInbox = this.inboxService.findInbox(leaveChatRequest.receiverPublicKey());
        Inbox senderInbox = this.inboxService.findInbox(leaveChatRequest.senderPublicKey());

        if(
                !this.whitelistService.isSenderInWhitelistApproved(leaveChatRequest.senderPublicKey(), receiverInbox)
        ) {
            log.warn("Sender [{}] does not have permissions to chat with [{}]",
                    leaveChatRequest.senderPublicKey(),
                    receiverInbox);
            throw new WhitelistMissingException();
        }

        this.whitelistService.deleteFromWhiteList(receiverInbox, leaveChatRequest.senderPublicKey());
        this.whitelistService.deleteFromWhiteList(senderInbox, leaveChatRequest.receiverPublicKey());

        return this.saveMessageToInboxAndSendNotification(new SendMessageToInboxQuery(
                leaveChatRequest.senderPublicKey(),
                leaveChatRequest.receiverPublicKey(),
                receiverInbox,
                leaveChatRequest.message(),
                MessageType.DELETE_CHAT
        ), true);

    }

    @Transactional
    public Message sendDisapprovalMessage(@Valid SendMessageToInboxQuery query) {
        advisoryLockService.lock(
                ModuleLockNamespace.MESSAGE,
                MessageAdvisoryLock.SEND_MESSAGE.name(),
                query.receiverPublicKey(), query.senderPublicKey()
        );

        return this.saveMessageToInboxAndSendNotification(query, true);
    }

    @Transactional
    public void deleteAllMessages(Inbox inbox) {
        advisoryLockService.lock(
                ModuleLockNamespace.MESSAGE,
                MessageAdvisoryLock.DELETE_ALL_MESSAGES.name(),
                inbox.getPublicKey()
        );

        this.messageRepository.deleteAllMessages(inbox);
    }

    @Transactional(readOnly = true)
    public List<StatsDto> retrieveStats(final StatsKey... statsKeys) {
        final List<StatsDto> statsDtos = new ArrayList<>();
        Arrays.stream(statsKeys).forEach(statKey -> {
            switch (statKey) {
                case MESSAGES_ABSOLUTE_SUM -> statsDtos.add(new StatsDto(
                        MESSAGES_ABSOLUTE_SUM,
                        this.messageRepository.getLastValueInSequenceForMessage()
                ));
                case MESSAGES_NOT_PULLED_SUM -> statsDtos.add(new StatsDto(
                        MESSAGES_NOT_PULLED_SUM,
                        this.messageRepository.getNotPulledMessagesCount()
                ));
            }
        });
        return statsDtos;
    }

    @Transactional
    public void save(final Message message) {
        this.messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    @Transactional(readOnly = true)
    public int getTotalMessagesCount() {
        return messageRepository.getLastValueInSequenceForMessage();
    }

    @Transactional(readOnly = true)
    public int getNotPulledMessagesCount() {
        return messageRepository.getNotPulledMessagesCount();
    }

    private void verifyBatchChallenges(SendMessageBatchRequest request, final int cryptoVersion) {
        final List<VerifySignedChallengeQuery> queryListForVerification = new ArrayList<>();
        request.data().forEach(batch -> queryListForVerification.add(new VerifySignedChallengeQuery(batch.senderPublicKey(), batch.signedChallenge())));
        challengeService.verifySignedChallengeForBatch(queryListForVerification, cryptoVersion);
    }

    private boolean areAllSendersInReceiversWhitelistApproved(SendMessageBatchRequest request) {
        List<String> receiverPublicKeys = new ArrayList<>();
        request.data().forEach(it -> it.messages().forEach(message -> receiverPublicKeys.add(message.receiverPublicKey())));

        return this.whitelistService.areAllSendersInReceiversWhitelistApproved(
                request.data().stream().map(SendMessageBatchRequest.BatchData::senderPublicKey).toList(),
                receiverPublicKeys
        );
    }

    private Message saveMessageToInboxAndSendNotification(SendMessageToInboxQuery query, boolean sendNotification) {

        final Message messageEntity = createMessageEntity(query.senderPublicKey(), query.receiverInbox(), query.message(), query.messageType());
        final Message savedMessage = this.messageRepository.save(messageEntity);

        if (sendNotification && query.receiverInbox().getToken() != null) {
            this.applicationEventPublisher.publishEvent(
                    new NewMessageReceivedEvent(
                            query.receiverInbox().getToken(),
                            query.receiverInbox().getClientVersion(),
                            query.receiverInbox().getPlatform(),
                            query.messageType(),
                            query.receiverPublicKey(),
                            query.senderPublicKey()
                    ));
        }

        return savedMessage;
    }

    private List<Message> saveBatchMessagesToInboxAndSendNotifications(List<SendMessageToInboxQuery> queryList) {
        final List<Message> messageEntities = new ArrayList<>();
        queryList.forEach(it -> messageEntities.add(createMessageEntity(it.senderPublicKey(), it.receiverInbox(), it.message(), it.messageType())));

        List<Message> savedMessages = this.messageRepository.saveAll(messageEntities);

        queryList.forEach(query -> {
            if (query.receiverInbox().getToken() != null) {
                this.applicationEventPublisher.publishEvent(
                        new NewMessageReceivedEvent(
                                query.receiverInbox().getToken(),
                                query.receiverInbox().getClientVersion(),
                                query.receiverInbox().getPlatform(),
                                query.messageType(),
                                query.receiverPublicKey(),
                                query.senderPublicKey()
                        ));
            }
        });

        return savedMessages;
    }

    private Message createMessageEntity(String senderPublicKey, Inbox receiverInbox,
                                        String message, MessageType messageType) {
        return Message.builder()
                .message(message)
                .inbox(receiverInbox)
                .senderPublicKey(senderPublicKey)
                .type(messageType)
                .build();
    }
}
