package ui.test.cn.xiaoyitong.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ui.test.cn.xiaoyitong.R;

/**
 * Created by asus on 2017/4/8.
 */

public class GridviewAdapter extends BaseAdapter {
    Context context;
    int[] imgs = {R.drawable.grid_kuaidi,R.drawable.grid_print,R.drawable.grid_eat,R.drawable.grid_gruid,R.drawable.grid_tingche
            ,R.drawable.grid_baodao,R.drawable.grid_rili,R.drawable.grid_qita};

    String grid_name[] = {"快递收发","成绩查询","课表查询","校内导航","停车导航","报道流程","校历","社团"};

    public GridviewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return imgs.length;
    }

    @Override
    public Object getItem(int i) {
        return imgs[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //通过布局填充器LayoutInflater填充网格单元格内的布局
        View v = LayoutInflater.from(context).inflate(R.layout.grid_item,null);
        ImageView img = (ImageView) v.findViewById(R.id.grid_img);
        TextView grid_txt = (TextView) v.findViewById(R.id.grid_txt);
        //引用数组内元素设置布局内图片以及文字的内容
        img.setImageResource(imgs[i]);
        grid_txt.setText(grid_name[i]);
        //返回值一定为单元格整体布局v
        return v;
    }
}
