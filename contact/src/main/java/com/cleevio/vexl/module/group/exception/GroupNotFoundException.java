package com.cleevio.vexl.module.group.exception;

import com.cleevio.vexl.common.exception.ApiException;
import com.cleevio.vexl.common.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GroupNotFoundException extends ApiException {

    @Override
	protected ApiException.Module getModule() {
		return Module.GROUP;
	}

	@Override
	protected ErrorType getErrorType() {
		return GroupErrorType.GROUP_NOT_FOUND;
	}
}
