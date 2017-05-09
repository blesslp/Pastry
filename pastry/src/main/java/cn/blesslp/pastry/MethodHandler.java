package cn.blesslp.pastry;

import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import cn.blesslp.pastry.adpt.ReturnHandler;
import okhttp3.Call;
import okhttp3.Request;

/**
 * @作者: Liufan
 * @时间: 2017/3/22
 * @功能描述:
 * 解析方法的注解信息
 */


public class MethodHandler {
    private RequestBuilder mBuilder;

    //映射到哪个方法
    private String mappingMethod;
    //泛型返回值
    private Type genericReturnType;
    //当前的方法
    private Method presentMethod;
    //方法上加的注解
    private Annotation[] methodAnnos;
    //方法参数上加的注解
    private Annotation[][] parameterAnnos;
    private boolean isReturnString;

    private ReturnHandler adpt;

    public void setAdpt(ReturnHandler adpt) {
        this.adpt = adpt;
    }

    public ReturnHandler getAdpt() {
        return adpt;
    }

    public MethodHandler(Method method) {
        initial(method);
        parseAnnotation();
    }

    public Method getPresentMethod() {
        return presentMethod;
    }

    /**
     * 解析注解
     */
    private void parseAnnotation() {
        this.mBuilder = new RequestBuilder(this);
        for (Annotation methodAnno : methodAnnos) {
            AnnotationHandler.processMethod(this,methodAnno,this.mBuilder,null);
        }
    }
    /**
     * 初始化不变的信息
     * @param method
     */
    private void initial(Method method) {
        this.methodAnnos = method.getDeclaredAnnotations();
        this.parameterAnnos = method.getParameterAnnotations();
        this.presentMethod = method;
        this.genericReturnType = method.getGenericReturnType();

        List<ReturnHandler> returnValHandlers = PastryConfig.getInstance().getReturnValHandlers();
        for (ReturnHandler rh : returnValHandlers) {
            boolean isParameterizedType = (this.genericReturnType instanceof ParameterizedType);
            if (isParameterizedType) {
                //是泛型参数
                if(rh.acceptArameterizedType()) {
                    //接受泛型参数
                    if (rh.apply(this.genericReturnType)) {
                        this.adpt = rh;
                        break;
                    }
                }
            }else{
                if (rh.apply(this.genericReturnType)) {
                    this.adpt = rh;
                    break;
                }
            }
        }
    }

    public void parseParameters(Object[] args) {
        if (args == null) {
            return;
        }
        for(int i=0,len = args.length;i < len;i++) {
            Annotation[] parameterAnno = this.parameterAnnos[i];
            if (parameterAnno == null || parameterAnno.length == 0) {
                throw new IllegalArgumentException(String.format("%s类中,%s方法的第%s个参数必须加相应的注解",presentMethod.getDeclaringClass().getName(),presentMethod.getName(),i));
            }
            for (Annotation annotation : parameterAnno) {
                AnnotationHandler.processParameters(this, annotation, this.mBuilder, args[i]);
            }
        }
    }


    public String getMappingMethod() {
        return TextUtils.isEmpty(mappingMethod)?presentMethod.getName():mappingMethod;
    }

    public String getPresentMethodName() {
        return presentMethod.getName();
    }

    public void setMappingMethod(String mappingMethod) {
        this.mappingMethod = mappingMethod;
    }

    public Type getGenericReturnType() {
        return genericReturnType;
    }

    public Request getRequest() {
       return mBuilder.makeRequestBody();
    }
}
