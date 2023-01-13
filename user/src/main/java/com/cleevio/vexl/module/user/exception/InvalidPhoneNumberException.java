package com.cleevio.vexl.module.user.exception;

import com.cleevio.vexl.common.exception.ApiException;
import com.cleevio.vexl.common.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidPhoneNumberException extends ApiException {

	@Override
	protected Module getModule() {
		return Module.USER;
	}

	@Override
	protected ErrorType getErrorType() {
		return UserErrorType.USER_PHONE_INVALID;
	}

}
