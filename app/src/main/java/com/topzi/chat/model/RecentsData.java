package com.topzi.chat.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created on 5/7/18.
 */

public class RecentsData {

    @SerializedName("status")
    public String status;

    @SerializedName("result")
    public ArrayList<MessagesData> result = new ArrayList<>();
}
