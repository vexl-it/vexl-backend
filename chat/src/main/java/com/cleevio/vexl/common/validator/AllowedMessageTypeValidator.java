package com.cleevio.vexl.common.validator;

import com.cleevio.vexl.common.annotation.CheckAllowedMessageType;
import com.cleevio.vexl.module.message.constant.MessageType;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.cleevio.vexl.module.message.constant.MessageType.NOT_ALLOWED_MESSAGE_TYPES;

public class AllowedMessageTypeValidator implements ConstraintValidator<CheckAllowedMessageType, String> {

    @Override
    public void initialize(CheckAllowedMessageType constraintAnnotation) {
    }

    @Override
    public boolean isValid(@Nullable String type, ConstraintValidatorContext constraintContext) {
        return type != null && !NOT_ALLOWED_MESSAGE_TYPES.contains(type);
    }
}
