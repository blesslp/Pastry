package cn.blesslp.plugins.injection_tools;

import java.lang.reflect.Field;

import cn.blesslp.pastry.Pastry;

/**
 * @作者: Liufan
 * @时间: 2017/5/5
 * @功能描述:Pastry专属的注入器
 * Example:
 * @InjectApi(Api.class)
 * private Api api;
 *
 * private Pastry mPastry;
 * public void onCreate(Bundle savedInstance) {
 *     super.onCreate(savedInstance);
 *
 *     ...
 *     InjectApiProcessor.injectApi(mPastry,this);
 * }
 */


public class InjectApiProcessor {

    public static final void injectApi(Pastry mPastry, Object me) throws Exception {
        Class<?> meClass = me.getClass();
        Field[] declaredFields = meClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            boolean hasAnnotation = declaredField.isAnnotationPresent(InjectApi.class);
            if(!hasAnnotation)continue;
            handleInject(me, declaredField, mPastry);
        }
    }

    private static void handleInject(Object me, Field declaredField, Pastry mPastry) throws Exception {
        InjectApi injectApi = declaredField.getAnnotation(InjectApi.class);
        Class<?> apiInterface = declaredField.getType();
        if(!apiInterface.isInterface())
            throw new IllegalArgumentException(String.format("@InjectApi期望一个接口类型,但是接受的却是:%s", apiInterface.getName()));
        Object api = mPastry.create(apiInterface);
        declaredField.setAccessible(true);
        declaredField.set(me, api);
    }
}
