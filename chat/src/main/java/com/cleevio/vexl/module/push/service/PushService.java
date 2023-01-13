package com.cleevio.vexl.module.push.service;

import com.cleevio.vexl.module.inbox.event.NewMessageReceivedEvent;
import com.cleevio.vexl.module.push.dto.PushMessageDto;
import lombok.RequiredArgsConstructor;
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
                    notificationService.sendPushNotification(createPushMessageDto(event, "Approval", "You have been approved."));
            case REQUEST_MESSAGING ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "New request", "You have received a new request."));
            case DISAPPROVE_MESSAGING ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "Approval rejected", "You have been rejected."));
            case DELETE_CHAT ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "Chat deletion", "One of your chats has been deleted."));
            case REQUEST_REVEAL ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "Request reveal", "You have been requested for reveal."));
            case APPROVE_REVEAL ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "Approve reveal", "Reveal was approved."));
            case DISAPPROVE_REVEAL ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "Disapproval reveal", "Request for reveal was disapprove."));
            case BLOCK_CHAT ->
                    notificationService.sendPushNotification(createPushMessageDto(event, "Chat blocked", "User blocked chat with you."));
        }
    }

    private PushMessageDto createPushMessageDto(NewMessageReceivedEvent event, String title, String text) {
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
