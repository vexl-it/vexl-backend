package com.cleevio.vexl.module.cryptocurrency.exception;

import com.cleevio.vexl.common.exception.ApiException;
import com.cleevio.vexl.common.exception.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class CoinException extends ApiException {

	@Override
	protected ApiException.Module getModule() {
		return ApiException.Module.CRYPTOCURRENCY;
	}

	@Override
	protected ErrorType getErrorType() {
		return CryptocurrencyErrorType.COINGECKO_ERROR;
	}
}
