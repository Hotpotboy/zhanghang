package com.souhu.hangzhang209526.zhanghang.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public abstract class BaseViewHolderAdapter extends BaseAdapter {
    protected Context mContext;
    /**
     * 视图加载器
     */
    protected LayoutInflater mLayoutInflater;
    /**
     * 数据
     */
    protected ArrayList mDatas;

    public BaseViewHolderAdapter(Context context, ArrayList list) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDatas = list;
    }

    public void setDatas(ArrayList datas){
        mDatas = datas;
        notifyDataSetInvalidated();
    }

    public void addData(Object data){
        if(mDatas!=null){
            mDatas.add(data);
            notifyDataSetChanged();
        }
    }

    public int getPosistionForObject(Object object){
        if(mDatas!=null){
            return  mDatas.indexOf(object);
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HashMap<String, View> baseViewHolder;
        if (convertView == null) {
            baseViewHolder = new HashMap<String, View>();
            convertView = inflaterView(position);
            convertView.setTag(baseViewHolder);
        } else {
            baseViewHolder = (HashMap<String, View>) convertView.getTag();
        }
        reBindDataAndView(position, baseViewHolder, convertView);
        return convertView;
    }

    /**
     * 当视图还未生成的时候，用以生成视图的方法
     *
     * @return
     */
    protected abstract View inflaterView(int position);

    /**
     * 根据tag查找item视图中的某一个子视图
     *
     * @param id             所要查找的子视图的资源ID
     * @param tag            所要查找的子视图的映射key
     * @param baseViewHolder 所要查找的子视图与其对应的映射key的哈希表
     * @param convertView    所要查找的子视图的父视图，也就是item视图
     * @return
     */
    protected View getViewByTag(int id, String tag, HashMap<String, View> baseViewHolder, View convertView) {
        View view;
        if (!baseViewHolder.containsKey(tag)) {
            view = convertView.findViewById(id);
            baseViewHolder.put(tag, view);
        } else view = baseViewHolder.get(tag);
        return view;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * 重新给item视图，绑定指定位置上的数据
     *
     * @param position       指定的位置
     * @param baseViewHolder item视图中的子视图和值的映射关系，一定不为空
     * @param convertView    item视图，一定不为空
     */
    protected abstract void reBindDataAndView(int position, HashMap<String, View> baseViewHolder, View convertView);


}
