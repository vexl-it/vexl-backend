package com.cleevio.vexl.module.contact.exception;

import com.cleevio.vexl.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContactErrorType implements ErrorType {

    FACEBOOK("100", "Issue on Facebook side"),
    INVALID_TOKEN("101", "Expired Facebook token"),
    MISSING_CONTACTS("102", "Import list is empty. Nothing to import."),
    INVALID_LEVEL("103", "Invalid connection level. Options - first, second, all. No case sensitive."),
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
