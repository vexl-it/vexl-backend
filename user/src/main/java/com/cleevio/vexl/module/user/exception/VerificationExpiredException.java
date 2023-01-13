package com.cleevio.vexl.module.user.exception;

import com.cleevio.vexl.common.exception.ApiException;
import com.cleevio.vexl.common.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class VerificationExpiredException extends ApiException {

	@Override
	protected ApiException.Module getModule() {
		return ApiException.Module.USER;
	}

	@Override
	protected ErrorType getErrorType() {
		return UserErrorType.VERIFICATION_EXPIRED;
	}
}
