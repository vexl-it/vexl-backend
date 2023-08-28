package com.cleevio.vexl.module.offer.exception;

import com.cleevio.vexl.common.exception.ApiException;
import com.cleevio.vexl.common.exception.ErrorType;

public class ReportLimitReachedException extends ApiException {

        @Override
        protected ApiException.Module getModule() {
            return ApiException.Module.OFFER;
        }

        @Override
        protected ErrorType getErrorType() {
            return OfferErrorType.REPORT_LIMIT_REACHED;
        }
}
