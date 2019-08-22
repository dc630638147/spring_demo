package com.af.formework.annotation;

import java.lang.annotation.*;

/**
 * @RequestMapping
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AfRequestMapper {
    String value() default "";
}
