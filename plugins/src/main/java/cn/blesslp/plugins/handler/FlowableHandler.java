package cn.blesslp.plugins.handler;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import cn.blesslp.pastry.MethodHandler;
import cn.blesslp.pastry.Pastry;
import cn.blesslp.pastry.Utils;
import cn.blesslp.pastry.adpt.ReturnHandler;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @作者: Liufan
 * @时间: 2017/3/24
 * @功能描述:处理Flowable返回值
 */

public class FlowableHandler extends ReturnHandler<Flowable> {

    @Override
    public Flowable adapter(final Pastry pastry, final MethodHandler m) {
        Request request = m.getRequest();
        OkHttpClient okHttpClient = pastry.getPastryConfig().getOkHttpClient();
        final Call call = okHttpClient.newCall(request);

        return Flowable.create(new FlowableOnSubscribe() {
            @Override
            public void subscribe(FlowableEmitter e) throws Exception {
                try {
                    e.onNext(call.execute());
                    e.onComplete();
                } catch (Exception ex) {
                    e.onError(ex);
                }
            }
        }, BackpressureStrategy.DROP)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Function<Response, Object>() {
                    @Override
                    public Object apply(@NonNull Response o) throws Exception {
                        final String json = o.body().string();
                        Type genericReturnType = m.getGenericReturnType();
                        if (genericReturnType instanceof ParameterizedType) {
                            return parseObject(json, genericReturnType, pastry);
                        } else {
                            return json;
                        }
                    }
                }).observeOn(AndroidSchedulers.mainThread());

    }

    private Object parseObject(String json, Type genericReturnType, Pastry pastry) {
        Type callResponseType = Utils.getCallResponseType(genericReturnType);
        if (Utils.getRawType(callResponseType) == String.class) {
            return json;
        }
        try {
            return pastry.getPastryConfig().getGson().fromJson(json, callResponseType);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 接受io.reactivex.Flowable
     *
     * @param receiveType
     * @return
     */
    @Override
    public boolean apply(Type receiveType) {
        try {
            final Class<?> flowable = Class.forName("io.reactivex.Flowable");
            return Utils.getRawType(receiveType) == flowable;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 接受泛型
     *
     * @return
     */
    @Override
    public boolean acceptArameterizedType() {
        return true;
    }
}
