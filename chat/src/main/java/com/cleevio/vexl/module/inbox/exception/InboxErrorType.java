package com.cleevio.vexl.module.inbox.exception;

import com.cleevio.vexl.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InboxErrorType implements ErrorType {

    DUPLICATED_PUBLIC_KEY("100", "Public key is already used. You cannot create Inbox again."),
    INBOX_NOT_FOUND("101", "Inbox with sent public key does not exist. Create the inbox first."),
    MISSING_ON_WHITELIST("102", "You are not on whitelist. Either you are blocked or you have not yet been approve by the recipient to send messages."),
    PERMISSION_NOT_ALLOWED("103", "You cannot send request for messaging. Either you are blocked or you already have sent a request to the recipient."),
    BLOCK_EXCEPTION("104", "Contact you want to block/unblock is not on your whitelist. In order to block someone, they must first get on your whitelist." +
            " They will get on the whitelist if you confirm it via approval EP."),
    ALREADY_APPROVED("153", "You cannot approve or disapprove one user twice. If you blocked the user before, use unblock EP. " +
            "If you disapprove the user before, you can not take it back."),

    REQUEST_CANCELLED("106", "Request has been canceled by the other side. You can not accept canceled request."),
    SENDER_INBOX_NOT_FOUND("107", "Inbox of sender does not exist"),
    ;

    /**
     * Error custom code
     */
    private final String code;

    /**
     * Error custom message
     */
    private final String message;
}
