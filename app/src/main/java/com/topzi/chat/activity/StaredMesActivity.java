package com.topzi.chat.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.topzi.chat.BuildConfig;
import com.topzi.chat.R;
import com.topzi.chat.external.ProgressWheel;
import com.topzi.chat.external.RecyclerItemClickListener;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.DownloadFiles;
import com.topzi.chat.helper.FileUploadService;
import com.topzi.chat.helper.ImageDownloader;
import com.topzi.chat.helper.NetworkUtil;
import com.topzi.chat.helper.StorageManager;
import com.topzi.chat.model.MessagesData;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.topzi.chat.activity.StaredMesActivity.MessageListAdapter.VIEW_TYPE_DATE;
import static com.topzi.chat.helper.NetworkUtil.NOT_CONNECT;
import static com.topzi.chat.utils.Constants.API_VERSION;
import static com.topzi.chat.utils.Constants.BASE_URL;
import static com.topzi.chat.utils.Constants.STAR_MSG;

public class StaredMesActivity extends AppCompatActivity implements View.OnClickListener {

    private RelativeLayout mainLay,chatUserLay,forwordLay;
    private RecyclerView recyclerView;
    private ImageView   copyBtn, forwordBtn, deleteBtn, starBtn;

    String TAG = "StarChatActivity";
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    DatabaseHandler dbhelper;
    StorageManager storageManager;
    int fontSize = 20;
    List<MessagesData> messagesList = new ArrayList<>();
    ArrayList<MessagesData> selectedChatPos = new ArrayList<>();
    MediaPlayer mediaPlayer = new MediaPlayer();
    boolean chatLongPressed = false;
    LinearLayoutManager linearLayoutManager;
    MessageListAdapter messageListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stared_mes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainLay = findViewById(R.id.mainLay);
        chatUserLay = findViewById(R.id.chatUserLay);
        forwordLay = findViewById(R.id.forwordLay);
        forwordBtn = findViewById(R.id.forwordBtn);
        copyBtn = findViewById(R.id.copyBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        starBtn = findViewById(R.id.starBtn);

        recyclerView = findViewById(R.id.recyclerView);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        messageListAdapter = new MessageListAdapter(this, messagesList);
        recyclerView.setAdapter(messageListAdapter);

        DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        divider.setDrawable(getResources().getDrawable(R.drawable.emptychat_divider));
        recyclerView.addItemDecoration(divider);

        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(0);
            }
        });

        AndroidNetworking.initialize(StaredMesActivity.this);
        pref = StaredMesActivity.this.getSharedPreferences(Constants.PREFERENCE_NAME, MODE_PRIVATE);
        editor = pref.edit();

        copyBtn.setOnClickListener(this);
        forwordBtn.setOnClickListener(this);
        deleteBtn.setOnClickListener(this);
        starBtn.setOnClickListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();
        switch (pref.getInt(Constants.PREF_FONT_SIZE, Constants.FONT_SIZE_MEDIUM)){
            case Constants.FONT_SIZE_SMALL:
                fontSize = 15;
                break;
            case Constants.FONT_SIZE_MEDIUM:
                fontSize = 20;
                break;
            case Constants.FONT_SIZE_LARGE:
                fontSize = 25;
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.forwordBtn:
                if (isNetworkConnected().equals(NOT_CONNECT)) {
                    networkSnack();
                } else {
                    Intent f = new Intent(StaredMesActivity.this, ForwardActivity.class);
                    f.putExtra("from", "chat");
                    f.putExtra("data", selectedChatPos.get(0));
                    startActivityForResult(f, 222);
                }
                break;
            case R.id.copyBtn:
                if (selectedChatPos.size() > 0) {
                    String msg = selectedChatPos.get(0).message;
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Copied Message", msg);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, getString(R.string.message_copied), Toast.LENGTH_SHORT).show();
                    selectedChatPos.clear();
                    messageListAdapter.notifyDataSetChanged();
                    chatUserLay.setVisibility(View.VISIBLE);
                    forwordLay.setVisibility(View.GONE);
                    chatLongPressed = false;
                }
                break;
            case R.id.deleteBtn:
                deleteMessageConfirmDialog(selectedChatPos);
                break;
            case R.id.starBtn:
                Log.e("LLLLL_CHatID: ",selectedChatPos.get(0).getChat_id());
                Log.e("LLLLL_MessID: ",selectedChatPos.get(0).getMessage_id());
                Log.e("LLLLL_Mess: ",selectedChatPos.get(0).getMessage());
                addStarMes(selectedChatPos.get(0).getMessage_id());
                break;
        }
    }

    private void deleteMessageConfirmDialog(ArrayList<MessagesData> mData) {
        final Dialog dialog = new Dialog(this);
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
        title.setText(R.string.really_delete_msg);
        no.setVisibility(View.VISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                dbhelper.deleteMessageFromId(mData);
                for (int i = 0; i < mData.size(); i++)
                    messagesList.remove(mData.get(i));
                Toast.makeText(StaredMesActivity.this, getString(R.string.message_deleted), Toast.LENGTH_SHORT).show();
                selectedChatPos.clear();
                messageListAdapter.notifyDataSetChanged();
                chatUserLay.setVisibility(View.VISIBLE);
                forwordLay.setVisibility(View.GONE);
                chatLongPressed = false;
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

    private void addStarMes(String chatID){
        Log.e("LLLLL_ID: ",pref.getString("userId",""));
        AndroidNetworking.post(BASE_URL+API_VERSION+STAR_MSG)
                .addBodyParameter("userId",pref.getString("userId",""))
                .addBodyParameter("chatId",chatID)
                .addBodyParameter("star", String.valueOf(1))
                .setPriority(Priority.IMMEDIATE)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("LLLLL_Star: ",response.toString());
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("LLLLLL_Error_Stra: ",anError.getErrorBody());
                    }
                });
    }

    public class MessageListAdapter extends RecyclerView.Adapter {
        public static final int VIEW_TYPE_MESSAGE_SENT = 1;
        public static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
        public static final int VIEW_TYPE_IMAGE_SENT = 3;
        public static final int VIEW_TYPE_IMAGE_RECEIVED = 4;
        public static final int VIEW_TYPE_CONTACT_SENT = 5;
        public static final int VIEW_TYPE_CONTACT_RECEIVED = 6;
        public static final int VIEW_TYPE_FILE_SENT = 7;
        public static final int VIEW_TYPE_FILE_RECEIVED = 8;
        public static final int VIEW_TYPE_DATE = 9;

        private Context mContext;
        private List<MessagesData> mMessageList;

        public MessageListAdapter(Context context, List<MessagesData> messageList) {
            mContext = context;
            mMessageList = messageList;
        }

        @Override
        public int getItemCount() {
            return mMessageList.size();
        }

        // Determines the appropriate ViewType according to the sender of the message.
        @Override
        public int getItemViewType(int position) {
            MessagesData message = mMessageList.get(position);
            if (message.user_id != null && message.user_id.equals(GetSet.getUserId())) {
                switch (message.message_type) {
                    case "text":
                        return VIEW_TYPE_MESSAGE_SENT;
                    case "image":
                    case "video":
                    case "location":
                        return VIEW_TYPE_IMAGE_SENT;
                    case "contact":
                        return VIEW_TYPE_CONTACT_SENT;
                    case "date":
                        return VIEW_TYPE_DATE;
                    default:
                        return VIEW_TYPE_FILE_SENT;
                }
            } else {
                switch (message.message_type) {
                    case "text":
                        return VIEW_TYPE_MESSAGE_RECEIVED;
                    case "image":
                    case "video":
                    case "location":
                        return VIEW_TYPE_IMAGE_RECEIVED;
                    case "contact":
                        return VIEW_TYPE_CONTACT_RECEIVED;
                    case "date":
                        return VIEW_TYPE_DATE;
                    default:
                        return VIEW_TYPE_FILE_RECEIVED;
                }
            }
        }

        // Inflates the appropriate layout according to the ViewType.
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;

            if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_text_bubble_sent, parent, false);
                return new MessageListAdapter.SentMessageHolder(view);
            } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_text_bubble_receive, parent, false);
                return new MessageListAdapter.ReceivedMessageHolder(view);
            } else if (viewType == VIEW_TYPE_IMAGE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_image_bubble_sent, parent, false);
                return new MessageListAdapter.SentImageHolder(view);
            } else if (viewType == VIEW_TYPE_IMAGE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_image_bubble_receive, parent, false);
                return new MessageListAdapter.ReceivedImageHolder(view);
            } else if (viewType == VIEW_TYPE_CONTACT_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_contact_bubble_sent, parent, false);
                return new MessageListAdapter.SentContactHolder(view);
            } else if (viewType == VIEW_TYPE_CONTACT_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_contact_bubble_receive, parent, false);
                return new MessageListAdapter.ReceivedContactHolder(view);
            } else if (viewType == VIEW_TYPE_FILE_SENT) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_file_bubble_sent, parent, false);
                return new MessageListAdapter.SentFileHolder(view);
            } else if (viewType == VIEW_TYPE_FILE_RECEIVED) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_file_bubble_received, parent, false);
                return new MessageListAdapter.ReceivedFileHolder(view);
            } else if (viewType == VIEW_TYPE_DATE) {
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.chat_date_layout, parent, false);
                return new MessageListAdapter.DateHolder(view);
            }

            return null;
        }

        // Passes the message object to a ViewHolder so that the contents can be bound to UI.
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            MessagesData message = mMessageList.get(position);

            switch (holder.getItemViewType()) {
                case VIEW_TYPE_MESSAGE_SENT:
                    ((MessageListAdapter.SentMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_MESSAGE_RECEIVED:
                    ((MessageListAdapter.ReceivedMessageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_IMAGE_SENT:
                    ((MessageListAdapter.SentImageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_IMAGE_RECEIVED:
                    ((MessageListAdapter.ReceivedImageHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_FILE_SENT:
                    ((MessageListAdapter.SentFileHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_FILE_RECEIVED:
                    ((MessageListAdapter.ReceivedFileHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_CONTACT_SENT:
                    ((MessageListAdapter.SentContactHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_CONTACT_RECEIVED:
                    ((MessageListAdapter.ReceivedContactHolder) holder).bind(message);
                    break;
                case VIEW_TYPE_DATE:
                    ((MessageListAdapter.DateHolder) holder).bind(message);
                    break;
            }
        }

        private class SentMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;
            ImageView tickimage;

            SentMessageHolder(View itemView) {
                super(itemView);

                messageText = itemView.findViewById(R.id.text_message_body);
                timeText = itemView.findViewById(R.id.text_message_time);
                tickimage = itemView.findViewById(R.id.tickimage);
            }

            void bind(MessagesData message) {
                messageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                messageText.setText(message.message + Html.fromHtml(" &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
                Linkify.addLinks(messageText, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                switch (message.delivery_status) {
                    case "read":
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick);
                        break;
                    case "sent":
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick_unseen);
                        break;
                    default:
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.single_tick);
                        break;
                }

                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
            }
        }

        private class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;

            ReceivedMessageHolder(View itemView) {
                super(itemView);

                messageText = (TextView) itemView.findViewById(R.id.text_message_body);
                timeText = (TextView) itemView.findViewById(R.id.text_message_time);
            }

            void bind(MessagesData message) {
                messageText.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                messageText.setText(message.message + Html.fromHtml(
                        " &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
                Linkify.addLinks(messageText, Linkify.EMAIL_ADDRESSES | Linkify.PHONE_NUMBERS | Linkify.WEB_URLS);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time.replace(".0", ""))));

                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
            }
        }

        private class SentImageHolder extends RecyclerView.ViewHolder {
            TextView timeText;
            ImageView tickimage, uploadimage, downloadicon;
            RelativeLayout progresslay;
            ProgressWheel progressbar;

            SentImageHolder(View itemView) {
                super(itemView);

                uploadimage = itemView.findViewById(R.id.uploadimage);
                timeText = itemView.findViewById(R.id.text_message_time);
                tickimage = itemView.findViewById(R.id.tickimage);
                progresslay = itemView.findViewById(R.id.progresslay);
                progressbar = itemView.findViewById(R.id.progressbar);
                downloadicon = itemView.findViewById(R.id.downloadicon);
            }

            void bind(final MessagesData message) {
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }

                if (message.message_type.equals("image")) {
                    if (message.delivery_status.equals("read")) {
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick);
                        tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
                    } else if (message.delivery_status.equals("sent")) {
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick_unseen);
                        tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.white));
                    } else if (message.progress.equals("completed")) {
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.single_tick);
                        tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.white));
                    } else {
                        tickimage.setVisibility(View.GONE);
                    }

                    downloadicon.setImageResource(R.drawable.upload);
                    switch (message.progress) {
                        case "": {
                            progresslay.setVisibility(View.VISIBLE);
                            progressbar.setVisibility(View.VISIBLE);
                            progressbar.spin();
                            File file = storageManager.getImage("sent", getFileName(message.attachment));
                            if (file != null) {
                                Log.v(TAG, "checkChat=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                            break;
                        }
                        case "completed": {
                            progresslay.setVisibility(View.GONE);
                            progressbar.setVisibility(View.GONE);
                            progressbar.stopSpinning();
                            File file = storageManager.getImage("sent", message.attachment);
                            if (file != null) {
                                Log.v(TAG, "checkChat=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                            break;
                        }
                        case "error": {
                            progresslay.setVisibility(View.VISIBLE);
                            progressbar.setVisibility(View.VISIBLE);
                            progressbar.stopSpinning();
                            File file = storageManager.getImage("sent", getFileName(message.attachment));
                            if (file != null) {
                                Log.v(TAG, "checkChat=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                            break;
                        }
                    }

                } else if (message.message_type.equals("location")) {
                    switch (message.delivery_status) {
                        case "read":
                            tickimage.setVisibility(View.VISIBLE);
                            tickimage.setImageResource(R.drawable.double_tick);
                            tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
                            break;
                        case "sent":
                            tickimage.setVisibility(View.VISIBLE);
                            tickimage.setImageResource(R.drawable.double_tick_unseen);
                            tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.white));
                            break;
                        default:
                            tickimage.setVisibility(View.VISIBLE);
                            tickimage.setImageResource(R.drawable.single_tick);
                            tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.white));
                            break;
                    }
                    progresslay.setVisibility(View.GONE);
                    int size = ApplicationClass.dpToPx(mContext, 170);
                    String url = "http://maps.google.com/maps/api/staticmap?center=" + message.lat + "," + message.lon + "&zoom=18&size=" + size + "x" + size + "&sensor=false" + "&key=" + Constants.GOOGLE_MAPS_KEY;
                    Glide.with(mContext).load(url).thumbnail(0.5f)
                            .into(uploadimage);

                    uploadimage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = new Intent(StaredMesActivity.this, LocationActivity.class);
                            i.putExtra("from", "view");
                            i.putExtra("lat", message.lat);
                            i.putExtra("lon", message.lon);
                            startActivity(i);
                        }
                    });
                } else if (message.message_type.equals("video")) {
                    if (message.delivery_status.equals("read")) {
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick);
                        tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
                    } else if (message.delivery_status.equals("sent")) {
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick_unseen);
                        tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.white));
                    } else if (message.progress.equals("completed")) {
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.single_tick);
                        tickimage.setColorFilter(ContextCompat.getColor(mContext, R.color.white));
                    } else {
                        tickimage.setVisibility(View.GONE);
                    }

                    progresslay.setVisibility(View.VISIBLE);
                    switch (message.progress) {
                        case "": {
                            progressbar.setVisibility(View.VISIBLE);
                            progressbar.spin();
                            downloadicon.setImageResource(R.drawable.upload);
                            File file = storageManager.getImage("sent", getFileName(message.thumbnail));
                            if (file != null) {
                                Log.v(TAG, "file=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                            break;
                        }
                        case "completed": {
                            progressbar.setVisibility(View.GONE);
                            progressbar.stopSpinning();
                            downloadicon.setImageResource(R.drawable.play);
                            File file = storageManager.getImage("sent", message.thumbnail);
                            if (file != null) {
                                Log.v(TAG, "file=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                            break;
                        }
                        case "error": {
                            progressbar.setVisibility(View.VISIBLE);
                            progressbar.stopSpinning();
                            downloadicon.setImageResource(R.drawable.upload);
                            File file = storageManager.getImage("sent", getFileName(message.thumbnail));
                            if (file != null) {
                                Log.v(TAG, "file=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                            break;
                        }
                        default:

                            break;
                    }

                }
            }
        }

        private class ReceivedImageHolder extends RecyclerView.ViewHolder {
            TextView timeText;
            ImageView uploadimage, downloadicon;
            RelativeLayout progresslay, videoprogresslay;
            ProgressWheel progressbar, videoprogressbar;

            ReceivedImageHolder(View itemView) {
                super(itemView);

                uploadimage = itemView.findViewById(R.id.uploadimage);
                progresslay = itemView.findViewById(R.id.progresslay);
                timeText = itemView.findViewById(R.id.text_message_time);
                progressbar = itemView.findViewById(R.id.progressbar);
                downloadicon = itemView.findViewById(R.id.downloadicon);
                videoprogresslay = itemView.findViewById(R.id.videoprogresslay);
                videoprogressbar = itemView.findViewById(R.id.videoprogressbar);
            }

            void bind(final MessagesData message) {
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }

                switch (message.message_type) {
                    case "image":
                        videoprogresslay.setVisibility(View.GONE);
                        downloadicon.setImageResource(R.drawable.download);
                        if (storageManager.checkifImageExists("thumb", message.attachment)) {
                            progresslay.setVisibility(View.GONE);
                            File file = storageManager.getImage("thumb", message.attachment);
                            ExifInterface exif = null;
                            int orientation = 0;
                            if (file != null) {
                                Glide.with(mContext).load(file).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                        } else {
                            progresslay.setVisibility(View.VISIBLE);
                            progressbar.setVisibility(View.VISIBLE);
                            progressbar.stopSpinning();
                            Glide.with(mContext).load(Constants.CHAT_IMG_PATH + message.attachment).thumbnail(0.5f)
                                    .apply(RequestOptions.overrideOf(18, 18))
                                    .into(uploadimage);
                        }

                        timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));

                        break;
                    case "location":
                        progresslay.setVisibility(View.GONE);
                        videoprogresslay.setVisibility(View.GONE);
                        int size = ApplicationClass.dpToPx(mContext, 170);
                        String url = "http://maps.google.com/maps/api/staticmap?center=" + message.lat + "," + message.lon + "&zoom=18&size=" + size + "x" + size + "&sensor=false" + "&key=" + Constants.GOOGLE_MAPS_KEY;
                        Glide.with(mContext).load(url).thumbnail(0.5f)
                                .into(uploadimage);
                        timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                        uploadimage.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(StaredMesActivity.this, LocationActivity.class);
                                i.putExtra("from", "view");
                                i.putExtra("lat", message.lat);
                                i.putExtra("lon", message.lon);
                                startActivity(i);
                            }
                        });
                        break;
                    case "video":
                        progresslay.setVisibility(View.VISIBLE);
                        progressbar.setVisibility(View.GONE);
                        downloadicon.setImageResource(R.drawable.play);
                        timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                        if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive") &&
                                storageManager.checkifImageExists("thumb", message.thumbnail)) {
                            Log.v("dddd", "video-if");
                            videoprogresslay.setVisibility(View.GONE);
                            File file = storageManager.getImage("thumb", message.thumbnail);
                            if (file != null) {
                                Log.v(TAG, "file=" + file.getAbsolutePath());
                                Glide.with(mContext).load(Uri.fromFile(file)).thumbnail(0.5f)
                                        .into(uploadimage);
                            }
                        } else {
                            Log.v("dddd", "video-else=" + message.thumbnail);
                            Glide.with(mContext).load(Constants.CHAT_IMG_PATH + message.thumbnail).thumbnail(0.5f)
                                    .apply(RequestOptions.overrideOf(18, 18))
                                    .into(uploadimage);
                            videoprogresslay.setVisibility(View.VISIBLE);
                            videoprogressbar.setVisibility(View.VISIBLE);
                            videoprogressbar.stopSpinning();
                        }

                        break;
                }
            }
        }

        private class SentFileHolder extends RecyclerView.ViewHolder {
            TextView filename, timeText, file_type_tv;
            ImageView tickimage, icon, uploadicon;
            RelativeLayout file_body_lay;
            ProgressWheel progressbar;

            SentFileHolder(View itemView) {
                super(itemView);

                filename = itemView.findViewById(R.id.filename);
                timeText = itemView.findViewById(R.id.text_message_time);
                tickimage = itemView.findViewById(R.id.tickimage);
                icon = itemView.findViewById(R.id.icon);
                file_body_lay = itemView.findViewById(R.id.file_body_lay);
                progressbar = itemView.findViewById(R.id.progressbar);
                uploadicon = itemView.findViewById(R.id.uploadicon);
                file_type_tv = itemView.findViewById(R.id.file_type_tv);
            }

            void bind(final MessagesData message) {
                filename.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                if (message.delivery_status.equals("read")) {
                    tickimage.setVisibility(View.VISIBLE);
                    tickimage.setImageResource(R.drawable.double_tick);
                } else if (message.delivery_status.equals("sent")) {
                    tickimage.setVisibility(View.VISIBLE);
                    tickimage.setImageResource(R.drawable.double_tick_unseen);
                } else if (message.progress.equals("completed")) {
                    tickimage.setVisibility(View.VISIBLE);
                    tickimage.setImageResource(R.drawable.single_tick);
                } else {
                    tickimage.setVisibility(View.GONE);
                }

                if (message.message_type.equals("document")) {
                    icon.setImageResource(R.drawable.icon_file_unknown);
                    file_type_tv.setVisibility(View.VISIBLE);
                    file_type_tv.setText(firstThree(FilenameUtils.getExtension(message.attachment)));
                } else if (message.message_type.equals("audio")) {
                    icon.setImageResource(R.drawable.mp3);
                    file_type_tv.setVisibility(View.GONE);
                }

                switch (message.progress) {
                    case "":
                        progressbar.setVisibility(View.VISIBLE);
                        progressbar.spin();
                        uploadicon.setVisibility(View.VISIBLE);
                        filename.setText(R.string.uploading);
                        break;
                    case "completed":
                        progressbar.setVisibility(View.GONE);
                        progressbar.stopSpinning();
                        uploadicon.setVisibility(View.GONE);
                        if (message.message_type.equals("voice"))
                            filename.setText("VoiceNote");
                        else
                            filename.setText(message.message);
                        break;
                    case "error":
                        progressbar.setVisibility(View.VISIBLE);
                        progressbar.stopSpinning();
                        uploadicon.setVisibility(View.VISIBLE);
                        filename.setText(R.string.retry);
                        break;
                }

                file_body_lay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (message.progress.equals("error")) {
                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                networkSnack();
                            } else {
                                try {
                                    progressbar.setVisibility(View.VISIBLE);
                                    progressbar.spin();
                                    uploadicon.setVisibility(View.VISIBLE);
                                    filename.setText(getString(R.string.uploading));
                                    dbhelper.updateMessageData(message.message_id, Constants.TAG_PROGRESS, "");
                                    message.progress = "";
                                    Intent service = new Intent(StaredMesActivity.this, FileUploadService.class);
                                    Bundle b = new Bundle();
                                    b.putSerializable("mdata", message);
                                    b.putString("filepath", message.attachment);
                                    b.putString("chatType", "chat");
                                    service.putExtras(b);
                                    startService(service);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        } else if (message.progress.equals("completed")) {
                            if (storageManager.checkifFileExists(message.attachment, message.message_type, "sent")) {
                                try {
                                    if(message.message_type.equals("voice")){
                                        playVoice(message);
                                    }
                                    else{
                                        Intent intent = new Intent();
                                        intent.setAction(android.content.Intent.ACTION_VIEW);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        File file = storageManager.getFile(message.attachment, message.message_type, "sent");
                                        Uri photoURI = FileProvider.getUriForFile(mContext,
                                                BuildConfig.APPLICATION_ID + ".provider", file);

                                        MimeTypeMap mime = MimeTypeMap.getSingleton();
                                        String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                        String type = mime.getMimeTypeFromExtension(ext);

                                        intent.setDataAndType(photoURI, type);

                                        startActivity(intent);
                                    }

                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(StaredMesActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(StaredMesActivity.this, getString(R.string.no_media), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            }
        }

        private class ReceivedFileHolder extends RecyclerView.ViewHolder {
            TextView filename, timeText, file_type_tv;
            ImageView icon, downloadicon;
            RelativeLayout file_body_lay;
            ProgressWheel progressbar;

            ReceivedFileHolder(View itemView) {
                super(itemView);

                filename = itemView.findViewById(R.id.filename);
                timeText = itemView.findViewById(R.id.text_message_time);
                icon = itemView.findViewById(R.id.icon);
                file_body_lay = itemView.findViewById(R.id.file_body_lay);
                downloadicon = itemView.findViewById(R.id.downloadicon);
                progressbar = itemView.findViewById(R.id.progressbar);
                file_type_tv = itemView.findViewById(R.id.file_type_tv);
            }

            void bind(final MessagesData message) {
                filename.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                filename.setText(message.message);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                if (message.message_type.equals("document")) {
                    icon.setImageResource(R.drawable.icon_file_unknown);
                    file_type_tv.setVisibility(View.VISIBLE);
                    file_type_tv.setText(firstThree(FilenameUtils.getExtension(message.attachment)));
                } else if (message.message_type.equals("audio")) {
                    file_type_tv.setVisibility(View.GONE);
                    icon.setImageResource(R.drawable.mp3);
                }

                if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                    downloadicon.setVisibility(View.GONE);
                    progressbar.setVisibility(View.GONE);
                } else {
                    downloadicon.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.VISIBLE);
                    progressbar.stopSpinning();
                }
                file_body_lay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (storageManager.checkifFileExists(message.attachment, message.message_type, "receive")) {
                            try {
                                Intent intent = new Intent();
                                intent.setAction(android.content.Intent.ACTION_VIEW);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                File file = storageManager.getFile(message.attachment, message.message_type, "receive");
                                Uri photoURI = FileProvider.getUriForFile(mContext,
                                        BuildConfig.APPLICATION_ID + ".provider", file);

                                MimeTypeMap mime = MimeTypeMap.getSingleton();
                                String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                                String type = mime.getMimeTypeFromExtension(ext);

                                intent.setDataAndType(photoURI, type);

                                startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                Toast.makeText(StaredMesActivity.this, getString(R.string.no_application), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        } else {
                            if (isNetworkConnected().equals(NOT_CONNECT)) {
                                networkSnack();
                            } else {
                                DownloadFiles downloadFiles = new DownloadFiles(StaredMesActivity.this) {
                                    @Override
                                    protected void onPostExecute(String downPath) {
                                        progressbar.setVisibility(View.GONE);
                                        progressbar.stopSpinning();
                                        downloadicon.setVisibility(View.GONE);
                                        if (downPath == null) {
                                            Log.v("Download Failed", "Download Failed");
                                            Toast.makeText(mContext, "7:"+getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                                        } else {
                                            //Toast.makeText(mContext, getString(R.string.downloaded), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                };
                                downloadFiles.execute(Constants.CHAT_IMG_PATH + message.attachment, message.message_type);
                                progressbar.setVisibility(View.VISIBLE);
                                progressbar.spin();
                                downloadicon.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        }

        private class SentContactHolder extends RecyclerView.ViewHolder {
            TextView username, phoneno, timeText;
            ImageView tickimage;

            SentContactHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.username);
                phoneno = itemView.findViewById(R.id.phoneno);
                tickimage = itemView.findViewById(R.id.tickimage);
                timeText = itemView.findViewById(R.id.text_message_time);
            }

            void bind(MessagesData message) {
                username.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                phoneno.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                username.setText(message.contact_name);
                phoneno.setText(message.contact_phone_no);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                switch (message.delivery_status) {
                    case "read":
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick);
                        break;
                    case "sent":
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.double_tick_unseen);
                        break;
                    default:
                        tickimage.setVisibility(View.VISIBLE);
                        tickimage.setImageResource(R.drawable.single_tick);
                        break;
                }
            }
        }

        private class ReceivedContactHolder extends RecyclerView.ViewHolder {
            TextView username, phoneno, timeText, addcontact;

            ReceivedContactHolder(View itemView) {
                super(itemView);
                username = itemView.findViewById(R.id.username);
                phoneno = itemView.findViewById(R.id.phoneno);
                timeText = itemView.findViewById(R.id.text_message_time);
                addcontact = itemView.findViewById(R.id.addcontact);
            }

            void bind(final MessagesData message) {
                username.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                phoneno.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
                username.setText(message.contact_name);
                phoneno.setText(message.contact_phone_no);
                timeText.setText(ApplicationClass.getTime(Long.parseLong(message.chat_time)));
                if (selectedChatPos.contains(message)) {
                    itemView.setSelected(true);
                } else {
                    itemView.setSelected(false);
                }
                addcontact.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, message.contact_phone_no);
                        intent.putExtra(ContactsContract.Intents.Insert.NAME, message.contact_name);
                        startActivity(intent);
                    }
                });
            }
        }

        private class DateHolder extends RecyclerView.ViewHolder {
            TextView timeText;

            DateHolder(View itemView) {
                super(itemView);
                timeText = itemView.findViewById(R.id.text_message_time);
            }

            void bind(final MessagesData message) {
                timeText.setText(getFormattedDate(mContext, Long.parseLong(message.chat_time)));
            }
        }
    }

    public RecyclerItemClickListener chatItemClick(Context mContext, final RecyclerView recyclerView) {
        return new RecyclerItemClickListener(mContext, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (chatLongPressed) {
                    String messageType = messagesList.get(position).message_type;
                    int chatType = recyclerView.getAdapter().getItemViewType(position);
                    if (chatType != VIEW_TYPE_DATE && isForwardable(messagesList.get(position))) {
                        if (selectedChatPos.contains(messagesList.get(position))) {
                            selectedChatPos.remove(messagesList.get(position));
                            chatUserLay.setVisibility(View.VISIBLE);
                            forwordLay.setVisibility(View.GONE);
                            chatLongPressed = false;
                        } else {
                            selectedChatPos.clear();
                            selectedChatPos.add(messagesList.get(position));
                            chatUserLay.setVisibility(View.GONE);
                            forwordLay.setVisibility(View.VISIBLE);
                            if (messageType.equals("text")) {
                                copyBtn.setVisibility(View.VISIBLE);
                            } else {
                                copyBtn.setVisibility(View.GONE);
                            }
                        }
                        messageListAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!chatLongPressed) {
                    String messageType = messagesList.get(position).message_type;
                    int chatType = recyclerView.getAdapter().getItemViewType(position);
                    if (chatType != VIEW_TYPE_DATE && isForwardable(messagesList.get(position))) {
                        chatLongPressed = true;
                        if (selectedChatPos.contains(messagesList.get(position))) {
                            selectedChatPos.remove(messagesList.get(position));
                            chatUserLay.setVisibility(View.VISIBLE);
                            forwordLay.setVisibility(View.GONE);
                            chatLongPressed = false;
                        } else {
                            selectedChatPos.clear();
                            selectedChatPos.add(messagesList.get(position));
                            chatUserLay.setVisibility(View.GONE);
                            forwordLay.setVisibility(View.VISIBLE);
                            if (messageType.equals("text")) {
                                copyBtn.setVisibility(View.VISIBLE);
                            } else {
                                copyBtn.setVisibility(View.GONE);
                            }
                        }
                        messageListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private boolean isForwardable(MessagesData mData) {
        switch (mData.message_type) {
            case "video":
            case "document":
            case "audio":
                if (mData.user_id.equals(GetSet.getUserId()) && !mData.progress.equals("completed")) {
                    return false;
                } else if (!mData.user_id.equals(GetSet.getUserId()) && !storageManager.checkifFileExists(mData.attachment, mData.message_type, "receive")) {
                    return false;
                }
                return true;
            case "image":
                if (mData.user_id.equals(GetSet.getUserId()) && !mData.progress.equals("completed")) {
                    return false;
                } else if (!mData.user_id.equals(GetSet.getUserId()) && !storageManager.checkifImageExists("receive", mData.attachment)) {
                    return false;
                }
                return true;
            default:
                return true;
        }
    }

    private String getFileName(String url) {
        String imgSplit = url;
        int endIndex = imgSplit.lastIndexOf("/");
        if (endIndex != -1) {
            imgSplit = imgSplit.substring(endIndex + 1, imgSplit.length());
        }
        return imgSplit;
    }

    public String getFormattedDateTime(Context context, long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        smsTime.setTimeInMillis(smsTimeInMilis * 1000L);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "h:mm aa";
        final String dateTimeFormatString = "EEEE, MMMM d, h:mm aa";
        final long HOURS = 60 * 60 * 60;
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return getString(R.string.today) + " " + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return getString(R.string.yesterday) + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return DateFormat.format("MMMM dd yyyy, h:mm aa", smsTime).toString();
        }
    }

    public String getFormattedDate(Context context, long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis * 1000L);

        Calendar now = Calendar.getInstance();

        final String dateTimeFormatString = "d MMMM yyyy";
        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return getString(R.string.today);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return getString(R.string.yesterday);
        } else {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        }
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

    public String firstThree(String str) {
        return str.length() < 3 ? str : str.substring(0, 3);
    }

    public void playVoice(MessagesData data) {

        try {


            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                clearMediaPlayer();
                data.playProgress = 0;
                data.isPlaying = true;
            }


            if (!data.isPlaying) {

                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                }

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        Log.d("MediaPlayer","Completed");
                        clearMediaPlayer();
                    }
                });

                //AssetFileDescriptor descriptor = getAssets().openFd("suits.mp3");
                File file = storageManager.getFile(data.attachment, data.message_type, "sent");
                mediaPlayer.setDataSource(file.getAbsolutePath());
                //descriptor.close();

                mediaPlayer.prepare();
                //mediaPlayer.setVolume(0.5f, 0.5f);
                mediaPlayer.setLooping(false);
                data.maxPlayProgress = mediaPlayer.getDuration();

                mediaPlayer.start();
                //new Thread(this).start();

            }

            //wasPlaying = false;
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void clearMediaPlayer() {
        mediaPlayer.reset();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }



}