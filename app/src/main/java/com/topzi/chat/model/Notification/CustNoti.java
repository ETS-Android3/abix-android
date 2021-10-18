package com.topzi.chat.model.Notification;

public class CustNoti {

    String userId,notificationTone,vibrate,popUpNoti,lightColor,callTone,callVibrate;
    boolean notificationType;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNotificationTone() {
        return notificationTone;
    }

    public void setNotificationTone(String notificationTone) {
        this.notificationTone = notificationTone;
    }

    public String getVibrate() {
        return vibrate;
    }

    public void setVibrate(String vibrate) {
        this.vibrate = vibrate;
    }

    public String getPopUpNoti() {
        return popUpNoti;
    }

    public void setPopUpNoti(String popUpNoti) {
        this.popUpNoti = popUpNoti;
    }

    public String getLightColor() {
        return lightColor;
    }

    public void setLightColor(String lightColor) {
        this.lightColor = lightColor;
    }

    public String getCallTone() {
        return callTone;
    }

    public void setCallTone(String callTone) {
        this.callTone = callTone;
    }

    public String getCallVibrate() {
        return callVibrate;
    }

    public void setCallVibrate(String callVibrate) {
        this.callVibrate = callVibrate;
    }

    public boolean isNotificationType() {
        return notificationType;
    }

    public void setNotificationType(boolean notificationType) {
        this.notificationType = notificationType;
    }
}
