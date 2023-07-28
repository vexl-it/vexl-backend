package com.cleevio.vexl.module.push.service;

import com.cleevio.vexl.module.inbox.event.NewMessageReceivedEvent;
import com.cleevio.vexl.module.push.dto.PushMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;


@Service
@Validated
@RequiredArgsConstructor
public class PushService {

    private final NotificationService notificationService;

    public void sendPushNotification(@Valid NewMessageReceivedEvent event) {
        switch (event.messageType()) {
            case MESSAGE ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "New message", "You have received a new message."));
            case APPROVE_MESSAGING ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "Request approved!", "Your request was approved."));
            case REQUEST_MESSAGING ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "New request!", "You have received a new request."));
            case DISAPPROVE_MESSAGING ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "Request denied", "Your request was denied."));
            case DELETE_CHAT ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "Chat deleted", "One of your chats has been deleted."));
            case REQUEST_REVEAL ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "Identity request received", "You have been requested to reveal your identity."));
            case APPROVE_REVEAL ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "Identity revealed!", "Your request to reveal identities was approved."));
            case DISAPPROVE_REVEAL ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "Identity request denied", "Your request to reveal identities was denied."));
            case BLOCK_CHAT ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "You've been blocked", "Someone just blocked you."));
            case CANCEL_REQUEST_MESSAGING ->
                    notificationService.sendPushNotification(createPushMessageDto(event, null, null));
        }
    }

    private PushMessageDto createPushMessageDto(
            NewMessageReceivedEvent event,
            @Nullable
            String title,
            @Nullable
            String text
    ) {
        return new PushMessageDto(
                title,
                text,
                event.token(),
                event.platform(),
                event.messageType(),
                event.receiverPublicKey(),
                event.senderPublicKey()
        );
    }
}
