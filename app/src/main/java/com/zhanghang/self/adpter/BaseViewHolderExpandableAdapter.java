package com.zhanghang.self.adpter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hangzhang209526 on 2016/1/29.
 */
public abstract class BaseViewHolderExpandableAdapter extends BaseExpandableListAdapter {
    protected Context mContext;
    /**
     * 视图加载器
     */
    protected LayoutInflater mLayoutInflater;
    /**
     * Group数据
     */
    protected ArrayList mGroupDatas;
    /**
     * Child数据
     */
    protected SparseArray<ArrayList> mChildDatas;

    public BaseViewHolderExpandableAdapter(Context context, ArrayList list, SparseArray<ArrayList> childList) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mGroupDatas = list;
        mChildDatas = childList;
    }

    public void setDatas(ArrayList datas,SparseArray<ArrayList> childList){
        mGroupDatas = datas;
        mChildDatas = childList;
        notifyDataSetInvalidated();
    }

    public void addParentData(Object object){
        if(object!=null){
            mGroupDatas.add(object);
            notifyDataSetChanged();
        }
    }

    public void removeParentData(int parentPosition){
        if(mGroupDatas!=null) {
            int groupCount = getGroupCount();
            if (parentPosition >= 0 && parentPosition < groupCount) {
                mGroupDatas.remove(parentPosition);
                SparseArray<ArrayList> tmp = new SparseArray<>();
                for(int i=0;i<groupCount;i++){
                    if(i<parentPosition){
                        tmp.put(i,mChildDatas.get(i));
                    }else if(i>parentPosition){
                        tmp.put(i,mChildDatas.get(i-1));
                    }
                }
                mChildDatas = tmp;
                notifyDataSetChanged();
            }
        }
    }

    /**
     * 判断给定的子类是否位于给定父类的ArrayList之中
     * @param parent           给定的子数据
     * @return
     */
    public boolean isInParent(Object parent){
        if(mGroupDatas==null) return false;
        return mGroupDatas.contains(parent);
    }

    public void addChildData(int parentPosition,Object object){
        if(object!=null){
            ArrayList childDatas = mChildDatas.get(parentPosition);
            if(childDatas==null){
                childDatas = new ArrayList();
            }
            childDatas.add(object);
            mChildDatas.put(parentPosition, childDatas);
            notifyDataSetChanged();
        }
    }

    /**
     * 判断给定的子类是否位于给定父类的ArrayList之中
     * @param parentPosition  给定的父数据的索引
     * @param child           给定的子数据
     * @return
     */
    public boolean isInSpecailParent(int parentPosition,Object child){
        if(mChildDatas==null) return false;
        if(parentPosition<0&&parentPosition>=mChildDatas.size()) return false;
        ArrayList list = mChildDatas.get(parentPosition);
        if(list==null) return false;
        return list.contains(child);
    }

    @Override
    public int getGroupCount() {
        return mGroupDatas.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(mChildDatas!=null&&mChildDatas.get(groupPosition)!=null){
            return mChildDatas.get(groupPosition).size();
        }
        return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroupDatas.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildDatas.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * 当父视图还未生成的时候，用以生成父视图的方法
     *
     * @return
     */
    protected abstract View inflaterGroupView(int position);

    /**
     * 当item视图还未生成的时候，用以生成item视图的方法
     *
     * @return
     */
    protected abstract View inflaterChildView(int groupPosition, int childPosition);

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        HashMap<String, View> baseViewHolder;
        if (convertView == null) {
            baseViewHolder = new HashMap<String, View>();
            convertView = inflaterGroupView(groupPosition);
            convertView.setTag(baseViewHolder);
        } else {
            baseViewHolder = (HashMap<String, View>) convertView.getTag();
        }
        reBindDataAndGroupView(groupPosition, isExpanded, baseViewHolder, convertView);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        HashMap<String, View> baseViewHolder;
        if (convertView == null) {
            baseViewHolder = new HashMap<String, View>();
            convertView = inflaterChildView(groupPosition, childPosition);
            convertView.setTag(baseViewHolder);
        } else {
            baseViewHolder = (HashMap<String, View>) convertView.getTag();
        }
        reBindDataAndChildView(groupPosition, childPosition,isLastChild, baseViewHolder, convertView);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

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

    /**
     * 重新给group视图，绑定指定位置上的数据
     *
     * @param groupPosition  指定的Group位置
     * @param isExpanded     是否展开
     * @param baseViewHolder group视图的子视图和值的映射关系，一定不为空
     * @param convertView    group视图，一定不为空
     */
    protected abstract void reBindDataAndGroupView(int groupPosition, boolean isExpanded, HashMap<String, View> baseViewHolder, View convertView);
    /**
     * 重新给Child视图，绑定指定位置上的数据
     *
     * @param groupPosition  指定的Group位置
     * @param childPosition  指定的child位置
     * @param isLastChild    是否是最后一个
     * @param baseViewHolder item视图的子视图和值的映射关系，一定不为空
     * @param convertView    item视图，一定不为空
     */
    protected abstract void reBindDataAndChildView(int groupPosition, int childPosition, boolean isLastChild, HashMap<String, View> baseViewHolder, View convertView);


}
