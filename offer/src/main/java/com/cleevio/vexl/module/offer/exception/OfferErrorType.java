package com.cleevio.vexl.module.offer.exception;

import com.cleevio.vexl.common.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OfferErrorType implements ErrorType {

	OFFER_NOT_FOUND("100", "Offer not found"),
	SIGNATURE_ERROR("101", "Error occurred during creating signature"),
	OFFER_ALREADY_EXISTS("102", "Offer with id already exists"),
	OFFER_NOT_CREATED("103", "Offer was not created. Error occurred during creating offerId."),
	MISSING_OWNER_PRIVATE_PART("104", "Cannot create/update an offer. Missing private part encrypted by offer owner's public key."),
	DATE_FORMAT_EXCEPTION("105", "Wrong date format. Correct date format example - 2022-04-09T09:42:53.000Z. Date MUST be in UTC."),
	DUPLICATED_PUBLIC_KEY("106", "There is more than one private part with the same public key. This is not allowed."),
	INCORRECT_ADMIN_ID_FORMAT("107", "Incorrect admin id format."),
	REPORT_LIMIT_REACHED("108", "You have reached the limit of reports. Try again later."),
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
