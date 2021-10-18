package com.topzi.chat.model;

import com.google.gson.annotations.SerializedName;

public class UpMyChatModel {
    @SerializedName("STATUS")
    public String status;
    @SerializedName("MSG")
    public String msg;
    @SerializedName("RESULT")
    public Result result;

    public String getStatus() {
        return status;
    }

    public String getMsg() {
        return status;
    }

    public Result getResult() {
        return result;
    }


    public class Result {
        @SerializedName("user_image")
        public String userImage;

        public String getImage() {
            return userImage;
        }
    }


}