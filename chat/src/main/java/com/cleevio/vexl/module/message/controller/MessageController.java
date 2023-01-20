package com.cleevio.vexl.module.message.controller;

import com.cleevio.vexl.common.security.filter.SecurityFilter;
import com.cleevio.vexl.common.util.NumberUtils;
import com.cleevio.vexl.module.challenge.service.ChallengeService;
import com.cleevio.vexl.module.challenge.service.query.VerifySignedChallengeQuery;
import com.cleevio.vexl.module.inbox.entity.Inbox;
import com.cleevio.vexl.module.inbox.service.InboxService;
import com.cleevio.vexl.module.message.dto.request.MessageRequest;
import com.cleevio.vexl.module.message.dto.request.SendMessageBatchRequest;
import com.cleevio.vexl.module.message.dto.request.SendMessageRequest;
import com.cleevio.vexl.module.message.dto.response.MessagesResponse;
import com.cleevio.vexl.module.message.entity.Message;
import com.cleevio.vexl.module.message.mapper.MessageMapper;
import com.cleevio.vexl.module.message.service.MessageService;
import com.cleevio.vexl.module.message.service.query.SendMessageToInboxQuery;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "Message")
@RestController
@RequestMapping("/api/v1/inboxes/messages")
@RequiredArgsConstructor
public class MessageController {

    private final InboxService inboxService;
    private final MessageService messageService;
    private final ChallengeService challengeService;
    private final MessageMapper messageMapper;

    @Autowired
    public MessageController(
            InboxService inboxService,
            MessageService messageService,
            ChallengeService challengeService,
            MessageMapper messageMapper,
            MeterRegistry registry
    ) {
        this.inboxService = inboxService;
        this.messageService = messageService;
        this.challengeService = challengeService;
        this.messageMapper = messageMapper;

        Gauge.builder("analytics.messages.count_total", messageService, MessageService::getTotalMessagesCount)
                .description("Total number of messages")
                .register(registry);

        Gauge.builder("analytics.messages.count_total", messageService, MessageService::getNotPulledMessagesCount)
                .description("Number of messages that users has not pulled")
                .register(registry);
    }

    @PutMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve messages from my inbox and set them as 'pulled'.", description = """
            Every user and every offer must have own inbox.\040
            Signature in the request params is to verify that the client owns the private key to the public key that he claims is his.\040
            First you need to retrieve challenge for verification in challenge API. Then sign it with private key and the signature send here.
            """)
    MessagesResponse retrieveMessages(@Valid @RequestBody MessageRequest request,
                                      @RequestHeader(value = SecurityFilter.HEADER_CRYPTO_VERSION, defaultValue = "1") final String cryptoVersionRaw) {
        final int cryptoVersion = NumberUtils.parseIntOrFallback(cryptoVersionRaw, 1);
        challengeService.verifySignedChallenge(new VerifySignedChallengeQuery(request.publicKey(), request.signedChallenge()), cryptoVersion);

        Inbox inbox = this.inboxService.findInbox(request.publicKey());
        List<Message> messages = this.messageService.retrieveMessages(inbox);
        return new MessagesResponse(messageMapper.mapList(messages));
    }

    @PostMapping
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @Operation(summary = "Send a message to the inbox.",
            description = "When user wants to contact someone, use this EP.")
    MessagesResponse.MessageResponse sendMessage(@Valid @RequestBody SendMessageRequest request,
                                                 @RequestHeader(value = SecurityFilter.HEADER_CRYPTO_VERSION, defaultValue = "1") final String cryptoVersionRaw) {
        final int cryptoVersion = NumberUtils.parseIntOrFallback(cryptoVersionRaw, 1);
        challengeService.verifySignedChallenge(new VerifySignedChallengeQuery(request.senderPublicKey(), request.signedChallenge()), cryptoVersion);

        Inbox receiverInbox = this.inboxService.findInbox(request.receiverPublicKey());
        return messageMapper.mapSingle(
                this.messageService.sendMessageToInbox(
                        new SendMessageToInboxQuery(
                                request.senderPublicKey(),
                                request.receiverPublicKey(),
                                receiverInbox,
                                request.message(),
                                request.messageType()
                        )
                )
        );
    }

    @PostMapping("/batch")
    @SecurityRequirements({
            @SecurityRequirement(name = SecurityFilter.HEADER_PUBLIC_KEY),
            @SecurityRequirement(name = SecurityFilter.HEADER_HASH),
            @SecurityRequirement(name = SecurityFilter.HEADER_SIGNATURE),
    })
    @Operation(summary = "Send more messages to the inboxes in one request.",
            description = """
                    Example for usage is when an user delete his app,
                    user must send to all his contacts info about it via message.
                    EP returns only successfully sent messages in response.
                    """)
    List<MessagesResponse.MessageResponse> sendMessagesInBatch(@RequestBody SendMessageBatchRequest request,
                                                               @RequestHeader(value = SecurityFilter.HEADER_CRYPTO_VERSION, defaultValue = "1") final String cryptoVersionRaw) {
        final int cryptoVersion = NumberUtils.parseIntOrFallback(cryptoVersionRaw, 1);
        return messageMapper.mapList(this.messageService.sendMessagesBatch(request, cryptoVersion));
    }
}
