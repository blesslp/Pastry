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

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Logger.d("%s: %s",request.method() , request.url());
            RequestBody body = request.body();
            if (body instanceof FormBody) {
                FormBody temp = (FormBody) body;
                handle(temp);
            } else if (body instanceof MultipartBody) {
                MultipartBody temp = (MultipartBody) body;
                handle(temp);
            } else if (body instanceof RequestBuilder.JsonBody) {
                RequestBuilder.JsonBody temp = (RequestBuilder.JsonBody) body;
                handle(temp);
            }
            Response proceed = chain.proceed(request);
            long l = proceed.body().contentLength();
            MediaType mediaType = proceed.body().contentType();
            String string = proceed.body().string();
            Logger.json(string);
            return proceed.newBuilder().body(ResponseBody.create(mediaType,string)).build();
        }

    private static final void handle(FormBody formBody) {
//        Logger.d("%s",formBody.contentType());
        HashMap<String, String> params = new HashMap<>();
        for(int i =0,len = formBody.size();i<len;i++) {
            params.put(formBody.name(i), formBody.value(i));
        }
        Logger.json(GSON.toJson(params));
    }

    private static final void handle(MultipartBody multipartBody) {
//        Logger.d("%s",multipartBody.contentType());
        HashMap<String, String> params = new HashMap<>();
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
        Logger.json(GSON.toJson(params));
    }

    private static final void handle(RequestBuilder.JsonBody jsonBody) {
//        Logger.d("%s",jsonBody.contentType());
        Logger.json(jsonBody.getJsonContent());
    }
}
