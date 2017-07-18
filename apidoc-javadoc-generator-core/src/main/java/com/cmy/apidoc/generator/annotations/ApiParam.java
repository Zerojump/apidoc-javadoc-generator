package com.cmy.apidoc.generator.annotations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>Date: 2017/7/10
 * <p>
 * To change this template use File | Settings | File and Code Templates | Includes .
 */
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiParam {

    @AliasFor("value")
    String name() default "";

    @AliasFor("name")
    String value() default "";

    boolean required() default true;

    String size() default "";

    String allowedValues() default "";

    String desc() default "";

    String group() default "";

    String defaultValue() default "";
}
