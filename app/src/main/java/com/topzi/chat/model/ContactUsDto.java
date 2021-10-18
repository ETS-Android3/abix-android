package com.topzi.chat.model;

import com.google.gson.annotations.SerializedName;

public class ContactUsDto {
    @SerializedName("STATUS")
    public boolean status;
    @SerializedName("MSG")
    public String message;
    @SerializedName("RESULT")
    public String result;
}
