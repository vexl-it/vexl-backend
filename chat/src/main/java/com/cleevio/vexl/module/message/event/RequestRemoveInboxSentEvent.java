package com.cleevio.vexl.module.message.event;

import com.cleevio.vexl.module.inbox.entity.Inbox;

import javax.validation.constraints.NotNull;

public record RequestRemoveInboxSentEvent(

        @NotNull
        Inbox inbox
) {
}
