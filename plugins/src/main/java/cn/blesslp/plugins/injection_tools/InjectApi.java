package cn.blesslp.plugins.injection_tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @作者: Liufan
 * @时间: 2017/5/5
 * @功能描述:Api接口标记
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectApi {
}
