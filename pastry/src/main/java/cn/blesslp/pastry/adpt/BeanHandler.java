package cn.blesslp.pastry.adpt;

import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import cn.blesslp.pastry.MethodHandler;
import cn.blesslp.pastry.Pastry;
import cn.blesslp.pastry.PastryConfig;
import cn.blesslp.pastry.Utils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @作者: Liufan
 * @时间: 2017/3/23
 * @功能描述:
 */


public class BeanHandler extends ReturnHandler {

    private Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    public Object adapter(final Pastry pastry, final MethodHandler m) {
        //解析结果
        if(pastry.getTarget().get() == null) return null;
        OkHttpClient okHttpClient = pastry.getPastryConfig().getOkHttpClient();
        Request request = m.getRequest();
        Call call = okHttpClient.newCall(request);
        pastry.addCaller(call);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Object target = pastry.getTarget().get();
                Type genericReturnType = m.getGenericReturnType();
                getMethodAndInvoke(target, m.getMappingMethod(), Utils.getRawType(genericReturnType),null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                Type genericReturnType = m.getGenericReturnType();
                Object bean =null;
                if (m.isReturnVoid()) {
                    bean = null;
                } else if (m.isReturnBean()) {
                    bean = pastry.getPastryConfig().getGson().fromJson(json, genericReturnType);
                } else if (m.isReturnString()) {
                    bean = json;
                }
                Object target = pastry.getTarget().get();
                getMethodAndInvoke(target, m.getMappingMethod(), Utils.getRawType(genericReturnType),bean);
                call.cancel();
            }
        });
        //反射调用
        return null;
    }

    private void getMethodAndInvoke(final Object target, String methodname, Type returnType, final Object arg) {
        if(target == null)return;
        Class<?> aClass = target.getClass();
        try {
            final Method declaredMethod = aClass.getDeclaredMethod(methodname, (Class<?>) returnType);
            declaredMethod.setAccessible(true);
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        declaredMethod.invoke(target, arg);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("在%s中没有声明回调方法void %s(%s)",target.getClass().getName(),methodname,((Class<?>) returnType).getName()));
        }
    }
}
