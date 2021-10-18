package com.topzi.chat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SigninResponse {
    @SerializedName("STATUS")
    @Expose
    private Boolean sTATUS;
    @SerializedName("MSG")
    @Expose
    private String mSG;
    @SerializedName("RESULT")
    @Expose
    private RESULT rESULT;

    public Boolean getSTATUS() {
        return sTATUS;
    }

    public void setSTATUS(Boolean sTATUS) {
        this.sTATUS = sTATUS;
    }

    public String getMSG() {
        return mSG;
    }

    public void setMSG(String mSG) {
        this.mSG = mSG;
    }

    public RESULT getRESULT() {
        return rESULT;
    }

    public void setRESULT(RESULT rESULT) {
        this.rESULT = rESULT;
    }


    public class RESULT {

        @SerializedName("Status")
        @Expose
        private Object status;
        @SerializedName("_id")
        @Expose
        private Integer id;
        @SerializedName("user_name")
        @Expose
        private String userName;
        @SerializedName("user_token")
        @Expose
        private String userToken;
        @SerializedName("user_image")
        @Expose
        private String userImage;
        @SerializedName("privacy_about")
        @Expose
        private String privacyAbout;
        @SerializedName("phone_no")
        @Expose
        private String phoneNo;
        @SerializedName("country_code")
        @Expose
        private String countryCode;
        @SerializedName("privacy_profile_image")
        @Expose
        private String privacyProfileImage;
        @SerializedName("privacy_last_seen")
        @Expose
        private String privacyLastSeen;
        @SerializedName("about")
        @Expose
        private String about;

        public Object getStatus() {
            return status;
        }

        public void setStatus(Object status) {
            this.status = status;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserToken() {
            return userToken;
        }

        public void setUserToken(String userToken) {
            this.userToken = userToken;
        }

        public String getUserImage() {
            return userImage;
        }

        public void setUserImage(String userImage) {
            this.userImage = userImage;
        }

        public String getPrivacyAbout() {
            return privacyAbout;
        }

        public void setPrivacyAbout(String privacyAbout) {
            this.privacyAbout = privacyAbout;
        }

        public String getPhoneNo() {
            return phoneNo;
        }

        public void setPhoneNo(String phoneNo) {
            this.phoneNo = phoneNo;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getPrivacyProfileImage() {
            return privacyProfileImage;
        }

        public void setPrivacyProfileImage(String privacyProfileImage) {
            this.privacyProfileImage = privacyProfileImage;
        }

        public String getPrivacyLastSeen() {
            return privacyLastSeen;
        }

        public void setPrivacyLastSeen(String privacyLastSeen) {
            this.privacyLastSeen = privacyLastSeen;
        }

        public String getAbout() {
            return about;
        }

        public void setAbout(String about) {
            this.about = about;
        }
    }
}