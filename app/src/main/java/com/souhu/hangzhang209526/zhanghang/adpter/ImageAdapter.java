package com.souhu.hangzhang209526.zhanghang.adpter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.souhu.hangzhang209526.zhanghang.R;
import com.souhu.hangzhang209526.zhanghang.utils.ImageCache;

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

        //³õÊ¼»¯Í¼Æ¬¼ÓÔØÆ÷
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
        viewHolder.imageView.setDefaultImageResId(R.drawable.deafult);
        viewHolder.imageView.setImageUrl(imgeUrl,imageLoader);
        return convertView;
    }

    public class ViewHolder{
        public NetworkImageView imageView;
    }
}
