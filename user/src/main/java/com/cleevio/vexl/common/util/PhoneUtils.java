package com.cleevio.vexl.common.util;

import com.cleevio.vexl.module.user.exception.InvalidPhoneNumberException;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PhoneUtils {

    public static String trimAndDeleteSpacesFromPhoneNumber(String phoneNumber) throws InvalidPhoneNumberException {
        try {
        Phonenumber.PhoneNumber parsed = PhoneNumberUtil.getInstance().parse(phoneNumber, null);
        return PhoneNumberUtil.getInstance().format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            throw new InvalidPhoneNumberException();
        }
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        try {
            var number = PhoneNumberUtil.getInstance().parse(phoneNumber, null);
            return PhoneNumberUtil.getInstance().isValidNumber(number);
        } catch (Exception e) {
            return false;
        }
    }

    public static int getCountryPrefix(String phoneNumber) {
        try {
            Phonenumber.PhoneNumber parsed = PhoneNumberUtil.getInstance().parse(phoneNumber, null);
            return parsed.getCountryCode();
        } catch (Exception e) {
            return -1;
        }
    }
}
