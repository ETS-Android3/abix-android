package com.topzi.chat.utils;

import com.topzi.chat.model.AdminChannel;
import com.topzi.chat.model.AdminChannelMsg;
import com.topzi.chat.model.BlocksData;
import com.topzi.chat.model.CallData;
import com.topzi.chat.model.ChangeNumberResult;
import com.topzi.chat.model.ChannelChatResult;
import com.topzi.chat.model.ChannelResult;
import com.topzi.chat.model.ContactUsDto;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.GroupChatResult;
import com.topzi.chat.model.GroupImageModel;
import com.topzi.chat.model.GroupInvite;
import com.topzi.chat.model.GroupResult;
import com.topzi.chat.model.GroupUpdateResult;
import com.topzi.chat.model.HelpData;
import com.topzi.chat.model.ProfileUpdatResModel;
import com.topzi.chat.model.RecentsData;
import com.topzi.chat.model.SaveMyContacts;
import com.topzi.chat.model.SigninResponse;
import com.topzi.chat.model.UpMyChatModel;
import com.topzi.chat.model.UserProfileModel;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created on 24/1/18.
 */

public interface ApiInterface {

    @GET("service/admindatas")
    Call<HashMap<String, String>> adminData();

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "user/signin")
    Call<SigninResponse> signin(@Field("data") String data);

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "contacts")
    Call<ContactsData> updatemycontacts(@Field("userToken") String token, @Field("userId") String userId, @Field("phone") String phone, @Field("contacts") String contactsModel);

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "user/updateProfile")
    Call<ProfileUpdatResModel> updatemyprofile(@Field("data") String data);

    @Multipart
    @POST(Constants.API_VERSION + "user/upmyprofile")
    Call<ProfileUpdatResModel> upmyprofile(@Part MultipartBody.Part image, @Part("userId") RequestBody user_id);

    @Multipart
    @POST(Constants.API_VERSION + "chats/upmychat")
    Call<UpMyChatModel> upmychat(@Header("Authorization") String user_token, @Part MultipartBody.Part attachment, @Part("user_id") RequestBody user_id);

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "devices/saveDevice")
    Call<SigninResponse> pushsignin(@Field("userId") String userId,
                                    @Field("deviceType") String deviceType,
                                    @Field("deviceId") String deviceId,
                                    @Field("deviceToken") String deviceToken);

    @FormUrlEncoded
    @POST("service/pushsignout")
    Call<Map<String, String>> pushsignout(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @GET(Constants.API_VERSION + "user/getUserProfile")
    Call<UserProfileModel> getuserprofile(@Query("userId") String userId);

    @GET(Constants.API_VERSION + "chats/recentchats")
    Call<RecentsData> recentchats(@Header("Authorization") String user_token, @Query("user_id") String user_id);

    @GET(Constants.API_VERSION + "user/blockStatus")
    Call<BlocksData> getblockstatus(@Query("userId") String user_id);

//    @Multipart
//    @POST("service/modifyGroupimage")
//    Call<HashMap<String, String>> uploadGroupImage(@Header("Authorization") String user_token, @Part MultipartBody.Part image, @Part("group_id") RequestBody groupId);

    @GET(Constants.API_VERSION + "groups/groupinvites")
    Call<GroupInvite> getGroupInvites(@Header("Authorization") String token, @Query("user_id") String userId);

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "groups/getGroupInfo")
    Call<GroupResult> getGroupInfo(@Field("userId") String token, @Field("groupId") String group_id);

    @FormUrlEncoded
    @POST("service/deviceinfo")
    Call<Map<String, String>> deviceinfo(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

//    @Multipart
//    @POST("service/upmychat")
//    Call<Map<String, String>> upchat(@Header("Authorization") String user_token, @Part MultipartBody.Part attachment, @Part("user_id") RequestBody user_id);

    @Multipart
    @POST(Constants.API_VERSION + "chats/upmygroupchat")
    Call<GroupImageModel> upMyGroupChat(@Part MultipartBody.Part attachment, @Part("userId") RequestBody user_id, @Part("group_id") RequestBody group_id);

//    @Multipart
//    @POST("service/upmygroupchat")
//    Call<HashMap<String, String>> uploadGroupChat(@Header("Authorization") String user_token, @Part MultipartBody.Part attachment, @Part("user_id") RequestBody user_id);

//    @Multipart
//    @POST("service/upmygroupchat")
//    Call<Map<String, String>> upGroupchat(@Header("Authorization") String user_token, @Part MultipartBody.Part attachment, @Part("user_id") RequestBody user_id);

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "chats/chatreceived")
    Call<Map<String, String>> chatreceived(@Header("Authorization") String user_token, @Field("userId") String uderId, @Field("friendId") String friendId, @Field("messageId") String messageId);

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "groups/modifyGroupinfo")
    Call<GroupUpdateResult> updateGroup(@Header("Authorization") String user_token, @Field("group_id") String groupId,
                                        @Field("group_name") String groupName);

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "groups/modifyGroupinfo")
    Call<GroupUpdateResult> updateGroup(@Header("Authorization") String user_token, @Field("group_id") String groupId,
                                        @Field("group_members") JSONArray members);

    @GET(Constants.API_VERSION + "chats/recentgroupchats")
    Call<GroupChatResult> getRecentGroupChats(@Header("Authorization") String token, @Query("user_id") String userId);

    @FormUrlEncoded
    @POST("service/savemycontacts")
    Call<SaveMyContacts> saveMyContacts(@Header("Authorization") String user_token, @FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "user/updatePrivacy")
    Call<UserProfileModel> updateMyPrivacy(@Field("userId") String userId, @Field("privacyLastSeen") String privacyLastScene, @Field("privacyProfile") String privacyProfile, @Field("privacyAbout") String privacyAbout);

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "groups/modifyGroupmembers")
    Call<HashMap<String, String>> modifyGroupmembers(@Header("Authorization") String user_token, @Field("user_id") String userId, @Field("group_id") String groupId,
                                                     @Field("group_members") JSONArray members);

    @GET("service/MySubscribedChannels/{user_id}")
    Call<ChannelResult> getMySubscribedChannels(@Header("Authorization") String user_token, @Path("user_id") String userId);

    @GET("service/MyChannels/{user_id}")
    Call<ChannelResult> getMyChannels(@Header("Authorization") String user_token, @Path("user_id") String userId);

    @GET("service/helps")
    Call<HelpData> getHelpList();

    @Multipart
    @POST("service/modifyChannelImage")
    Call<HashMap<String, String>> uploadChannelImage(@Header("Authorization") String user_token, @Part MultipartBody.Part body, @Part("channel_id") RequestBody channelID);

    @GET(Constants.API_VERSION + "calls/recentcalls")
    Call<CallData> recentcalls(@Query("user_id") String user_id);

    @FormUrlEncoded
    @POST("service/channelinfo")
    Call<ChannelResult> getChannelInfo(@Header("Authorization") String user_token, @Field("channel_list") JSONArray channelList);

    @FormUrlEncoded
    @POST("service/updatemychannel")
    Call<HashMap<String, String>> updateChannel(@Header("Authorization") String user_token, @FieldMap HashMap<String, String> map);

    @Multipart
    @POST("service/upmychannelchat")
    Call<HashMap<String, String>> uploadChannelChat(@Header("Authorization") String user_token, @Part MultipartBody.Part attachment, @Part("channel_id") RequestBody channel_Id, @Part("user_id") RequestBody userId);

    @Multipart
    @POST("service/upmychannelchat")
    Call<Map<String, String>> upChannelChat(@Header("Authorization") String user_token, @Part MultipartBody.Part attachment, @Part("channel_id") RequestBody channel_Id, @Part("user_id") RequestBody userId);

    @GET("service/recentChannelChats/{user_id}")
    Call<ChannelChatResult> recentChannelChats(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("service/adminchannels/{user_id}")
    Call<AdminChannel> getAdminChannels(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("service/msgfromadminchannels/{timestamp}")
    Call<AdminChannelMsg> getMsgFromAdminChannels(@Header("Authorization") String user_token, @Path("timestamp") String timestamp);

    @GET("service/recentChannelInvites/{user_id}")
    Call<ChannelResult> getRecentChannelInvites(@Header("Authorization") String user_token, @Path("user_id") String user_id);

    @GET("service/AllPublicChannels/{user_id}/{search_string}/{offset}/{limit}")
    Call<ChannelResult> getAllPublicChannels(@Header("Authorization") String user_token, @Path("user_id") String user_id, @Path("search_string") String search, @Path("offset") String offSet, @Path("limit") String limit);

    @GET("service/channelSubscribers/{channel_id}/{phone_no}/{offset}/{limit}")
    Call<ContactsData> getChannelSubscribers(@Header("Authorization") String user_token, @Path("channel_id") String channel_id, @Path("phone_no") String phoneNo, @Path("offset") String offSet, @Path("limit") String limit);

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "groups/getMyGroups")
    Call<GroupResult> getMyGroups(@Field("userId") String userId);

    @GET(Constants.API_VERSION + "user/delete")
    Call<HashMap<String, String>> deleteMyAccount(@Header("Authorization") String user_token, @Query("phone") String userId);

    @GET(Constants.API_VERSION + "user/numberVerify")
    Call<Map<String, String>> verifyNewNumber(@Query("phone") String phoneNumber);

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "user/changePhoneNo")
    Call<ChangeNumberResult> changeMyNumber(@Field("userId") String userId, @Field("phone") String phoneNumber, @Field("countryCode") String countryCode);

    @GET("service/checkforupdates")
    Call<HashMap<String, String>> checkForUpdates();

    @FormUrlEncoded
    @POST("service/reportchannel")
    Call<HashMap<String, String>> reportChannel(@Header("Authorization") String user_token, @FieldMap HashMap<String, String> hashMap);

    @FormUrlEncoded
    @POST(Constants.API_VERSION + "user/webSignIn")
    Call<UpMyChatModel> webSignin(@Field("webKey") String webKey,
                                  @Field("phone") String pnone);

    @Multipart
    @POST(Constants.API_VERSION +"supports/contactUs")
    Call<ContactUsDto> supportContactus(@Header("Authorization") String user_token,
                                        @Part ArrayList<MultipartBody.Part> contactImages,
                                        @Field("message") String message,
                                        @Field("userId") String userId);
}

