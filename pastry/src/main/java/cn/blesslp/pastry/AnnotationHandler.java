package cn.blesslp.pastry;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.blesslp.pastry.annotations.FileField;
import cn.blesslp.pastry.annotations.GET;
import cn.blesslp.pastry.annotations.GlobalParam;
import cn.blesslp.pastry.annotations.Header;
import cn.blesslp.pastry.annotations.Headers;
import cn.blesslp.pastry.annotations.JsonField;
import cn.blesslp.pastry.annotations.JsonParam;
import cn.blesslp.pastry.annotations.Mapping;
import cn.blesslp.pastry.annotations.POST;
import cn.blesslp.pastry.annotations.Param;
import cn.blesslp.pastry.annotations.Path;

/**
 * @作者: Liufan
 * @时间: 2017/3/22
 * @功能描述:处理器
 */


public class AnnotationHandler {

    private static List<? extends Processor> methodsProcessor;
    private static List<? extends Processor> parameterProcessor;
    private static List<? extends Processor> classProcessor;
    private static Gson gson = new Gson();

    abstract static class Processor {
        abstract void parse(MethodHandler methodHandler, Annotation annotation,RequestBuilder requestBuilder,Object arg) ;
        abstract boolean accept(Annotation annotation);
    }

    static {
        methodsProcessor = Arrays.asList(new $GlobalParam(),new $GET(),new $POST(),new $Mapping(),new $Headers());
        parameterProcessor = Arrays.asList(new $Param(),new $Path(),new $JsonParam(),new $JsonField(),new $File(),new $Header());
        classProcessor = Arrays.asList(new $GlobalParam());
    }

    public static void processMethod(MethodHandler handler,Annotation anno,RequestBuilder builder,Object arg) {
        for (Processor processor : methodsProcessor) {
            if(processor.accept(anno)) {
                processor.parse(handler,anno,builder,arg);
            }
        }
    }

    public static void processParameters(MethodHandler handler,Annotation anno,RequestBuilder builder,Object arg) {
        for (Processor processor : parameterProcessor) {
            if(processor.accept(anno)) {
                processor.parse(handler,anno,builder,arg);
            }
        }
    }

    public static void processClass(MethodHandler handler, Annotation anno, RequestBuilder builder, Object args) {
        for (Processor processor : classProcessor) {
            if (processor.accept(anno)) {
                processor.parse(handler, anno, builder, args);
            }
        }
    }

    /**88888888888888888888888888888888888888888888888888888888888888888888888888
     * 方法注解
     */



    final static class $GET extends Processor {

        @Override
        void parse(MethodHandler methodHandler, Annotation annotation, RequestBuilder requestBuilder, Object arg) {
            if(requestBuilder.isPost() || requestBuilder.isMultipart()) {
                throw new IllegalArgumentException(String.format("@GET时,不能存在@POST或包含Multipart参数,如:File等"));
            }
            requestBuilder.setGet(true);
            requestBuilder.setPathURL(((GET)annotation).value());
        }

        @Override
        boolean accept(Annotation annotation) {
            return annotation.annotationType() == GET.class;
        }
    }

    static final class $POST extends Processor{

        @Override
        void parse(MethodHandler methodHandler, Annotation annotation, RequestBuilder requestBuilder, Object arg) {
            if (requestBuilder.isGet()) {
                throw new IllegalArgumentException(String.format("@POST时,不能存在@GET"));
            }
            requestBuilder.setPost(true);
            requestBuilder.setPathURL(((POST)annotation).value());
        }

        @Override
        boolean accept(Annotation annotation) {
            return annotation.annotationType() == POST.class;
        }
    }

    static final class $Mapping extends Processor {

        @Override
        void parse(MethodHandler methodHandler, Annotation annotation, RequestBuilder requestBuilder, Object arg) {
            methodHandler.setMappingMethod(((Mapping)annotation).value());
        }

        @Override
        boolean accept(Annotation annotation) {
            return annotation.annotationType() == Mapping.class;
        }
    }

    static final class $Headers extends Processor {

        @Override
        void parse(MethodHandler methodHandler, Annotation annotation, RequestBuilder requestBuilder, Object arg) {
            String[] value = ((Headers) annotation).value();
            if(value == null || value.length == 0) return ;
            for (String keyValuePair : value) {
                String[] kv = keyValuePair.split("[\\:\\=]");
                if(kv == null || kv.length == 0) throw new IllegalArgumentException(String.format("%s类中%s方法,@Headers({%s})分隔符错误.只接受[:或=]分隔符"));
                requestBuilder.addHeader(kv[0],kv[1]);
            }
        }

        @Override
        boolean accept(Annotation annotation) {
            return annotation.annotationType() == Headers.class;
        }
    }

    /**88888888888888888888888888888888888888888888888888888888888888888888888888
     * 参数注解
     */
    static final class $GlobalParam extends Processor {

        @Override
        void parse(MethodHandler methodHandler, Annotation annotation, RequestBuilder requestBuilder, Object arg) {
            final String key = ((GlobalParam)annotation).value();
            Map<String, String> globalParam = PastryConfig.getInstance().getGlobalParam(key);
            if (globalParam != null && !globalParam.isEmpty()) {
                Set<Map.Entry<String, String>> entries = globalParam.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    requestBuilder.addParam(entry.getKey(), entry.getValue());
                }
            }
        }

        @Override
        boolean accept(Annotation annotation) {
            return annotation.annotationType() == GlobalParam.class;
        }
    }


    /**88888888888888888888888888888888888888888888888888888888888888888888888888
     * 参数注解
     */
    static final class $Param extends Processor {

        @Override
        void parse(MethodHandler methodHandler, Annotation annotation, RequestBuilder requestBuilder, Object arg) {
            String key = ((Param)annotation).value();
            if(TextUtils.isEmpty(key)) {
                throw new IllegalArgumentException(String.format("方法:%s中存在@Param(\'\'),key为空的情况",methodHandler.getPresentMethodName()));
            }
            String value;
            if(arg == null) return;

            if (arg instanceof String) {
                value = arg.toString();
            }else{
                value = gson.toJson(arg);
            }
            requestBuilder.addParam(key, value);
        }

        @Override
        boolean accept(Annotation annotation) {
            return annotation.annotationType() == Param.class;
        }
    }

    static final class $Path extends Processor {

        @Override
        void parse(MethodHandler methodHandler, Annotation annotation, RequestBuilder requestBuilder, Object arg) {
            String pathURL = requestBuilder.getPathURL();
            String urlPattern = ((Path)annotation).value();

            if(requestBuilder.isPost()) {
                throw new IllegalArgumentException(String.format("方法:%s是POST请求,不可用@Path"));
            }

            if(TextUtils.isEmpty(urlPattern) || arg == null) {
                throw new IllegalArgumentException(String.format("方法:%s中存在@Path(\'\')为空的情况",methodHandler.getPresentMethodName()));
            }
            requestBuilder.setPathURL(pathURL.replaceAll(String.format("\\{%s\\}",urlPattern),arg.toString()));
        }

        @Override
        boolean accept(Annotation annotation) {
            return annotation.annotationType() == Path.class;
        }
    }

    static final class $JsonParam extends Processor{

        @Override
        void parse(MethodHandler methodHandler, Annotation annotation, RequestBuilder requestBuilder, Object arg) {
            if (arg == null) {
                return;
            }
            JsonElement paramJson = gson.toJsonTree(arg);
            if (paramJson.isJsonArray()) {
                throw new IllegalArgumentException(String.format("方法%s,@JsonParam标注的不能是一个列表数据,建议: Javabean / Map",methodHandler.getPresentMethodName()));
            }
            if (paramJson.isJsonPrimitive()) {
                throw new IllegalArgumentException(String.format("方法%s,@JsonParam标注的不能是一个基本类型数据,建议: Javabean / Map",methodHandler.getPresentMethodName()));
            }

            JsonObject asJsonObject = paramJson.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entries = asJsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entries) {
                JsonElement value = entry.getValue();
                if(value == null || value.isJsonNull()) continue;
                requestBuilder.addParam(entry.getKey(), value.getAsString());
            }
        }

        @Override
        boolean accept(Annotation annotation) {
            return annotation.annotationType() == JsonParam.class;
        }
    }

    static final class $JsonField extends Processor {

        @Override
        void parse(MethodHandler methodHandler, Annotation annotation, RequestBuilder requestBuilder, Object arg) {
            if(requestBuilder.isGet()) {
                throw new IllegalArgumentException(String.format("方法:%s,@JsonField只能用于@POST情况",methodHandler.getPresentMethodName()));
            }

            requestBuilder.setJson(true);
            requestBuilder.setJsonField(arg);
        }

        @Override
        boolean accept(Annotation annotation) {
            return annotation.annotationType() == JsonField.class;
        }
    }

    static final class $File extends Processor {

        @Override
        void parse(MethodHandler methodHandler, Annotation annotation, RequestBuilder requestBuilder, Object arg) {
            if (requestBuilder.isGet()) {
                throw new IllegalArgumentException(String.format("方法:%s,@FileField只能用于@POST情况",methodHandler.getPresentMethodName()));
            }
            if (arg == null) {
                return;
            }
            String key = ((FileField)annotation).value();
            if (TextUtils.isEmpty(key)) {
                throw new IllegalArgumentException(String.format("方法:%s,@FileField(\'这里不能为空\')",methodHandler.getPresentMethodName()));
            }
            requestBuilder.setMultipart(true);
            if (arg instanceof File) {
                requestBuilder.addPart(key,(File)arg);
            } else if (arg instanceof File[]) {
                requestBuilder.addPart(key, (File[]) arg);
            }
        }

        @Override
        boolean accept(Annotation annotation) {
            return annotation.annotationType() == FileField.class;
        }
    }

    static final class $Header extends Processor {

        @Override
        void parse(MethodHandler methodHandler, Annotation annotation, RequestBuilder requestBuilder, Object arg) {
            String key = ((Header)annotation).value();
            if (TextUtils.isEmpty(key) || arg == null) return;
            requestBuilder.addHeader(key, arg.toString());
        }

        @Override
        boolean accept(Annotation annotation) {
            return annotation.annotationType() == Header.class;
        }
    }


}
