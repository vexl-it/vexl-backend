package com.cleevio.vexl.common.integration.firebase.exception;

import com.cleevio.vexl.common.exception.ApiException;
import com.cleevio.vexl.common.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class FirebaseException extends ApiException {

    @Override
	protected ApiException.Module getModule() {
		return Module.FIREBASE;
	}

	@Override
	protected ErrorType getErrorType() {
		return FirebaseErrorType.FIREBASE_DEEPLINK_EXCEPTION;
	}
}
