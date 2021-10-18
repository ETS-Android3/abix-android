package com.topzi.chat.model;

import android.media.MediaPlayer;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created on 20/6/18.
 */

public class MessagesData implements Serializable {

    @SerializedName("chatId")
    public String chat_id;

    @SerializedName("messageId")
    public String message_id;

    @SerializedName("reply_to")
    public String reply_to;

    @SerializedName("to_group_id")
    public String groupId;

    @SerializedName("userId")
    public String user_id;

    @SerializedName("user_name")
    public String user_name;

    @SerializedName("message_type")
    public String message_type;

    @SerializedName("message")
    public String message;

    @SerializedName("attachment")
    public String attachment;

    @SerializedName("lat")
    public String lat;

    @SerializedName("lon")
    public String lon;

    @SerializedName("contact_name")
    public String contact_name;

    @SerializedName("contact_phone_no")
    public String contact_phone_no;

    @SerializedName("contact_country_code")
    public String contact_country_code;

    @SerializedName("chat_time")
    public String chat_time;

    @SerializedName("friendId")
    public String receiver_id;

    @SerializedName("sender_id")
    public String sender_id;

    @SerializedName("delivery_status")
    public String delivery_status;

    @SerializedName("thumbnail")
    public String thumbnail;

    @SerializedName("progress")
    public String progress;

    @SerializedName("chat_type")
    public String chatType;

    public boolean isPlaying;

    public int playProgress;

    public int maxPlayProgress;

    public MediaPlayer mediaPlayer = null;

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getReply_to() {
        return reply_to;
    }

    public void setReply_to(String reply_to) {
        this.reply_to = reply_to;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getMessage_type() {
        return message_type;
    }

    public void setMessage_type(String message_type) {
        this.message_type = message_type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_phone_no() {
        return contact_phone_no;
    }

    public void setContact_phone_no(String contact_phone_no) {
        this.contact_phone_no = contact_phone_no;
    }

    public String getContact_country_code() {
        return contact_country_code;
    }

    public void setContact_country_code(String contact_country_code) {
        this.contact_country_code = contact_country_code;
    }

    public String getChat_time() {
        return chat_time;
    }

    public void setChat_time(String chat_time) {
        this.chat_time = chat_time;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getDelivery_status() {
        return delivery_status;
    }

    public void setDelivery_status(String delivery_status) {
        this.delivery_status = delivery_status;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }
}
