package cn.blesslp.pastry;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.CookieManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.blesslp.pastry.adpt.BeanHandler;
import cn.blesslp.pastry.adpt.CallHandler;
import cn.blesslp.pastry.adpt.ReturnHandler;
import cn.blesslp.pastry.gson.NullStringToEmptyAdapterFactory;
import cn.blesslp.pastry.provider.GlobalParamProvider;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
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
    private static List<ReturnHandler> returnValHandlers = new ArrayList<>();
    private HashMap<String, GlobalParamProvider> globalParamProviderHashMap = new HashMap<>();

    static {
        returnValHandlers.add(new CallHandler());
        returnValHandlers.add(new BeanHandler());
    }



    private Gson gson = new GsonBuilder().registerTypeAdapterFactory(new NullStringToEmptyAdapterFactory()).create();
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

    public PastryConfig addNetworkInterceptor(Interceptor interceptor) {
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

    public PastryConfig addGlobalParamProvider(String key, GlobalParamProvider globalParamProvider) {
        globalParamProviderHashMap.put(String.valueOf(key), globalParamProvider);
        return this;
    }

    public PastryConfig setGlobalParamProvider(GlobalParamProvider globalParamProvider) {
        return addGlobalParamProvider(null, globalParamProvider);
    }

    protected Map<String, String> getGlobalParam(String key) {
        if (globalParamProviderHashMap.isEmpty()) {
            return null;
        }
        return globalParamProviderHashMap.get(key).provider();
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
    }

    /**
     * 添加返回值处理器
     *
     * @param returnValHandler
     * @return
     */
    public PastryConfig addReturnValHandler(ReturnHandler returnValHandler) {
        returnValHandlers.add(0, returnValHandler);
        return this;
    }

    /**
     * 获取返回值处理器
     *
     * @return
     */
    protected List<ReturnHandler> getReturnValHandlers() {
        return returnValHandlers;
    }

}
