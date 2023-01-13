package com.cleevio.vexl.module.user.validator;

import com.cleevio.vexl.module.user.annotation.ValidAvatar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Base64;

public class AvatarSizeValidator implements ConstraintValidator<ValidAvatar, String> {

    @Value("${user.avatar.limit}")
    private int maxSize;

    public void initialize(ValidAvatar constraintAnnotation) {
    }

    @Override
    public boolean isValid(@Nullable String avatar, ConstraintValidatorContext constraintValidatorContext) {

        if (avatar == null) {
            return true;
        }

        final String[] data = avatar.split(",");

        if (data.length != 2) {
            return true;
        }

        try {
            final byte[] bytes = Base64.getDecoder().decode(data[1]);

            final int length = bytes.length;

            return length <= maxSize;
        } catch (IllegalArgumentException e) {
            // Skip validation if base64 is invalid
            return true;
        }
    }
}
