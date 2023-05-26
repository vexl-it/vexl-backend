package com.cleevio.vexl.module.message.constant;

import java.util.EnumSet;

public enum MessageType {

    MESSAGE,
    REQUEST_REVEAL,
    APPROVE_REVEAL,
    DISAPPROVE_REVEAL,
    REQUEST_MESSAGING,
    APPROVE_MESSAGING,
    DISAPPROVE_MESSAGING,
    DELETE_CHAT,
    BLOCK_CHAT,
    OFFER_DELETED,
    INBOX_DELETED
    ;

    public final static EnumSet<MessageType> NOT_ALLOWED_MESSAGE_TYPES = EnumSet.of(DISAPPROVE_MESSAGING, REQUEST_MESSAGING, APPROVE_MESSAGING);
}
