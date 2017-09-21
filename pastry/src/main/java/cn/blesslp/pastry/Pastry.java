package cn.blesslp.pastry;


import android.os.Build;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * @作者: Liufan
 * @时间: 2017/3/22
 * @功能描述:动态代理的本体,解析方法,适配结果,转发
 */


public final class Pastry {

    private WeakHashMap<Method, MethodHandler> methodCache = new WeakHashMap<>(10);

    private MethodHandler loadMethod(Method method) {
        if (!methodCache.containsKey(method)) {
            MethodHandler temp = new MethodHandler(method);
            methodCache.put(method, temp);
        }
        return methodCache.get(method);
    }

    public <T> T create(Class<T> intf) {
        //检查被代理接口,是否接口或单接口
        Utils.validateServiceInterface(intf);
        return (T) Proxy.newProxyInstance(intf.getClassLoader(), new Class[]{intf}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //用户调用的是Object的方法
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(this, args);
                }
                MethodHandler methodHandler = loadMethod(method);
                //解析类上的注解  优先级最低
                methodHandler.parseClassAnnotation();
                //解析方法上的注解 优先级中
                methodHandler.parseAnnotation();
                //解析参数上的注解  优先级最高
                methodHandler.parseParameters(args);
                return methodHandler.getAdpt().adapter(Pastry.this, methodHandler);
            }
        });

    }

    private WeakReference<Object> target ;

    public WeakReference<Object> getTarget() {
        return target;
    }


    private Pastry(){
    }

    private PastryConfig pastryConfig;

    public PastryConfig getPastryConfig() {
        return pastryConfig;
    }

    public static Pastry newInstance(Object invoker) {
        Pastry pastry = new Pastry();
        pastry.target = new WeakReference(invoker);
        pastry.pastryConfig = PastryConfig.getInstance();
        if (TextUtils.isEmpty(pastry.pastryConfig.getHostUrl())) {
            throw new NullPointerException("PastryConfig必须通过setHost设置根url");
        }
        if (pastry.getPastryConfig().getOkHttpClient() == null) {
            pastry.getPastryConfig().applyConfig();
        }
        return pastry;
    }

    private List<WeakReference<Call>> callers = new ArrayList<>();
    public void addCaller(Call call) {
        this.callers.add(new WeakReference<Call>(call));
    }

    public void cancelAllTask() {
        for (WeakReference<Call> caller : callers) {
            Call call = caller.get();
            if (call != null && !call.isCanceled()) {
                call.cancel();
            }
            caller.clear();
        }
    }

}
