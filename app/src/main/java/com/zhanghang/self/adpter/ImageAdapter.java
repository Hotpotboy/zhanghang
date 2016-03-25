package com.zhanghang.self.adpter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.zhanghang.zhanghang.R;
import com.zhanghang.self.utils.ImageCache;

import java.util.ArrayList;

/**
 * Created by hangzhang209526 on 2015/7/23.
 */
public class ImageAdapter extends BaseAdapter{
    private ArrayList<String> datas;
    private Context mContext;
    private ImageLoader imageLoader;

    public ImageAdapter(Context context,ArrayList<String> _datas){
        datas = _datas;
        mContext = context;

        //初始化图片加载器
        imageLoader = new ImageLoader(Volley.newRequestQueue(mContext), new ImageCache(8*1024*1024));
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView==null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.layout_item,null);
            viewHolder.imageView = (NetworkImageView) convertView.findViewById(R.id.net_image);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder)convertView.getTag();
        }
        String imgeUrl = datas.get(position);
        viewHolder.imageView.setDefaultImage(mContext.getResources().getDrawable(R.drawable.deafult));
        viewHolder.imageView.setImageUrl(imgeUrl,imageLoader);
        return convertView;
    }

    public class ViewHolder{
        public NetworkImageView imageView;
    }
}
