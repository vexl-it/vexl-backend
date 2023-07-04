package com.cleevio.vexl.module.inbox.service;

import com.cleevio.vexl.module.inbox.constant.WhitelistState;
import com.cleevio.vexl.module.inbox.dto.request.BlockInboxRequest;
import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.inbox.entity.Whitelist;
import com.cleevio.vexl.module.inbox.exception.AlreadyApprovedException;
import com.cleevio.vexl.module.inbox.exception.RequestCancelledException;
import com.cleevio.vexl.module.inbox.exception.WhitelistMissingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class WhitelistService {

    @Value("${whitelist.chat_request_timeout_days}")
    private int REQUEST_TIMEOUT_DAYS;

    private final WhitelistRepository whitelistRepository;

    public boolean isSenderInWhitelistApproved(String publicKeySenderHash, Inbox receiverInbox) {
        return this.whitelistRepository.isSenderInWhitelist(publicKeySenderHash, receiverInbox, WhitelistState.APPROVED);
    }

    public boolean areAllSendersInReceiversWhitelistApproved(List<String> publicKeySender, List<String> receiverPublicKeys) {
        if (publicKeySender.size() == receiverPublicKeys.size() && receiverPublicKeys.size() > 0) {
            return this.whitelistRepository.areAllSendersInReceiversWhitelistApproved(publicKeySender, receiverPublicKeys, WhitelistState.APPROVED, (long) publicKeySender.size());
        }
        return false;
    }

    public boolean isSenderInWhitelist(String publicKeySenderHash, Inbox receiverInbox) {
        final Optional<Whitelist> whitelistRecordOptional = this.whitelistRepository.findOnWhitelist(receiverInbox, publicKeySenderHash);
        if(whitelistRecordOptional.isEmpty()) return false;
        Whitelist whitelistRecord = whitelistRecordOptional.get();

        // If request is pending or cancelled, check if timeout was reached
        if(whitelistRecord.getState().equals(WhitelistState.WAITING) || whitelistRecord.getState().equals(WhitelistState.CANCELED)) {
            LocalDate canRequestAgainFrom = whitelistRecord.getDate().plusDays(REQUEST_TIMEOUT_DAYS);
            LocalDate now = LocalDate.now();

            return now.isBefore(canRequestAgainFrom);
        }

        // For all other states of whitelist record, sender is in whitelist
        return true;
    }

    public void cancelSenderRequest(String publicKeySenderHash, Inbox receiverInbox) {
        Whitelist whitelistRecord = this.whitelistRepository
                .findOnWhitelist(receiverInbox, publicKeySenderHash)
                .orElseThrow(WhitelistMissingException::new);

        if(!whitelistRecord.getState().equals(WhitelistState.WAITING)) {
            throw new AlreadyApprovedException();
        }

        whitelistRecord.setState(WhitelistState.CANCELED);
        whitelistRepository.save(whitelistRecord);
    }

    @Transactional
    public void connectRequesterAndReceiver(Inbox inbox, Inbox requesterInbox, String senderPublicKey, String publicKeyToConfirm) {
        Whitelist whitelist = this.findWaitingWhitelistByInboxAndPublicKey(inbox, publicKeyToConfirm);

        whitelist.setState(WhitelistState.APPROVED);
        this.whitelistRepository.save(whitelist);

        createWhiteListEntity(requesterInbox, senderPublicKey, WhitelistState.APPROVED, LocalDate.now());
        log.info("New public key [{}] was successfully saved into whitelist for inbox [{}]", publicKeyToConfirm, inbox);
    }

    @Transactional
    public void upsertSenderEntity(Inbox inbox, String publicKey, WhitelistState state, LocalDate date) {
        var whitelistEntityOptional = whitelistRepository.findOnWhitelist(inbox, publicKey);
        if(whitelistEntityOptional.isEmpty()) {
            createWhiteListEntity(inbox, publicKey, state, date);
            return;
        }
        Whitelist whitelistEntity = whitelistEntityOptional.get();
        whitelistEntity.setState(state);
        whitelistEntity.setDate(date);
        this.whitelistRepository.save(whitelistEntity);
    }

    @Transactional
    public void createWhiteListEntity(Inbox inbox, String publicKey, WhitelistState state, LocalDate date) {
        Whitelist whitelist = Whitelist.builder()
                .publicKey(publicKey)
                .state(state)
                .inbox(inbox)
                .date(date)
                .build();
        this.whitelistRepository.save(whitelist);
    }

    @Transactional
    public void blockPublicKey(@NotNull Inbox inbox, @Valid BlockInboxRequest request) {
        Whitelist whitelist = this.whitelistRepository.findOnWhitelist(inbox, request.publicKeyToBlock())
                .orElseThrow(WhitelistMissingException::new);

        whitelist.setState(request.block() ? WhitelistState.BLOCKED : WhitelistState.APPROVED);
        this.whitelistRepository.save(whitelist);
        log.info("[{}] has been blocked [{}]", whitelist, request.block());
    }

    @Transactional
    public void deletePendingFromWhiteList(Inbox inbox, String publicKey) {
        this.whitelistRepository.delete(this.findWaitingWhitelistByInboxAndPublicKey(inbox, publicKey));
    }

    @Transactional
    public void deleteFromWhiteList(Inbox inbox, String publicKey) {
        this.whitelistRepository.findOnWhitelist(inbox, publicKey)
                .ifPresent(this.whitelistRepository::delete);
    }


    private Whitelist findWaitingWhitelistByInboxAndPublicKey(Inbox inbox, String publicKey) {
        var whitelistEntity = this.whitelistRepository.findOnWhitelist(inbox, publicKey)
                .orElseThrow(AlreadyApprovedException::new);

        switch (whitelistEntity.getState()) {
            case WAITING -> {
                return whitelistEntity;
            }
            case APPROVED -> {
                throw new AlreadyApprovedException();
            }
            case CANCELED -> {
                throw new RequestCancelledException();
            }
            case BLOCKED -> {
                throw new WhitelistMissingException();
            }
            default -> throw new WhitelistMissingException();
        }
    }
}
