package com.cleevio.vexl.common.validator;

import com.cleevio.vexl.common.annotation.CheckValidImageExtension;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class AllowedImageExtensionValidator implements ConstraintValidator<CheckValidImageExtension, String> {

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpeg", "jpg");

    @Override
    public void initialize(CheckValidImageExtension constraintAnnotation) {
    }

    @Override
    public boolean isValid(String extension, ConstraintValidatorContext constraintContext) {
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }
}
