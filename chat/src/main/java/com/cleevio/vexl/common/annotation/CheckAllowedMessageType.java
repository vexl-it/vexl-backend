package com.cleevio.vexl.common.annotation;

import com.cleevio.vexl.common.validator.AllowedMessageTypeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@ReportAsSingleViolation
@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = AllowedMessageTypeValidator.class)
@Documented
public @interface CheckAllowedMessageType {

    String message() default "Not allowed message type.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
