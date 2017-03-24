# Pastry 
#### Retrofit风格的网络库,支持:
1.POST
2.GET
3.Param("key")//单个key-value
4.Path("urlpattern")String value //@GET("xxx/{id}/{name}") @Path("id")String id
5.JsonParam JavaBean/Map 拆分为key-value参数	
6.JsonField JavaBean/Map 提交 application/json数据
7.FileField File/File[]
```groovy
allprojects {
		repositories {
			maven { url 'https://jitpack.io' }
		}
	}
```
```groovy
compile 'com.github.blesslp.Pastry:plugins:v1.0.0'    //自定义的一些插件,日志打印,Rxjava适配等
compile 'com.github.blesslp.Pastry:pastry:v1.0.0'     //网络库
```
