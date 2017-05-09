package cn.blesslp.plugins.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @作者: Liufan
 * @时间: 2017/5/5
 * @功能描述:
 */


public abstract class BaseInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request mRequest = chain.request();
        this.onBefore(mRequest);
        final Response mResponse = chain.proceed(mRequest);
        return this.onVisit(mRequest,mResponse);
    }

    /**
     *
     * @param request       请求体包装类
     * @param response      响应体包装类
     * @return  Response    如果不用处理请求相关,则原样返回Response
     */
    public Response onVisit(Request request, Response response){
        return response;
    }

    public void onBefore(Request request) {

    }

    /**
     * 如果消费了Response,在返回是不行的
     * 这时候需要重装一个新的Response给后面的处理器链
     * @param body              原Response的body()
     * @param oldResponse       原Response
     * @param content           新Response携带的响应数据
     * @return  新的Response
     */
    public Response newResponse(ResponseBody body,Response oldResponse,String content) {
        final MediaType mediaType = body.contentType();
        return oldResponse.newBuilder().body(ResponseBody.create(mediaType, content)).build();
    }

}
