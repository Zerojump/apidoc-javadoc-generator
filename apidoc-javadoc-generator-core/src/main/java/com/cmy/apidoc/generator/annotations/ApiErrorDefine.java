package com.cmy.apidoc.generator.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ${DESCRIPTION}
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>Date: 2017/7/16
 * <p>
 * To change this template use File | Settings | File and Code Templates | Includes .
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorDefine {

    String group() default "";
}
