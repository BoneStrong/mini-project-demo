package com.dzz.graphql;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author zoufeng
 * @date 2020-3-20
 *
 * 用于获取spring绑定参数的名称
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GraphqlParam {

    @AliasFor("name")
    String value() default "";

    @AliasFor("value")
    String name() default "";

    boolean required() default true;
}
