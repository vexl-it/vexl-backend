package com.cleevio.vexl.module.export.exception;

import com.cleevio.vexl.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExportErrorType implements ErrorType {

    EXPORT_FAILED("100", "Export failed.."),
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
