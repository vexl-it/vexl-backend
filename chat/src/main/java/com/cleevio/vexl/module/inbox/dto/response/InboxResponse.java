package com.cleevio.vexl.module.inbox.dto.response;

import com.cleevio.vexl.module.inbox.entity.Inbox;

public record InboxResponse(

        String firebaseToken

) {

    public InboxResponse(Inbox inbox) {
        this(inbox.getToken());
    }
}
