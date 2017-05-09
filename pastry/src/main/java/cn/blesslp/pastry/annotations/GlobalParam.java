package cn.blesslp.pastry.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @作者: Liufan
 * @时间: 2017/5/9
 * @功能描述:
 */


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
public @interface GlobalParam {
    String value() default "null";
}
