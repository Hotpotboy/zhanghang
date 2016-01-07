package com.souhu.hangzhang209526.zhanghang.widget.PullListView.deafult;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ProgressBar;

import com.souhu.hangzhang209526.zhanghang.R;
import com.souhu.hangzhang209526.zhanghang.widget.PullListView.PullLinearView;


/**
 * Created by hangzhang209526 on 2015/12/4.
 */
public class DeafultPullFooter extends PullLinearView {
    public DeafultPullFooter(Context context) {
        super(context);
    }

    public DeafultPullFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void processViewUpdateByOnclick(PullLinearView pullLinearView) {
        ProgressBar bar = (ProgressBar) pullLinearView.findViewById(R.id.footer_progressbar);
        bar.setVisibility(View.VISIBLE);
    }
    @Override
    protected Object[] loadDataByASync(PullLinearView pullLinearView) {
        if(mAttchedListView!=null){
            String[] datas = new String[20];
            for(int i=0;i<20;i++)
                datas[i] = "增加数据"+i;
            Object[] result = new Object[1];
            result[0] = datas;
            return result;
        }
        return null;
    }

    @Override
    protected void processViewUpdateAfterLoadedData(PullLinearView pullLinearView,Object[] result) {
        if(result!=null&&result.length==1&&result[0] instanceof String[]){
            ArrayAdapter adapter = (ArrayAdapter) ((HeaderViewListAdapter)mAttchedListView.getAdapter()).getWrappedAdapter();
            for(String item:(String[])result[0])
                adapter.add(item);
        }
        ProgressBar bar = (ProgressBar) pullLinearView.findViewById(R.id.footer_progressbar);
        bar.setVisibility(View.GONE);
    }
}
