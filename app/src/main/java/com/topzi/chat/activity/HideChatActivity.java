package com.topzi.chat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topzi.chat.R;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.topzi.chat.utils.ObjectSerializer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.topzi.chat.utils.Constants.TAG_CONTACT_STATUS;
import static com.topzi.chat.utils.Constants.TAG_MY_CONTACTS;
import static com.topzi.chat.utils.Constants.TAG_NOBODY;
import static com.topzi.chat.utils.Constants.TAG_PRIVACY_PROFILE;
import static com.topzi.chat.utils.Constants.TRUE;

public class HideChatActivity extends AppCompatActivity implements SocketConnection.RecentChatReceivedListener{

    private final String TAG = this.getClass().getSimpleName();
    RecyclerViewAdapter recyclerViewAdapter;
    RecyclerView recyclerView;
    RelativeLayout progressLay;
    LinearLayout nullLay;
    TextView nullText;
    ImageView imgBack;
    ArrayList<HashMap<String, String>> chatAry = new ArrayList<>();
    ArrayList<HashMap<String, String>> chatAry1 = new ArrayList<>();
    private ArrayList<String> hideChatID = new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    DatabaseHandler dbhelper;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hide_chat);

        Log.v(TAG, "onCreateView");
        pref = getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        progressLay = findViewById(R.id.progress);
        nullLay = findViewById(R.id.nullLay);
        nullText = findViewById(R.id.nullText);
        recyclerView = findViewById(R.id.recyclerView);
        imgBack = findViewById(R.id.img_back);

        dbhelper = DatabaseHandler.getInstance(HideChatActivity.this);
        SocketConnection.getInstance(HideChatActivity.this).setRecentChatReceivedListener(this);

        linearLayoutManager = new LinearLayoutManager(HideChatActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        chatAry1.clear();
        chatAry1.addAll(dbhelper.getAllRecentsMessages(HideChatActivity.this));

        chatAry.clear();
        if (getHideChat().isEmpty()){
            recyclerView.setVisibility(View.GONE);
            nullLay.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            nullLay.setVisibility(View.GONE);
            for (int i = 0; i < getHideChat().size(); i++) {
                String archive = getHideChat().get(i);
                for (int j = 0; j < chatAry1.size(); j++) {
                    String id = chatAry1.get(i).get(Constants.TAG_USER_ID);
                    if (!archive.equals(id)){
                        chatAry.add(chatAry1.get(j));
                    }
                }
            }
        }
//        List<ContactsData.Result> favList = dbhelper.getFavContacts(HideChatActivity.this);
//        if (favList.size() > 0) {
//            chatAry.add(0, null);
//        }
        recyclerViewAdapter = new RecyclerViewAdapter(HideChatActivity.this, chatAry);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();

        nullText.setText(R.string.no_chat_yet_buddy);
        if (chatAry.size() == 0) {
            nullLay.setVisibility(View.VISIBLE);
        } else {
            nullLay.setVisibility(View.GONE);
        }

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }


    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        ArrayList<HashMap<String, String>> Items;
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.Items = Items;
            this.context = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_item, parent, false);
                return new RecyclerViewAdapter.MyViewHolder(itemView);
            } else if (viewType == TYPE_HEADER) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.favorites_header, parent, false);
                return new RecyclerViewAdapter.HeaderView(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

            if (viewHolder instanceof RecyclerViewAdapter.MyViewHolder) {
                RecyclerViewAdapter.MyViewHolder holder = (RecyclerViewAdapter.MyViewHolder) viewHolder;
                holder.typing.setVisibility(View.GONE);
                holder.messageLay.setVisibility(View.VISIBLE);
                HashMap<String, String> map = Items.get(position);
                holder.name.setText(map.get(Constants.TAG_USER_NAME));
                holder.message.setText(map.get(Constants.TAG_MESSAGE));

                if (map.get(Constants.TAG_CHAT_TIME) != null) {
                    holder.time.setText(getFormattedDate(context, Long.parseLong(map.get(Constants.TAG_CHAT_TIME).replace(".0", ""))));
                }

                if (map.get(Constants.TAG_BLOCKED_ME).equals("block")) {
                    Glide.with(context).load(R.drawable.change_camera).thumbnail(0.5f)
                            .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
                            .into(holder.profileimage);
                } else {
                    DialogActivity.setProfileImage(dbhelper.getContactDetail(map.get(Constants.TAG_USER_ID)), holder.profileimage, context);
                }

                if (map.get(Constants.TAG_SENDER_ID) != null && map.get(Constants.TAG_SENDER_ID).equals(GetSet.getUserId())) {
                    holder.tickimage.setVisibility(View.VISIBLE);
                    if (map.get(Constants.TAG_DELIVERY_STATUS).equals("read")) {
                        holder.tickimage.setImageResource(R.drawable.double_tick);
                    } else if (map.get(Constants.TAG_DELIVERY_STATUS).equals("sent")) {
                        holder.tickimage.setImageResource(R.drawable.double_tick_unseen);
                    } else if (map.get(Constants.TAG_PROGRESS).equals("completed") && (map.get(Constants.TAG_MESSAGE_TYPE).equals("image") ||
                            map.get(Constants.TAG_MESSAGE_TYPE).equals("video") || map.get(Constants.TAG_MESSAGE_TYPE).equals("file") || map.get(Constants.TAG_MESSAGE_TYPE).equals("audio"))) {
                        holder.tickimage.setImageResource(R.drawable.single_tick);
                    } else if (map.get(Constants.TAG_MESSAGE_TYPE).equals("text") || map.get(Constants.TAG_MESSAGE_TYPE).equals("contact") || map.get(Constants.TAG_MESSAGE_TYPE).equals("location")) {
                        holder.tickimage.setImageResource(R.drawable.single_tick);
                    } else {
                        holder.tickimage.setVisibility(View.GONE);
                    }
                } else {
                    holder.tickimage.setVisibility(View.GONE);
                }

                if (map.get(Constants.TAG_MESSAGE_TYPE) != null) {
                    switch (map.get(Constants.TAG_MESSAGE_TYPE)) {
                        case "image":
                        case "video":
                            holder.typeicon.setVisibility(View.VISIBLE);
                            holder.typeicon.setImageResource(R.drawable.upload_gallery);
                            break;
                        case "location":
                            holder.typeicon.setVisibility(View.VISIBLE);
                            holder.typeicon.setImageResource(R.drawable.upload_location);
                            break;
                        case "audio":
                            holder.typeicon.setVisibility(View.VISIBLE);
                            holder.typeicon.setImageResource(R.drawable.upload_audio);
                            break;
                        case "contact":
                            holder.typeicon.setVisibility(View.VISIBLE);
                            holder.typeicon.setImageResource(R.drawable.upload_contact);
                            break;
                        case "file":
                            holder.typeicon.setVisibility(View.VISIBLE);
                            holder.typeicon.setImageResource(R.drawable.upload_file);
                            break;
                        default:
                            holder.typeicon.setVisibility(View.GONE);
                            break;
                    }
                } else {
                    holder.typeicon.setVisibility(View.GONE);
                }

                if (map.get(Constants.TAG_MUTE_NOTIFICATION).equals("true")) {
                    holder.mute.setVisibility(View.VISIBLE);
                } else {
                    holder.mute.setVisibility(View.GONE);
                }

                if (map.get(Constants.TAG_UNREAD_COUNT).equals("") || map.get(Constants.TAG_UNREAD_COUNT).equals("0")) {
                    holder.unseenLay.setVisibility(View.GONE);
                } else {
                    holder.unseenLay.setVisibility(View.VISIBLE);
                    holder.unseenCount.setText(map.get(Constants.TAG_UNREAD_COUNT));
                }
            } else if (viewHolder instanceof RecyclerViewAdapter.HeaderView) {
                RecyclerViewAdapter.HeaderView holder = (RecyclerViewAdapter.HeaderView) viewHolder;
                Log.v("header", "header");
                List<ContactsData.Result> favList = dbhelper.getFavContacts(context);
                LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                holder.favrecyclerView.setLayoutManager(layoutManager);
                holder.favrecyclerView.setAdapter(new FavAdapter(context, favList));
                holder.favrecyclerView.setHasFixedSize(true);
            }
        }

        @Override
        public int getItemCount() {
            return Items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return Items.get(position) == null ? TYPE_HEADER : TYPE_ITEM;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

            LinearLayout parentlay, messageLay;
            RelativeLayout unseenLay;
            TextView name, message, time, unseenCount, typing;
            ImageView tickimage, typeicon, mute;
            CircleImageView profileimage;
            View profileview;

            public MyViewHolder(View view) {
                super(view);

                parentlay = view.findViewById(R.id.parentlay);
                message = view.findViewById(R.id.message);
                time = view.findViewById(R.id.time);
                name = view.findViewById(R.id.name);
                profileimage = view.findViewById(R.id.profileimage);
                tickimage = view.findViewById(R.id.tickimage);
                typeicon = view.findViewById(R.id.typeicon);
                unseenLay = view.findViewById(R.id.unseenLay);
                unseenCount = view.findViewById(R.id.unseenCount);
                profileview = view.findViewById(R.id.profileview);
                typing = view.findViewById(R.id.typing);
                messageLay = view.findViewById(R.id.messageLay);
                mute = view.findViewById(R.id.mute);

                parentlay.setOnClickListener(this);
                profileimage.setOnClickListener(this);
                parentlay.setOnLongClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.parentlay:
                        if (Items.size() > 0 && getAdapterPosition() != -1) {
                            Intent i = new Intent(context, ChatActivity.class);
                            i.putExtra("user_id", Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                            startActivity(i);
                        }
                        break;
                    case R.id.profileimage:
                        openUserDialog(profileview, Items.get(getAdapterPosition()), context);
                        break;

                }
            }

            @Override
            public boolean onLongClick(View view) {
                switch (view.getId()) {
                    case R.id.parentlay:
                        View bottomView = getLayoutInflater().inflate(R.layout.chat_longpress_dialog, null);
                        BottomSheetDialog dialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.setContentView(bottomView);

                        String userId = Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID);
                        ContactsData.Result results = dbhelper.getContactDetail(userId);
                        TextView txtFavourite = bottomView.findViewById(R.id.txtFavourite);
                        TextView txtView = bottomView.findViewById(R.id.txtView);
                        TextView txtClear = bottomView.findViewById(R.id.txtClear);
                        TextView txtDelete = bottomView.findViewById(R.id.txtDelete);
                        TextView txtArchive = bottomView.findViewById(R.id.txtArchive);
                        TextView txtHideChat = bottomView.findViewById(R.id.txtHideChat);

                        if (results.favourited.equals("true")) {
                            txtFavourite.setText(getString(R.string.remove_favourite));
                        } else {
                            txtFavourite.setText(getString(R.string.mark_favourite));
                        }

                        txtArchive.setVisibility(View.GONE);
                        txtHideChat.setText("Unhide Chat");

                        txtHideChat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (dialog.isShowing() && dialog!=null)
                                    dialog.dismiss();
                                archiveChats(getAdapterPosition(),userId);
                            }
                        });

                        txtFavourite.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                                if (results.favourited.equals("true")) {
                                    dbhelper.updateFavUser(userId, "false");
                                    Toast.makeText(context, getString(R.string.removed_favourites), Toast.LENGTH_SHORT).show();
                                } else {
                                    dbhelper.updateFavUser(userId, "true");
                                    Toast.makeText(context, getString(R.string.marked_favourite), Toast.LENGTH_SHORT).show();
                                }
                                List<ContactsData.Result> favList = dbhelper.getFavContacts(HideChatActivity.this);
                                if (chatAry.size() > 0 && chatAry.get(0) != null) {
                                    chatAry.add(0, null);
                                } else if (favList.size() == 0 && chatAry.size() > 0 && chatAry.get(0) == null) {
                                    chatAry.remove(0);
                                }
                                recyclerViewAdapter.notifyDataSetChanged();
                            }
                        });

                        txtView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                                Intent profile = new Intent(context, ProfileActivity.class);
                                profile.putExtra(Constants.TAG_USER_ID, userId);
                                startActivity(profile);
                            }
                        });

                        txtClear.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                                deleteChatConfirmDialog(context, userId, getAdapterPosition(), true);
                            }
                        });

                        txtDelete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                                deleteChatConfirmDialog(context, userId, getAdapterPosition(), false);
                            }
                        });

                        dialog.show();
                        break;
                }
                return false;
            }
        }

        public class HeaderView extends RecyclerView.ViewHolder implements View.OnClickListener {

            RecyclerView favrecyclerView;
            ConstraintLayout parentLay;

            public HeaderView(View view) {
                super(view);
                favrecyclerView = view.findViewById(R.id.favrecyclerView);
                parentLay = view.findViewById(R.id.parentLay);
            }

            @Override
            public void onClick(View v) {

            }
        }
    }

    private ArrayList<String> getHideChat(){
        if (null == hideChatID) {
            hideChatID = new ArrayList<>();
        }

        try {
            hideChatID = (ArrayList<String>) ObjectSerializer.deserialize(pref.getString("hideId", ObjectSerializer.serialize(new ArrayList<String>())));
            Log.e("LLLLL_Hide: ", String.valueOf(hideChatID));
            return hideChatID;
        } catch (IOException e) {
            Log.e("LLLLLLL_EX11: ", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void archiveChats(int pos,String userID){
            ArrayList<String> hideChatID1 = new ArrayList<>();
            hideChatID1.clear();
            for (int i = 0; i < getHideChat().size(); i++) {
                String archive = getHideChat().get(i);
                if (!userID.equalsIgnoreCase(archive)){
                    hideChatID1.add(archive);
                }
            }

            Log.e("LLLLL_Hide2211: ", String.valueOf(hideChatID1));
            try {
                editor.putString("hideId", ObjectSerializer.serialize(hideChatID1));
            } catch (IOException e) {
                Log.e("LLLLLLL_Hide2EX11: ", Objects.requireNonNull(e.getMessage()));
                e.printStackTrace();
            }
            editor.commit();
            chatAry.remove(pos);
            recyclerViewAdapter.notifyDataSetChanged();

            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();

    }

    public class FavAdapter extends RecyclerView.Adapter<FavAdapter.MyViewHolder> {

        List<ContactsData.Result> favList;
        Context context;

        public FavAdapter(Context context, List<ContactsData.Result> favList) {
            this.context = context;
            this.favList = favList;
        }

        @Override
        public FavAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fav_item, parent, false);

            return new FavAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final FavAdapter.MyViewHolder holder, int position) {

            ContactsData.Result result = favList.get(position);

            if (result.blockedme.equals("block")) {
                Glide.with(context).load(R.drawable.change_camera)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
                        .into(holder.profileimage);
            } else {
                if (result.privacy_profile_image.equalsIgnoreCase(TAG_MY_CONTACTS)) {
                    if (result.contactstatus != null && result.contactstatus.equalsIgnoreCase(TRUE)) {
                        Glide.with(context).load(Constants.USER_IMG_PATH + result.user_image).thumbnail(0.5f)
                                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
                                .into(holder.profileimage);
                    } else {
                        Glide.with(context).load(R.drawable.change_camera).thumbnail(0.5f)
                                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
                                .into(holder.profileimage);
                    }

                } else if (result.privacy_profile_image.equalsIgnoreCase(TAG_NOBODY)) {
                    Glide.with(context).load(R.drawable.change_camera).thumbnail(0.5f)
                            .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
                            .into(holder.profileimage);
                } else {
                    Glide.with(context).load(Constants.USER_IMG_PATH + result.user_image).thumbnail(0.5f)
                            .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
                            .into(holder.profileimage);
                }
            }

            holder.txtName.setText(result.user_name);
        }

        @Override
        public int getItemCount() {
            return favList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            LinearLayout parentlay;
            CircleImageView profileimage;
            TextView txtName;

            public MyViewHolder(View view) {
                super(view);

                parentlay = view.findViewById(R.id.parentlay);
                profileimage = view.findViewById(R.id.userImage);
                txtName = view.findViewById(R.id.txtName);

                parentlay.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.parentlay:
                        if (favList.size() > 0 && getAdapterPosition() != -1) {
                            Intent i = new Intent(context, ChatActivity.class);
                            i.putExtra("user_id", favList.get(getAdapterPosition()).user_id);
                            startActivity(i);
                        }
                        break;
                }
            }
        }

    }

    private void deleteChatConfirmDialog(Context context, String userId, int position, boolean clearChat) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title = dialog.findViewById(R.id.title);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);
        yes.setText(getString(R.string.im_sure));
        no.setText(getString(R.string.nope));
        if(clearChat) {
            title.setText(R.string.really_delete_chat_history);
        } else {
            title.setText(R.string.really_delete_chat);
        }
        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(clearChat) {
                    dbhelper.deleteAllChats(GetSet.getUserId() + userId);
                    dbhelper.updateRecentChat(GetSet.getUserId() + userId,Constants.TAG_UNREAD_COUNT,"0");
                } else {
                    dbhelper.deleteAllChats(GetSet.getUserId() + userId);
                    dbhelper.deleteRecentChat(GetSet.getUserId() + userId);
                }
                /*chatAry.get(position).put(Constants.TAG_MESSAGE, "");
                chatAry.get(position).put(Constants.TAG_DELIVERY_STATUS, "");
                chatAry.get(position).put(Constants.TAG_DELIVERY_STATUS, "");*/
                chatAry.clear();
                chatAry.addAll(dbhelper.getAllRecentsMessages(HideChatActivity.this));
                List<ContactsData.Result> favList = dbhelper.getFavContacts(HideChatActivity.this);
                if (favList.size() > 0) {
                    chatAry.add(0, null);
                }
                recyclerViewAdapter.notifyDataSetChanged();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void openUserDialog(View view, HashMap<String, String> hashMap, Context context) {
        try {
            Intent i = new Intent(context, DialogActivity.class);
            i.putExtra(Constants.TAG_USER_ID, hashMap.get(Constants.TAG_USER_ID));
            i.putExtra(Constants.TAG_USER_NAME, hashMap.get(Constants.TAG_USER_NAME));
            if (hashMap.get(TAG_PRIVACY_PROFILE).equalsIgnoreCase(TAG_MY_CONTACTS)) {
                if (hashMap.get(TAG_CONTACT_STATUS) != null && hashMap.get(TAG_CONTACT_STATUS) != null && hashMap.get(TAG_CONTACT_STATUS).equalsIgnoreCase(TRUE)) {
                    i.putExtra(Constants.TAG_USER_IMAGE, hashMap.get(Constants.TAG_USER_IMAGE));
                } else {
                    i.putExtra(Constants.TAG_USER_IMAGE, "");
                }
            } else if (hashMap.get(TAG_PRIVACY_PROFILE).equalsIgnoreCase(TAG_NOBODY)) {
                i.putExtra(Constants.TAG_USER_IMAGE, "");
            } else {
                i.putExtra(Constants.TAG_USER_IMAGE, hashMap.get(Constants.TAG_USER_IMAGE));
            }
            i.putExtra(Constants.TAG_BLOCKED_ME, hashMap.get(Constants.TAG_BLOCKED_ME));

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(((MainActivity) context), view, getURLForResource(R.drawable.change_camera));
            startActivity(i, options.toBundle());
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public static String getURLForResource(int resourceId) {
        return Uri.parse("android.resource://com.topzi.chat/" + resourceId).toString();
    }

    public String getFormattedDate(Context context, long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis * 1000L);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEE, MMM d";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return String.valueOf(DateFormat.format(timeFormatString, smsTime));
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return getString(R.string.yesterday);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return DateFormat.format("MMM dd yyyy", smsTime).toString();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");
        SocketConnection.getInstance(HideChatActivity.this).setRecentChatReceivedListener(this);
        if (recyclerViewAdapter != null) {
            chatAry.clear();
            chatAry.addAll(dbhelper.getAllRecentsMessages(HideChatActivity.this));
            List<ContactsData.Result> favList = dbhelper.getFavContacts(HideChatActivity.this);
            if (favList.size() > 0) {
                chatAry.add(0, null);
            }
            recyclerViewAdapter.notifyDataSetChanged();
            if (chatAry.size() == 0) {
                nullLay.setVisibility(View.VISIBLE);
            } else {
                nullLay.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause");
        SocketConnection.getInstance(HideChatActivity.this).setRecentChatReceivedListener(null);
    }

    @Override
    public void onRecentChatReceived() {
        Log.v("Chat", "onGroupRecentReceived");
        HideChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recyclerViewAdapter != null) {
                    chatAry.clear();
                    chatAry.addAll(dbhelper.getAllRecentsMessages(HideChatActivity.this));
                    List<ContactsData.Result> favList = dbhelper.getFavContacts(HideChatActivity.this);
                    if (favList.size() > 0) {
                        chatAry.add(0, null);
                    }
                    recyclerViewAdapter.notifyDataSetChanged();
                    if (chatAry.size() > 0) {
                        nullLay.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    @Override
    public void onUserImageChange(final String user_id, final String user_image) {
        Log.v("Chat", "onUserImageChange");
        HideChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recyclerViewAdapter != null && chatAry.size() > 0) {
                    for (int i = 0; i < chatAry.size(); i++) {
                        if (chatAry.get(i) != null && user_id.equals(chatAry.get(i).get(Constants.TAG_USER_ID))) {
                            chatAry.get(i).put(Constants.TAG_USER_IMAGE, user_image);
                            recyclerViewAdapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onBlockStatus(final JSONObject data) {
        HideChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recyclerViewAdapter != null && chatAry.size() > 0) {
                    try {
                        String sender_id = data.getString(Constants.TAG_SENDER_ID);
                        String type = data.getString(Constants.TAG_TYPE);
                        for (int i = 0; i < chatAry.size(); i++) {
                            if (chatAry.get(i) != null && sender_id.equals(chatAry.get(i).get(Constants.TAG_USER_ID))) {
                                chatAry.get(i).put(Constants.TAG_BLOCKED_ME, type);
                                recyclerViewAdapter.notifyItemChanged(i);
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onUpdateChatStatus(final String user_id) {
        Log.v("Chat", "onUpdateChatStatus");
        HideChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recyclerViewAdapter != null && chatAry.size() > 0) {
                    for (int i = 0; i < chatAry.size(); i++) {
                        if (chatAry.get(i) != null && user_id.equals(chatAry.get(i).get(Constants.TAG_USER_ID)) && linearLayoutManager.findViewByPosition(i) != null) {
                            String msgId = chatAry.get(i).get(Constants.TAG_MESSAGE_ID);
                            Log.v("msgId", "msgId=" + msgId);
                            if (msgId != null) {
                                MessagesData mdata = dbhelper.getSingleMessage(msgId);
                                if (mdata != null) {
                                    chatAry.get(i).put(Constants.TAG_DELIVERY_STATUS, mdata.delivery_status);
                                    View itemView = linearLayoutManager.findViewByPosition(i);
                                    ImageView tickimage = itemView.findViewById(R.id.tickimage);
                                    if (mdata.sender_id != null && mdata.sender_id.equals(GetSet.getUserId())) {
                                        tickimage.setVisibility(View.VISIBLE);
                                        if (mdata.delivery_status.equals("read")) {
                                            tickimage.setImageResource(R.drawable.double_tick);
                                        } else if (mdata.delivery_status.equals("sent")) {
                                            tickimage.setImageResource(R.drawable.double_tick_unseen);
                                        } else if (mdata.progress.equals("completed") && (mdata.message_type.equals("image") ||
                                                mdata.message_type.equals("video") || mdata.message_type.equals("file") || mdata.message_type.equals("audio"))) {
                                            tickimage.setImageResource(R.drawable.single_tick);
                                        } else if (mdata.message_type.equals("text") || mdata.message_type.equals("contact") || mdata.message_type.equals("location")) {
                                            tickimage.setImageResource(R.drawable.single_tick);
                                        } else {
                                            tickimage.setVisibility(View.GONE);
                                        }
                                    } else {
                                        tickimage.setVisibility(View.GONE);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onListenTyping(final JSONObject data) {
        Log.v("Chat", "onListenGroupTyping");
        HideChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recyclerViewAdapter != null && linearLayoutManager != null && chatAry.size() > 0) {
                    try {
                        for (int i = 0; i < chatAry.size(); i++) {
                            if (chatAry.get(i) != null && data.get(Constants.TAG_SENDER_ID).equals(chatAry.get(i).get(Constants.TAG_USER_ID))
                                    && linearLayoutManager.findViewByPosition(i) != null) {
                                View itemView = linearLayoutManager.findViewByPosition(i);
                                LinearLayout messageLay;
                                messageLay = itemView.findViewById(R.id.messageLay);
                                TextView typing = itemView.findViewById(R.id.typing);
                                if (data.get(Constants.TAG_SENDER_ID).equals(chatAry.get(i).get(Constants.TAG_USER_ID)) && data.get("type").equals("typing")) {
                                    typing.setText(getString(R.string.typing));
                                    typing.setVisibility(View.VISIBLE);
                                    messageLay.setVisibility(View.INVISIBLE);
                                } else {
                                    typing.setVisibility(View.GONE);
                                    messageLay.setVisibility(View.VISIBLE);
                                }
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onPrivacyChanged(final JSONObject jsonObject) {
//        Log.i(TAG, "onPrivacyChanged: " + jsonObject);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recyclerViewAdapter != null) {
                    chatAry.clear();
                    chatAry.addAll(dbhelper.getAllRecentsMessages(HideChatActivity.this));
                    List<ContactsData.Result> favList = dbhelper.getFavContacts(HideChatActivity.this);
                    if (favList.size() > 0) {
                        chatAry.add(0, null);
                    }
                    recyclerViewAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(HideChatActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
    
}
