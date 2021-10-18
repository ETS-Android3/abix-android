package com.topzi.chat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.topzi.chat.R;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.DataStorageModel;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.topzi.chat.utils.Constants.setStatusBarGradiant;

public class DetailStorageActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.chatUser)
    RecyclerView chatUser;
    @BindView(R.id.img_back)
    ImageView img_back;

    StorageUserAdapter storageUserAdapter;
    ArrayList<DataStorageModel> dataStorageModels1 = new ArrayList<>();
    ContactsData.Result results;
    DatabaseHandler dbhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarGradiant(DetailStorageActivity.this);
        setContentView(R.layout.activity_detail_storage);

        ButterKnife.bind(DetailStorageActivity.this);
        dbhelper = DatabaseHandler.getInstance(this);

        dataStorageModels1.clear();
        dataStorageModels1.addAll(dbhelper.getStorageRecords());
        chatUser.setLayoutManager(new LinearLayoutManager(DetailStorageActivity.this, RecyclerView.VERTICAL, false));
        storageUserAdapter = new StorageUserAdapter(DetailStorageActivity.this,dataStorageModels1);
        chatUser.setAdapter(storageUserAdapter);

        img_back.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.img_back:
                onBackPressed();
                break;
        }
    }

    private class StorageUserAdapter extends RecyclerView.Adapter<StorageUserAdapter.MyClassView> {

        Context context;
        ArrayList<DataStorageModel> dataStorageModels = new ArrayList<>();

        public StorageUserAdapter(Context context, ArrayList<DataStorageModel> dataStorageModels) {
            this.context = context;
            this.dataStorageModels = dataStorageModels;
        }

        @NonNull
        @Override
        public MyClassView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.data_storage, parent, false);
            return new MyClassView(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyClassView holder, int position) {
            DataStorageModel storageModel = dataStorageModels.get(position);
            results = dbhelper.getContactDetail(storageModel.getUserId());
            holder.tvUsername.setText(ApplicationClass.getContactName(context, results.phone_no));
            DialogActivity.setProfileImage(dbhelper.getContactDetail(storageModel.getUserId()), holder.userImage, context);

            long total = Long.parseLong(storageModel.getSent_photos_size()) +
                    Long.parseLong(storageModel.getSent_videos_size()) +
                    Long.parseLong(storageModel.getSent_aud_size()) +
                    Long.parseLong(storageModel.getSent_doc_size());

            holder.tvSize.setText(humanReadableByteCountSI(total));

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(DetailStorageActivity.this,StorageDetailUser.class);
                intent.putExtra("UserID",storageModel.getUserId());
                startActivity(intent);
                finish();
            });
        }

        @Override
        public int getItemCount() {
            return dataStorageModels.size();
        }

        public class MyClassView extends RecyclerView.ViewHolder {

            CircleImageView userImage;
            TextView tvUsername, tvSize;


            public MyClassView(@NonNull View itemView) {
                super(itemView);

                userImage = itemView.findViewById(R.id.userImage);
                tvUsername = itemView.findViewById(R.id.tvUsername);
                tvSize = itemView.findViewById(R.id.tvSize);
            }
        }
    }

    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DetailStorageActivity.this, DataStorage.class);
        startActivity(intent);
        finish();
    }

}