package ui.test.cn.xiaoyitong.ui.sonfragmeng;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ui.test.cn.xiaoyitong.R;

public class Tab02_FirstFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.tab02_frg_01, null);
    }
}