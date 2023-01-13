package com.cleevio.vexl.module.user.exception;

import com.cleevio.vexl.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorType implements ErrorType {

    USER_DUPLICATE("100", "User already exists"),
    USER_NOT_FOUND("101", "User not found"),
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
