package cn.blesslp.pastry.adpt;

import cn.blesslp.pastry.MethodHandler;
import cn.blesslp.pastry.Pastry;

/**
 * @作者: Liufan
 * @时间: 2017/3/23
 * @功能描述:
 */


public abstract class ReturnHandler<T> {
    public abstract T adapter(Pastry pastry,MethodHandler m);
}
