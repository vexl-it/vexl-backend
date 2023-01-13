package com.cleevio.vexl.util;

import com.cleevio.vexl.module.user.dto.request.CodeConfirmRequest;
import com.cleevio.vexl.module.user.dto.request.PhoneConfirmRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CreateRequestUtilTest {

    public static PhoneConfirmRequest createPhoneConfirmRequest(String phoneNumber) {
        return new PhoneConfirmRequest(
                phoneNumber
        );
    }

    public static CodeConfirmRequest createCodeConfirmRequest(Long id, String code, String publicKey) {
        return new CodeConfirmRequest(
                id,
                code,
                publicKey
        );
    }
}
