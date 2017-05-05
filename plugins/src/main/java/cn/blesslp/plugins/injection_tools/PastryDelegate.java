package cn.blesslp.plugins.injection_tools;

import cn.blesslp.pastry.Pastry;

/**
 * @作者: Liufan
 * @时间: 2017/5/5
 * @功能描述:Pastry的注入和回收的代理类,降低Pastry的存在感
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
 */


public class PastryDelegate {
    private Pastry mPastry;
    private Object me;
    public Pastry getPastry() {
        return mPastry;
    }

    private PastryDelegate(Object me) {
        this.mPastry = Pastry.newInstance(me);
        this.me = me;
    }

    public static PastryDelegate create(Object me) {
        return new PastryDelegate(me);
    }

    public void autoInject() throws Exception {
        InjectApiProcessor.injectApi(this.mPastry, this.me);
    }

    public void cancelAll() {
        mPastry.cancelAllTask();
    }

}
