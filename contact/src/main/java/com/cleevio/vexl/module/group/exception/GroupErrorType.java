package com.cleevio.vexl.module.group.exception;

import com.cleevio.vexl.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
enum GroupErrorType implements ErrorType {

    GROUP_NOT_FOUND("100", "Group not found."),
    QR_CODE_ERROR("101", "Error occurred during generating group QR code."),
    ;

	private final String code;
	private final String message;
}
