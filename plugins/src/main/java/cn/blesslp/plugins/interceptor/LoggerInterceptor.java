package cn.blesslp.plugins.interceptor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.HashMap;

import cn.blesslp.pastry.RequestBuilder;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @作者: Liufan
 * @时间: 2017/3/24
 * @功能描述:日志打印器,基于Logger
 */


public class LoggerInterceptor implements Interceptor{

    public enum LOG_TYPE {
        LOG_FULL,
        LOG_NONE
    }

    private LOG_TYPE log_type;

    public LoggerInterceptor(LOG_TYPE log_type) {
        this.log_type = log_type;
    }

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (this.log_type == LOG_TYPE.LOG_NONE) {
                return chain.proceed(request);
            }

            HashMap<String, Object> params = new HashMap<>();
            params.put(request.method() , request.url().toString());
            RequestBody body = request.body();
            if (body instanceof FormBody) {
                FormBody temp = (FormBody) body;
                handle(temp,params);
            } else if (body instanceof MultipartBody) {
                MultipartBody temp = (MultipartBody) body;
                handle(temp,params);
            } else if (body instanceof RequestBuilder.JsonBody) {
                RequestBuilder.JsonBody temp = (RequestBuilder.JsonBody) body;
                handle(temp,params);
            }
            Response proceed = chain.proceed(request);
            MediaType mediaType = proceed.body().contentType();
            String content = proceed.body().string();
            Logger.json(GSON.toJson(params));
            Logger.json(content);
            return proceed.newBuilder().body(ResponseBody.create(mediaType,content)).build();
        }

    private static void handle(FormBody formBody,HashMap<String,Object> params) {
        for(int i =0,len = formBody.size();i<len;i++) {
            params.put(formBody.name(i), formBody.value(i));
        }
    }

    private static void handle(MultipartBody multipartBody,HashMap<String,Object> params) {
        for(int i =0,len = multipartBody.size();i<len;i++) {
            MultipartBody.Part part = multipartBody.part(i);
            RequestBody body = part.body();
            if(body instanceof RequestBuilder.JsonBody){
                RequestBuilder.JsonBody temp = (RequestBuilder.JsonBody) body;
                params.put(temp.getDebugKey(), temp.getJsonContent());
            } else if (body instanceof RequestBuilder.FileBody) {
                RequestBuilder.FileBody temp = (RequestBuilder.FileBody) body;
                params.put(temp.getDebugKey(), "文件:" + temp.getSource().getAbsolutePath());
            }
        }
    }

    private static  void handle(RequestBuilder.JsonBody jsonBody,HashMap<String,Object> params) {
        params.put("JSON", jsonBody.getJsonContent());
    }
}
