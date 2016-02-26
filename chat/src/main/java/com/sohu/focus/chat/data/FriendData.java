package com.sohu.focus.chat.data;

/**
 * Created by hangzhang209526 on 2016/2/23.
 */
public class FriendData extends BaseData {
    private long id;
    private String nickName;
    private String userName;
    private long createDate;
    private long lastOfflineTime;
    private String headPhoto;
    private long lastLoginDate;
    private String password;
    private String qrcodePhoto;

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
}
