package com.topzi.chat.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.topzi.chat.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.helper.Utils;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;

/**
 * Created on 29/7/18.
 */

public class CallFragment extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    RecyclerViewAdapter callAdapter;
    RecyclerView recyclerView;
    RelativeLayout progressLay;
    LinearLayout nullLay;
    TextView nullText;
    LinearLayoutManager linearLayoutManager;
    DatabaseHandler dbhelper;
    ArrayList<HashMap<String, String>> callList = new ArrayList<>();
    SocketConnection socketConnection;
    public static CallFragment callFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        progressLay = view.findViewById(R.id.progress);
        nullLay = view.findViewById(R.id.nullLay);
        nullText = view.findViewById(R.id.nullText);
        recyclerView = view.findViewById(R.id.recyclerView);

        socketConnection = SocketConnection.getInstance(getActivity());
        dbhelper = DatabaseHandler.getInstance(getActivity());
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        callList.clear();
        callList = dbhelper.getRecentCall();
        callAdapter = new RecyclerViewAdapter(getActivity(), callList);
        recyclerView.setAdapter(callAdapter);
        callAdapter.notifyDataSetChanged();
        nullText.setText(R.string.no_calls_yet_buddy);
        if (callList.size() == 0) {
            nullLay.setVisibility(View.VISIBLE);
        } else {
            nullLay.setVisibility(View.GONE);
        }

        callFragment = this;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAdapter();
    }

    public void refreshAdapter() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (callAdapter != null) {
                    callList.clear();
                    callList.addAll(dbhelper.getRecentCall());
                    callAdapter.notifyDataSetChanged();
                }
                if (callList.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    nullLay.setVisibility(View.GONE);
                }
            }
        });
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        ArrayList<HashMap<String, String>> callList = new ArrayList<>();
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<HashMap<String, String>> callList) {
            this.callList = callList;
            this.context = context;
        }

        @Override
        public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_call, parent, false);

            return new RecyclerViewAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final RecyclerViewAdapter.MyViewHolder holder, int position) {

            final HashMap<String, String> map = callList.get(position);

            if (Utils.isProfileEnabled(dbhelper.getContactDetail(map.get(Constants.TAG_USER_ID)))) {
                Glide.with(context).load(Constants.USER_IMG_PATH + map.get(Constants.TAG_USER_IMAGE)).thumbnail(0.5f)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.person).error(R.drawable.person).override(ApplicationClass.dpToPx(context, 70)))
                        .into(holder.profileImage);
            } else {
                Glide.with(context).load(R.drawable.person).thumbnail(0.5f)
                        .transition(new DrawableTransitionOptions().crossFade())
                        .into(holder.profileImage);
            }

//            Glide.with(context).load(Constants.USER_IMG_PATH + map.get(Constants.TAG_USER_IMAGE)).thumbnail(0.5f)
//                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.person).error(R.drawable.person).override(ApplicationClass.dpToPx(context, 70)))
//                    .into(holder.profileImage);
            holder.txtName.setText(ApplicationClass.getContactName(context, map.get(Constants.TAG_PHONE_NUMBER)));
            if (map.get(Constants.TAG_CREATED_AT) != null) {
                holder.txtTime.setText(getFormattedDate(context, Long.parseLong(map.get(Constants.TAG_CREATED_AT).replace(".", ""))));
            }

            if (map.get(Constants.TAG_TYPE).equals("audio")) {
                holder.callType.setImageResource(R.drawable.call);
            } else {
                holder.callType.setImageResource(R.drawable.videocall);
            }

            if (map.get(Constants.TAG_CALL_STATUS) != null) {
                switch (map.get(Constants.TAG_CALL_STATUS)) {
                    case "incoming":
                        holder.statusIcon.setImageResource(R.drawable.incoming);
                        break;
                    case "missed":
                        holder.statusIcon.setImageResource(R.drawable.missed);
                        break;
                    case "outgoing":
                        holder.statusIcon.setImageResource(R.drawable.outgoing);
                        break;
                }
            }
        }

        @Override
        public int getItemCount() {
            return callList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            LinearLayout parentLay;
            TextView txtName, txtTime;
            CircleImageView profileImage;
            ImageView statusIcon, callType;
            View profileView;

            public MyViewHolder(View view) {
                super(view);

                parentLay = view.findViewById(R.id.parentLay);
                txtTime = view.findViewById(R.id.txtTime);
                txtName = view.findViewById(R.id.txtName);
                profileImage = view.findViewById(R.id.profileImage);
                profileView = view.findViewById(R.id.profileView);
                statusIcon = view.findViewById(R.id.statusIcon);
                callType = view.findViewById(R.id.callType);

                callType.setOnClickListener(this);
                profileImage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.callType:
                        ContactsData.Result result = dbhelper.getContactDetail(callList.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                        if (callList.get(getAdapterPosition()).get(Constants.TAG_TYPE).equals("audio")) {
                            if (!checkPermissions()) {
                                requestPermission(new String[]{CAMERA, RECORD_AUDIO, WAKE_LOCK, READ_PHONE_STATE}, 100);
                            } else if (result.blockedbyme.equals("block")) {
                                blockChatConfirmDialog(result.user_id);
                            } else {
                                callType.setOnClickListener(null);
                                Intent video = new Intent(context, CallActivity.class);
                                video.putExtra("from", "send");
                                video.putExtra("type", "audio");
                                video.putExtra("user_id", callList.get(getAdapterPosition()).get(Constants.TAG_USER_ID));
                                startActivity(video);
                            }
                        } else {
                            if (!checkPermissions()) {
                                requestPermission(new String[]{CAMERA, RECORD_AUDIO, WAKE_LOCK, READ_PHONE_STATE}, 100);
                            } else if (result.blockedbyme.equals("block")) {
                                blockChatConfirmDialog(result.user_id);
                            } else {
                                callType.setOnClickListener(null);
                                Intent video = new Intent(context, CallActivity.class);
                                video.putExtra("from", "send");
                                video.putExtra("type", "video");
                                video.putExtra("user_id", result.user_id);
                                startActivity(video);
                            }
                        }
                        break;
                    case R.id.profileImage:
                        break;

                }
            }
        }
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

    private void blockChatConfirmDialog(String userId) {
        final Dialog dialog = new Dialog(getActivity());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {

            boolean isPermissionEnabled = false;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    isPermissionEnabled = false;
                    break;
                } else {
                    isPermissionEnabled = true;
                }
            }

            if (!isPermissionEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(CAMERA) &&
                            shouldShowRequestPermissionRationale(RECORD_AUDIO) &&
                            shouldShowRequestPermissionRationale(WAKE_LOCK) &&
                            shouldShowRequestPermissionRationale(READ_PHONE_STATE)) {
                        requestPermission(new String[]{CAMERA, RECORD_AUDIO, WAKE_LOCK, READ_PHONE_STATE}, 100);
                    } else {
                        Toast.makeText(getActivity(),getString(R.string.call_permission_error),Toast.LENGTH_SHORT).show();
                    }
                }
//                ActivityCompat.requestPermissions(CallActivity.this, new String[]{CAMERA, RECORD_AUDIO, READ_PHONE_STATE, WAKE_LOCK}, 101);
            }
        }
    }

    private boolean checkPermissions() {
        int permissionCamera = ContextCompat.checkSelfPermission(getActivity(),
                CAMERA);
        int permissionAudio = ContextCompat.checkSelfPermission(getActivity(),
                RECORD_AUDIO);
        int permissionPhoneState = ContextCompat.checkSelfPermission(getActivity(),
                READ_PHONE_STATE);
        int permissionWakeLock = ContextCompat.checkSelfPermission(getActivity(),
                WAKE_LOCK);
        return permissionCamera == PackageManager.PERMISSION_GRANTED &&
                permissionAudio == PackageManager.PERMISSION_GRANTED &&
                permissionWakeLock == PackageManager.PERMISSION_GRANTED &&
                permissionPhoneState == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermission(String[] permissions, int requestCode) {
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        callFragment = null;
    }
}
