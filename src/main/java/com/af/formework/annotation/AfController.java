package com.af.formework.annotation;

import java.lang.annotation.*;

/**
 * @Controller注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfController {
    String value() default "";
}
