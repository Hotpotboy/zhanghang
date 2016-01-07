package com.souhu.hangzhang209526.zhanghang.widget.PullListView.deafult;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.souhu.hangzhang209526.zhanghang.R;
import com.souhu.hangzhang209526.zhanghang.widget.PullListView.PullLinearView;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2015/12/4.
 */
public class DeafultPullHeader extends PullLinearView {
    private static final long  DEAFULT_TIME  = 1000;
    private TextView tip;
    private TextView arrow;
    public DeafultPullHeader(Context context) {
        super(context);
    }

    public DeafultPullHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void initView(){
        if(tip==null) tip = (TextView)findViewById(R.id.header_text);
        if(arrow==null) arrow = (TextView) findViewById(R.id.down_arrow_image);
    }

    @Override
    public void processViewUpdateByTouch(float percent, PullLinearView pullLinearView) {
        initView();
        tip.setText("下拉更新");
        arrow.setVisibility(View.VISIBLE);
//        arrow.getCompoundDrawables()[3].setLevel((int) (percent * 10000));
    }

    @Override
    protected Object[] loadDataByASync(PullLinearView pullLinearView) {
        if(mAttchedListView!=null){
            String[] datas = new String[20];
            for(int i=0;i<20;i++)
                datas[i] = "更新数据"+i;
            Object[] result = new Object[1];
            result[0] = datas;
            return result;
        }
        return null;
    }

    @Override
    protected void processViewUpdateAfterLoadedData(PullLinearView pullLinearView,Object[] result) {
        if(result!=null&&result.length==1&&result[0] instanceof String[]){
            ArrayList<String> datas = new ArrayList<String>();
            for(String item:(String[])result[0])
                datas.add(item);
            ArrayAdapter adapter = new ArrayAdapter(mContext,R.layout.layout_item,R.id.text,datas);
            mAttchedListView.setAdapter(adapter);
        }
        initView();
        tip.setText("更新完成……");
        arrow.setVisibility(View.GONE);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                startScrollBack();
            }
        },DEAFULT_TIME);
    }
}
