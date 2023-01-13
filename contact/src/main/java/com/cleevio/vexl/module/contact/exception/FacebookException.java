package com.cleevio.vexl.module.contact.exception;

import com.cleevio.vexl.common.exception.ApiException;
import com.cleevio.vexl.common.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FacebookException extends ApiException {

    @Override
	protected ApiException.Module getModule() {
		return Module.CONTACT;
	}

	@Override
	protected ErrorType getErrorType() {
		return ContactErrorType.FACEBOOK;
	}
}
