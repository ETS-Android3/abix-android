package com.topzi.chat.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topzi.chat.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.topzi.chat.external.RandomString;
import com.topzi.chat.helper.NetworkUtil;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.helper.StorageManager;
import com.topzi.chat.model.ChannelMessage;
import com.topzi.chat.model.ChannelResult;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.GroupMessage;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.model.SearchData;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.topzi.chat.helper.NetworkUtil.NOT_CONNECT;
import static com.topzi.chat.activity.ForwardActivity.RecyclerViewAdapter.VIEW_TYPE_CHANNELS;
import static com.topzi.chat.activity.ForwardActivity.RecyclerViewAdapter.VIEW_TYPE_CHANNEL_HEADER;
import static com.topzi.chat.activity.ForwardActivity.RecyclerViewAdapter.VIEW_TYPE_CHATS;
import static com.topzi.chat.activity.ForwardActivity.RecyclerViewAdapter.VIEW_TYPE_CHATS_HEADER;
import static com.topzi.chat.activity.ForwardActivity.RecyclerViewAdapter.VIEW_TYPE_CONTACTS;
import static com.topzi.chat.activity.ForwardActivity.RecyclerViewAdapter.VIEW_TYPE_CONTACTS_HEADER;
import static com.topzi.chat.activity.ForwardActivity.RecyclerViewAdapter.VIEW_TYPE_GROUPS;
import static com.topzi.chat.activity.ForwardActivity.RecyclerViewAdapter.VIEW_TYPE_GROUP_HEADER;
import static com.topzi.chat.utils.Constants.TAG_GROUP;

/**
 * Created on 9/8/18.
 */

public class ForwardActivity extends BaseActivity implements View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    TextView title;
    ImageView backbtn, searchbtn, optionbtn, cancelbtn;
    RecyclerView recyclerView;
    EditText searchView;
    RelativeLayout searchLay;
    RelativeLayout mainLay;
    LinearLayout buttonLayout, btnNext;
    int chatCount = 0, groupCount = 0, channelCount = 0;
    String from = "";
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    List<SearchData> filteredList;
    List<SearchData> searchList = new ArrayList<>();
    List<SearchData> chatList = new ArrayList<>();
    List<SearchData> groupList = new ArrayList<>();
    List<SearchData> channelList = new ArrayList<>();
    List<SearchData> selectedList = new ArrayList<>();
    List<SearchData> contactsList = new ArrayList<>();
    List<SearchData> sdataList = new ArrayList<>();
    StorageManager storageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forward_activity);

        title = findViewById(R.id.title);
        backbtn = findViewById(R.id.backbtn);
        searchbtn = findViewById(R.id.searchbtn);
        optionbtn = findViewById(R.id.optionbtn);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        buttonLayout = findViewById(R.id.buttonLayout);
        cancelbtn = findViewById(R.id.cancelbtn);
        searchLay = findViewById(R.id.searchLay);
        mainLay = findViewById(R.id.mainLay);
        btnNext = findViewById(R.id.btnNext);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        searchbtn.setVisibility(View.VISIBLE);
        optionbtn.setVisibility(View.GONE);

        searchbtn.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        cancelbtn.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        storageManager = StorageManager.getInstance(this);
        title.setText(getString(R.string.forward_to));
        backbtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primarytext));

        from = getIntent().getExtras().getString("from");
        switch (from) {
            case "chat": {
                ArrayList<MessagesData> messageList = (ArrayList<MessagesData>) getIntent().getSerializableExtra("data");
                for (int i = 0; i < messageList.size(); i++) {
                    MessagesData mdata = messageList.get(i);
                    SearchData sData = new SearchData();
                    sData.user_id = mdata.user_id;
                    sData.user_name = mdata.user_name;
                    sData.chat_id = mdata.chat_id;
                    sData.message_id = mdata.message_id;
                    sData.message_type = mdata.message_type;
                    sData.message = mdata.message;
                    sData.attachment = mdata.attachment;
                    sData.lat = mdata.lat;
                    sData.lon = mdata.lon;
                    sData.contact_name = mdata.contact_name;
                    sData.contact_phone_no = mdata.contact_phone_no;
                    sData.contact_country_code = mdata.contact_country_code;
                    sData.chatTime = mdata.chat_time;
                    sData.receiver_id = mdata.receiver_id;
                    sData.sender_id = mdata.sender_id;
                    sData.delivery_status = mdata.delivery_status;
                    sData.thumbnail = mdata.thumbnail;
                    sData.progress = mdata.progress;
                    sdataList.add(sData);
                }
                break;
            }
            case "group": {
                ArrayList<GroupMessage> messageList = (ArrayList<GroupMessage>) getIntent().getSerializableExtra("data");
                for (int i = 0; i < messageList.size(); i++) {
                    GroupMessage mdata = messageList.get(i);
                    SearchData sData = new SearchData();
                    sData.groupId = mdata.groupId;
                    sData.groupName = mdata.groupName;
                    sData.groupImage = mdata.groupImage;
                    sData.message_id = mdata.messageId;
                    sData.message_type = mdata.messageType;
                    sData.message = mdata.message;
                    sData.groupAdminId = mdata.groupAdminId;
                    sData.memberId = mdata.memberId;
                    sData.memberName = mdata.memberName;
                    sData.memberNo = mdata.memberNo;
                    sData.attachment = mdata.attachment;
                    sData.chatTime = mdata.chatTime;
                    sData.contact_name = mdata.contactName;
                    sData.contact_phone_no = mdata.contactPhoneNo;
                    sData.contact_country_code = mdata.contactCountryCode;
                    sData.lat = mdata.lat;
                    sData.lon = mdata.lon;
                    sData.delivery_status = mdata.deliveryStatus;
                    sData.thumbnail = mdata.thumbnail;
                    sData.progress = mdata.progress;
                    sdataList.add(sData);
                }
                break;
            }
//            case "channel": {
//                ChannelMessage mdata = (ChannelMessage) getIntent().getSerializableExtra("data");
//                sdataList.channelId = mdata.channelId;
//                sdataList.channelName = mdata.channelName;
//                sdataList.channelAdminId = mdata.channelAdminId;
//                sdataList.message_id = mdata.messageId;
//                sdataList.message_type = mdata.messageType;
//                sdataList.message = mdata.message;
//                sdataList.attachment = mdata.attachment;
//                sdataList.lat = mdata.lat;
//                sdataList.lon = mdata.lon;
//                sdataList.contact_name = mdata.contactName;
//                sdataList.contact_phone_no = mdata.contactPhoneNo;
//                sdataList.contact_country_code = mdata.contactCountryCode;
//                sdataList.chatTime = mdata.chatTime;
//                sdataList.delivery_status = mdata.deliveryStatus;
//                sdataList.thumbnail = mdata.thumbnail;
//                sdataList.progress = mdata.progress;
//                break;
//            }
        }

        SearchData data = new SearchData();

        List<SearchData> tempChat = new ArrayList<>();
        for (HashMap<String, String> hashMap : dbhelper.getAllRecentsMessages(this)) {
            if (!hashMap.get(Constants.TAG_USER_ID).equals(GetSet.getUserId())) {
                data = new SearchData();
                data.viewType = VIEW_TYPE_CHATS;
                data.user_id = hashMap.get(Constants.TAG_USER_ID);
                data.user_name = hashMap.get(Constants.TAG_USER_NAME);
                data.user_image = hashMap.get(Constants.TAG_USER_IMAGE);
                data.phone_no = hashMap.get(Constants.TAG_PHONE_NUMBER);
                data.blockedbyme = hashMap.get(Constants.TAG_BLOCKED_BYME);
                data.blockedme = hashMap.get(Constants.TAG_BLOCKED_ME);
                tempChat.add(data);
            }
        }
        if (tempChat.size() > 0) {
            data = new SearchData();
            data.viewType = VIEW_TYPE_CHATS_HEADER;
            tempChat.add(0, data);/*First item - Contact Header*/
        }
        searchList.addAll(tempChat);

        List<SearchData> tempGroup = new ArrayList<>();
        for (HashMap<String, String> hashMap : dbhelper.getGroupRecentMessages(this)) {
            if (dbhelper.isMemberExist(GetSet.getUserId(), hashMap.get(Constants.TAG_GROUP_ID))) {
                data = new SearchData();
                data.viewType = VIEW_TYPE_GROUPS;
                data.groupId = hashMap.get(Constants.TAG_GROUP_ID);
                data.groupName = hashMap.get(Constants.TAG_GROUP_NAME);
                data.groupImage = hashMap.get(Constants.TAG_GROUP_IMAGE);
                tempGroup.add(data);
            }
        }
        if (tempGroup.size() > 0) {
            data = new SearchData();
            data.viewType = VIEW_TYPE_GROUP_HEADER;
            tempGroup.add(0, data);/*First item - Group Header*/
        }
        searchList.addAll(tempGroup);

        List<SearchData> tempChannel = new ArrayList<>();
        for (ChannelResult.Result result : dbhelper.getMyChannels(GetSet.getUserId())) {
//            if (!id.equals(result.channelId)) {
            data = new SearchData();
            data.viewType = VIEW_TYPE_CHANNELS;
            data.channelId = result.channelId;
            data.channelName = result.channelName;
            data.channelImage = result.channelImage;
            tempChannel.add(data);
//            }
        }
        if (tempChannel.size() > 0) {
            data = new SearchData();
            data.viewType = VIEW_TYPE_CHANNEL_HEADER;
            tempChannel.add(0, data);/*First item - Group Header*/
        }
        searchList.addAll(tempChannel);

        List<SearchData> tempContacts = new ArrayList<>();
        for (ContactsData.Result result : dbhelper.getAllContacts(this)) {
            if (!result.user_id.equals(GetSet.getUserId()) && !isUserChatAlready(result.user_id, tempChat)) {
                data = new SearchData();
                data.viewType = VIEW_TYPE_CONTACTS;
                data.user_id = result.user_id;
                data.user_name = result.user_name;
                data.user_image = result.user_image;
                data.phone_no = result.phone_no;
                data.blockedme = result.blockedme;
                data.blockedbyme = result.blockedbyme;
                tempContacts.add(data);
            }
        }
        if (tempContacts.size() > 0) {
            data = new SearchData();
            data.viewType = VIEW_TYPE_CONTACTS_HEADER;
            tempContacts.add(0, data);/*First item - Contact Header*/
        }
        searchList.addAll(tempContacts);

        filteredList = new ArrayList<>();
        filteredList.addAll(searchList);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        recyclerViewAdapter = new RecyclerViewAdapter(this);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.notifyDataSetChanged();

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    cancelbtn.setVisibility(View.VISIBLE);
                } else {
                    cancelbtn.setVisibility(View.GONE);
                }
                recyclerViewAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private boolean isUserChatAlready(String user_id, List<SearchData> tempChat) {
        for (SearchData searchData : tempChat) {
            if (user_id.equals(searchData.user_id)) {
                return true;
            }
        }
        return false;
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter implements Filterable {

        public static final int VIEW_TYPE_CHATS_HEADER = 1;
        public static final int VIEW_TYPE_CHATS = 2;
        public static final int VIEW_TYPE_GROUP_HEADER = 3;
        public static final int VIEW_TYPE_GROUPS = 4;
        public static final int VIEW_TYPE_CHANNEL_HEADER = 5;
        public static final int VIEW_TYPE_CHANNELS = 6;
        public static final int VIEW_TYPE_CONTACTS_HEADER = 7;
        public static final int VIEW_TYPE_CONTACTS = 8;

        Context context;
        private RecyclerViewAdapter.SearchFilter mFilter;

        public RecyclerViewAdapter(Context context) {
            this.context = context;
            mFilter = new RecyclerViewAdapter.SearchFilter(RecyclerViewAdapter.this);
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }

        public class SearchFilter extends Filter {
            private RecyclerViewAdapter mAdapter;

            private SearchFilter(RecyclerViewAdapter mAdapter) {
                super();
                this.mAdapter = mAdapter;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                filteredList.clear();
                chatList.clear();
                channelList.clear();
                groupList.clear();
                contactsList.clear();
                final FilterResults results = new FilterResults();
                if (constraint.length() == 0) {
                    filteredList.addAll(searchList);
                } else {
                    final String filterPattern = constraint.toString().toLowerCase().trim();

                    for (SearchData data : searchList) {
                        if (data.viewType == VIEW_TYPE_CHATS_HEADER) {
                            chatList.add(data);
                        } else if (data.viewType == VIEW_TYPE_CHATS) {
                            if (data.user_name.toLowerCase().startsWith(filterPattern)) {
                                chatList.add(data);
                            }
                        } else if (data.viewType == VIEW_TYPE_GROUP_HEADER) {
                            groupList.add(data);
                        } else if (data.viewType == VIEW_TYPE_GROUPS) {
                            if (data.groupName.toLowerCase().startsWith(filterPattern)) {
                                groupList.add(data);
                            }
                        } else if (data.viewType == VIEW_TYPE_CHANNEL_HEADER) {
                            channelList.add(data);
                        } else if (data.viewType == VIEW_TYPE_CHANNELS) {
                            if (data.channelName.toLowerCase().startsWith(filterPattern)) {
                                channelList.add(data);
                            }
                        } else if (data.viewType == VIEW_TYPE_CONTACTS_HEADER) {
                            contactsList.add(data);
                        } else if (data.viewType == VIEW_TYPE_CONTACTS) {
                            if (data.user_name.toLowerCase().startsWith(filterPattern)) {
                                contactsList.add(data);
                            }
                        }
                    }

                    if (chatList.size() > 1) {
                        filteredList.addAll(chatList);
                    }
                    if (groupList.size() > 1) {
                        filteredList.addAll(groupList);
                    }
                    if (channelList.size() > 1) {
                        filteredList.addAll(channelList);
                    }
                    if (contactsList.size() > 1) {
                        filteredList.addAll(contactsList);
                    }
                }

                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                this.mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = null;

            if (viewType == VIEW_TYPE_CHATS_HEADER) {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_search_header, parent, false);
                return new RecyclerViewAdapter.HeaderViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_CHATS) {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_blocked_contacts, parent, false);
                return new RecyclerViewAdapter.MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_GROUP_HEADER) {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_search_header, parent, false);
                return new RecyclerViewAdapter.HeaderViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_GROUPS) {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_blocked_contacts, parent, false);
                return new RecyclerViewAdapter.MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_CHANNEL_HEADER) {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_search_header, parent, false);
                return new RecyclerViewAdapter.HeaderViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_CHANNELS) {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_blocked_contacts, parent, false);
                return new RecyclerViewAdapter.MyViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_CONTACTS_HEADER) {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_search_header, parent, false);
                return new RecyclerViewAdapter.HeaderViewHolder(itemView);
            } else if (viewType == VIEW_TYPE_CONTACTS) {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_blocked_contacts, parent, false);
                return new RecyclerViewAdapter.MyViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            if (getItemViewType(position) == VIEW_TYPE_CHATS_HEADER) {
                ((HeaderViewHolder) holder).txtHeader.setText(getString(R.string.chat));
            } else if (getItemViewType(position) == VIEW_TYPE_GROUP_HEADER) {
                ((HeaderViewHolder) holder).txtHeader.setText(getString(R.string.group));
            } else if (getItemViewType(position) == VIEW_TYPE_CHANNEL_HEADER) {
                ((HeaderViewHolder) holder).txtHeader.setText(getString(R.string.channels));
            } else if (getItemViewType(position) == VIEW_TYPE_CONTACTS_HEADER) {
                ((HeaderViewHolder) holder).txtHeader.setText(getString(R.string.contacts));
            } else {
                final SearchData data = filteredList.get(position);

                if (selectedList.contains(filteredList.get(position))) {
                    ((MyViewHolder) holder).btnSelect.setChecked(true);
                } else {
                    ((MyViewHolder) holder).btnSelect.setChecked(false);
                }

                if (getItemViewType(position) == VIEW_TYPE_CHATS || getItemViewType(position) == VIEW_TYPE_CONTACTS) {
                    ((MyViewHolder) holder).name.setText(data.user_name);
                    if (data.user_id != null) {
                        ContactsData.Result result = dbhelper.getContactDetail(data.user_id);
                        DialogActivity.setProfileImage(result, ((MyViewHolder) holder).profileimage, context);
                    } else {
                        Glide.with(context).load(R.drawable.change_camera)
                                .apply(new RequestOptions().placeholder(R.drawable.change_camera).error(R.drawable.temp))
                                .into(((MyViewHolder) holder).profileimage);
                    }
                } else if ((getItemViewType(position) == VIEW_TYPE_GROUPS)) {
                    ((MyViewHolder) holder).name.setText(data.groupName);
                    Glide.with(context).load(Constants.GROUP_IMG_PATH + data.user_image)
                            .apply(new RequestOptions().placeholder(R.drawable.create_group).error(R.drawable.create_group))
                            .into(((MyViewHolder) holder).profileimage);

                } else if (getItemViewType(position) == VIEW_TYPE_CHANNELS) {
                    ((MyViewHolder) holder).name.setText("" + data.channelName);
                    Glide.with(context).load(Constants.CHANNEL_IMG_PATH + data.channelImage)
                            .apply(new RequestOptions().placeholder(R.drawable.temp).error(R.drawable.temp))
                            .into(((MyViewHolder) holder).profileimage);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (filteredList.get(position).viewType == VIEW_TYPE_CHATS_HEADER) {
                return VIEW_TYPE_CHATS_HEADER;
            } else if (filteredList.get(position).viewType == VIEW_TYPE_CHATS) {
                return VIEW_TYPE_CHATS;
            } else if (filteredList.get(position).viewType == VIEW_TYPE_GROUP_HEADER) {
                return VIEW_TYPE_GROUP_HEADER;
            } else if (filteredList.get(position).viewType == VIEW_TYPE_GROUPS) {
                return VIEW_TYPE_GROUPS;
            } else if (filteredList.get(position).viewType == VIEW_TYPE_CHANNEL_HEADER) {
                return VIEW_TYPE_CHANNEL_HEADER;
            } else if (filteredList.get(position).viewType == VIEW_TYPE_CHANNELS) {
                return VIEW_TYPE_CHANNELS;
            } else if (filteredList.get(position).viewType == VIEW_TYPE_CONTACTS_HEADER) {
                return VIEW_TYPE_CONTACTS_HEADER;
            } else if (filteredList.get(position).viewType == VIEW_TYPE_CONTACTS) {
                return VIEW_TYPE_CONTACTS;
            }
            return 0;
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            LinearLayout parentlay;
            TextView name;
            ImageView profileimage;
            View profileview;
            AppCompatRadioButton btnSelect;

            public MyViewHolder(View view) {
                super(view);

                parentlay = view.findViewById(R.id.parentlay);
                profileimage = view.findViewById(R.id.profileimage);
                name = view.findViewById(R.id.txtName);
                profileview = view.findViewById(R.id.profileview);
                btnSelect = view.findViewById(R.id.btnSelect);

                btnSelect.setVisibility(View.VISIBLE);
                parentlay.setOnClickListener(this);
                btnSelect.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.parentlay:
                    case R.id.btnSelect:
                        if (filteredList.get(getAdapterPosition()).viewType == VIEW_TYPE_CHATS) {
                            ContactsData.Result result = dbhelper.getContactDetail(filteredList.get(getAdapterPosition()).user_id);
                            if (result.blockedbyme.equals("block")) {
                                btnSelect.setChecked(false);
                                blockChatConfirmDialog(result.user_id);
                            } else {
                                if (!selectedList.contains(filteredList.get(getAdapterPosition()))) {
                                    selectedList.add(filteredList.get(getAdapterPosition()));
                                    btnSelect.setChecked(true);
                                } else {
                                    btnSelect.setChecked(false);
                                    selectedList.remove(filteredList.get(getAdapterPosition()));
                                }
                                notifyDataSetChanged();
                            }
                        } else {
                            if (!selectedList.contains(filteredList.get(getAdapterPosition()))) {
                                selectedList.add(filteredList.get(getAdapterPosition()));
                                btnSelect.setChecked(true);
                            } else {
                                btnSelect.setChecked(false);
                                selectedList.remove(filteredList.get(getAdapterPosition()));
                            }
                            notifyDataSetChanged();
                        }
                        break;
                }
            }
        }

        public class HeaderViewHolder extends RecyclerView.ViewHolder {

            TextView txtHeader;

            public HeaderViewHolder(View view) {
                super(view);
                txtHeader = view.findViewById(R.id.txtHeader);
            }
        }
    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backbtn:
                if (searchLay.getVisibility() == View.VISIBLE) {
                    searchView.setText("");
                    searchLay.setVisibility(View.GONE);
                    title.setVisibility(View.VISIBLE);
                    buttonLayout.setVisibility(View.VISIBLE);
                    ApplicationClass.hideSoftKeyboard(this, searchView);
                } else {
                    finish();
                }
                break;
            case R.id.searchbtn:
                title.setVisibility(View.GONE);
                searchLay.setVisibility(View.VISIBLE);
                buttonLayout.setVisibility(View.GONE);
                ApplicationClass.showKeyboard(this, searchView);
                break;
            case R.id.cancelbtn:
                searchView.setText("");
                break;
            case R.id.btnNext:
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    try {
                        Toast.makeText(this, getString(R.string.sending_message), Toast.LENGTH_LONG).show();
                        for (SearchData searchData : selectedList) {
                            if (searchData.viewType == VIEW_TYPE_CHATS || searchData.viewType == VIEW_TYPE_CONTACTS) {
                                for (int i = 0; i < sdataList.size(); i++) {
                                    SearchData sData = sdataList.get(i);
                                    String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                                    String chatId = GetSet.getUserId() + searchData.user_id;
                                    RandomString randomString = new RandomString(10);
                                    String messageId = GetSet.getUserId() + randomString.nextString();

                                    if (!searchData.blockedme.equals("block")) {
//                                    JSONObject jobj = new JSONObject();
                                        JSONObject message = new JSONObject();
                                        message.put(Constants.TAG_USER_ID, GetSet.getUserId());
                                        message.put(Constants.TAG_USER_NAME, GetSet.getUserName());
                                        message.put(Constants.TAG_MESSAGE_TYPE, sData.message_type);
                                        message.put(Constants.TAG_MESSAGE, sData.message);
                                        message.put(Constants.TAG_CHAT_TIME, unixStamp);
                                        message.put(Constants.TAG_CHAT_ID, chatId);
                                        message.put(Constants.TAG_CONTACT_NAME, sData.contact_name);
                                        message.put(Constants.TAG_CONTACT_PHONE_NO, sData.contact_phone_no);
                                        message.put(Constants.TAG_CONTACT_COUNTRY_CODE, sData.contact_country_code);
                                        message.put(Constants.TAG_MESSAGE_ID, messageId);
                                        message.put(Constants.TAG_FRIENDID, searchData.user_id);
                                        message.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                                        message.put(Constants.TAG_CHAT_TYPE, Constants.TAG_SINGLE);
                                        message.put(Constants.TAG_ATTACHMENT, sData.attachment);
                                        message.put(Constants.TAG_THUMBNAIL, sData.thumbnail);
                                        message.put(Constants.TAG_LAT, sData.lat);
                                        message.put(Constants.TAG_LON, sData.lon);
//                                    jobj.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
//                                    jobj.put(Constants.TAG_RECEIVER_ID, searchData.user_id);
//                                    jobj.put("message_data", message);
                                        Log.v("startchat", "startchat=" + message);
                                        socketConnection.startChat(message);
                                    }

                                    dbhelper.addMessageDatas(chatId, messageId, GetSet.getUserId(), GetSet.getUserName(), sData.message_type,
                                            sData.message, sData.attachment, sData.lat, sData.lon, sData.contact_name, sData.contact_phone_no,
                                            sData.contact_country_code, unixStamp, searchData.user_id, GetSet.getUserId(), "", sData.thumbnail, sData.reply_to, sData.groupId);
                                    dbhelper.updateMessageData(messageId, Constants.TAG_PROGRESS, "completed");
                                    dbhelper.addRecentMessages(chatId, searchData.user_id, messageId, unixStamp, "0");

                                    //                            if (id.equals(searchData.user_id)) {
                                    if (SocketConnection.chatCallbackListener != null) {
                                        SocketConnection.chatCallbackListener.onReceiveChat(dbhelper.getSingleMessage(messageId));
                                    }
                                    if (SocketConnection.recentChatReceivedListener != null) {
                                        SocketConnection.recentChatReceivedListener.onRecentChatReceived();
                                    }

                                    if (sData.message_type.equals("image") && !storageManager.checkifImageExists("sent", sData.attachment)) {
                                        storageManager.saveImageToSentPath(storageManager.getImage("receive", sData.attachment).getAbsolutePath(), sData.attachment);
                                    } else if ((sData.message_type.equals("video") || sData.message_type.equals("file") || sData.message_type.equals("audio")) && !storageManager.checkifImageExists("sent", sData.attachment)) {
                                        storageManager.moveFilesToSentPath(ForwardActivity.this, sData.message_type, storageManager.getFile(sData.attachment, sData.message_type, "receive").getAbsolutePath(), sData.attachment);
                                    }
                                }
                                //                            }

                            } else if (searchData.viewType == VIEW_TYPE_GROUPS) {
                                for (int i = 0; i < sdataList.size(); i++) {
                                    SearchData sData = sdataList.get(i);
                                    String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
                                    RandomString randomString = new RandomString(10);
                                    String messageId = searchData.groupId + randomString.nextString();

                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put(Constants.TAG_GROUP_ID, searchData.groupId);
                                    jsonObject.put(Constants.TAG_GROUP_NAME, searchData.groupName);
                                    jsonObject.put(Constants.TAG_CHAT_TYPE, TAG_GROUP);
                                    jsonObject.put(Constants.TAG_MEMBER_ID, GetSet.getUserId());
                                    jsonObject.put(Constants.TAG_MEMBER_NAME, GetSet.getUserName());
                                    jsonObject.put(Constants.TAG_MEMBER_NO, GetSet.getphonenumber());
                                    jsonObject.put(Constants.TAG_MESSAGE_ID, messageId);
                                    jsonObject.put(Constants.TAG_MESSAGE_TYPE, sData.message_type);
                                    jsonObject.put(Constants.TAG_MESSAGE, sData.message);
                                    jsonObject.put(Constants.TAG_LAT, sData.lat);
                                    jsonObject.put(Constants.TAG_LON, sData.lon);
                                    jsonObject.put(Constants.TAG_CONTACT_NAME, sData.contact_name);
                                    jsonObject.put(Constants.TAG_CONTACT_PHONE_NO, sData.contact_phone_no);
                                    jsonObject.put(Constants.TAG_CONTACT_COUNTRY_CODE, sData.contact_country_code);
                                    jsonObject.put(Constants.TAG_ATTACHMENT, sData.attachment);
                                    jsonObject.put(Constants.TAG_THUMBNAIL, sData.thumbnail);
                                    jsonObject.put(Constants.TAG_CHAT_TIME, unixStamp);
                                    socketConnection.startGroupChat(jsonObject);

                                    dbhelper.addGroupMessages(messageId, searchData.groupId, GetSet.getUserId(), "", sData.message_type,
                                            sData.message, sData.attachment, sData.lat, sData.lon, sData.contact_name, sData.contact_phone_no, sData.contact_country_code,
                                            unixStamp, sData.thumbnail, "read", sData.reply_to);
                                    dbhelper.updateGroupMessageData(messageId, Constants.TAG_PROGRESS, "completed");
                                    dbhelper.addGroupRecentMsgs(searchData.groupId, messageId, GetSet.getUserId(), unixStamp, "0");

                                    //                            if (id.equals(searchData.groupId)) {
                                    if (SocketConnection.groupChatCallbackListener != null) {
                                        SocketConnection.groupChatCallbackListener.onGroupChatReceive(dbhelper.getSingleGroupMessage(searchData.groupId, messageId));
                                    }
                                    if (SocketConnection.groupRecentReceivedListener != null) {
                                        SocketConnection.groupRecentReceivedListener.onGroupRecentReceived();
                                    }

                                    if (sData.message_type.equals("image") && !storageManager.checkifImageExists("sent", sData.attachment)) {
                                        storageManager.saveImageToSentPath(storageManager.getImage("receive", sData.attachment).getAbsolutePath(), sData.attachment);
                                    } else if ((sData.message_type.equals("video") || sData.message_type.equals("file") || sData.message_type.equals("audio")) && !storageManager.checkifImageExists("sent", sData.attachment)) {
                                        storageManager.moveFilesToSentPath(ForwardActivity.this, sData.message_type, storageManager.getFile(sData.attachment, sData.message_type, "receive").getAbsolutePath(), sData.attachment);
                                    }
                                }
                            }
//                            else if (searchData.viewType == VIEW_TYPE_CHANNELS) {
//                                String unixStamp = String.valueOf(System.currentTimeMillis() / 1000L);
//                                RandomString randomString = new RandomString(10);
//                                String messageId = searchData.channelId + randomString.nextString();
//
//                                JSONObject jsonObject = new JSONObject();
//                                jsonObject.put(Constants.TAG_CHANNEL_ID, searchData.channelId);
//                                jsonObject.put(Constants.TAG_CHANNEL_NAME, searchData.channelName);
//                                jsonObject.put(Constants.TAG_ADMIN_ID, GetSet.getUserId());
//                                jsonObject.put(Constants.TAG_CHAT_TYPE, Constants.TAG_CHANNEL);
//                                jsonObject.put(Constants.TAG_MESSAGE_ID, messageId);
//                                jsonObject.put(Constants.TAG_MESSAGE_TYPE, sdataList.message_type);
//                                jsonObject.put(Constants.TAG_MESSAGE, sdataList.message);
//                                jsonObject.put(Constants.TAG_LAT, sdataList.lat);
//                                jsonObject.put(Constants.TAG_LON, sdataList.lon);
//                                jsonObject.put(Constants.TAG_CONTACT_NAME, sdataList.contact_name);
//                                jsonObject.put(Constants.TAG_CONTACT_PHONE_NO, sdataList.contact_phone_no);
//                                jsonObject.put(Constants.TAG_CONTACT_COUNTRY_CODE, sdataList.contact_country_code);
//                                jsonObject.put(Constants.TAG_ATTACHMENT, sdataList.attachment);
//                                jsonObject.put(Constants.TAG_THUMBNAIL, sdataList.thumbnail);
//                                jsonObject.put(Constants.TAG_CHAT_TIME, unixStamp);
//                                socketConnection.startChannelChat(jsonObject);
//
//                                dbhelper.addChannelMessages(searchData.channelId, Constants.TAG_CHANNEL, messageId, sdataList.message_type,
//                                        sdataList.message, sdataList.attachment, sdataList.lat, sdataList.lon, sdataList.contact_name, sdataList.contact_phone_no,
//                                        sdataList.contact_country_code, unixStamp, sdataList.thumbnail, "read");
//                                dbhelper.updateChannelMessageData(messageId, Constants.TAG_PROGRESS, "completed");
//                                dbhelper.addChannelRecentMsgs(searchData.channelId, messageId, unixStamp, "0");
//
//                                //                            if (id.equals(searchData.channelId)) {
//                                if (SocketConnection.channelChatCallbackListener != null) {
//                                    SocketConnection.channelChatCallbackListener.onChannelChatReceive(dbhelper.getSingleChannelMessage(searchData.channelId, messageId));
//                                }
//                                if (SocketConnection.channelRecentReceivedListener != null) {
//                                    SocketConnection.channelRecentReceivedListener.onChannelRecentReceived();
//                                }
//                                //                            }
//                            }

//                            if (sdataList.message_type.equals("image") && !storageManager.checkifImageExists("sent", sdataList.attachment)) {
//                                storageManager.saveImageToSentPath(storageManager.getImage("receive", sdataList.attachment).getAbsolutePath(), sdataList.attachment);
//                            } else if ((sdataList.message_type.equals("video") || sdataList.message_type.equals("file") || sdataList.message_type.equals("audio")) && !storageManager.checkifImageExists("sent", sdataList.attachment)) {
//                                storageManager.moveFilesToSentPath(ForwardActivity.this, sdataList.message_type, storageManager.getFile(sdataList.attachment, sdataList.message_type, "receive").getAbsolutePath(), sdataList.attachment);
//                            }
                        }
                        switch (from) {
                            case "chat": {
                                Intent i = new Intent(ForwardActivity.this, ChatActivity.class);
                                setResult(RESULT_OK, i);
                                finish();
                                break;
                            }
                            case "group": {
                                Intent i = new Intent(ForwardActivity.this, GroupChatActivity.class);
                                setResult(RESULT_OK, i);
                                finish();
                                break;
                            }
                            case "channel": {
                                Intent i = new Intent(ForwardActivity.this, ChannelChatActivity.class);
                                setResult(RESULT_OK, i);
                                finish();
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void blockChatConfirmDialog(String userId) {
        final Dialog dialog = new Dialog(ForwardActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        TextView title = dialog.findViewById(R.id.title);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);

        yes.setText(getString(R.string.unblock));
        no.setText(getString(R.string.cancel));
        title.setText(R.string.unblock_message);

        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
                    jsonObject.put(Constants.TAG_RECEIVER_ID, userId);
                    jsonObject.put(Constants.TAG_TYPE, "unblock");
                    Log.v(TAG, "block=" + jsonObject);
                    socketConnection.block(jsonObject);
                    dbhelper.updateBlockStatus(userId, Constants.TAG_BLOCKED_BYME, "unblock");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    private String isNetworkConnected() {
        return NetworkUtil.getConnectivityStatusString(this);
    }

    private void networkSnack() {
        Snackbar snackbar = Snackbar
                .make(mainLay, getString(R.string.network_failure), Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

}
