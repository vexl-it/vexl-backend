package com.cleevio.vexl.common.annotation;

import com.cleevio.vexl.common.validator.AvatarSizeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {AvatarSizeValidator.class})
public @interface ValidAvatar {

    String message() default "avatar is exceeding maximum size";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

