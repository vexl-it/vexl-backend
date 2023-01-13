package com.cleevio.vexl.module.message.event.listener;

import com.cleevio.vexl.module.message.event.RequestRemoveInboxSentEvent;
import com.cleevio.vexl.module.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.validation.Valid;

@Component
@RequiredArgsConstructor
class InboxMessageEventListener {

    private final MessageService messageService;


    @EventListener
    public void onFirebaseTokenInvalidatedEvent(@Valid RequestRemoveInboxSentEvent event) {
        this.messageService.deleteAllMessages(event.inbox());
    }

}
