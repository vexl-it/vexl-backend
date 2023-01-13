package com.cleevio.vexl.common.exception;

import org.springframework.lang.Nullable;

public interface ErrorType {

    String getCode();

    @Nullable
    String getMessage();
}
