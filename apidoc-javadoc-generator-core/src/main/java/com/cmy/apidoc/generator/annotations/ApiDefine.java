package com.cmy.apidoc.generator.annotations;

import com.cmy.apidoc.generator.enums.ApiDocEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 集合元素类型定义
 * <p>
 * <p>@author chenmingyi
 * <p>@version 1.0
 * <p>date: 2017/7/12
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiDefine {
    String id();

    ApiDocEnum component();

    String[] msg() default "";
}
