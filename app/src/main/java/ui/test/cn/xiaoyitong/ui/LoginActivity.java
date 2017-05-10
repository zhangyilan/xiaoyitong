package ui.test.cn.xiaoyitong.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easemob.EMCallBack;
import com.easemob.chat.EMChatManager;
import com.easemob.chat.EMGroupManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ui.test.cn.xiaoyitong.GetContext.MyApplication;
import ui.test.cn.xiaoyitong.R;
import ui.test.cn.xiaoyitong.dao.UserDao;
import ui.test.cn.xiaoyitong.entity.Constant;
import ui.test.cn.xiaoyitong.entity.User;
import ui.test.cn.xiaoyitong.httpHelper.HttpCallback;
import ui.test.cn.xiaoyitong.utils.CommonUtils;
import ui.test.cn.xiaoyitong.utils.DeviceInfoUtil;
import ui.test.cn.xiaoyitong.utils.NetUtil;
import ui.test.cn.xiaoyitong.utils.StatusBarUtil;

/**
 * Created by YanChunlin on 2017/4/19.
 */

public class LoginActivity extends Activity {
    private static final String TAG = "LoginActivity";
    public static final int REQUEST_CODE_SETNICK = 1;
    private EditText usernameEditText;
    private EditText passwordEditText;
//    private TextView btn_tourist;
    private TextView btn_register;

    private boolean progressShow;
    private boolean autoLogin = false;
    private String uuid;
    private String currentUsername;
    private String currentPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        StatusBarUtil.StatusBarLightMode(this);
        setContentView(R.layout.activity_login);
        usernameEditText = (EditText) findViewById(R.id.username);
        passwordEditText = (EditText) findViewById(R.id.password);
//        btn_tourist = (TextView) findViewById(R.id.tourist_login);
        btn_register = (TextView) findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(LoginActivity.this, RegisterActivity.class), 0);
            }
        });
//        //游客登陆
//        btn_tourist.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(LoginActivity.this, TouristActivity.class));
//            }
//        });

        // 如果用户名改变，清空密码
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordEditText.setText(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (MyApplication.getInstance().getUserName() != null) {
            usernameEditText.setText(MyApplication.getInstance().getUserName());
        }
    }

    /**
     * 登录
     *
     * @param view
     */
    public void login(View view) {

        if (!CommonUtils.isNetWorkConnected(this)) {
            Toast.makeText(this, R.string.network_isnot_available, Toast.LENGTH_SHORT).show();
            return;
        }
        currentUsername = usernameEditText.getText().toString().trim();
        currentPassword = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(currentUsername)) {
            Toast.makeText(this, R.string.User_name_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(currentPassword)) {
            Toast.makeText(this, R.string.Password_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        progressShow = true;
        final ProgressDialog pd = new ProgressDialog(LoginActivity.this);
        pd.setCanceledOnTouchOutside(false);
        pd.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                progressShow = false;
            }
        });
        pd.setMessage(getString(R.string.Is_landing));
        pd.show();

        final long start = System.currentTimeMillis();
        uuid= DeviceInfoUtil.getUniqueNumber(this);
        String url="http://123.206.92.38:80/SimpleSchool/userservlet?opt=login&name="+currentUsername+"&password="+currentPassword+"&uuid="+uuid+"";
        Log.d("url",url);
        Log.d("用户名:",currentUsername);
        Log.d("密码:",currentPassword);
        NetUtil netUtil=new NetUtil();
        if(netUtil.isNetworkAvailable(LoginActivity.this)){
            netUtil.sendData(url,"POST",new HttpCallback() {
                @Override
                public void onFinish(String response) {

                    if (response.equals("true")){
                        // 调用sdk登陆方法登陆聊天服务器
                        EMChatManager.getInstance().login(currentUsername, currentPassword, new EMCallBack() {
                            @Override
                            public void onSuccess() {
                                if (!progressShow) {
                                    return;
                                }
                                // 登陆成功，保存用户名密码
                                MyApplication.getInstance().setUserName(currentUsername);
                                MyApplication.getInstance().setPassword(currentPassword);

                                try {
                                    //第一次登录或者之前logout后再登录，加载所有本地群和回话
                                    EMGroupManager.getInstance().loadAllGroups();
                                    EMChatManager.getInstance().loadAllConversations();
                                    // 处理好友和群组
                                    initializeContacts();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    // 取好友或者群聊失败，不让进入主页面
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            pd.dismiss();
                                            MyApplication.getInstance().logout(null);
                                            Toast.makeText(getApplicationContext(), R.string.login_failure_failed, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }
                                // 更新当前用户的nickname 此方法的作用是在ios离线推送时能够显示用户nick
                                boolean updatenick = EMChatManager.getInstance().updateCurrentUserNick(
                                        MyApplication.currentUserNick.trim());
                                if (!updatenick) {
                                    Log.e("LoginActivity", "update current user nick fail");
                                }
                                if (!LoginActivity.this.isFinishing() && pd.isShowing()) {
                                    pd.dismiss();
                                }
                            }

                            @Override
                            public void onProgress(int progress, String status) {
                            }

                            @Override
                            public void onError(final int code, final String message) {
                                if (!progressShow) {
                                    return;
                                }
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        pd.dismiss();
                                        Toast.makeText(getApplicationContext(), getString(R.string.Login_failed) + message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                    Message message=new Message();
                    message.obj=response;
                    message.what=0;
                    handler.sendMessage(message);
                }

                @Override
                public void onerror(Exception e) {
                }
            });
        }
    }


    private void initializeContacts() {
        Map<String, User> userlist = new HashMap<String, User>();
        // 添加user"申请与通知"
        User newFriends = new User();
        newFriends.setUsername(Constant.NEW_FRIENDS_USERNAME);
        String strChat = getResources().getString(
                R.string.Application_and_notify);
        newFriends.setNick(strChat);
        userlist.put(Constant.NEW_FRIENDS_USERNAME, newFriends);

        // 存入内存
        MyApplication.getInstance().setContactList(userlist);
        // 存入db
        UserDao dao = new UserDao(LoginActivity.this);
        List<User> users = new ArrayList<User>(userlist.values());
        dao.saveContactList(users);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoLogin) {
            return;
        }
    }
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    if(msg.obj.equals("true")){
                        SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
                        editor.putString("uuid",uuid);
                        editor.commit();
                        SharedPreferences.Editor editor2=getSharedPreferences("user",MODE_PRIVATE).edit();
                        editor2.putString("user_name",currentUsername);
                        editor2.commit();
                        Toast.makeText(LoginActivity.this,"密码正确",Toast.LENGTH_SHORT).show();
                        // 进入主页面
                        Intent intent = new Intent(LoginActivity.this, BeasActivity.class);
                        startActivity(intent);
                        finish();
                    }else {

                        Toast.makeText(LoginActivity.this,"密码错误",Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
