package com.php25.common.validation.annotation;

import com.php25.common.validation.validator.Ipv4Validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author penghuiping
 * @date 2019/9/11 13:35
 */
@Target({FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = Ipv4Validator.class)
@Documented
public @interface Ipv4 {

    String message() default "ipv4地址不正确";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
