package com.cmy.apidoc.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>date: 2017/7/18
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDesc {
    String value() default "";

    String desc() default "";
}
