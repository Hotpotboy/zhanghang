package com.sohu.focus.chat.data;

/**
 * Created by hangzhang209526 on 2016/2/25.
 */
public class BaseResponseData extends BaseData {
    private int errorCode;
    private String errorMessage;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
