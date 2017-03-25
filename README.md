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


#集成方法
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
compile 'com.github.blesslp.Pastry:plugins:v1.0.3'    //自定义的一些插件,日志打印,Rxjava适配等
compile 'com.github.blesslp.Pastry:pastry:v1.0.3'     //网络库
```
