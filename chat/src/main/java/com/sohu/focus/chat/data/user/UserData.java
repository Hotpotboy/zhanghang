package com.sohu.focus.chat.data.user;

import com.sohu.focus.chat.data.BaseData;
import com.sohu.focus.chat.netcallback.UserDataCallBack;

/**
 * Created by hangzhang209526 on 2016/2/23.
 */
public class UserData extends BaseData {
    private long id;
    /**头像*/
    private String headPhoto;
    /**昵称*/
    private String nickName;
    /**用户名*/
    private String userName;
    private long createDate;
    /**上次登陆*/
    private long lastLoginDate;
    /**上次离线*/
    private long lastOfflineTime;
    private String password;
    private String qrcodePhoto;
    /**经纬度*/
    private int lat;
    private int lng;
    private int mapId;
    /**类别*/
    private int type = UserDataCallBack.SELF;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getLastOfflineTime() {
        return lastOfflineTime;
    }

    public void setLastOfflineTime(long lastOfflineTime) {
        this.lastOfflineTime = lastOfflineTime;
    }

    public String getHeadPhoto() {
        return headPhoto;
    }

    public void setHeadPhoto(String headPhoto) {
        this.headPhoto = headPhoto;
    }

    public long getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(long lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getQrcodePhoto() {
        return qrcodePhoto;
    }

    public void setQrcodePhoto(String qrcodePhoto) {
        this.qrcodePhoto = qrcodePhoto;
    }

    @Override
    public boolean equals(Object other){
        if(other==null) return false;
        if(!(other instanceof UserData)) return false;
        return id==((UserData)other).getId();
    }

    public int getLat() {
        return lat;
    }

    public void setLat(int lat) {
        this.lat = lat;
    }

    public int getLng() {
        return lng;
    }

    public void setLng(int lng) {
        this.lng = lng;
    }

    public int getType() {
        return type;
    }

    public void setType(int _type) {
        type = _type;
    }

    public int getMapId() {
        return mapId;
    }

    public void setMapId(int mapId) {
        this.mapId = mapId;
    }
}
