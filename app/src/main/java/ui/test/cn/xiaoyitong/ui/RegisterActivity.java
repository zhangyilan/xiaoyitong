package ui.test.cn.xiaoyitong.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.easemob.EMError;
import com.easemob.chat.EMChatManager;
import com.easemob.exceptions.EaseMobException;

import ui.test.cn.xiaoyitong.GetContext.MyApplication;
import ui.test.cn.xiaoyitong.R;
import ui.test.cn.xiaoyitong.httpHelper.HttpCallback;
import ui.test.cn.xiaoyitong.utils.HttpUtil;
import ui.test.cn.xiaoyitong.utils.StatusBarUtil;
import ui.test.cn.xiaoyitong.utils.TimeButton;


/**
 * Created by YanChunlin on 2017/4/19.
 */

public class RegisterActivity extends Activity implements View.OnClickListener{
    private String resp="123";
    private EditText phone;
    private EditText auth_code;
    private TimeButton btn_code;
    private EditText password;
    private Button register;

    private ImageView iv_hide;
    private ImageView iv_show;
    private ImageView mImg_Background;
    private FrameLayout de_frm_backgroud;
    private LinearLayout root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        }
        StatusBarUtil.StatusBarLightMode(this);

        de_frm_backgroud = (FrameLayout) findViewById(R.id.de_frm_backgroud);
        root = (LinearLayout) findViewById(R.id.root);

        //获取电话号码
        phone= (EditText) findViewById(R.id.phone);
        //验证码
        auth_code= (EditText) findViewById(R.id.auth_code);
        //获取验证码
        btn_code= (TimeButton) findViewById(R.id.btn_code);
        //密码
        password= (EditText) findViewById(R.id.password);
        //注册
        register= (Button) findViewById(R.id.register);

        iv_hide = (ImageView) findViewById(R.id.iv_hide);
        iv_show = (ImageView) findViewById(R.id.iv_show);
        iv_hide.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                iv_hide.setVisibility(View.GONE);
                iv_show.setVisibility(View.VISIBLE);
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                // 切换后将EditText光标置于末尾
                CharSequence charSequence = password.getText();
                if (charSequence instanceof Spannable) {
                    Spannable spanText = (Spannable) charSequence;
                    Selection.setSelection(spanText, charSequence.length());
                }
            }

        });
        iv_show.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                iv_show.setVisibility(View.GONE);
                iv_hide.setVisibility(View.VISIBLE);
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                // 切换后将EditText光标置于末尾
                CharSequence charSequence = password.getText();
                if (charSequence instanceof Spannable) {
                    Spannable spanText = (Spannable) charSequence;
                    Selection.setSelection(spanText, charSequence.length());
                }
            }

        });
        btn_code.onCreate(savedInstanceState);
        btn_code.setTextAfter("秒后重新获取").setTextBefore("获取验证码").setLenght(60 * 1000);

        mImg_Background = (ImageView) findViewById(R.id.de_img_backgroud);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.translate_anim);
                mImg_Background.startAnimation(animation);
            }
        }, 200);
    }

    /**
     * 注册
     *
     * @param view
     */
    public void register(View view) {
        final String user_phone = phone.getText().toString().trim();
        final String user_auth_code=auth_code.getText().toString().trim();
        final String pwd = password.getText().toString().trim();
        if (TextUtils.isEmpty(user_phone)) {
            Toast.makeText(this, getResources().getString(R.string.User_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
            phone.requestFocus();
            return;
        }else if (TextUtils.isEmpty(user_auth_code)) {
            Toast.makeText(this, getResources().getString(R.string.Auth_code_cannot_be_empty), Toast.LENGTH_SHORT).show();
            auth_code.requestFocus();
            return;
        } else if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(this, getResources().getString(R.string.Password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            password.requestFocus();
            return;
        } else if (pwd.length()<=6 && pwd.length()>=20) {
            Toast.makeText(this, "密码格式不正确！", Toast.LENGTH_SHORT).show();
            password.requestFocus();
            return;
        } else if (!auth_code.getText().toString().equals(resp)){
            Toast.makeText(this, getResources().getString(R.string.Two_input_phone_code), Toast.LENGTH_SHORT).show();
            auth_code.requestFocus();
            return;
        }

        if (!TextUtils.isEmpty(user_phone) && !TextUtils.isEmpty(pwd)) {
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage(getResources().getString(R.string.Is_the_registered));
            pd.show();
            String url="http://123.206.92.38:80/SimpleSchool/userservlet?opt=insert_temporary&phone="+user_phone+"&password="+pwd+"";
            HttpUtil httpUtil = new HttpUtil();
            if (httpUtil.isNetworkAvailable(RegisterActivity.this)) {
                httpUtil.getData(url, new HttpCallback() {
                    @Override
                    public void onFinish(String respose) {
                        if (respose.equals("true")) {
                            try {
                                // 调用sdk注册方法
                                EMChatManager.getInstance().createAccountOnServer(user_phone, pwd);
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (!RegisterActivity.this.isFinishing())
                                            pd.dismiss();
                                        // 保存用户名
                                        MyApplication.getInstance().setUserName(user_phone);
                                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                });
                            } catch (final EaseMobException e) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (!RegisterActivity.this.isFinishing())
                                            pd.dismiss();
                                        int errorCode = e.getErrorCode();
                                        if (errorCode == EMError.NONETWORK_ERROR) {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
                                        } else if (errorCode == EMError.USER_ALREADY_EXISTS) {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
                                        } else if (errorCode == EMError.UNAUTHORIZED) {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
                                        } else if (errorCode == EMError.ILLEGAL_USER_NAME) {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "注册失败:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                    @Override
                    public void onerror(Exception e) {

                    }
                });
            }
        }
    }
    public void get_code(View view) {
        String url="http://123.206.92.38:80/SimpleSchool/phonecodeServlet?opt=get_phone_code&phone="+phone.getText().toString().trim()+"";
        HttpUtil httpUtil = new HttpUtil();
        if (httpUtil.isNetworkAvailable(RegisterActivity.this)){
            httpUtil.getData(url, new HttpCallback() {
                @Override
                public void onFinish(String respose) {
                    resp = respose;
                }
                @Override
                public void onerror(Exception e) {
                }
            });
        }

    }

    public void back(View view) {
        finish();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        btn_code.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

    }
}
