package com.topzi.chat.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.quickblox.chat.QBChatService;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.topzi.chat.R;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.QbUsersDbManager;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.service.CallService;
import com.topzi.chat.service.LoginService;
import com.topzi.chat.utils.CollectionsUtils;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;
import com.topzi.chat.utils.PushNotificationSender;
import com.topzi.chat.utils.SharedPrefsHelper;
import com.topzi.chat.utils.ToastUtils;
import com.topzi.chat.utils.UiUtils;
import com.topzi.chat.utils.WebRtcSessionManager;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;

public class GroupCallActivity extends BaseActivity implements View.OnClickListener {

    RecyclerView rvSelected, rvUserList;
    LinearLayout buttonLayout;
    ImageView audioCallBtn,videoCallBtn,backbtn;
    TextView title;
    List<ContactsData.Result> contactList = new ArrayList<>();
    List<ContactsData.Result> filterList = new ArrayList<>();
    List<ContactsData.Result> selectedUser = new ArrayList<>();

    List<QBUser> selectedOppList = new ArrayList<>();

    DatabaseHandler dbhelper;
    protected QbUsersDbManager dbManager;
    private UsersAdapter usersAdapter;
    private SelectedUsersAdapter selectedUsersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_call);

        dbhelper = DatabaseHandler.getInstance(this);
        dbManager = QbUsersDbManager.getInstance(GroupCallActivity.this);

        rvSelected = findViewById(R.id.rvSelected);
        rvUserList = findViewById(R.id.rvUserList);
        buttonLayout = findViewById(R.id.buttonLayout);
        videoCallBtn = findViewById(R.id.videoCallBtn);
        audioCallBtn = findViewById(R.id.audioCallBtn);
        backbtn = findViewById(R.id.backbtn);
        title = findViewById(R.id.title);

        title.setText("Add Group Call");

        List<QBUser> currentOpponentsList = new ArrayList<>();
        contactList.addAll(dbhelper.getStoredContacts(GroupCallActivity.this));
        for (int i = 0; i < contactList.size(); i++) {
            ContactsData.Result result = contactList.get(i);
            for (int j = 0; j < dbManager.getAllUsers().size(); j++) {
                QBUser qbUser = dbManager.getAllUsers().get(j);
                if (result.user_id.equals(qbUser.getLogin())) {
                    filterList.add(result);
                    currentOpponentsList.add(qbUser);
                }
            }
        }

        currentOpponentsList.remove(sharedPrefsHelper.getQbUser());
        Log.e("LLLL_QbUser: ", dbhelper.getStoredContacts(GroupCallActivity.this).size() + "     " + dbManager.getAllUsers().size());
        if (usersAdapter == null) {
            Constants.qbUsersList.clear();
            usersAdapter = new UsersAdapter(GroupCallActivity.this, currentOpponentsList, filterList);
            rvUserList.setLayoutManager(new LinearLayoutManager(GroupCallActivity.this));
            rvUserList.setAdapter(usersAdapter);
        } else {
            usersAdapter.updateUsersList(currentOpponentsList);
        }

        Log.e("LLLL_QbUser: ", dbhelper.getStoredContacts(GroupCallActivity.this).size() + "     " + dbManager.getAllUsers().size());
        if (selectedUsersAdapter == null) {
            Constants.qbUsersList.clear();
            selectedUsersAdapter = new SelectedUsersAdapter(GroupCallActivity.this, selectedOppList, selectedUser);
            rvSelected.setLayoutManager(new LinearLayoutManager(GroupCallActivity.this,RecyclerView.HORIZONTAL,false));
            rvSelected.setAdapter(selectedUsersAdapter);
        } else {
            selectedUsersAdapter.updateUsersList(currentOpponentsList);
        }

        audioCallBtn.setOnClickListener(this);
        videoCallBtn.setOnClickListener(this);
        backbtn.setOnClickListener(this);
        startLoginService();
    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isIncomingCall = SharedPrefsHelper.getInstance().get(Constants.EXTRA_IS_INCOMING_CALL, false);
        if (isCallServiceRunning(CallService.class)) {
            Log.d("LLLL_quick: ", "CallService is running now");
            CallActivity.start(this, isIncomingCall);
        }
    }

    private boolean isCallServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.backbtn:
                onBackPressed();
                break;
            case R.id.audioCallBtn:
                if (ContextCompat.checkSelfPermission(GroupCallActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(GroupCallActivity.this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(GroupCallActivity.this, WAKE_LOCK) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(GroupCallActivity.this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GroupCallActivity.this, new String[]{CAMERA, RECORD_AUDIO, WAKE_LOCK, READ_PHONE_STATE}, 100);
                }  else {
                    if (checkIsLoggedInChat()) {
                        startCall(false);
                    }
                }
                break;
            case R.id.videoCallBtn:
                if (ContextCompat.checkSelfPermission(GroupCallActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(GroupCallActivity.this, RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(GroupCallActivity.this, WAKE_LOCK) != PackageManager.PERMISSION_GRANTED ||
                        ContextCompat.checkSelfPermission(GroupCallActivity.this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GroupCallActivity.this, new String[]{CAMERA, RECORD_AUDIO, WAKE_LOCK, READ_PHONE_STATE}, 101);
                } else {
                    if (checkIsLoggedInChat()) {
                        startCall(true);
                    }
                }
                break;
        }
    }

    public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

        private Context context;
        List<QBUser> usersList;
        List<ContactsData.Result> filterList;


        public UsersAdapter(Context context, List<QBUser> usersList, List<ContactsData.Result> filterList) {
            this.context = context;
            this.usersList = usersList;
            this.filterList = filterList;
        }

        @NonNull
        @Override
        public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new UsersAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_opponents_list, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder holder, int position) {
            QBUser user = usersList.get(position);
            ContactsData.Result result = filterList.get(position);

            Log.e("LLLLL_Name: ",user.getFullName()+"   "+result.user_name);

            holder.opponentName.setText(user.getFullName());
            if (Constants.qbUsersList.contains(user)) {
                holder.imgSelecte.setVisibility(View.VISIBLE);
//                holder.rootLayout.setBackgroundResource(R.color.background_color_selected_user_item);
                holder.opponentIcon.setBackgroundDrawable(
                        UiUtils.getColoredCircleDrawable(context.getResources().getColor(R.color.icon_background_color_selected_user)));
                holder.opponentIcon.setImageResource(R.drawable.ic_checkmark);
            } else {
                holder.imgSelecte.setVisibility(View.GONE);
//                holder.rootLayout.setBackgroundResource(R.color.background_color_normal_user_item);
                holder.opponentIcon.setBackgroundDrawable(UiUtils.getColorCircleDrawable(user.getId()));
                holder.opponentIcon.setImageResource(R.drawable.ic_person);
            }

            if (filterList.get(position).blockedme.equals("block")) {
                Glide.with(context).load(R.drawable.change_camera)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
                        .into(holder.opponentIcon);
            } else {
                DialogActivity.setProfileImage(dbhelper.getContactDetail(filterList.get(position).user_id), holder.opponentIcon, context);
            }

            holder.rootLayout.setOnClickListener(v -> toggleSelection(user, result));
        }

        @Override
        public int getItemCount() {
            return usersList.size();
        }

        public void updateUsersList(List<QBUser> usersList) {
            this.usersList = usersList;
            notifyDataSetChanged();
        }

        private void toggleSelection(QBUser qbUser, ContactsData.Result result) {

            if (Constants.qbUsersList.contains(qbUser)) {
                selectedUser.remove(result);
                Constants.qbUsersList.remove(qbUser);
                selectedOppList.remove(qbUser);
            } else {
                selectedUser.add(result);
                Constants.qbUsersList.add(qbUser);
                selectedOppList.add(qbUser);
            }

            if (selectedUser.size()>0){
                buttonLayout.setVisibility(View.VISIBLE);
            }

            selectedUsersAdapter.notifyDataSetChanged();
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView opponentIcon;
            ImageView imgSelecte;
            TextView opponentName;
            LinearLayout rootLayout;

            public ViewHolder(@NonNull View view) {
                super(view);
                opponentIcon = view.findViewById(R.id.image_opponent_icon);
                imgSelecte = view.findViewById(R.id.imgSelecte);
                opponentName = view.findViewById(R.id.opponents_name);
                rootLayout = view.findViewById(R.id.root_layout);
            }
        }

    }

    public class SelectedUsersAdapter extends RecyclerView.Adapter<SelectedUsersAdapter.ViewHolder> {

        private Context context;
        List<QBUser> usersList;
        List<ContactsData.Result> filterList;


        public SelectedUsersAdapter(Context context, List<QBUser> usersList, List<ContactsData.Result> filterList) {
            this.context = context;
            this.usersList = usersList;
            this.filterList = filterList;
        }

        @NonNull
        @Override
        public SelectedUsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SelectedUsersAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.selected_lay, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SelectedUsersAdapter.ViewHolder holder, int position) {
            QBUser user = usersList.get(position);
            ContactsData.Result result = filterList.get(position);

            Log.e("LLLLL_Name: ",user.getFullName()+"   "+result.user_name);

            if (Constants.qbUsersList.contains(user)) {
//                holder.rootLayout.setBackgroundResource(R.color.background_color_selected_user_item);
                holder.opponentIcon.setBackgroundDrawable(
                        UiUtils.getColoredCircleDrawable(context.getResources().getColor(R.color.icon_background_color_selected_user)));
                holder.opponentIcon.setImageResource(R.drawable.ic_checkmark);
            } else {
//                holder.rootLayout.setBackgroundResource(R.color.background_color_normal_user_item);
                holder.opponentIcon.setBackgroundDrawable(UiUtils.getColorCircleDrawable(user.getId()));
                holder.opponentIcon.setImageResource(R.drawable.ic_person);
            }

            if (filterList.get(position).blockedme.equals("block")) {
                Glide.with(context).load(R.drawable.change_camera)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
                        .into(holder.opponentIcon);
            } else {
                DialogActivity.setProfileImage(dbhelper.getContactDetail(filterList.get(position).user_id), holder.opponentIcon, context);
            }

            holder.rootLayout.setOnClickListener(v -> toggleSelection(user, result));
        }

        @Override
        public int getItemCount() {
            return selectedUser.size();
        }

        public void updateUsersList(List<QBUser> usersList) {
            this.usersList = usersList;
            notifyDataSetChanged();
        }

        private void toggleSelection(QBUser qbUser, ContactsData.Result result) {

            if (Constants.qbUsersList.contains(qbUser)) {
                selectedUser.remove(result);
                Constants.qbUsersList.remove(qbUser);
                selectedOppList.remove(qbUser);
            } else {
                selectedUser.add(result);
                Constants.qbUsersList.add(qbUser);
                selectedOppList.add(qbUser);
            }

            if (selectedUser.size()>0){
                buttonLayout.setVisibility(View.VISIBLE);
            }

            usersAdapter.notifyDataSetChanged();
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView opponentIcon;
            RelativeLayout rootLayout;

            public ViewHolder(@NonNull View view) {
                super(view);
                opponentIcon = view.findViewById(R.id.image_opponent_icon);
                rootLayout = view.findViewById(R.id.root_layout);
            }
        }

    }

    private boolean checkIsLoggedInChat() {
        if (!QBChatService.getInstance().isLoggedIn()) {
            startLoginService();
            ToastUtils.shortToast(R.string.dlg_relogin_wait);
            return false;
        }
        return true;
    }

    private void startLoginService() {
        if (sharedPrefsHelper.hasQbUser()) {
            QBUser qbUser = sharedPrefsHelper.getQbUser();
            LoginService.start(this, qbUser);
        }
    }

    private void startCall(boolean isVideoCall) {
        Log.d("LLLL_Quick: ", "Starting Call");

        if (Constants.qbUsersList.size() > Constants.MAX_OPPONENTS_COUNT) {
            ToastUtils.longToast(String.format(getString(R.string.error_max_opponents_count),
                    Constants.MAX_OPPONENTS_COUNT));
            return;
        }

        ArrayList<Integer> opponentsList = CollectionsUtils.getIdsSelectedOpponents(Constants.qbUsersList);
        QBRTCTypes.QBConferenceType conferenceType = isVideoCall
                ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
        Log.d("LLLL_Quick: ", "conferenceType = " + conferenceType);

        QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());
        QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);
        WebRtcSessionManager.getInstance(this).setCurrentSession(newQbRtcSession);
        PushNotificationSender.sendPushMessage(opponentsList, GetSet.getUserName());
        CallActivity.start(this, false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}