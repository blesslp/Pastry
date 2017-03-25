package cn.blesslp.pastry;

import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * @作者: Liufan
 * @时间: 2017/3/22
 * @功能描述:
 */


public class RequestBuilder {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String pathURL;

    private MethodHandler methodHandler;

    public RequestBuilder(MethodHandler methodHandler) {
        this.methodHandler = methodHandler;
    }

    public void setPathURL(String pathURL) {
        this.pathURL = pathURL;
    }

    public String getPathURL() {
        return pathURL;
    }

    private boolean isGet;
    private boolean isPost;
    private boolean isMultipart;
    private boolean isJson;

    public boolean isPost() {
        return isPost;
    }

    public void setPost(boolean post) {
        isPost = post;
    }

    public void setGet(boolean get) {
        isGet = get;
    }


    public void setJson(boolean json) {
        isJson = json;
    }

    public void setMultipart(boolean multipart) {
        isMultipart = multipart;
    }

    public boolean isGet() {
        return isGet;
    }

    public boolean isJson() {
        return isJson;
    }

    public boolean isMultipart() {
        return isMultipart;
    }

    //普通参数
    private HashMap<String, Object> params = new HashMap<>(10);
    //multipart参数
    private HashMap<String, File> streams = new HashMap<>(10);
    private Object jsonField;

    public void setJsonField(Object jsonField) {
        this.jsonField = jsonField;
    }


    public void addParam(String key, Object value) {
        this.params.put(key, value);
    }

    public void addPart(String key, File value) {
        this.streams.put(key, value);
    }

    public void addPart(String key, File[] values) {
        if (values == null) {
            return;
        }
        for(int i=0,len=values.length;i<len;i++) {
            addPart(key + "_" + i, values[i]);
        }
    }

    public Request makeRequestBody() {
        if (!isGet && !isPost && !isJson && !isMultipart) {
            throw new IllegalArgumentException(String.format("%s类中%s是一个普通方法?[无@POST,@GET等注解]",methodHandler.getPresentMethod().getDeclaringClass().getName(),methodHandler.getPresentMethodName()));
        }

        if (isGet) {
            return handleGet();
        }

        if (isJson) {
            return handleJson();
        }

        if (isMultipart) {
            return handleMultipart();
        }

        if (isPost) {
            return handlePost();
        }

        return null;
    }

    private Request handlePost() {
        FormBody.Builder builder = new FormBody.Builder();
        Set<Map.Entry<String, Object>> entries = params.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            builder.addEncoded(entry.getKey(), entry.getValue().toString());
        }

        return buildRequest()
                .post(builder.build())
                .build();
    }

    private Request.Builder buildRequest() {
        return new Request.Builder().url(Utils.comboURL(PastryConfig.getInstance().getHostUrl(), this.pathURL));
    }

    private Request handleMultipart() {

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        Set<Map.Entry<String, Object>> entries = params.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
//            builder.addFormDataPart(entry.getKey(), entry.getValue().toString());
            builder.addPart(MultipartBody.Part.createFormData(entry.getKey(), null, new JsonBody(entry.getKey(),entry.getValue().toString())));
        }

        Set<Map.Entry<String, File>> streamEntries = streams.entrySet();
        for (Map.Entry<String, File> streamEntry : streamEntries) {
            builder.addPart(MultipartBody.Part.createFormData(streamEntry.getKey(), streamEntry.getValue().getName(), new FileBody(streamEntry.getKey(), streamEntry.getValue())));
        }

        return buildRequest().post(builder.build()).build();
    }

    private Request handleJson() {

        RequestBody requestBody = new JsonBody(PastryConfig.getInstance().getGson().toJson(jsonField));
        return buildRequest()
                .post(requestBody)
                .build();
    }

    private Request handleGet() {
        Uri.Builder builder = Uri.parse(Utils.comboURL(PastryConfig.getInstance().getHostUrl(), this.pathURL)).buildUpon();
        Set<Map.Entry<String, Object>> entries = params.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue().toString());
        }
        return new Request.Builder()
                .url(builder.toString())
                .build();
    }

    public final static class FileBody extends RequestBody {

        public static final MediaType FILE = MediaType.parse("application/octet-stream");

        private File file;
        private String debugKey;

        public String getDebugKey() {
            return debugKey;
        }

        public File getSource() {
            return file;
        }

        public FileBody(String key, File file) {
            this.debugKey = key;
            this.file = file;
        }

        @Override
        public MediaType contentType() {
            return FILE;
        }

        @Override public long contentLength() {
            return file.length();
        }


        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            Source source = null;
            try {
                source = Okio.source(file);
                sink.writeAll(source);
            } finally {
                Util.closeQuietly(source);
            }
        }
    }

    public final static class JsonBody extends RequestBody {

        public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        private String jsonContent;
        private byte[] content;
        private String debugKey;
        public JsonBody(String jsonContent) {
            this.jsonContent = jsonContent;
            this.content = jsonContent.getBytes();
        }

        public JsonBody(String key,String jsonContent) {
            this(jsonContent);
            debugKey = key;
        }

        public String getDebugKey() {
            return debugKey;
        }

        public String getJsonContent() {
            return jsonContent;
        }

        @Override
        public MediaType contentType() {
            return JSON;
        }

        @Override public long contentLength() {
            return content.length;
        }

        @Override public void writeTo(BufferedSink sink) throws IOException {
            sink.write(content, 0, content.length);
        }
    }


}