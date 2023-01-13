package com.cleevio.vexl.module.export.exception;

import com.cleevio.vexl.common.exception.ApiException;
import com.cleevio.vexl.common.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ExportFailedException extends ApiException {

	@Override
	protected Module getModule() {
		return Module.EXPORT;
	}

	@Override
	protected ErrorType getErrorType() {
		return ExportErrorType.EXPORT_FAILED;
	}
}
