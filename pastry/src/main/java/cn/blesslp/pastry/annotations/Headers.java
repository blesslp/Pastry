package cn.blesslp.pastry.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @作者: Liufan
 * @时间: 2017/3/25
 * @功能描述:批量添加Header参数
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Headers {
    String[] value();
}
