package cn.blesslp.pastry.provider;

import java.util.Map;

/**
 * @作者: Liufan
 * @时间: 2017/5/9
 * @功能描述: 提供一个全局参数
 */
public interface GlobalParamProvider {

    public Map<String, String> provider();
}
