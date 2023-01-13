package com.cleevio.vexl.module.challenge.exception;

import com.cleevio.vexl.common.exception.ApiException;
import com.cleevio.vexl.common.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidChallengeSignature extends ApiException {

	@Override
	protected ApiException.Module getModule() {
		return Module.CHALLENGE;
	}

	@Override
	protected ErrorType getErrorType() {
		return ChallengeErrorType.INVALID_CHALLENGE;
	}
}
