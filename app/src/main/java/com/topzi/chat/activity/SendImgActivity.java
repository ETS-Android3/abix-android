package com.topzi.chat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topzi.chat.R;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.helper.Utils;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.droidninja.imageeditengine.ImageEditor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.topzi.chat.activity.GroupChatActivity.getFormattedDate;
import static com.topzi.chat.helper.Utils.getURLForResource;
import static com.topzi.chat.utils.Constants.TAG_GROUP_ID;
import static com.topzi.chat.utils.Constants.TAG_MEMBER;
import static com.topzi.chat.utils.Constants.TAG_MEMBER_ID;
import static com.topzi.chat.utils.Constants.TAG_MEMBER_NO;
import static com.topzi.chat.utils.Constants.TAG_MY_CONTACTS;
import static com.topzi.chat.utils.Constants.TAG_NOBODY;
import static com.topzi.chat.utils.Constants.TRUE;
import static com.topzi.chat.utils.Constants.setStatusBarGradiant;

public class SendImgActivity extends AppCompatActivity {

    @BindView(R.id.img_back)
    ImageView img_back;
    @BindView(R.id.rv_chat)
    RecyclerView recyclerView;
    @BindView(R.id.img_status_setting)
    ImageView img_status_setting;
    @BindView(R.id.rv_group)
    RecyclerView rv_group;
    @BindView(R.id.tvMystatus)
    TextView tvMystatus;

    private ArrayList<HashMap<String, String>> chatAry = new ArrayList<>();
    private ArrayList<HashMap<String, String>> chatAry1 = new ArrayList<>();
    private ArrayList<HashMap<String, String>> chatAry2 = new ArrayList<>();
    private DatabaseHandler dbhelper;
    private LinearLayoutManager linearLayoutManager;
    private LinearLayoutManager linearLayoutManager1;
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerGrpViewAdapter recyclerGrpViewAdapter;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ArrayList<HashMap<String, String>> groupList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(SendImgActivity.this);
        setContentView(R.layout.activity_send_img);
        ButterKnife.bind(SendImgActivity.this);

        dbhelper = DatabaseHandler.getInstance(SendImgActivity.this);

        img_back.setOnClickListener(v -> onBackPressed());

        linearLayoutManager = new LinearLayoutManager(SendImgActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        pref = SendImgActivity.this.getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        editor = pref.edit();

        List<ContactsData.Result> favList = dbhelper.getFavContacts(SendImgActivity.this);
        if (favList.size() > 0) {
            chatAry.add(0, null);
        }
        recyclerViewAdapter = new RecyclerViewAdapter(SendImgActivity.this, chatAry);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();

        chatAry.clear();
        chatAry.addAll(dbhelper.getAllRecentsMessages(SendImgActivity.this));

        linearLayoutManager1 = new LinearLayoutManager(SendImgActivity.this, LinearLayoutManager.VERTICAL, false);
        rv_group.setLayoutManager(linearLayoutManager1);
        rv_group.setHasFixedSize(true);

        groupList.clear();
        groupList.addAll(dbhelper.getGroupRecentMessages(SendImgActivity.this));
        recyclerGrpViewAdapter = new RecyclerGrpViewAdapter(SendImgActivity.this, groupList);
        rv_group.setAdapter(recyclerGrpViewAdapter);
        recyclerGrpViewAdapter.notifyDataSetChanged();

        tvMystatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String type = intent.getType();
                String action = intent.getAction();
                if (Intent.ACTION_SEND.equals(action) && type != null) {
                    if (type.startsWith("image/")) {
                        handleStatusSendImage(intent,"");
                    }
                }
            }
        });

        img_status_setting.setOnClickListener(v -> {
            Intent intent = new Intent(SendImgActivity.this, StatusPrivacy.class);
            startActivity(intent);
        });

    }
    private void handleSendImage(Intent intent,String userId) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            Log.e("LLLLLLL_Image: ", getPath(imageUri));

            editor.putString("sendImageUSerID",userId);
            editor.apply();
            editor.commit();

            new ImageEditor.Builder(this, getPath(imageUri),"Chat")
                    .setStickerAssets("stickers")
                    .open();
            finish();
        } else {
            Toast.makeText(this, "Error occured, URI is invalid", Toast.LENGTH_LONG).show();
        }

    }

    private void handleGrpSendImage(Intent intent,String group_id) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            Log.e("LLLLLLL_Image: ", getPath(imageUri));

            editor.putString("sendGrpImage",group_id);
            editor.apply();
            editor.commit();

            new ImageEditor.Builder(this, getPath(imageUri),"Group")
                    .setStickerAssets("stickers")
                    .open();
            finish();
        } else {
            Toast.makeText(this, "Error occured, URI is invalid", Toast.LENGTH_LONG).show();
        }

    }

    private void handleStatusSendImage(Intent intent,String userID) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            Log.e("LLLLLLL_Image: ", getPath(imageUri));

            editor.putString("sendStatusImage",userID);
            editor.apply();
            editor.commit();

            new ImageEditor.Builder(this, getPath(imageUri),"Status")
                    .setStickerAssets("stickers")
                    .open();
            finish();
        } else {
            Toast.makeText(this, "Error occured, URI is invalid", Toast.LENGTH_LONG).show();
        }

    }


    public String getPath(Uri uri)
    {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index =             cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.parentlay:
                        if (Items.size() > 0 && getAdapterPosition() != -1) {
                            Intent intent = getIntent();
                            String type = intent.getType();
                            String action = intent.getAction();
                            if (Intent.ACTION_SEND.equals(action) && type != null) {
                                if (type.startsWith("image/")) {
                                    handleSendImage(intent,Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                                }
                            }
//                            if (Intent.ACTION_SEND_MULTIPLE.equals(action) && getIntent().hasExtra(Intent.EXTRA_STREAM)) {
//                                ArrayList<Parcelable> list = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
//                                Log.e("LLLLLLL_Image: ",list.toString());
//
//                                for (int i = 0; i < list.size(); i++) {
//                                    editor.putString("sendImageUSerID",Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
//                                    editor.apply();
//                                    editor.commit();
//
//                                    new ImageEditor.Builder(this, getPath(list.get(i)))
//                                            .setStickerAssets("stickers")
//                                            .open();
//
//                                }
//
//                                Intent i = new Intent(SendImgActivity.this, DrawAtivity.class);
//                                i.putExtra("user_id", Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
//                                i.putExtra("ImageList", list);
//                                startActivity(i);
//                                finish();
//                            }
//                            Intent i = new Intent(context, ChatActivity.class);
//                            i.putExtra("user_id", Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
//                            startActivity(i);
                        }
                        break;

                }
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
                            Intent intent = getIntent();
                            String type = intent.getType();
                            String action = intent.getAction();
                            if (Intent.ACTION_SEND.equals(action) && type != null) {
                                if (type.startsWith("image/")) {
//                                    handleSendImage(intent,favList.get(getAdapterPosition()));
                                }
                            }
                        }
                        break;
                }
            }
        }

    }

    public class RecyclerGrpViewAdapter extends RecyclerView.Adapter<RecyclerGrpViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> groupList;
        Context context;

        public RecyclerGrpViewAdapter(Context context, ArrayList<HashMap<String, String>> Items) {
            this.groupList = Items;
            this.context = context;
        }

        @Override
        public RecyclerGrpViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item, parent, false);

            return new RecyclerGrpViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerGrpViewAdapter.MyViewHolder holder, final int position) {

            final HashMap<String, String> groupData = groupList.get(position);
            holder.name.setText(groupData.get(Constants.TAG_GROUP_NAME));
            holder.typing.setVisibility(View.GONE);
            holder.messageLay.setVisibility(View.VISIBLE);
            holder.message.setText(groupData.get(Constants.TAG_MESSAGE) != null ? groupData.get(Constants.TAG_MESSAGE) : "");

            if (groupData.get(Constants.TAG_CHAT_TIME) != null) {
                holder.time.setText(Utils.getFormattedDate(context, Long.parseLong(groupData.get(Constants.TAG_CHAT_TIME).replace(".0", ""))));
            }

            Glide.with(context).load(Constants.GROUP_IMG_PATH + groupData.get(Constants.TAG_GROUP_IMAGE)).thumbnail(0.5f)
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.create_group).error(R.drawable.create_group).override(ApplicationClass.dpToPx(context, 70)))
                    .into(holder.profileimage);

            if (groupData.get(Constants.TAG_MESSAGE_TYPE) != null) {
                switch (groupData.get(Constants.TAG_MESSAGE_TYPE)) {
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
                    case "change_number":
                        if (!groupData.get(Constants.TAG_MEMBER_ID).equals(GetSet.getUserId())) {
                            holder.message.setText(groupData.get(Constants.TAG_MESSAGE) != null ? groupData.get(Constants.TAG_MESSAGE) : "");
                        } else {
                            holder.message.setText("");
                        }
                        break;
                    default:
                        holder.typeicon.setVisibility(View.GONE);
                        break;
                }
            } else {
                holder.typeicon.setVisibility(View.GONE);
            }

            if (groupData.get(Constants.TAG_MUTE_NOTIFICATION).equals("true")) {
                holder.mute.setVisibility(View.VISIBLE);
            } else {
                holder.mute.setVisibility(View.GONE);
            }

            if (groupData.get(Constants.TAG_UNREAD_COUNT).equals("") || groupData.get(Constants.TAG_UNREAD_COUNT).equals("0")) {
                holder.unseenLay.setVisibility(View.GONE);
            } else {
                holder.unseenLay.setVisibility(View.VISIBLE);
                holder.unseenCount.setText(groupData.get(Constants.TAG_UNREAD_COUNT));
            }
        }

        @Override
        public int getItemCount() {
            return groupList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.parentlay:
                        Intent intent = getIntent();
                        String type = intent.getType();
                        String action = intent.getAction();
                        if (Intent.ACTION_SEND.equals(action) && type != null) {
                            if (type.startsWith("image/")) {
                                handleGrpSendImage(intent,groupList.get(getAdapterPosition()).get(TAG_GROUP_ID));
                            }
                        }
                        break;
                    case R.id.profileimage:
                        openUserDialog(profileview, groupList.get(getAdapterPosition()), context);
                        break;

                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recyclerViewAdapter != null) {
            groupList.clear();
            groupList.addAll(dbhelper.getGroupRecentMessages(SendImgActivity.this));
            recyclerViewAdapter.notifyDataSetChanged();
        }

    }

    private void openUserDialog(View view, HashMap<String, String> data, Context context) {
        Intent i = new Intent(context, DialogActivity.class);
        i.putExtra(Constants.TAG_GROUP_ID, data.get(Constants.TAG_GROUP_ID));
        i.putExtra(Constants.TAG_GROUP_NAME, data.get(Constants.TAG_GROUP_NAME));
        i.putExtra(Constants.TAG_GROUP_IMAGE, data.get(Constants.TAG_GROUP_IMAGE));
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(((MainActivity) context), view, getURLForResource(R.drawable.change_camera));
        startActivity(i, options.toBundle());
    }

    public static HashMap<String, String> getMessages(DatabaseHandler dbhelper, Context mContext, HashMap<String, String> groupData){
        if (groupData.get(Constants.TAG_MESSAGE_TYPE) != null){
            switch (groupData.get(Constants.TAG_MESSAGE_TYPE)) {
                case "text":
                case "image":
                case "video":
                case "file":
                case "location":
                case "contact":
                case "audio":
                    groupData.put(Constants.TAG_MESSAGE, groupData.get(Constants.TAG_MESSAGE) != null ? groupData.get(Constants.TAG_MESSAGE) : "");
                    break;
                case "create_group":
                    if (groupData.get(Constants.TAG_GROUP_ADMIN_ID).equals(GetSet.getUserId())) {
                        groupData.put(Constants.TAG_MESSAGE, mContext.getString(R.string.you_created_the_group));
                    } else {
                        if (dbhelper.isUserExist(groupData.get(Constants.TAG_GROUP_ADMIN_ID))) {
                            groupData.put(Constants.TAG_MESSAGE, ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupData.get(Constants.TAG_GROUP_ADMIN_ID))) + " " + mContext.getString(R.string.created_the_group));
                        } else {
                            groupData.put(Constants.TAG_MESSAGE, mContext.getString(R.string.group_created));
                        }
                    }
                    break;
                case "add_member":
                    if (groupData.get(Constants.TAG_ATTACHMENT).equals("")) {
                        if (dbhelper.isUserExist(groupData.get(Constants.TAG_GROUP_ADMIN_ID))) {
                            groupData.put(Constants.TAG_MESSAGE, ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupData.get(Constants.TAG_GROUP_ADMIN_ID)) + " " + mContext.getString(R.string.added_you)));
                        } else {
                            groupData.put(Constants.TAG_MESSAGE, mContext.getString(R.string.you_were_added));
                        }
                    } else {
                        try {
                            JSONArray jsonArray = new JSONArray(groupData.get(Constants.TAG_ATTACHMENT));
                            ArrayList<String> members = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                if (jsonObject.getString(TAG_MEMBER_ID).equals(GetSet.getUserId())) {
                                    members.add(mContext.getString(R.string.you));
                                } else if (dbhelper.isUserExist(jsonObject.getString(Constants.TAG_MEMBER_ID))) {
                                    members.add(ApplicationClass.getContactName(mContext, jsonObject.getString(TAG_MEMBER_NO)));
                                }
                            }
                            String memberstr = members.toString().replaceAll("[\\[\\]]|(?<=,)\\s+", "");
                            if (groupData.get(Constants.TAG_MEMBER_ID).equals(GetSet.getUserId())) {
                                groupData.put(Constants.TAG_MESSAGE, mContext.getString(R.string.you_added) + " " + memberstr);
                            } else {
                                groupData.put(Constants.TAG_MESSAGE, ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupData.get(Constants.TAG_MEMBER_ID))) +" "+ mContext.getString(R.string.added)+" " + memberstr);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "group_image":
                    if (groupData.get(Constants.TAG_MEMBER_ID).equalsIgnoreCase(GetSet.getUserId())) {
                        groupData.put(Constants.TAG_MESSAGE, mContext.getString(R.string.you) + " " + groupData.get(Constants.TAG_MESSAGE));
                    } else {
                        groupData.put(Constants.TAG_MESSAGE, ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupData.get(Constants.TAG_MEMBER_ID))) + " " + groupData.get(Constants.TAG_MESSAGE));
                    }
                    break;
                case "subject":
                    if (groupData.get(Constants.TAG_MEMBER_ID).equalsIgnoreCase(GetSet.getUserId())) {
                        groupData.put(Constants.TAG_MESSAGE, mContext.getString(R.string.you) + " " + groupData.get(Constants.TAG_MESSAGE));
                    } else {
                        groupData.put(Constants.TAG_MESSAGE, ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupData.get(Constants.TAG_MEMBER_ID))) + " " + groupData.get(Constants.TAG_MESSAGE));
                    }
                    break;
                case "left":
                    if (groupData.get(Constants.TAG_MEMBER_ID).equalsIgnoreCase(GetSet.getUserId())) {
                        groupData.put(Constants.TAG_MESSAGE, mContext.getString(R.string.you_left));
                    } else {
                        groupData.put(Constants.TAG_MESSAGE, ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupData.get(Constants.TAG_MEMBER_ID))) + " " + mContext.getString(R.string.left));
                    }
                    break;
                case "remove_member":
                    if (groupData.get(Constants.TAG_GROUP_ADMIN_ID).equals(GetSet.getUserId())) {
                        groupData.put(Constants.TAG_MESSAGE, mContext.getString(R.string.you_removed) + " " + ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupData.get(Constants.TAG_MEMBER_ID))));
                    } else {
                        if (groupData.get(Constants.TAG_MEMBER_ID).equals(GetSet.getUserId())) {
                            groupData.put(Constants.TAG_MESSAGE, ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupData.get(Constants.TAG_GROUP_ADMIN_ID)) + " " + mContext.getString(R.string.removed_you)));
                        } else {
                            groupData.put(Constants.TAG_MESSAGE, ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupData.get(Constants.TAG_GROUP_ADMIN_ID))) + " " + mContext.getString(R.string.removed) + " " +
                                    ApplicationClass.getContactName(mContext, dbhelper.getContactPhone(groupData.get(Constants.TAG_MEMBER_ID))));
                        }
                    }
                    break;
                case "admin":
                    if (groupData.get(Constants.TAG_ATTACHMENT).equals(TAG_MEMBER)) {
                        groupData.put(Constants.TAG_MESSAGE, mContext.getString(R.string.you_are_no_longer_as_admin));
                    } else {
                        groupData.put(Constants.TAG_MESSAGE, mContext.getString(R.string.you_are_now_an_admin));
                    }
                    break;
                case "date":
                    groupData.put(Constants.TAG_MESSAGE, Utils.getFormattedDate(mContext, Long.parseLong(groupData.get(Constants.TAG_CHAT_TIME))));
                    break;
            }
        } else {
            groupData.put(Constants.TAG_MESSAGE, "");
        }

        return groupData;
    }


}
