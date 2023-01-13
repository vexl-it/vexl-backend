package com.cleevio.vexl.module.cryptocurrency.exception;

import com.cleevio.vexl.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CryptocurrencyErrorType implements ErrorType {

    COINGECKO_ERROR("100", "Coingecko was not able to process request."),
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
