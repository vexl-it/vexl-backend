package com.cleevio.vexl.module.contact.event.listener;

import com.cleevio.vexl.module.contact.service.ContactService;
import com.cleevio.vexl.module.contact.service.ImportService;
import com.cleevio.vexl.module.group.event.GroupCreatedEvent;
import com.cleevio.vexl.module.group.event.GroupJoinRequestedEvent;
import com.cleevio.vexl.module.group.event.GroupLeftEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;

@Component
@Validated
@RequiredArgsConstructor
class GroupContactEventListener {

    private final ImportService importService;
    private final ContactService contactService;

    @EventListener
    public void onGroupImportedEvent(@Valid final GroupCreatedEvent event) {
        this.importService.importGroupToContacts(event.user(), event.groupUuid());
    }

    @EventListener
    public void onGroupJoinRequestedEvent(@Valid final GroupJoinRequestedEvent event) {
        this.importService.importGroupToContacts(event.user(), event.groupUuid());
        this.contactService.storeNotificationsForLaterProcessing(event.groupUuid(), event.user().getPublicKey());
    }

    @EventListener
    public void onGroupLeftEvent(@Valid final GroupLeftEvent event) {
        this.contactService.deleteContactByHash(event.hash(), event.groupUuidHash());
    }

}
