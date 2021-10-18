package com.topzi.chat.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import com.quickblox.users.model.QBUser;
import com.topzi.chat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 12/3/18.
 */

public class Constants {

//    public final static String BASE_URL = "http://139.59.77.194:3002/";
//    public static final String SOCKETURL = "http://139.59.77.194:8085";
    //http://ec2-13-233-46-200.ap-south-1.compute.amazonaws.com:3000/api/v1/chats/updateMuteStatus
//    public final static String BASE_URL = "http://ec2-13-233-46-200.ap-south-1.compute.amazonaws.com:3000";
    public final static String BASE_URL = "http://s1-topzi-dev.voxelkraftz.com:2082";
//    public final static String BASE_URL = "http://s1-topzi-prod.voxelkraftz.com:2082";
    public final static String API_VERSION = "/api/v1/";

    public static final String STUN_URL_1 = "stun:stun.callwithus.com";
    public static final String STUN_URL_2 = "stun:stun.xten.com";
    public static final String TAG_FRIENDID = "friendId";
    public static final String TAG_STATUS_ID = "statusId";
    public static final String TAG_COMMENT = "comment";
    public static final String CALL_RESPONSE_ACTION_KEY = "CALL RESPONSE ACTION KEY";
    public static final String CALL_RECEIVE_ACTION = "CALL RECEIVE";
    public static final String CALL_CANCEL_ACTION = "CALL CANCEL";
    public static final String TAG_CHANNEL_NAME_CALL = "Topzi Call";
    public static final String FCM_DATA_KEY = "FCM DATA";
    public static final String STATUS = "STATUS";
    public static final String TWO_STEP = "user/updateTwoStep";
    public static final String STAR_MSG = "chats/updateStarChats";
    public static final String MUTE_NOTI = "chats/updateMuteStatus";
    public static final String REPORT_USER = "reports/reportUser";
    public static final String TAG_TO_GROUP_ID = "to_group_id";
    public static final CharSequence YOU = "You";
    public static List<String> TURN_URLS = new ArrayList<String>() {{
        add("turn:192.158.29.39:3478?transport=tcp");
    }};

    public static final List<String> TURN_URLS_USERNAME = new ArrayList<String>() {{
        add("28224511:1379330808");
    }};
    public static final List<String> TURN_URLS_PASSWORD = new ArrayList<String>() {{
        add("JZEOEt2V3Qb0y27GRntt2u2PAYA=");
    }};

    public static int ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS = 422;
    public static final String USER_IMG_PATH = BASE_URL;
    public static final String CHAT_IMG_PATH = BASE_URL;
    public static final String GROUP_IMG_PATH = BASE_URL;
    public static final String CHANNEL_IMG_PATH = BASE_URL;
    public static final String GOOGLE_MAPS_KEY = "AIzaSyBHvJYF5IzhX3u5wVrvPSBJcBLKcGM0pWs";

    // QuickBox
    public static final String APP_ID = "84636";
    public static final String AUTH_KEY = "mRAThTrbGMNdyy8";
    public static final String AUTH_SECRET = "k2bXRHgE5HEMRTQ";
    public static final String ACCOUNT_KEY = "LBcttVDJrfXnGgnFRRF_";


    public static int MAX_OPPONENTS_COUNT = 6;
    public static int MAX_LOGIN_LENGTH = 15;
    public static int MAX_FULLNAME_LENGTH = 20;

    public static String EXTRA_QB_USER = "qb_user";
    public static String EXTRA_USER_ID = "user_id";
    public static String EXTRA_USER_LOGIN = "user_login";
    public static String EXTRA_USER_PASSWORD = "user_password";
    public static String EXTRA_PENDING_INTENT = "pending_Intent";
    public static String EXTRA_CONTEXT = "context";
    public static String EXTRA_OPPONENTS_LIST = "opponents_list";
    public static String EXTRA_CONFERENCE_TYPE = "conference_type";

    public static String EXTRA_IS_INCOMING_CALL = "conversation_reason";

    public static String EXTRA_LOGIN_RESULT = "login_result";
    public static String EXTRA_LOGIN_ERROR_MESSAGE = "login_error_message";
    public static int EXTRA_LOGIN_RESULT_CODE = 1002;

    public static String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    // Table column name:
    public static String TAG_STATUS = "status";
    public static String TAG_PHONE_NUMBER = "phone_no";
    public static String TAG_COUNTRY_CODE = "country_code";
    public static String TAG_USER_NAME = "user_name";
    public static String TAG_USER_IMAGE = "user_image";
    public static String TAG_ABOUT = "about";
    public static String TAG_PRIVACY_PROFILE = "privacy_profile_image";
    public static String TAG_PRIVACY_ABOUT = "privacy_about";
    public static String TAG_USER_ID = "userId";
    public static String TAG_CONTACT_ID = "contact_id";
    public static String TAG_PRIVACY_LAST_SEEN = "privacy_last_seen";
    public static String TAG_CONTACTS = "contacts";
    public static String TAG_CONTACT_STATUS = "contactstatus";
    public static String TAG_SENDER_ID = "sender_id";
    public static String TAG_RECEIVER_ID = "receiver_id";
    public static String TAG_MESSAGE_ID = "messageId";
    public static String TAG_MESSAGE_TYPE = "message_type";
    public static String TAG_CHAT_TYPE = "chat_type";
    public static String TAG_MESSAGE = "message";
    public static String TAG_REPLY_TO = "reply_to";
    public static String TAG_MESSAGE_DATA = "message_data";
    public static String TAG_ATTACHMENT = "attachment";
    public static String TAG_LAT = "lat";
    public static String TAG_LON = "lon";
    public static String TAG_CONTACT_NAME = "contact_name";
    public static String TAG_CONTACT_PHONE_NO = "contact_phone_no";
    public static String TAG_CONTACT_COUNTRY_CODE = "contact_country_code";
    public static String TAG_CHAT_TIME = "chat_time";
    public static String TAG_DELIVERY_STATUS = "delivery_status";
    public static String TAG_CHAT_ID = "chat_id";
    public static String TAG_UNREAD_COUNT = "unread_count";
    public static String TAG_BLOCKED_BYME = "blockedbyme";
    public static String TAG_BLOCKED_ME = "blockedme";
    public static String TAG_TYPE = "type";
    public static String TAG_ID = "_id";
    public static String TAG_GROUP_ID = "group_id";
    public static String TAG_GROUP_ADMIN_ID = "group_admin_id";
    public static String TAG_GROUP_NAME = "group_name";
    public static String TAG_GROUP_MEMBERS = "group_members";
    public static String TAG_GROUP_DESC = "group_description";
    public static String TAG_GROUP_IMAGE = "group_image";
    public static String TAG_GROUP_CREATED_BY = "group_created_by";
    public static String TAG_CREATED_AT = "created_at";
    public static String TAG_CREATED_TIME = "created_time";
    public static String TAG_SINGLE = "single";
    public static String TAG_GROUP = "group";
    public static String TAG_CHANNEL = "channel";
    public static String TAG_ADMIN_CHANNEL = "admin_channel";
    public static String TAG_USER_CHANNEL = "user_channel";
    public static String TAG_CALL = "call";
    public static String TAG_CALLS = "CALLS";
    public static String TAG_MEMBER_ID = "member_id";
    public static String TAG_MEMBER_NAME = "member_name";
    public static String TAG_MEMBER_PICTURE = "member_picture";
    public static String TAG_MEMBER_NO = "member_no";
    public static String TAG_MEMBER_ABOUT = "member_about";
    public static String TAG_MEMBER_ROLE = "member_role";
    public static String TAG_MEMBER_KEY = "member_key";
    public static final String TAG_RESULT = "result";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String TAG_THUMBNAIL = "thumbnail";
    public static final String TAG_PROGRESS = "progress";
    public static final String TAG_MUTE_NOTIFICATION = "mute_notification";
    public static final String TAG_ADMIN = "1";
    public static final String TAG_MEMBER = "0";
    public static final String TAG_MY_CONTACTS = "mycontacts";
    public static final String TAG_EVERYONE = "everyone";
    public static final String TAG_NOBODY = "nobody";
    public static final String TAG_CHANNEL_NAME = "channel_name";
    public static final String TAG_CHANNEL_ID = "channel_id";
    public static final String TAG_CHANNEL_DES = "channel_des";
    public static final String TAG_CHANNEL_IMAGE = "channel_image";
    public static final String TAG_CHANNEL_TYPE = "channel_type";
    public static final String TAG_INVITE_SUBSCRIBERS = "invite_subscribers";
    public static final String TAG_PUBLIC = "public";
    public static final String TAG_PRIVATE = "private";
    public static final String TAG_ADMIN_ID = "admin_id";
    public static final String TAG_CHANNEL_ADMIN_ID = "channel_admin_id";
    public static final String TAG_CHANNEL_ADMIN_NAME = "channel_admin_name";
    public static final String TAG_TOTAL_SUBSCRIBERS = "total_subscribers";
    public static final String SentFileHolder = "created_at";
    public static final String TAG_CALL_ID = "call_id";
    public static final String TAG_CALLER_ID = "caller_id";
    public static final String TAG_CALL_STATUS = "call_status";
    public static final String TAG_CHANNEL_LIST = "channel_list";
    public static final String TAG_TIME_STAMP = "timestamp";
    public static final String TAG_SUBSCRIBE_STATUS = "subscribe_status";
    public static final String TAG_BLOCK_STATUS = "block_status";
    public static final String TAG_CHANNEL_CATEGORY = "channel_category";
    public static final String NEW = "new";
    public static final String TAG_FAVOURITED = "favourited";

    public static final String TAG_NOTI_ID = "Notification_ID";
    public static final String NOTI_TONE = "notification_tone";
    public static final String VIBRATE = "notification_vibrate";
    public static final String POP_UP_NOTI = "pop_up_notification";
    public static final String NOTIF_LIGHT = "notification_light";
    public static final String HIGH_NOTIF = "notification_high";
    public static final String NOTIF_CALL_TONE = "notification_call_tone";
    public static final String NOTIF_CALL_VIBRATE = "notification_call_vibrate";
    public static final String NOTI_SET = "notification_set";
    public static final String NOTI_TONE_NAME = "noti_tone_name";
    public static final String CALL_TONE_NAME = "call_tone_name";

    // Data Storage Table Column

    public static final String TAG_DATA_ID = "data_id";
    public static final String MSG_COUNT = "message_count";
    public static final String SENT_CONTACT = "sent_contact";
    public static final String SENT_LOCATION = "sent_location";
    public static final String SENT_PHOTOS = "sent_photos";
    public static final String SENT_VID = "sent_videos";
    public static final String SENT_AUD = "sent_aud";
    public static final String SENT_DOC = "sent_doc";
    public static final String SENT_PHOTOS_SIZE = "sent_photos_size";
    public static final String SENT_VID_SIZE = "sent_videos_size";
    public static final String SENT_AUD_SIZE = "sent_aud_size";
    public static final String SENT_DOC_SIZE = "sent_doc_size";

    /*Used for Intent and other purpose*/
    public static final String TAG_LANGUAGE_CODE = "language_code";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_HINDI = "hi";
    public static final String LANGUAGE_MALAYALAM = "ml";
    public static final String LANGUAGE_TAMIL = "ta";
    public static final String LANGUAGE_FRENCH = "fr";
    public static final String TAG_DEFAULT_LANGUAGE_CODE = LANGUAGE_ENGLISH;
    public static final String TAG_BLOCK = "block";
    public static final String TAG_UNBLOCK = "unblock";
    public static final String TAG_GROUP_LIST = "group_list";
    public static final String IS_FROM = "IS_FROM";
    public static final String IS_EDIT = "IS_EDIT";
    public static final String TAG_GROUP_INVITATION = "groupinvitation";
    public static final String TAG_CHANNEL_INVITATION = "channelinvitation";
    public static final String TAG_TITLE = "title";
    public static final String ID = "id";
    public static String USER_ID = "user_id";
    public static final String TAG_NOTIFICATION = "notification";
    public static final String TAG_REPORT = "report";

    public static final String _ID = "_ID";
    public static final String WALLPAPER_NEW_PATH = "wallpapaer_new_path";
    public static final String WALLPAPER_OLD_PATH = "wallpapaer_old_path";
    public static final String SOLID_COLOR = "Solid_color";

    public static List<QBUser> qbUsersList = new ArrayList<>();

    /*For Get Contacts*/
    public static final String[] PROJECTION = new String[]{
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.STARRED,
            ContactsContract.RawContacts.ACCOUNT_TYPE,
            ContactsContract.CommonDataKinds.Contactables.DATA,
            ContactsContract.CommonDataKinds.Contactables.TYPE
    };
    public static final String SELECTION = ContactsContract.Data.MIMETYPE + " in (?, ?)" + " AND " +
            ContactsContract.Data.HAS_PHONE_NUMBER + " = '" + 1 + "'";

    public static final String[] SELECTION_ARGS = {
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
    };
    public static final String SORT_ORDER = ContactsContract.Contacts.SORT_KEY_ALTERNATIVE;

    public static int DIALOG_TIME = 5000;

    public static boolean isChatOpened = false, isGroupChatOpened = false, isChannelChatOpened = false;
    public static Context chatContext, groupContext, channelContext;
    public static String phone = "Phone";
    public static String countryCode = "Country code";
    public static String webKey = "ABIX-6WzvepUnBsVEObp2gZ44";
    public static String show = "Show";
    public static String scanQr = "Scan QR code";
    public static final int statusCameraImage = 201;
    public static final int statusGallery = 202;
    public static final int statusAudio = 203;
    public static String status = "Status";
    public static String myStatus = "My status";
    public static String image = "image";
    public static String video = "video";
    public static String audio = "audio";
    public static String read = "read";
    public static String from = "From";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarGradiant(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            Drawable background = activity.getResources().getDrawable(R.drawable.gradient);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            window.setNavigationBarColor(activity.getResources().getColor(android.R.color.white));
            window.setBackgroundDrawable(background);
        }
    }

    public static String getConnectionType(Context context){
        String type = "";
        ConnectivityManager connectivitymanager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivitymanager.getAllNetworkInfo();
        for (NetworkInfo netInfo : networkInfo) {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
                if (netInfo.isConnected())
                    type = "wifiData";
            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                if (netInfo.isConnected())
                    type = "mobileData";
        }

        return type;
    }

    public static boolean isDataRoamingEnabled(Context context) {
        try {
            // return true or false if data roaming is enabled or not
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.DATA_ROAMING) == 1;
        }
        catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    /*Used for Chat Settings Preference*/
    public static final String PREFERENCE_NAME = "SavedPref";
    public static final String NETWORK_USAGE = "NetworkUsage";
    public static final String PREF_ENTER_IS_SEND = "PREF_ENTER_IS_SEND";
    public static final String PREF_MEDIA_VISIBILITY = "PREF_MEDIA_VISIBILITY";
    public static final String PREF_FONT_SIZE = "PREF_FONT_SIZE";
    public static final String PREF_WALLPAPER_TYPE = "PREF_WALLPAPER_TYPE";
    public static final String PREF_WALLPAPER_VALUE = "PREF_WALLPAPER_VALUE";


    public static final String WALLPAPER_TYPE_NO_WALLPAPER = "WALLPAPER_TYPE_NO_WALLPAPER";
    public static final String WALLPAPER_TYPE_GALLERY = "WALLPAPER_TYPE_GALLERY";
    public static final String WALLPAPER_TYPE_SOLID_COLOR = "WALLPAPER_TYPE_SOLID_COLOR";
    public static final String WALLPAPER_TYPE_DEFAULT = "WALLPAPER_TYPE_DEFAULT";

    public static final int FONT_SIZE_SMALL = 0;
    public static final int FONT_SIZE_MEDIUM = 1;
    public static final int FONT_SIZE_LARGE = 2;

    public static boolean isShow = false;

}
