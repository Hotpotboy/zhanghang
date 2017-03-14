package com.sohu.focus.chat.data;

/**
 * Created by hangzhang209526 on 2016/3/7.
 */
public class FileResponseData extends BaseResponseData {
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data _data) {
        this.data = _data;
    }

    public class Data{
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
