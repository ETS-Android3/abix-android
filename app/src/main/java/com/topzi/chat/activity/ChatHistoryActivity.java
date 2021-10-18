package com.topzi.chat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

import com.topzi.chat.R;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.topzi.chat.utils.ObjectSerializer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatHistoryActivity extends AppCompatActivity implements View.OnClickListener {

    RecyclerViewAdapter recyclerViewAdapter;

    LinearLayout ll_main,nullLay;
    RecyclerView recyclerView;
    TextView tvExportChat,tvArchive,tvClearAll,tvDeleteAll;
    TextView nullText;
    Toolbar toolbar;
    ImageView btnBack;
    TextView txtTitle;

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    DatabaseHandler dbhelper;
    LinearLayoutManager linearLayoutManager;
    List<MessagesData> messagesList = new ArrayList<>();
    ArrayList<HashMap<String, String>> chatAry = new ArrayList<>();
    ArrayList<String> archiveUserID = new ArrayList<>();

    ArrayList<HashMap<String, String>> groupList = new ArrayList<>();

    ContactsData.Result results;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        pref = ChatHistoryActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        dbhelper = DatabaseHandler.getInstance(this);

        toolbar = findViewById(R.id.actionbar);
        btnBack = toolbar.findViewById(R.id.backbtn);
        txtTitle = toolbar.findViewById(R.id.title);
        tvExportChat = findViewById(R.id.tv_export_chat);
        tvArchive = findViewById(R.id.tv_archive);
        tvClearAll = findViewById(R.id.tv_clear_all);
        tvDeleteAll = findViewById(R.id.tv_delete_all);
        ll_main = findViewById(R.id.ll_main);
        recyclerView = findViewById(R.id.recyclerView);
        nullLay = findViewById(R.id.nullLay);
        nullText = findViewById(R.id.nullText);

        initToolBar();
        if (getarchiveChat().isEmpty()){
            tvArchive.setText(getString(R.string.archive_all_chat));
        } else {
            if (pref.getBoolean("archiveAll",false))
                tvArchive.setText(getString(R.string.archive_all_chat));
            else
                tvArchive.setText(getString(R.string.unarchive_all_chat));
        }

        linearLayoutManager = new LinearLayoutManager(ChatHistoryActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        chatAry.clear();
        chatAry.addAll(dbhelper.getAllRecentsMessages(ChatHistoryActivity.this));
        List<ContactsData.Result> favList = dbhelper.getFavContacts(ChatHistoryActivity.this);
        if (favList.size() > 0) {
            chatAry.add(0, null);
        }

        groupList.clear();
        groupList.addAll(dbhelper.getGroupRecentMessages(ChatHistoryActivity.this));

        getarchiveChat();
        recyclerViewAdapter = new RecyclerViewAdapter(ChatHistoryActivity.this, chatAry);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();

        nullText.setText(R.string.no_chat_yet_buddy);

        tvExportChat.setOnClickListener(this);
        tvArchive.setOnClickListener(this);
        tvClearAll.setOnClickListener(this);
        tvDeleteAll.setOnClickListener(this);
    }

    private void initToolBar() {
        txtTitle.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        txtTitle.setText(R.string.chat_history);
        btnBack.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primarytext));
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onBackPressed();
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.tv_export_chat){
            ll_main.setVisibility(View.GONE);
            if (chatAry.size() == 0) {
                recyclerView.setVisibility(View.GONE);
                nullLay.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                nullLay.setVisibility(View.GONE);
            }
        } else if (v.getId()==R.id.tv_archive){
            archiveChats();
        } else if (v.getId()==R.id.tv_clear_all){
            deleteChatConfirmDialog(ChatHistoryActivity.this, true);
        } else if (v.getId()==R.id.tv_delete_all){
            deleteAllChatConfirmDialog(ChatHistoryActivity.this,true);
        }
    }

    private void deleteChatConfirmDialog(Context context, boolean clearChat) {
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
                for (int i = 0; i < chatAry.size(); i++) {
                    String userId = chatAry.get(i).get(Constants.TAG_USER_ID);
                    if (clearChat) {
                        dbhelper.deleteAllChats(GetSet.getUserId() + userId);
                        dbhelper.updateRecentChat(GetSet.getUserId() + userId, Constants.TAG_UNREAD_COUNT, "0");
                    } else {
                        dbhelper.deleteAllChats(GetSet.getUserId() + userId);
                        dbhelper.deleteRecentChat(GetSet.getUserId() + userId);
                    }

                }

                for (int i = 0; i < groupList.size(); i++) {
                    final HashMap<String, String> groupData = groupList.get(i);
                    dbhelper.deleteGroupMessages(groupData.get(Constants.TAG_GROUP_ID));
                    dbhelper.updateGroupMessageData(GetSet.getUserId() + userId, Constants.TAG_UNREAD_COUNT, "0");
                }

                chatAry.clear();
                chatAry.addAll(dbhelper.getAllRecentsMessages(ChatHistoryActivity.this));
                List<ContactsData.Result> favList = dbhelper.getFavContacts(ChatHistoryActivity.this);
                if (favList.size() > 0) {
                    chatAry.add(0, null);
                }
                recyclerViewAdapter.notifyDataSetChanged();
                dialog.dismiss();
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

    private void deleteAllChatConfirmDialog(Context context, boolean clearChat) {
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
                for (int i = 0; i < chatAry.size(); i++) {
                    String userId = chatAry.get(i).get(Constants.TAG_USER_ID);
                    if (clearChat) {
                        dbhelper.deleteAllChats(GetSet.getUserId() + userId);
                        dbhelper.deleteRecentChat(GetSet.getUserId() + userId);
                    } else {
                        dbhelper.deleteAllChats(GetSet.getUserId() + userId);
                        dbhelper.updateRecentChat(GetSet.getUserId() + userId, Constants.TAG_UNREAD_COUNT, "0");
                    }
                }

                for (int i = 0; i < groupList.size(); i++) {
                    final HashMap<String, String> groupData = groupList.get(i);
                    dbhelper.deleteGroupMessages(groupData.get(Constants.TAG_GROUP_ID));
                    dbhelper.deleteGroupRecentChats(groupData.get(Constants.TAG_GROUP_ID));
                }

                recyclerViewAdapter.notifyDataSetChanged();
                dialog.dismiss();
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
                            holder.typeicon.setImageResource(R.drawable.person);
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
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.parentlay:
                        if (Items.size() > 0 && getAdapterPosition() != -1) {
                            userId = Items.get(getAdapterPosition()).get(Constants.TAG_USER_ID);
                            results = dbhelper.getContactDetail(userId);
                            try {
                                messagesList.addAll(getMessagesAry(dbhelper.getMessages(GetSet.getUserId() + userId, "0", "20"), null));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            new LongOperation().execute();
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

    private List<MessagesData> getMessagesAry(List<MessagesData> tmpList, MessagesData lastData) {
        List<MessagesData> msgList = new ArrayList<>();
        if (tmpList.size() == 0 && lastData != null) {
            MessagesData mdata = new MessagesData();
            mdata.message_type = "date";
            mdata.chat_time = lastData.chat_time;
            msgList.add(mdata);
            Log.v("diff", "diff pos=ss" + "&msg=" + lastData.message);
        } else {
            for (int i = 0; i < tmpList.size(); i++) {
                Calendar cal1 = Calendar.getInstance();
                cal1.setTimeInMillis(Long.parseLong(tmpList.get(i).chat_time) * 1000L);

                if (i + 1 < tmpList.size()) {
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTimeInMillis(Long.parseLong(tmpList.get(i + 1).chat_time) * 1000L);

                    boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);

                    if (sameDay) {
                        msgList.add(tmpList.get(i));
                        Log.v("diff", "same pos=" + i + "&msg=" + tmpList.get(i).message);
                    } else {
                        msgList.add(tmpList.get(i));
                        MessagesData mdata = new MessagesData();
                        mdata.message_type = "date";
                        mdata.chat_time = tmpList.get(i).chat_time;
                        msgList.add(mdata);
                        Log.v("diff", "diff pos=" + i + "&msg=" + tmpList.get(i).message);
                    }
                } else {
                    msgList.add(tmpList.get(i));
                }
            }
        }
        return msgList;
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

    public void generateNoteOnSD(Context context) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "topzi/topzi doccuments");
            if (!root.exists()) {
                root.mkdirs();
            }
            String File_name = ApplicationClass.getContactName(this, results.phone_no)+".txt";

            File gpxfile = new File(root, "Topzi chat with "+File_name);
            FileWriter writer = new FileWriter(gpxfile);

            ArrayList<MessagesData> mesgData = new ArrayList<>(messagesList);
            Collections.reverse(mesgData);
            for (int i = 0; i < mesgData.size(); i++) {
                MessagesData messagesData = mesgData.get(i);
                if (messagesData.message!=null) {
                    if (dbhelper.getContactPhone(messagesData.sender_id).equals(""))
                        writer.append(ApplicationClass.getDateTime(Long.parseLong(messagesData.getChat_time()))).append(" - ").append(ApplicationClass.getContactName(this,pref.getString("phoneNumber", null))).append(": ").append(messagesData.getMessage()).append("\n");
                    else
                        writer.append(ApplicationClass.getDateTime(Long.parseLong(messagesData.getChat_time()))).append(" - ").append(ApplicationClass.getContactName(this,dbhelper.getContactPhone(messagesData.sender_id))).append(": ").append(messagesData.getMessage()).append("\n");
                }
            }

            writer.flush();
            writer.close();

            final Uri data = FileProvider.getUriForFile(ChatHistoryActivity.this, "com.topzi.chat.provider", gpxfile);
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/*");
            sharingIntent.putExtra(Intent.EXTRA_STREAM, data);
            startActivity(Intent.createChooser(sharingIntent, "share file with"));

        } catch (IOException e) {
            Log.e("LLLLLL_Error: ", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }
    }

    private final class LongOperation extends AsyncTask<Void, Void, String> {

        ProgressDialog progressDialog;

        public LongOperation() {
            this.progressDialog = new ProgressDialog(ChatHistoryActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Please Wait...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            generateNoteOnSD(ChatHistoryActivity.this);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog.isShowing()&& progressDialog!=null)
                progressDialog.dismiss();

        }
    }

    private void archiveChats(){

        final Dialog dialog = new Dialog(ChatHistoryActivity.this);
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

        if (getarchiveChat().isEmpty()){
            title.setText("Are you sure you want to archive ALL chats");
        } else {
            if (pref.getBoolean("archiveAll",false))
                title.setText("Are you sure you want to archive ALL chats");
            else
                title.setText("Are you sure you want to Unarchive ALL chats");
        }

        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (getarchiveChat().isEmpty()){

                    archiveUserID.clear();
                    try {
                        editor.putString("archiveId", ObjectSerializer.serialize(archiveUserID));
                        editor.commit();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < chatAry.size(); i++) {
                        String userId = chatAry.get(i).get(Constants.TAG_USER_ID);
                        archiveUserID.add(userId);
                    }

                    // save the task list to preference
                    Log.e("LLLLL_Archive11: ", String.valueOf(archiveUserID));
                    try {
                        editor.putString("archiveId", ObjectSerializer.serialize(archiveUserID));
//                    editor.putBoolean("archiveAll", true);
                    } catch (IOException e) {
                        Log.e("LLLLLLL_EX11: ", Objects.requireNonNull(e.getMessage()));
                        e.printStackTrace();
                    }
                    editor.commit();

                    if (getarchiveChat().isEmpty()){
                        tvArchive.setText(getString(R.string.archive_all_chat));
                    } else {
                        if (pref.getBoolean("archiveAll",false))
                            tvArchive.setText(getString(R.string.archive_all_chat));
                        else
                            tvArchive.setText(getString(R.string.unarchive_all_chat));
                    }

                } else {
                    if (pref.getBoolean("archiveAll",false)) {

                        archiveUserID.clear();
                        try {
                            editor.putString("archiveId", ObjectSerializer.serialize(archiveUserID));
                            editor.commit();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        for (int i = 0; i < chatAry.size(); i++) {
                            String userId = chatAry.get(i).get(Constants.TAG_USER_ID);
                            archiveUserID.add(userId);
                        }

                        // save the task list to preference
                        Log.e("LLLLL_Archive11: ", String.valueOf(archiveUserID));
                        try {
                            editor.putString("archiveId", ObjectSerializer.serialize(archiveUserID));
//                    editor.putBoolean("archiveAll", true);
                        } catch (IOException e) {
                            Log.e("LLLLLLL_EX11: ", Objects.requireNonNull(e.getMessage()));
                            e.printStackTrace();
                        }
                        editor.commit();

                        if (getarchiveChat().isEmpty()){
                            tvArchive.setText(getString(R.string.archive_all_chat));
                        } else {
                            if (pref.getBoolean("archiveAll",false))
                                tvArchive.setText(getString(R.string.archive_all_chat));
                            else
                                tvArchive.setText(getString(R.string.unarchive_all_chat));
                        }
                    } else {
                        archiveUserID = new ArrayList<>();
                        try {
                            editor.putString("archiveId", ObjectSerializer.serialize(archiveUserID));
                            editor.commit();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (getarchiveChat().isEmpty()){
                            tvArchive.setText(getString(R.string.archive_all_chat));
                        } else {
                            if (pref.getBoolean("archiveAll",false))
                                tvArchive.setText(getString(R.string.archive_all_chat));
                            else
                                tvArchive.setText(getString(R.string.unarchive_all_chat));
                        }
                    }
                }

                dialog.dismiss();
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


    private ArrayList<String> getarchiveChat(){
        if (null == archiveUserID) {
            archiveUserID = new ArrayList<>();
        }

        try {
            archiveUserID = (ArrayList<String>) ObjectSerializer.deserialize(pref.getString("archiveId", ObjectSerializer.serialize(new ArrayList<String>())));
            Log.e("LLLLL_Archive: ", String.valueOf(archiveUserID));
            return archiveUserID;
        } catch (IOException e) {
            Log.e("LLLLLLL_EX: ", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    @Override
    public void onBackPressed() {
        if (recyclerView.getVisibility()==View.VISIBLE){
            recyclerView.setVisibility(View.GONE);
            ll_main.setVisibility(View.VISIBLE);
        } else {
           Intent intent = new Intent(ChatHistoryActivity.this,MainActivity.class);
           startActivity(intent);
           finish();
        }
    }
}
