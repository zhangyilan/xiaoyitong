package ui.test.cn.xiaoyitong.ui.sonfragmeng;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.imid.swipebacklayout.lib.app.SwipeBackActivity;
import ui.test.cn.xiaoyitong.R;
import ui.test.cn.xiaoyitong.adapter.NewsAdapter;
import ui.test.cn.xiaoyitong.entity.news;
import ui.test.cn.xiaoyitong.httpHelper.HttpCallBackListener;
import ui.test.cn.xiaoyitong.httpHelper.Httputil;
import ui.test.cn.xiaoyitong.uiManger.CustomProgressDialog;
import ui.test.cn.xiaoyitong.utils.HttpUtil;

public class newsMainActivity extends SwipeBackActivity {

    private List<news> newsList=new ArrayList<>();
    private NewsAdapter newsAdapter;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    newsList.addAll((List<news>) msg.obj);
                    newsAdapter .notifyDataSetChanged();
                    newsAdapter.myListViewClickListener(new NewsAdapter.ListViewClickListener() {
                        @Override
                        public void onRecycleViewClick(View view, int id) {
                            Intent intent=new Intent(newsMainActivity.this,Content_Activity.class);
                            intent.putExtra("id",id);
                            startActivity(intent);
                        }
                    });
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.news_list);
        sendRequest();

        ImageView back = (ImageView) findViewById(R.id.back);
        TextView biaoti= (TextView) findViewById(R.id.biaoti);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        biaoti.setText("校园资讯");
        newsAdapter=new NewsAdapter(newsMainActivity.this, R.layout.news_list_item,newsList);
        final ListView listView= (ListView) findViewById(R.id.new_list);
        listView.setAdapter(newsAdapter);

    }

    private void sendRequest() {
        String url="http://123.206.92.38/SimpleSchool/AppServlet?opt=getdata";

        final HttpUtil httpUtil = new HttpUtil();
        final CustomProgressDialog dialog = new CustomProgressDialog(newsMainActivity.this, "正在加载中", R.anim.frame,R.style.MyDialogStyle);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        Httputil.sendRequest(url, new HttpCallBackListener() {
            @Override
            public void onFinish(Object respones) {
                Message mes=new Message();
                mes.what=1;
                mes.obj=respones;
                handler.sendMessage(mes);
                dialog.dismiss();
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }
 }
