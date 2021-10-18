package com.topzi.chat.model.contacts;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ContactsModel {
    @SerializedName("number")
    @Expose
    private String number;
//    @SerializedName("userId")
//    @Expose
//    private String userId;
//    @SerializedName("phone")
//    @Expose
//    private String phone;
//    @SerializedName("contacts")
//    @Expose
//    private List<Contact> contacts = null;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

//    public String getUserId() {
//        return userId;
//    }
//
//    public void setUserId(String userId) {
//        this.userId = userId;
//    }
//
//    public String getPhone() {
//        return phone;
//    }
//
//    public void setPhone(String phone) {
//        this.phone = phone;
//    }
//
//    public List<Contact> getContacts() {
//        return contacts;
//    }
//
//    public void setContacts(List<Contact> contacts) {
//        this.contacts = contacts;
//    }
//
//    public class Contact {
//
//        @SerializedName("number")
//        @Expose
//        private String number;
//
//        public String getNumber() {
//            return number;
//        }
//
//        public void setNumber(String number) {
//            this.number = number;
//        }
//
    }
//}
