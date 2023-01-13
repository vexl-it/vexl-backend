package com.cleevio.vexl.module.user.exception;

import com.cleevio.vexl.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorType implements ErrorType {

    USER_DUPLICATE("101", "User already exists."),
	USER_NOT_FOUND("103", "User not found."),
	VERIFICATION_EXPIRED("104", "Verification code expired or you entered the wrong code."),
	SIGNATURE_ERROR("105", "Error occurred during creating signature."),
	CHALLENGE_ERROR("106", "Error occurred during generating challenge."),
	INVALID_PK_HASH("108", "Server could not create message for signature. Public key or hash is invalid."),
	USERNAME_NOT_AVAILABLE("109", "Username is not available. Choose different username."),
	USER_PHONE_INVALID("110", "Phone number is invalid."),
	PREVIOUS_VERIFICATION_CODE_NOT_EXPIRED("111", "Previous verification code is not expired."),
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
