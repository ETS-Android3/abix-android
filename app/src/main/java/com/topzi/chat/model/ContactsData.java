package com.topzi.chat.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created on 23/3/18.
 */

public class ContactsData implements Serializable {
    @SerializedName("STATUS")
    public String status;

    @SerializedName("RESULT")
    public ArrayList<ContactsData.Result> result = new ArrayList<>();

    public class Result implements Serializable {

        @SerializedName("id")
        public String user_id;

        @SerializedName("user_name")
        public String user_name;

        @SerializedName("user_image")
        public String user_image;

        @SerializedName("phone_no")
        public String phone_no;

        @SerializedName("country_code")
        public String country_code;

        @SerializedName("privacy_last_seen")
        public String privacy_last_seen;

        @SerializedName("privacy_last_scene")
        public String privacy_last_scene;

        @SerializedName("privacy_profile_image")
        public String privacy_profile_image;

        @SerializedName("privacy_about")
        public String privacy_about;

        @SerializedName("blockedme")
        public String blockedme;

        @SerializedName("blockedbyme")
        public String blockedbyme;

        @SerializedName("about")
        public String about;

        @SerializedName("mute_notification")
        public String mute_notification;

        @SerializedName("contactstatus")
        public String contactstatus;

        @SerializedName("favourited")
        public String favourited;
    }
}
