package cn.blesslp.pastry;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.blesslp.pastry.adpt.BeanHandler;
import cn.blesslp.pastry.adpt.CallHandler;
import cn.blesslp.pastry.adpt.ReturnHandler;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * @作者: Liufan
 * @时间: 2017/3/23
 * @功能描述:
 */


public final class PastryConfig {

    private OkHttpClient okHttpClient;
    private List<Interceptor> interceptors = new ArrayList<>();
    private List<Interceptor> networkInterceptors = new ArrayList<>();
    private Gson gson = new Gson();
    private Context appContext;
    private String appBaseUrl;

    public Gson getGson() {
        return gson;
    }

    private PastryConfig() {
    }

    private static final PastryConfig INSTANCE = new PastryConfig();

    private File getCacheDir() {
        Utils.checkNotNull(appContext, "PastryConfig.init(applicationContext);必须被调用");
        File cacheDir;
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
            cacheDir = appContext.getExternalCacheDir();
        } else {
            cacheDir = appContext.getCacheDir();
        }
        return cacheDir;
    }

    public String getHostUrl() {
        return appBaseUrl;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public PastryConfig setOkhttpClient(OkHttpClient okhttpClient) {
        this.okHttpClient = okhttpClient;
        return this;
    }

    public PastryConfig addInterceptor(Interceptor e) {
        interceptors.add(e);
        return this;
    }

    public PastryConfig addNetworkInterceptro(Interceptor interceptor) {
        networkInterceptors.add(interceptor);
        return this;
    }

    public static PastryConfig init(Context context) {
        INSTANCE.appContext = context.getApplicationContext();
        return INSTANCE;
    }

    protected static PastryConfig getInstance() {
        return INSTANCE;
    }

    public PastryConfig setHost(String appBaseUrl) {
        this.appBaseUrl = appBaseUrl;
        return this;
    }

    private void ifNullOkhttpClient() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient.Builder()
                    .cache(new Cache(getCacheDir(), 1024 * 1024 * 24))
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
    }

    public void applyConfig() {
        ifNullOkhttpClient();
        OkHttpClient.Builder builder = okHttpClient.newBuilder();
        for (Interceptor interceptor : interceptors) {
            builder.addInterceptor(interceptor);
        }

        for (Interceptor networkInterceptor : networkInterceptors) {
           builder.addNetworkInterceptor(networkInterceptor);
        }
        okHttpClient = null;
        okHttpClient = builder.build();
        applyDefaultConfig();
    }

    private void applyDefaultConfig() {

    }


    private ReturnHandler callHandler = new CallHandler();
    private ReturnHandler beanHandler = new BeanHandler();
    private ReturnHandler observerHandler;
    private ReturnHandler flowableHandler;

    public PastryConfig setObserverHandler(ReturnHandler observerHandler) {
        this.observerHandler = observerHandler;
        return this;
    }

    public PastryConfig setFlowableHandler(ReturnHandler flowableHandler) {
        this.flowableHandler = flowableHandler;
        return this;
    }

    public ReturnHandler provideCallHandler() {
        return callHandler;
    }

    public ReturnHandler provideBeanHandler() {
        return beanHandler;
    }

    public ReturnHandler provideObservableHandler() {
        return this.observerHandler;
    }

    public ReturnHandler provideFlowableHandler() {
        return this.flowableHandler;
    }

}
