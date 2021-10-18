package com.topzi.chat.Backup;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.topzi.chat.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;
import static com.topzi.chat.activity.ChatBackupActivity.REQUEST_CODE_CREATION;
import static com.topzi.chat.activity.ChatBackupActivity.REQUEST_CODE_OPENING;
import static com.topzi.chat.activity.ChatBackupActivity.REQUEST_CODE_SIGN_IN;
import static com.topzi.chat.helper.DatabaseHandler.DATABASE_NAME;

public class RemoteBackup {

    private static final String TAG = "Google Drive Activity";

    private DriveClient mDriveClient;
    private DriveResourceClient mDriveResourceClient;

    public TaskCompletionSource<DriveId> mOpenItemTaskSource;

    private Activity activity;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Dialog dialog;


    public RemoteBackup(Activity activity) {
        this.activity = activity;
        pref = activity.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
    }

    public void connectToDrive(boolean backup) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(activity);
        if (account == null) {
            buildGoogleSignInClient();
        } else {
            //Initialize the drive api
            mDriveClient = Drive.getDriveClient(activity, account);
            // Build a drive resource client.
            mDriveResourceClient = Drive.getDriveResourceClient(activity, account);
            if (backup)
                startDriveBackup();
            else
                startDriveRestore();
        }
    }


    private void buildGoogleSignInClient() {

        if (!pref.getString("backupEmail","").equals("")) {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(activity.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .setAccountName(pref.getString("backupEmail", ""))
                            .requestScopes(Drive.SCOPE_FILE)
                            .build();

            GoogleSignInClient GoogleSignInClient =  GoogleSignIn.getClient(activity, signInOptions);
            activity.startActivityForResult(GoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);

        } else {
            getAccounts();
        }

    }


    private void getAccounts(){
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(activity).getAccounts();

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialoge_g_account);
        dialog.getWindow().setLayout(activity.getResources().getDisplayMetrics().widthPixels * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        RadioGroup radioGrp = dialog.findViewById(R.id.radioGroup);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);

        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        for (int i = 0; i < accounts.length; i++) {
            Account account = accounts[i];
            if (emailPattern.matcher(account.name).matches()) {
                String possibleEmail = account.name;
                Log.e("LLLLL_Account_ID: ",possibleEmail);
                RadioButton radioButton = new RadioButton(activity);
                radioButton.setPadding(30,30,7,30);
                radioButton.setText(possibleEmail);
                radioButton.setId(i);
                radioGrp.addView(radioButton);
            }
        }

        //set listener to radio button group
        radioGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int checkedRadioButtonId = group.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) dialog.findViewById(checkedRadioButtonId);
                editor.putString("backupEmail", String.valueOf(radioBtn.getText()));
                editor.commit();
                dialog.dismiss();

                GoogleSignInOptions signInOptions =
                        new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestIdToken(activity.getString(R.string.default_web_client_id))
                                .requestEmail()
                                .setAccountName(pref.getString("backupEmail", ""))
                                .requestScopes(Drive.SCOPE_FILE)
                                .build();

                GoogleSignInClient GoogleSignInClient =  GoogleSignIn.getClient(activity, signInOptions);
                activity.startActivityForResult(GoogleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);

            }
        });

        dialog.show();

    }

    private void startDriveBackup() {
        mDriveResourceClient
                .createContents()
                .continueWithTask(
                        task -> createFileIntentSender(task.getResult()))
                .addOnFailureListener(
                        e -> Log.w(TAG, "Failed to create new contents.", e));

        mDriveResourceClient
                .createContents()
                .continueWithTask(
                        task -> createFileIntentSenderPref(task.getResult()))
                .addOnFailureListener(
                        e -> Log.w(TAG, "Failed to create new xml contents.", e));
        editor.putBoolean("remoteBackup",true);
        editor.commit();
    }

    private Task<Void> createFileIntentSender(DriveContents driveContents) {

        final String inFileName = activity.getDatabasePath(DATABASE_NAME).toString();

        try {
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);
            OutputStream outputStream = driveContents.getOutputStream();

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.e("LLLLLL_Exce_DB_Uoload:", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }

        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle("database_backup.db")
                .setMimeType("application/db")
                .build();


        CreateFileActivityOptions createFileActivityOptions =
                new CreateFileActivityOptions.Builder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(driveContents)
                        .build();

        return mDriveClient
                .newCreateFileActivityIntentSender(createFileActivityOptions)
                .continueWith(
                        task -> {
                            activity.startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATION, null, 0, 0, 0);
                            return null;
                        });
    }

    private Task<Void> createFileIntentSenderPref(DriveContents driveContents) {


        final String inFileName = activity.getApplicationInfo().dataDir+"/"+"shared_prefs/SavedPref.xml";
        Log.e("LLLLLL_XML: ", inFileName);

        try {
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);
            OutputStream outputStream = driveContents.getOutputStream();

            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.e("LLLLLL_Exce_DB_Uoload:", Objects.requireNonNull(e.getMessage()));
            e.printStackTrace();
        }

        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle("database_backup.db")
                .setMimeType("text/xml")
                .build();


        CreateFileActivityOptions createFileActivityOptions =
                new CreateFileActivityOptions.Builder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(driveContents)
                        .build();

        return mDriveClient
                .newCreateFileActivityIntentSender(createFileActivityOptions)
                .continueWith(
                        task -> {
                            activity.startIntentSenderForResult(task.getResult(), REQUEST_CODE_CREATION, null, 0, 0, 0);
                            return null;
                        });
    }


    private void startDriveRestore() {
        pickFile()
                .addOnSuccessListener(activity,
                        driveId -> retrieveContents(driveId.asDriveFile()))
                .addOnFailureListener(activity, e -> {
                    Log.e(TAG, "No file selected", e);
                });
    }

    private void retrieveContents(DriveFile file) {

        //DB Path
        final String inFileName = activity.getDatabasePath(DATABASE_NAME).toString();

        Task<DriveContents> openFileTask = mDriveResourceClient.openFile(file, DriveFile.MODE_READ_ONLY);

        openFileTask
                .continueWithTask(task -> {
                    DriveContents contents = task.getResult();
                    try {
                        ParcelFileDescriptor parcelFileDescriptor = contents.getParcelFileDescriptor();
                        FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());

                        // Open the empty db as the output stream
                        OutputStream output = new FileOutputStream(inFileName);

                        // Transfer bytes from the inputfile to the outputfile
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fileInputStream.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }

                        // Close the streams
                        output.flush();
                        output.close();
                        fileInputStream.close();
                        Toast.makeText(activity, "Import completed", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "Error on import", Toast.LENGTH_SHORT).show();
                    }
                    return mDriveResourceClient.discardContents(contents);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Unable to read contents", e);
                    Toast.makeText(activity, "Error on import", Toast.LENGTH_SHORT).show();
                });
    }

    private void retrieveContentsPref(DriveFile file) {

        final String inFileName = activity.getApplicationInfo().dataDir+"/"+"shared_prefs/SavedPref.xml";

        Task<DriveContents> openFileTask = mDriveResourceClient.openFile(file, DriveFile.MODE_READ_ONLY);

        openFileTask
                .continueWithTask(task -> {
                    DriveContents contents = task.getResult();
                    try {
                        ParcelFileDescriptor parcelFileDescriptor = contents.getParcelFileDescriptor();
                        FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());

                        // Open the empty db as the output stream
                        OutputStream output = new FileOutputStream(inFileName);

                        // Transfer bytes from the inputfile to the outputfile
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fileInputStream.read(buffer)) > 0) {
                            output.write(buffer, 0, length);
                        }

                        // Close the streams
                        output.flush();
                        output.close();
                        fileInputStream.close();
                        Toast.makeText(activity, "Import completed", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "Error on import", Toast.LENGTH_SHORT).show();
                    }
                    return mDriveResourceClient.discardContents(contents);

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Unable to read contents", e);
                    Toast.makeText(activity, "Error on import", Toast.LENGTH_SHORT).show();
                });
    }

    private Task<DriveId> pickItem(OpenFileActivityOptions openOptions) {
        mOpenItemTaskSource = new TaskCompletionSource<>();
        mDriveClient
                .newOpenFileActivityIntentSender(openOptions)
                .continueWith((Continuation<IntentSender, Void>) task -> {
                    activity.startIntentSenderForResult(
                            task.getResult(), REQUEST_CODE_OPENING, null, 0, 0, 0);
                    return null;
                });
        return mOpenItemTaskSource.getTask();
    }

    private Task<DriveId> pickFile() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, "application/db"))
                        .setActivityTitle("Select DB File")
                        .build();
        return pickItem(openOptions);
    }



}
