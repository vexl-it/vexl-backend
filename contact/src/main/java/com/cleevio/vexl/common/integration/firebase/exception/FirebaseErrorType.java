package com.cleevio.vexl.common.integration.firebase.exception;

import com.cleevio.vexl.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FirebaseErrorType implements ErrorType {

		FIREBASE_DEEPLINK_EXCEPTION("100", "Firebase deeplink exception."),
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
