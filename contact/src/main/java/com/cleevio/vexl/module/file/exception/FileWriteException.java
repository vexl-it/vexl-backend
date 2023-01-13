package com.cleevio.vexl.module.file.exception;

import com.cleevio.vexl.common.exception.ApiException;
import com.cleevio.vexl.common.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class FileWriteException extends ApiException {

	@Override
	protected Module getModule() {
		return Module.FILE;
	}

	@Override
	protected ErrorType getErrorType() {
		return FileErrorType.FILE_WRITE;
	}
}
