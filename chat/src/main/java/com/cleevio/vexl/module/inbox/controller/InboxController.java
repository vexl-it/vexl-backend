package com.cleevio.vexl.module.inbox.controller;

import com.cleevio.vexl.common.dto.ErrorResponse;
import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.common.util.NumberUtils;
import com.cleevio.vexl.module.challenge.service.ChallengeService;
import com.cleevio.vexl.module.challenge.service.query.VerifySignedChallengeQuery;
import com.cleevio.vexl.module.inbox.constant.Platform;
import com.cleevio.vexl.module.inbox.dto.request.*;
import com.cleevio.vexl.module.inbox.dto.response.InboxResponse;
import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.inbox.service.InboxService;
import com.cleevio.vexl.module.inbox.service.WhitelistService;
import com.cleevio.vexl.module.message.constant.MessageType;
import com.cleevio.vexl.module.message.dto.response.MessagesResponse;
import com.cleevio.vexl.module.message.entity.Message;
import com.cleevio.vexl.module.message.mapper.MessageMapper;
import com.cleevio.vexl.module.message.service.MessageService;
import com.cleevio.vexl.module.message.service.query.SendMessageToInboxQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Inbox")
@RestController
@RequestMapping("/api/v1/inboxes")
@RequiredArgsConstructor
public class InboxController {

    private final InboxService inboxService;
    private final ChallengeService challengeService;
    private final MessageService messageService;
    private final WhitelistService whitelistService;
    private final MessageMapper messageMapper;

    @PostMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Create a new inbox.", description = "Every user and every offer must have own inbox.")
    void createInbox(@RequestBody CreateInboxRequest request,
                     @RequestHeader(name = SecurityFilter.X_PLATFORM) String platform,
                     @RequestHeader(name = SecurityFilter.HEADER_CRYPTO_VERSION, defaultValue = "1") final String cryptoVersionRaw) {
        final int cryptoVersion = NumberUtils.parseIntOrFallback(cryptoVersionRaw, 1);
        challengeService.verifySignedChallenge(new VerifySignedChallengeQuery(request.publicKey(), request.signedChallenge()), cryptoVersion);
        this.inboxService.createInbox(request, Platform.valueOf(platform.toUpperCase()));
    }

    @PutMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Update a existing inbox.", description = "You can update only Firebase token.")
    ResponseEntity<InboxResponse> updateInbox(@RequestBody UpdateInboxRequest request,
                                              @RequestHeader(name = SecurityFilter.HEADER_CRYPTO_VERSION, defaultValue = "1") final String cryptoVersionRaw) {
        final int cryptoVersion = NumberUtils.parseIntOrFallback(cryptoVersionRaw, 1);
        challengeService.verifySignedChallenge(new VerifySignedChallengeQuery(request.publicKey(), request.signedChallenge()), cryptoVersion);
        return new ResponseEntity<>(new InboxResponse(this.inboxService.updateInbox(request)), HttpStatus.ACCEPTED);
    }

    @PutMapping("/block")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Block/unblock the public key so user can't send you a messages.")
    void blockInbox(@RequestBody BlockInboxRequest request,
                    @RequestHeader(name = SecurityFilter.HEADER_CRYPTO_VERSION, defaultValue = "1") final String cryptoVersionRaw) {
        final int cryptoVersion = NumberUtils.parseIntOrFallback(cryptoVersionRaw, 1);
        challengeService.verifySignedChallenge(new VerifySignedChallengeQuery(request.publicKey(), request.signedChallenge()), cryptoVersion);

        Inbox inbox = this.inboxService.findInbox(request.publicKey());
        this.whitelistService.blockPublicKey(inbox, request);
    }

    @PostMapping("/approval/request")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @Operation(summary = "Requesting of an approval to send a message.",
            description = "First of all you have to get to user's whitelist, if you want to send a message someone.")
    MessagesResponse.MessageResponse sendRequestToPermission(@RequestHeader(name = SecurityFilter.HEADER_PUBLIC_KEY) String publicKeySender,
                                                             @Valid @RequestBody ApprovalRequest request) {
        Inbox receiverInbox = this.inboxService.findInbox(request.publicKey());
        return messageMapper.mapSingle(
                this.messageService.sendRequestToPermission(
                        new SendMessageToInboxQuery(
                                publicKeySender,
                                request.publicKey(),
                                receiverInbox,
                                request.message(),
                                MessageType.REQUEST_MESSAGING
                        )

                )
        );
    }

    @PostMapping("/approval/cancel")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "404 (100104)", description = "No request sent yet there is nothing to cancel", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400 (100153)", description = "Already approved or denied. Approved or denied request can not be cancelled", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @Operation(summary = "Cancel approval",
            description = "Cancel approval to send messages to user.")
    MessagesResponse.MessageResponse sendCancelApproval(@RequestHeader(name = SecurityFilter.HEADER_PUBLIC_KEY) String publicKeySender,
                                                             @Valid @RequestBody ApprovalRequest request) {
        Inbox receiverInbox = this.inboxService.findInbox(request.publicKey());
        return messageMapper.mapSingle(
                this.messageService.sendCancelRequestToPermission(
                        new SendMessageToInboxQuery(
                                publicKeySender,
                                request.publicKey(),
                                receiverInbox,
                                request.message(),
                                MessageType.CANCEL_REQUEST_MESSAGING // TODO backward compatibility - this might cause the old app to crash
                        )

                )
        );
    }

    @PostMapping("/approval/confirm")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @Operation(summary = "Approve request for an user.",
            description = "You received request for approval to send messages. You can approve/disapprove it and add user to your whitelist with this EP.")
    @ApiResponse(responseCode = "400 (100153)", description = "Request was already approved", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400 (100106)", description = "Request was cancelled by the other side", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404 (100104)", description = "Request was not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))

    MessagesResponse.MessageResponse confirmPermission(@Valid @RequestBody ApprovalConfirmRequest request,
                                                       @RequestHeader(name = SecurityFilter.HEADER_CRYPTO_VERSION, defaultValue = "1") final String cryptoVersionRaw) {
        final int cryptoVersion = NumberUtils.parseIntOrFallback(cryptoVersionRaw, 1);
        challengeService.verifySignedChallenge(new VerifySignedChallengeQuery(request.publicKey(), request.signedChallenge()), cryptoVersion);

        Inbox requesterInbox = this.inboxService.findInbox(request.publicKeyToConfirm());
        Inbox inbox = this.inboxService.findInbox(request.publicKey());
        if (!request.approve()) {
            this.whitelistService.deletePendingFromWhiteList(inbox, request.publicKeyToConfirm());
            return messageMapper.mapSingle(
                    this.messageService.sendDisapprovalMessage(
                            new SendMessageToInboxQuery(
                                    request.publicKey(),
                                    request.publicKeyToConfirm(),
                                    requesterInbox,
                                    request.message(),
                                    MessageType.DISAPPROVE_MESSAGING
                            )
                    )
            );
        } else {
            this.whitelistService.connectRequesterAndReceiver(inbox, requesterInbox, request.publicKey(), request.publicKeyToConfirm());
            return messageMapper.mapSingle(
                    this.messageService.sendMessageToInbox(
                            new SendMessageToInboxQuery(
                                    request.publicKey(),
                                    request.publicKeyToConfirm(),
                                    requesterInbox,
                                    request.message(),
                                    MessageType.APPROVE_MESSAGING
                            )
                    )
            );
        }
    }

    @PostMapping("/leave-chat")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ApiResponse(responseCode = "404 (100104)", description = "You are not joined to this chat. Either there was no chat open or one side has already left", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Leave chat and remove any traces of relationship between you and the other side",
            description = "After calling this. New request will need to be sent in case of wanting to chat again.")
    MessagesResponse.MessageResponse leaveChat(@Valid @RequestBody LeaveChatRequest request,
                      @RequestHeader(name = SecurityFilter.HEADER_CRYPTO_VERSION, defaultValue = "1") final String cryptoVersionRaw) {
        final int cryptoVersion = NumberUtils.parseIntOrFallback(cryptoVersionRaw, 1);
        challengeService.verifySignedChallenge(new VerifySignedChallengeQuery(request.senderPublicKey(), request.signedChallenge()), cryptoVersion);

        return messageMapper.mapSingle(messageService.sendLeaveChat(request));
    }
    @DeleteMapping("/messages")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete messages what you already have pulled.",
            description = "After every pull, check if you have all messages and then remove them with this EP.")
    void deletePulledMessages(@Valid @RequestBody DeletionRequest request,
                              @RequestHeader(name = SecurityFilter.HEADER_CRYPTO_VERSION, defaultValue = "1") final String cryptoVersionRaw) {
        final int cryptoVersion = NumberUtils.parseIntOrFallback(cryptoVersionRaw, 1);
        challengeService.verifySignedChallenge(new VerifySignedChallengeQuery(request.publicKey(), request.signedChallenge()), cryptoVersion);

        Inbox inbox = this.inboxService.findInbox(request.publicKey());
        this.messageService.deletePulledMessages(inbox);
    }

    @DeleteMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete inbox with all messages.")
    void deleteInbox(@RequestBody DeletionRequest request,
                     @RequestHeader(name = SecurityFilter.HEADER_CRYPTO_VERSION, defaultValue = "1") final String cryptoVersionRaw) {
        final int cryptoVersion = NumberUtils.parseIntOrFallback(cryptoVersionRaw, 1);
        challengeService.verifySignedChallenge(new VerifySignedChallengeQuery(request.publicKey(), request.signedChallenge()), cryptoVersion);

        this.inboxService.deleteInbox(request);
    }

    @DeleteMapping("/batch")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Delete inboxes with all messages.")
    void deleteInboxBatch(@RequestBody BatchDeletionRequest request,
                          @RequestHeader(name = SecurityFilter.HEADER_CRYPTO_VERSION, defaultValue = "1") final String cryptoVersionRaw) {
        final int cryptoVersion = NumberUtils.parseIntOrFallback(cryptoVersionRaw, 1);
        final List<VerifySignedChallengeQuery> queryList = new ArrayList<>();
        request.dataForRemoval().forEach(it -> queryList.add(new VerifySignedChallengeQuery(it.publicKey(), it.signedChallenge())));
        challengeService.verifySignedChallengeForBatch(queryList, cryptoVersion);

        this.inboxService.deleteInboxBatch(request);
    }
}
