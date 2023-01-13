package com.cleevio.vexl.module.file.exception;

import com.cleevio.vexl.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileErrorType implements ErrorType {

	FILE_WRITE("100", "Issue with store of file."),
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
