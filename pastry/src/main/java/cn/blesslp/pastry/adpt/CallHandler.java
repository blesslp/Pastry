package cn.blesslp.pastry.adpt;

import cn.blesslp.pastry.MethodHandler;
import cn.blesslp.pastry.Pastry;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @作者: Liufan
 * @时间: 2017/3/23
 * @功能描述:
 */


public class CallHandler extends ReturnHandler<Call> {
    @Override
    public Call adapter(Pastry pastry,MethodHandler m) {
        OkHttpClient okHttpClient = pastry.getPastryConfig().getOkHttpClient();
        Request req = m.getRequest();
        Call call = okHttpClient.newCall(req);
        pastry.addCaller(call);
        return call;
    }
}
