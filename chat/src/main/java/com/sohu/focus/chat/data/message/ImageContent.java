package com.sohu.focus.chat.data.message;

import android.text.TextUtils;

import com.sohu.focus.chat.data.BaseData;

/**
 * Created by hangzhang209526 on 2016/2/25.
 */
public class ImageContent extends BaseData {
    private static final String SPLIT_STR = ":";
    private String thumbnail;
    private String imageUrl;
    private boolean landscape;

    public ImageContent(){

    }

    public ImageContent(String _content){
        if(_content!=null&&_content.indexOf(SPLIT_STR)>=0) {
            String[] contents = _content.split(SPLIT_STR);
            if(contents!=null&&contents.length==2) {
                thumbnail = contents[0];
                imageUrl = contents[1];
            }
        }
    }

    @Override
    public String toString(){
        StringBuffer result = new StringBuffer();
        if(TextUtils.isEmpty(thumbnail)){
            result.append("");
        }else{
            result.append(thumbnail);
        }
        result.append(SPLIT_STR);
        if(TextUtils.isEmpty(imageUrl)){
            result.append("");
        }else{
            result.append(imageUrl);
        }
        return result.toString();
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isLandscape() {
        return landscape;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }
}
