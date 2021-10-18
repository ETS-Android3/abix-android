package com.topzi.chat.activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;

import android.widget.TextView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCScreenCapturer;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.topzi.chat.R;

import com.topzi.chat.fragment.AudioConversationFragment;
import com.topzi.chat.fragment.BaseConversationFragment;
import com.topzi.chat.fragment.ConversationFragmentCallback;
import com.topzi.chat.fragment.IncomeCallFragment;
import com.topzi.chat.fragment.IncomeCallFragmentCallbackListener;
import com.topzi.chat.fragment.VideoConversationFragment;
import com.topzi.chat.helper.CallNotificationService;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.NetworkReceiver;
import com.topzi.chat.helper.NetworkUtil;
import com.topzi.chat.helper.PhoneStateReceiver;
import com.topzi.chat.helper.QbUsersDbManager;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.helper.Utils;
import com.topzi.chat.model.CallData;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.service.CallService;
import com.topzi.chat.service.LoginService;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.ErrorUtils;
import com.topzi.chat.utils.FragmentExecuotr;
import com.topzi.chat.utils.GetSet;
import com.topzi.chat.utils.PermissionsChecker;
import com.topzi.chat.utils.SettingsUtil;
import com.topzi.chat.utils.SharedPrefsHelper;
import com.topzi.chat.utils.ToastUtils;
import com.topzi.chat.utils.UsersUtils;
import com.topzi.chat.utils.WebRtcSessionManager;

import org.jivesoftware.smack.AbstractConnectionListener;
import org.jivesoftware.smack.ConnectionListener;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WAKE_LOCK;

public class CallActivity extends BaseActivity implements IncomeCallFragmentCallbackListener,
        QBRTCSessionStateCallback<QBRTCSession>, QBRTCClientSessionCallbacks,
        ConversationFragmentCallback {
    private static final String TAG = CallActivity.class.getSimpleName();

    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final int REQUEST_PERMISSION_SETTING = 545;

    private ArrayList<CurrentCallStateCallback> currentCallStateCallbackList = new ArrayList<>();
    private Handler showIncomingCallWindowTaskHandler;
    private ConnectionListenerImpl connectionListener;
    private ServiceConnection callServiceConnection;
    private Runnable showIncomingCallWindowTask;
    private boolean isInComingCall = false;
    private List<Integer> opponentsIdsList;
    private SharedPreferences sharedPref;
    private boolean isVideoCall = false;
    private PermissionsChecker checker;
    private CallService callService;
    protected SharedPrefsHelper sharedPrefsHelper;
    private QbUsersDbManager dbManager = QbUsersDbManager.getInstance(this);

    public static void start(Context context, boolean isIncomingCall) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.EXTRA_IS_INCOMING_CALL, isIncomingCall);
        SharedPrefsHelper.getInstance().save(Constants.EXTRA_IS_INCOMING_CALL, isIncomingCall);
        context.startActivity(intent);
        CallService.start(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        checker = new PermissionsChecker(this);
        sharedPrefsHelper = SharedPrefsHelper.getInstance();
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        
    }

    private void initScreen() {
        callService.setCallTimerCallback(new CallTimerCallback());
        isVideoCall = callService.isVideoCall();

        opponentsIdsList = callService.getOpponents();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        initSettingsStrategy();
        addListeners();

        if (callService.isCallMode()) {
            checkPermission();
            if (callService.isSharingScreenState()) {
                return;
            }
            addConversationFragment(isInComingCall);
        } else {
            if (getIntent() != null && getIntent().getExtras() != null) {
                isInComingCall = getIntent().getExtras().getBoolean(Constants.EXTRA_IS_INCOMING_CALL, false);
            } else {
                isInComingCall = sharedPrefsHelper.get(Constants.EXTRA_IS_INCOMING_CALL, false);
            }

            if (!isInComingCall) {
                callService.playRingtone();
            }
            startSuitableFragment(isInComingCall);
        }
    }

    private void addListeners() {
        addSessionEventsListener(this);
        addSessionStateListener(this);

        connectionListener = new ConnectionListenerImpl();
        addConnectionListener(connectionListener);
    }

    private void removeListeners() {
        removeSessionEventsListener(this);
        removeSessionStateListener(this);
        removeConnectionListener(connectionListener);
        callService.removeCallTimerCallback();
    }

    private void bindCallService() {
        callServiceConnection = new CallServiceConnection();
        Intent intent = new Intent(this, CallService.class);
        bindService(intent, callServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        Log.i(TAG, "onActivityResult requestCode=" + requestCode + ", resultCode= " + resultCode);
        if (resultCode == Constants.EXTRA_LOGIN_RESULT_CODE) {
            if (data != null) {
                boolean isLoginSuccess = data.getBooleanExtra(Constants.EXTRA_LOGIN_RESULT, false);
                if (isLoginSuccess) {
                    initScreen();
                } else {
                    CallService.stop(this);
                    finish();
                }
            }
        }
        if (requestCode == QBRTCScreenCapturer.REQUEST_MEDIA_PROJECTION
                && resultCode == Activity.RESULT_OK && data != null) {
            Log.i(TAG, "Starting Screen Capture");
        }
    }

    private void startSuitableFragment(boolean isInComingCall) {
        QBRTCSession session = WebRtcSessionManager.getInstance(this).getCurrentSession();
        if (session != null) {
            if (isInComingCall) {
                initIncomingCallTask();
                startLoadAbsentUsers();
                addIncomeCallFragment();
                checkPermission();
            } else {
                addConversationFragment(isInComingCall);
                getIntent().removeExtra(Constants.EXTRA_IS_INCOMING_CALL);
                sharedPrefsHelper.save(Constants.EXTRA_IS_INCOMING_CALL, false);
            }
        } else {
            finish();
        }
    }

    private void checkPermission() {
        boolean cam = SharedPrefsHelper.getInstance().get(Constants.PERMISSIONS[0], true);
        boolean mic = SharedPrefsHelper.getInstance().get(Constants.PERMISSIONS[1], true);

        if (isVideoCall && checker.lacksPermissions(Constants.PERMISSIONS)) {
            if (cam) {
                PermissionsActivity.startActivity(this, false, Constants.PERMISSIONS);
            } else {
                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                ErrorUtils.showSnackbar(rootView, R.string.error_permission_video, R.string.dlg_allow, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPermissionSystemSettings();
                    }
                });
            }
        } else if (checker.lacksPermissions(Constants.PERMISSIONS[1])) {
            if (mic) {
                PermissionsActivity.startActivity(this, true, Constants.PERMISSIONS);
            } else {
                View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
                ErrorUtils.showSnackbar(rootView, R.string.error_permission_audio, R.string.dlg_allow, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPermissionSystemSettings();
                    }
                });
            }
        }
    }

    private void startPermissionSystemSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
    }

    private void startLoadAbsentUsers() {
        ArrayList<QBUser> usersFromDb = dbManager.getAllUsers();
        ArrayList<Integer> allParticipantsOfCall = new ArrayList<>();

        if (opponentsIdsList != null) {
            allParticipantsOfCall.addAll(opponentsIdsList);
        }

        if (isInComingCall) {
            Integer callerID = callService.getCallerId();
            if (callerID != null) {
                allParticipantsOfCall.add(callerID);
            }
        }

        ArrayList<Integer> idsUsersNeedLoad = UsersUtils.getIdsNotLoadedUsers(usersFromDb, allParticipantsOfCall);
        if (!idsUsersNeedLoad.isEmpty()) {
            requestExecutor.loadUsersByIds(idsUsersNeedLoad, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    dbManager.saveAllUsers(users, false);
                    notifyCallStateListenersNeedUpdateOpponentsList(users);
                }
            });
        }
    }

    private void initSettingsStrategy() {
        if (opponentsIdsList != null) {
            SettingsUtil.setSettingsStrategy(opponentsIdsList, sharedPref, this);
        }
    }

    private void initIncomingCallTask() {
        showIncomingCallWindowTaskHandler = new Handler(Looper.myLooper());
        showIncomingCallWindowTask = new Runnable() {
            @Override
            public void run() {
                /*if (callService.currentSessionExist()) {
                    BaseSession.QBRTCSessionState currentSessionState = callService.getCurrentSessionState();
                    if (QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_NEW.equals(currentSessionState)) {
                        callService.rejectCurrentSession(new HashMap<>());
                    } else {
                        callService.stopRingtone();
                        hangUpCurrentSession();
                    }
                }*/
                // This is a fix to prevent call stop in case calling to user with more then one device logged in.
                ToastUtils.longToast("Call was stopped by UserNoActions timer");
                callService.clearCallState();
                callService.clearButtonsState();
                WebRtcSessionManager.getInstance(getApplicationContext()).setCurrentSession(null);
                CallService.stop(CallActivity.this);
                finish();
            }
        };
    }

    public void hangUpCurrentSession() {
        callService.stopRingtone();
        if (!callService.hangUpCurrentSession(new HashMap<>())) {
            CallService.stop(this);
            finish();
        }
    }


    private void startIncomeCallTimer(long time) {
        Log.d(TAG, "startIncomeCallTimer");
        showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + time);
    }

    private void stopIncomeCallTimer() {
        Log.d(TAG, "stopIncomeCallTimer");
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindCallService();
    }

    @Override
    public void onPause() {
        super.onPause();
        unbindService(callServiceConnection);
        if (callService != null) {
            removeListeners();
        }
    }

    @Override
    public void finish() {
        //Fix bug when user returns to call from service and the backstack doesn't have any screens
        CallService.stop(this);
//        OpponentsActivity.start(this);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        // To prevent returning from Call Fragment
    }

    private void addIncomeCallFragment() {
        Log.d(TAG, "Adding IncomeCallFragment");
        if (callService.currentSessionExist()) {
            IncomeCallFragment fragment = new IncomeCallFragment();
            FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT);
        } else {
            Log.d(TAG, "SKIP Adding IncomeCallFragment");
        }
    }

    private void addConversationFragment(boolean isIncomingCall) {
        BaseConversationFragment conversationFragment = BaseConversationFragment.newInstance(
                isVideoCall
                        ? new VideoConversationFragment()
                        : new AudioConversationFragment(),
                isIncomingCall);
        FragmentExecuotr.addFragment(getSupportFragmentManager(), R.id.fragment_container, conversationFragment, conversationFragment.getClass().getSimpleName());
    }

    private void showNotificationPopUp(final int text, final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout connectionView = (LinearLayout) View.inflate(CallActivity.this, R.layout.connection_popup, null);
                if (show) {
                    ((TextView) connectionView.findViewById(R.id.notification)).setText(text);
                    if (connectionView.getParent() == null) {
                        ((ViewGroup) CallActivity.this.findViewById(R.id.fragment_container)).addView(connectionView);
                    }
                } else {
                    ((ViewGroup) CallActivity.this.findViewById(R.id.fragment_container)).removeView(connectionView);
                }
            }
        });
    }

    ////////////////////////////// ConnectionListener //////////////////////////////
    private class ConnectionListenerImpl extends AbstractConnectionListener {
        @Override
        public void connectionClosedOnError(Exception e) {
            showNotificationPopUp(R.string.connection_was_lost, true);
        }

        @Override
        public void reconnectionSuccessful() {
            showNotificationPopUp(R.string.connection_was_lost, false);
        }
    }

    ////////////////////////////// QBRTCSessionStateCallbackListener ///////////////////////////
    @Override
    public void onDisconnectedFromUser(QBRTCSession session, Integer userID) {
        Log.d(TAG, "Disconnected from user: " + userID);
    }

    @Override
    public void onConnectedToUser(QBRTCSession session, final Integer userID) {
        notifyCallStateListenersCallStarted();
        if (isInComingCall) {
            stopIncomeCallTimer();
        }
        Log.d(TAG, "onConnectedToUser() is started");
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession session, Integer userID) {
        Log.d(TAG, "Connection closed for user: " + userID);
    }

    @Override
    public void onStateChanged(QBRTCSession qbrtcSession, BaseSession.QBRTCSessionState qbrtcSessionState) {

    }

    ////////////////////////////// QBRTCClientSessionCallbacks //////////////////////////////
    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userID) {
        if (callService.isCurrentSession(session)) {
            callService.stopRingtone();
        }
    }

    @Override
    public void onSessionStartClose(final QBRTCSession session) {
        if (callService.isCurrentSession(session)) {
            callService.removeSessionStateListener(this);
            notifyCallStateListenersCallStopped();
        }
    }

    @Override
    public void onReceiveHangUpFromUser(final QBRTCSession session, final Integer userID, Map<String, String> map) {
        if (callService.isCurrentSession(session)) {
            if (userID.equals(session.getCallerID())) {
                hangUpCurrentSession();
                Log.d(TAG, "Initiator hung up the call");
            }
            QBUser participant = dbManager.getUserById(userID);
            final String participantName = participant != null ? participant.getFullName() : String.valueOf(userID);
            ToastUtils.shortToast("User " + participantName + " " + getString(R.string.text_status_hang_up) + " conversation");
        }
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        if (callService.isCurrentSession(session)) {
            callService.stopRingtone();
        }
    }

    @Override
    public void onReceiveNewSession(final QBRTCSession session) {
        Log.d(TAG, "Session " + session.getSessionID() + " Received");
    }


    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        startIncomeCallTimer(0);
    }

    @Override
    public void onSessionClosed(final QBRTCSession session) {
        if (callService.isCurrentSession(session)) {
            Log.d(TAG, "Stopping session");
            callService.stopForeground(true);
            finish();
        }
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userID, Map<String, String> userInfo) {
        if (callService.isCurrentSession(session)) {
            callService.stopRingtone();
        }
    }

    ////////////////////////////// IncomeCallFragmentCallbackListener ////////////////////////////
    @Override
    public void onAcceptCurrentSession() {
        if (callService.currentSessionExist()) {
            addConversationFragment(true);
        } else {
            Log.d(TAG, "SKIP addConversationFragment method");
        }
    }

    @Override
    public void onRejectCurrentSession() {
        callService.rejectCurrentSession(new HashMap<>());
    }

    ////////////////////////////// ConversationFragmentCallback ////////////////////////////
    @Override
    public void addConnectionListener(ConnectionListener connectionCallback) {
        callService.addConnectionListener(connectionCallback);
    }

    @Override
    public void removeConnectionListener(ConnectionListener connectionCallback) {
        callService.removeConnectionListener(connectionCallback);
    }

    @Override
    public void addSessionStateListener(QBRTCSessionStateCallback clientConnectionCallbacks) {
        callService.addSessionStateListener(clientConnectionCallbacks);
    }

    @Override
    public void addSessionEventsListener(QBRTCSessionEventsCallback eventsCallback) {
        callService.addSessionEventsListener(eventsCallback);
    }

    @Override
    public void onSetAudioEnabled(boolean isAudioEnabled) {
        callService.setAudioEnabled(isAudioEnabled);
    }

    @Override
    public void onHangUpCurrentSession() {
        hangUpCurrentSession();
    }

    @TargetApi(21)
    @Override
    public void onStartScreenSharing() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        QBRTCScreenCapturer.requestPermissions(this);
    }

    @Override
    public void onSwitchCamera(CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler) {
        callService.switchCamera(cameraSwitchHandler);
    }

    @Override
    public void onSetVideoEnabled(boolean isNeedEnableCam) {
        callService.setVideoEnabled(isNeedEnableCam);
    }

    @Override
    public void onSwitchAudio() {
        callService.switchAudio();
    }

    @Override
    public void removeSessionStateListener(QBRTCSessionStateCallback clientConnectionCallbacks) {
        callService.removeSessionStateListener(clientConnectionCallbacks);
    }

    @Override
    public void removeSessionEventsListener(QBRTCSessionEventsCallback eventsCallback) {
        callService.removeSessionEventsListener(eventsCallback);
    }

    @Override
    public void addCurrentCallStateListener(CurrentCallStateCallback currentCallStateCallback) {
        if (currentCallStateCallback != null) {
            currentCallStateCallbackList.add(currentCallStateCallback);
        }
    }

    @Override
    public void removeCurrentCallStateListener(CurrentCallStateCallback currentCallStateCallback) {
        currentCallStateCallbackList.remove(currentCallStateCallback);
    }

    @Override
    public void addOnChangeAudioDeviceListener(OnChangeAudioDevice onChangeDynamicCallback) {

    }

    @Override
    public void removeOnChangeAudioDeviceListener(OnChangeAudioDevice onChangeDynamicCallback) {

    }

    @Override
    public void acceptCall(Map<String, String> userInfo) {
        callService.acceptCall(userInfo);
    }

    @Override
    public void startCall(Map<String, String> userInfo) {
        callService.startCall(userInfo);
    }

    @Override
    public boolean currentSessionExist() {
        return callService.currentSessionExist();
    }

    @Override
    public List<Integer> getOpponents() {
        return callService.getOpponents();
    }

    @Override
    public Integer getCallerId() {
        return callService.getCallerId();
    }

    @Override
    public void addVideoTrackListener(QBRTCClientVideoTracksCallbacks<QBRTCSession> callback) {
        callService.addVideoTrackListener(callback);
    }

    @Override
    public void removeVideoTrackListener(QBRTCClientVideoTracksCallbacks<QBRTCSession> callback) {
        callService.removeVideoTrackListener(callback);
    }

    @Override
    public BaseSession.QBRTCSessionState getCurrentSessionState() {
        return callService.getCurrentSessionState();
    }

    @Override
    public QBRTCTypes.QBRTCConnectionState getPeerChannel(Integer userId) {
        return callService.getPeerChannel(userId);
    }

    @Override
    public boolean isMediaStreamManagerExist() {
        return callService.isMediaStreamManagerExist();
    }

    @Override
    public boolean isCallState() {
        return callService.isCallMode();
    }

    @Override
    public HashMap<Integer, QBRTCVideoTrack> getVideoTrackMap() {
        return callService.getVideoTrackMap();
    }

    @Override
    public QBRTCVideoTrack getVideoTrack(Integer userId) {
        return callService.getVideoTrack(userId);
    }

    private void notifyCallStateListenersCallStarted() {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStarted();
        }
    }

    private void notifyCallStateListenersCallStopped() {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallStopped();
        }
    }

    private void notifyCallStateListenersNeedUpdateOpponentsList(final ArrayList<QBUser> newUsers) {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onOpponentsListUpdated(newUsers);
        }
    }

    private void notifyCallStateListenersCallTime(String callTime) {
        for (CurrentCallStateCallback callback : currentCallStateCallbackList) {
            callback.onCallTimeUpdate(callTime);
        }
    }

    private class CallServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CallService.CallServiceBinder binder = (CallService.CallServiceBinder) service;
            callService = binder.getService();
            if (callService.currentSessionExist()) {
                //we have already currentSession == null, so it's no reason to do further initialization
                if (QBChatService.getInstance().isLoggedIn()) {
                    initScreen();
                } else {
                    login();
                }
            } else {
                finish();
            }
        }

        private void login() {
            QBUser qbUser = SharedPrefsHelper.getInstance().getQbUser();
            Intent tempIntent = new Intent(CallActivity.this, LoginService.class);
            PendingIntent pendingIntent = createPendingResult(Constants.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
            LoginService.start(CallActivity.this, qbUser, pendingIntent);
        }
    }

    private class CallTimerCallback implements CallService.CallTimerListener {
        @Override
        public void onCallTimeUpdate(String time) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyCallStateListenersCallTime(time);
                }
            });
        }
    }

    public interface OnChangeAudioDevice {
        void audioDeviceChanged(AppRTCAudioManager.AudioDevice newAudioDevice);
    }


    public interface CurrentCallStateCallback {
        void onCallStarted();

        void onCallStopped();

        void onOpponentsListUpdated(ArrayList<QBUser> newUsers);

        void onCallTimeUpdate(String time);
    }
}