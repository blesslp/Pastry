package cn.blesslp.plugins.interceptor;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import org.afinal.simplecache.ACache;

import java.io.File;
import java.io.IOException;

import cn.blesslp.pastry.RequestBuilder;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @作者: Liufan
 * @时间: 2017/3/25
 * @功能描述:
 */


public class CacheInterceptor implements Interceptor {

    public final static String CACHE_CONTROL_KEY = "Pastry_CacheControl";
    public final static String CACHE_TIME_KEY = "Pastry_CacheTime";

    public final static String CACHE_CONTROL_ONLY_CACHE = "Pastry_CacheControl_OnlyCache";
    public final static String CACHE_CONTROL_CACHE_ELSE_NET = "Pastry_CacheControl_CacheElseNet";
    public final static String CACHE_CONTROL_NO = "Pastry_CacheControl_NO";

    private ACache mACache;

    public CacheInterceptor(Context context, File cacheDir) {
        if (cacheDir == null) {
            mACache = ACache.get(context);
        } else {
            mACache = ACache.get(cacheDir);
        }
    }

    public CacheInterceptor(Context context) {
        this(context, null);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        String headerControl = request.header(CACHE_CONTROL_KEY);
        String headerTime = request.header(CACHE_TIME_KEY);

        if (TextUtils.isEmpty(headerControl)) {
            return chain.proceed(request);
        }

        if (TextUtils.equals(CACHE_CONTROL_NO, headerControl)) {
            //仅仅网络,但是要缓存数据
            return getDataFromNet(request, chain, headerTime);
        } else if (TextUtils.equals(CACHE_CONTROL_CACHE_ELSE_NET, headerControl)) {
            //优先缓存,没有则网络,并缓存
            String dataFromCache = getDataFromCache(request);
            if (TextUtils.isEmpty(dataFromCache)) {
                return getDataFromNet(request, chain, headerTime);
            }
            return new Response.Builder().request(request).code(200).protocol(Protocol.HTTP_1_1).body(ResponseBody.create(RequestBuilder.JsonBody.JSON, dataFromCache)).build();
        } else if (TextUtils.equals(CACHE_CONTROL_ONLY_CACHE, headerControl)) {
            //仅仅缓存
            String dataFromCache = getDataFromCache(request);
            if (TextUtils.isEmpty(dataFromCache)) {
                return new Response.Builder().protocol(Protocol.HTTP_1_1).request(request).code(504).body(ResponseBody.create(null, "")).build();
            } else {
                return new Response.Builder().protocol(Protocol.HTTP_1_1).request(request).code(200).body(ResponseBody.create(RequestBuilder.JsonBody.JSON, dataFromCache)).build();

            }
        }

        return chain.proceed(request);
    }

    public String getUrl(Request request) {
        HttpUrl url = request.url();
        String baseUrl = url.toString();
        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        RequestBody body = request.body();
        if (body instanceof FormBody) {
            for (int i = 0, len = ((FormBody) body).size(); i < len; i++) {
                builder.appendQueryParameter(((FormBody) body).encodedName(i), ((FormBody) body).encodedValue(i));
            }
        } else if (body instanceof MultipartBody) {
            return null;
        } else if (body instanceof RequestBuilder.JsonBody) {
            return null;
        }
        return builder.toString();
    }

    public String getDataFromCache(Request request) {
        String uri = getUrl(request);
        return mACache.getAsString(uri);
    }

    public Response getDataFromNet(Request request, Chain chain, String cacheTime) throws IOException {
        String uri = getUrl(request);
        Response proceed = chain.proceed(request);
        if (proceed.isSuccessful()) {
            ResponseBody body = proceed.body();
            final MediaType mediaType = body.contentType();
            String content = body.string();
            int intCacheTime = 0;
            try {
                intCacheTime = Integer.parseInt(cacheTime);
            } catch (Exception e) {
                intCacheTime = ACache.TIME_HOUR;
            }
            mACache.put(uri, content, intCacheTime);
            return proceed.newBuilder().body(ResponseBody.create(mediaType, content)).build();
        } else {
            return proceed;
        }
    }
}
