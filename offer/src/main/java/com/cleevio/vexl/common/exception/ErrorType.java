package com.cleevio.vexl.common.exception;

import javax.annotation.Nullable;

public interface ErrorType {

    String getCode();

    @Nullable
    String getMessage();
}
