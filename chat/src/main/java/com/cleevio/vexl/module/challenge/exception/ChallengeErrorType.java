package com.cleevio.vexl.module.challenge.exception;

import com.cleevio.vexl.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengeErrorType implements ErrorType {

    COULD_NOT_CREATE_CHALLENGE("100", "Error occurred during a creating challenge."),
    CHALLENGE_EXPIRED("101", "Challenge expired. The challenge is only valid for 15 minutes and is for one-time use"),
    INVALID_CHALLENGE("102", "Challenge is invalid. You have sent wrong combination of public key and signature."),
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
