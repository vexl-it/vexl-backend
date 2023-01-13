package com.cleevio.vexl.common.annotation;

import com.cleevio.vexl.common.validator.AllowedImageExtensionValidator;

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
@Constraint(validatedBy = AllowedImageExtensionValidator.class)
@Documented
public @interface CheckValidImageExtension {

    String message() default "Not allowed extension type.";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}