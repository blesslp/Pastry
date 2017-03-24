package cn.blesslp.pastry.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @作者: Liufan
 * @时间: 2017/3/22
 * @功能描述:这个方法的结果将被映射到处理类的哪个方法
 */


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Mapping {
    String value();
}
