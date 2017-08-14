# Pastry 
#### Retrofit风格的网络库,支持:
1. POST
2. GET
3. Param("key")//单个key-value
4. Path("urlpattern")String value //@GET("xxx/{id}/{name}") @Path("id")String id
5. JsonParam JavaBean/Map 拆分为key-value参数	
6. JsonField JavaBean/Map 提交 application/json数据
7. FileField File/File[]
8. @Header("key")String value 方法参数级别
9. @Headers({"xx:xx",""}) 方法级别

#   集成方法

```java
    //设置初始化配置,根路径
                PastryConfig.init(this)
                .setHost("http://api.com")
                .applyConfig();
```

```java
//Api接口,
public interface Api {

/**
*返回JavaBean
*/
    @POST("App/User/User/get_u_member")
    public ResultBean<MemberInfo> getMemberInfo(@Param("member_id") String memberId);
/**
*返回okhttp3.Call
*/
    @POST("App/User/Register/get_register_code")
    public Call getRegisterCode(@Param("mobile") String mobile);
/**
*返回原json串
*/
    @GET("App/User/User/get_homepage")
    public String getHomePage(@Param("member_id") String memberId, @Param("type") int type, @Param("p") int p);
}

```
```java

public class MainActivity extends AppCompatActivity {

    private Pastry mPastry;
    private Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPastry = Pastry.newInstance(this);
        api = mPastry.create(Api.class);

        final TextView txt = (TextView) findViewById(R.id.txtInfo);

        findViewById(R.id.btnGetMemberInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.getMemberInfo("123");
            }
        });

        findViewById(R.id.btnRegisterCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call call = api.getRegisterCode("18086021234");
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        //okhttp流程
                    }
                });
            }
        });

        findViewById(R.id.btnGetHomePage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.getHomePage("234", 1, 1);
            }
        });

    }

    public void getHomePage(String homePage) {
        //api.getHomePage("234", 1, 1)异步请求的数据会直接被Pastry回调过来
        //这边的参数,就是api.getHomePage("234", 1, 1)的返回值
        //由于异步,直接调用api.getHomePage("234", 1, 1),并拿到的返回值永远是null
        //这个方法是UIThread , 应当用于处理UI显示
    }

    public void getMemberInfo(ResultBean<MemberInfo> infoResultBean) {
//        api.getMemberInfo("123"); 相应的回调方法,返回值为实体,则会直接转为相应的实体并回调给接类的同一名称方法
        TextView txt = (TextView) findViewById(R.id.txtInfo);
        txt.setText(String.format("%s %s %s", infoResultBean.getData().getId(), infoResultBean.getData().getNickname(), infoResultBean.getData().getHead_img()));
    }
}


```


```groovy
allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}
```
```groovy
compile 'com.github.blesslp.Pastry:plugins:v1.2.0'    //自定义的一些插件,日志打印,Rxjava适配等
compile 'com.github.blesslp.Pastry:pastry:v1.2.0'     //网络库
```

##  com.github.blesslp.Pastry:plugins 介绍
##  LoggerInterceptor
####    你可以这个开启,用以获得一个日志打印功能
```
PastryConfig.init(this)
.setHost("http://api.com/")
.addInterceptor(new LoggerInterceptor(LoggerInterceptor.LOG_TYPE.LOG_FULL))
.applyConfig();
```
LoggerInterceptor.LOG_FULL / LoggerInterceptor.LOG_NONE
```
D/PRETTYLOGGER: ╔════════════════════════════════════════════════════════════════════════════════════════
D/PRETTYLOGGER: ║ Thread: POST : http://api.com/App/User/User/get_u_member
D/PRETTYLOGGER: ╟────────────────────────────────────────────────────────────────────────────────────────
D/PRETTYLOGGER: ║ {
D/PRETTYLOGGER: ║   "POST": "http:\/\/api.com\/App\/User\/User\/get_u_member",
D/PRETTYLOGGER: ║   "member_id": "609"
D/PRETTYLOGGER: ║ }
D/PRETTYLOGGER: ╚════════════════════════════════════════════════════════════════════════════════════════
D/PRETTYLOGGER: ╔════════════════════════════════════════════════════════════════════════════════════════
D/PRETTYLOGGER: ║ Thread: POST : http://api.com/App/User/User/get_u_member
D/PRETTYLOGGER: ╟────────────────────────────────────────────────────────────────────────────────────────
D/PRETTYLOGGER: ║ {
D/PRETTYLOGGER: ║   "status": "1",
D/PRETTYLOGGER: ║   "msg": "获取成功！",
D/PRETTYLOGGER: ║   "data": {
D/PRETTYLOGGER: ║     "id": "609",
D/PRETTYLOGGER: ║     "nickname": "blesslp",
D/PRETTYLOGGER: ║     "type": "1"
D/PRETTYLOGGER: ║   }
D/PRETTYLOGGER: ║ }
D/PRETTYLOGGER: ╚════════════════════════════════════════════════════════════════════════════════════════

```
##  ObservableHandler
####    如果想要结合RxJava ,你可以这样
```
PastryConfig.init(this)
                .setHost("http://api.com/")
                .addInterceptor(new LoggerInterceptor(LoggerInterceptor.LOG_TYPE.LOG_FULL))
                .addReturnValHandler(new ObservableHandler())
                .applyConfig();
                

public interface Api {

    @POST("App/User/User/get_u_member")
    public Observable<ResultBean<MemberInfo>> getMemberInfo(@Param("member_id") String memberId);
}



findViewById(R.id.btnGetMemberInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                api.getMemberInfo("609").subscribe(new Consumer<ResultBean<MemberInfo>>() {
                    @Override
                    public void accept(@NonNull ResultBean<MemberInfo> infoResultBean) throws Exception {
                        String nickname = infoResultBean.getData().getNickname();
                        
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        
                    }
                });
            }
        });
```



##  PastryDelegate和@InjectApi
####    你可以这样将Pastry集成到你的项目的任何基类中
```
 * Example:
 * public class BaseActivity {
 *     private PastryDelegate mPastryDelegate;
 *
 *     public void onCreate(Bundle savedInstance) {
 *         super.onCreate(savedInstance);
 *         mPastryDelegate = PastryDelegate.create(this);
 *         mPastryDelegate.autoInject();
 *     }
 *     
 *     //在某个情况下取消网络请求
 *     public void onStop() {
 *         mPastryDelegate.cancelAll();
 *     }
 * }
 * 
 * Example2:
 * public class LoginActivity extends BaseActivity {
 *     @InjectApi
 *     private LoginModel mLoginModel;
 *     
 *     protected void onCreate(Bundle savedInstance) {
 *         super.onCreate(savedInstance);
 *         setContentView(R.layout.login_activity);
 *         findViewById(R.id.btnSubmit).setOnClickListener(view->mLoginModel.login(userName,password));
 *     }
 *     
 *     //登录成功的操作
 *     public void login(...) {
 *         ...
 *     }
 *     
 *     
 * }
 *
```

##  BaseInterceptor
####    更简单的添加拦截器
```


public class XxxInterceptor extends BaseInterceptor {
    @Override
    public Request onBefore(Request request) {
        /**
         * 统一添加header
         */
        return request.newBuilder()
                .addHeader("headerKey", "headerValue")
                .addHeader("headerKey2","headerValue2")
                .build();
    }

or...

    @Override
    public Request onBefore(Request request) {
        if (!NetworkUtils.hasConnection(TheApplication.getInstance())) {
            //无网提示
            ...
        }
        return super.onBefore(request);
    }
}
```

##  GlobalParamProvider
####    如果你要给某些类请求统一注入参数,那么这个将很有用
```
private GlobalParamProvider nullProvider = new GlobalParamProvider() {
        @Override
        public Map<String, String> provider() {
            HashMap<String, String> params = new HashMap<>();
            params.put("member_id", "123456");
            params.put("token", "wwqofjdsklnxvoewiurewojfdslkfjdsfdo12lkdsjfsd");
            return params;
        }

    };
    
  private GlobalParamProvider keyProvider = new GlobalParamProvider() {
        @Override
        public Map<String, String> provider() {
            HashMap<String, String> params = new HashMap<>();
            params.put("someParams", "1111");
            return params;
        }
    };    
```
```
PastryConfig.init(this)
                ...
                .setGlobalParamProvider(nullProvider)
                .addGlobalParamProvider("hello",keyProvider)
                .applyConfig();
```
####    怎么使用呢,
```

@GlobalParam
public interface Api {
...
}
or
@GlobalParam("hello")
public interface Api {
...
}
```
如此一来,Api里面的所有请求,都会被附加对应的参数,
```
@GlobalParam        使用了     setGlobalParamProvider(xx)  提供的map
@GlobalParam(key)   使用了     addGlobalParamProvider(key,xx)  提供的map
你可以定义多个,也可以给一个(api类或者方法)指定多个全局参数提供器
```
####    或者也可以
```
@GlobalParam
public interface Api {

    @GlobalParam("hello")
    @POST("App/User/User/get_u_member")
    public ResultBean<MemberInfo> getMemberInfo();
}
```
如此一来
```
api.getMemberInfo() 方法将会被附加上nullProvider和keyProvider提供的所有参数
```
这种方法在有些时候很方便,但是我更建议和后台统一把全局参数加到header中.

##  还有哪些东西?
##  ReturnHandler
####    它的子类:
```
Pastry自带的:
    CallHandler
    BeanHandler
plugins内的:
    FlowableHandler
    ObservableHandler
```
```
如果你继承它(ReturnHandler),
PastryConfig.addReturnValHandler(加入你的处理器类)
就可以轻松拓展Pastry
```

## One More Things?
####    即将开放自定义注解和注解处理器来轻松扩展Pastry
```
@POST , @GET , @Param("key")..
```

```
2017/8/14 修复 参数缓存导致null的情况下,用了旧的参数
```
