package com.topzi.chat.activity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.topzi.chat.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.topzi.chat.helper.DatabaseHandler;
import com.topzi.chat.helper.SocketConnection;
import com.topzi.chat.model.ContactsData;
import com.topzi.chat.model.SaveMyContacts;
import com.topzi.chat.model.contacts.ContactsModel;
import com.topzi.chat.utils.ApiClient;
import com.topzi.chat.utils.ApiInterface;
import com.topzi.chat.utils.Constants;
import com.topzi.chat.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * Created on 20/6/18.
 */

public class SelectContact extends BaseActivity implements View.OnClickListener, SocketConnection.SelectContactListener {

    private final String TAG = this.getClass().getSimpleName();
    TextView title, nullText;
    ImageView backbtn, searchbtn, optionbtn, cancelbtn;
    ProgressBar progressBar;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RecyclerViewAdapter recyclerViewAdapter;
    DatabaseHandler dbhelper;
    EditText searchView;
    RelativeLayout searchLay, mainLay;
    LinearLayout nullLay;
    LinearLayout buttonLayout;
    List<ContactsData.Result> contactList = new ArrayList<>();
    List<ContactsData.Result> filteredList = new ArrayList<>();
    static ApiInterface apiInterface;
    SharedPreferences pref;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_contact);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        pref = SelectContact.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        dbhelper = DatabaseHandler.getInstance(this);
        SocketConnection.getInstance(this).setSelectContactListener(this);

        title = findViewById(R.id.title);
        backbtn = findViewById(R.id.backbtn);
        searchbtn = findViewById(R.id.searchbtn);
        optionbtn = findViewById(R.id.optionbtn);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);
        buttonLayout = findViewById(R.id.buttonLayout);
        cancelbtn = findViewById(R.id.cancelbtn);
        searchLay = findViewById(R.id.searchLay);
        mainLay = findViewById(R.id.mainLay);
        nullLay = findViewById(R.id.nullLay);
        nullText = findViewById(R.id.nullText);

        title.setVisibility(View.VISIBLE);
        backbtn.setVisibility(View.VISIBLE);
        searchbtn.setVisibility(View.VISIBLE);
        optionbtn.setVisibility(View.VISIBLE);

        title.setText(getString(R.string.select_contact));
        backbtn.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.primarytext));

        dbhelper = DatabaseHandler.getInstance(this);
        backbtn.setOnClickListener(this);
        searchbtn.setOnClickListener(this);
        optionbtn.setOnClickListener(this);
        cancelbtn.setOnClickListener(this);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        contactList.addAll(dbhelper.getStoredContacts(this));
        Collections.sort(contactList, new Comparator<ContactsData.Result>() {
            public int compare(ContactsData.Result obj1, ContactsData.Result obj2) {
                // ## Ascending order
                return obj1.user_name.compareToIgnoreCase(obj2.user_name); // To compare string values
            }
        });
        filteredList = new ArrayList<>();
        filteredList.addAll(contactList);

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

        nullText.setText(getString(R.string.no_contact));
        if (contactList.size() == 0) {
            nullLay.setVisibility(View.VISIBLE);
        } else {
            nullLay.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNetworkChange(boolean isConnected) {

    }

    @Override
    public void onUserImageChange(final String user_id, final String user_image) {
        Log.v("Chat", "onUserImageChange");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recyclerViewAdapter != null && filteredList.size() > 0) {
                    for (int i = 0; i < filteredList.size(); i++) {
                        if (user_id.equals(filteredList.get(i).user_id)) {
                            filteredList.get(i).user_image = user_image;
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recyclerViewAdapter != null && filteredList.size() > 0) {
                    try {
                        String sender_id = data.getString(Constants.TAG_SENDER_ID);
                        String type = data.getString(Constants.TAG_TYPE);
                        for (int i = 0; i < filteredList.size(); i++) {
                            if (sender_id.equals(filteredList.get(i).user_id)) {
                                filteredList.get(i).blockedme = type;
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
    public void onPrivacyChanged(JSONObject jsonObject) {

//        Log.i(TAG, "onPrivacyChanged: " + jsonObject);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (recyclerViewAdapter != null) {
                    contactList.clear();
                    contactList.addAll(dbhelper.getStoredContacts(getApplicationContext()));
                    Collections.sort(contactList, new Comparator<ContactsData.Result>() {
                        public int compare(ContactsData.Result obj1, ContactsData.Result obj2) {
                            // ## Ascending order
                            return obj1.user_name.compareToIgnoreCase(obj2.user_name); // To compare string values
                        }
                    });

                    Log.e("LLLLLL_Data_List: ", String.valueOf(contactList.size()));
                    filteredList.clear();
                    filteredList.addAll(contactList);
                    recyclerViewAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter implements Filterable {

        private static final int VIEW_TYPE_HEADER = 1;
        private static final int VIEW_TYPE_CONTACTS = 2;

        List<ContactsData.Result> Items;
        Context context;
        private SearchFilter mFilter;

        public RecyclerViewAdapter(Context context) {
            this.context = context;
            mFilter = new SearchFilter(RecyclerViewAdapter.this);
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
                final FilterResults results = new FilterResults();
                if (constraint.length() == 0) {
                    filteredList.addAll(contactList);
                } else {
                    final String filterPattern = constraint.toString().toLowerCase().trim();
                    for (final ContactsData.Result result : contactList) {
                        if (result.user_name != null) {
                            if (result.user_name.toLowerCase().startsWith(filterPattern)) {
                                filteredList.add(result);
                            }
                        }
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                this.mAdapter.notifyDataSetChanged();
                Log.e("LLLLL_FileList: ", String.valueOf(filteredList.size()));
                if(filteredList.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    nullLay.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = null;

            if (viewType == VIEW_TYPE_HEADER) {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_create_group, parent, false);
                return new RecyclerViewAdapter.HeaderViewHolder(itemView);
            } else {
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.contact_list_item, parent, false);
                return new RecyclerViewAdapter.MyViewHolder(itemView);
            }

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == VIEW_TYPE_CONTACTS) {
                position = position - 1;
                if (ContextCompat.checkSelfPermission(SelectContact.this, READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    ((MyViewHolder) holder).name.setText(filteredList.get(position).user_name);
                } else {
                    ((MyViewHolder) holder).name.setText(filteredList.get(position).phone_no);
                }

                if (filteredList.get(position).about != null && !filteredList.get(position).about.equals("null")) {
                    ((MyViewHolder) holder).about.setVisibility(View.VISIBLE);
                    ((MyViewHolder) holder).about.setText(filteredList.get(position).about);
                } else {
                    ((MyViewHolder) holder).about.setText("");
                    ((MyViewHolder) holder).about.setVisibility(View.GONE);
                }
                if (filteredList.get(position).blockedme.equals("block")) {
                    Glide.with(context).load(R.drawable.change_camera)
                            .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.change_camera).error(R.drawable.change_camera).override(ApplicationClass.dpToPx(context, 70)))
                            .into(((MyViewHolder) holder).profileimage);
                    ((MyViewHolder) holder).about.setVisibility(View.GONE);
                } else {
                    ContactsData.Result contactDetails = dbhelper.getContactDetail(filteredList.get(position).user_id);
                    DialogActivity.setProfileImage(contactDetails, ((MyViewHolder) holder).profileimage, context);
                    ((MyViewHolder) holder).about.setVisibility(View.VISIBLE);
                    DialogActivity.setAboutUs(contactDetails, ((MyViewHolder) holder).about);
                }
            } else {
                if(filteredList.size() == 0) {
                    ((HeaderViewHolder)holder).groupLayout.setVisibility(View.GONE);
                } else {
                    ((HeaderViewHolder)holder).groupLayout.setVisibility(View.VISIBLE);
                }
            }

        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return VIEW_TYPE_HEADER;
            } else {
                return VIEW_TYPE_CONTACTS;
            }
        }

        @Override
        public int getItemCount() {
            return filteredList.size() + 1;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            LinearLayout parentlay;
            TextView name, about;
            CircleImageView profileimage;
            View profileview;

            public MyViewHolder(View view) {
                super(view);

                parentlay = view.findViewById(R.id.parentlay);
                profileimage = view.findViewById(R.id.profileimage);
                name = view.findViewById(R.id.name);
                about = view.findViewById(R.id.about);
                profileview = view.findViewById(R.id.profileview);

                parentlay.setOnClickListener(this);
                profileimage.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.parentlay:
                        Intent i = new Intent(SelectContact.this, ChatActivity.class);
                        i.putExtra("user_id", filteredList.get(getAdapterPosition() - 1).user_id);
                        startActivity(i);
                        break;
                    case R.id.profileimage:
                        openUserDialog(profileview, filteredList.get(getAdapterPosition() - 1));
                        break;
                }
            }
        }

        public class HeaderViewHolder extends RecyclerView.ViewHolder {

            LinearLayout groupLayout;
            TextView name;
            ImageView profileimage;
            View profileview;

            public HeaderViewHolder(View view) {
                super(view);

                groupLayout = view.findViewById(R.id.groupLayout);
                profileimage = view.findViewById(R.id.profileimage);
                name = view.findViewById(R.id.name);
                profileview = view.findViewById(R.id.profileview);

                groupLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent group = new Intent(getApplicationContext(), NewGroupActivity.class);
                        startActivity(group);
                    }
                });
            }
        }
    }

    private void openUserDialog(View view, ContactsData.Result data) {
        Intent i = new Intent(SelectContact.this, DialogActivity.class);
        i.putExtra(Constants.TAG_USER_ID, data.user_id);
        i.putExtra(Constants.TAG_USER_NAME, data.user_name);
        i.putExtra(Constants.TAG_USER_IMAGE, data.user_image);
        i.putExtra(Constants.TAG_BLOCKED_ME, data.blockedme);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(SelectContact.this, view, getURLForResource(R.drawable.change_camera));
        startActivity(i, options.toBundle());
    }

    public static String getURLForResource(int resourceId) {
        return Uri.parse("android.resource://com.topzi.chat/" + resourceId).toString();
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
            case R.id.optionbtn:
                Display display = this.getWindowManager().getDefaultDisplay();
                ArrayList<String> values = new ArrayList<>();
                values.add(getString(R.string.refresh));

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        R.layout.option_item, android.R.id.text1, values);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = layoutInflater.inflate(R.layout.option_layout, null);
                layout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft));
                final PopupWindow popup = new PopupWindow(SelectContact.this);
                popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                popup.setContentView(layout);
                popup.setWidth(display.getWidth() * 60 / 100);
                popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                popup.setFocusable(true);
                popup.showAtLocation(mainLay, Gravity.TOP | Gravity.RIGHT, ApplicationClass.dpToPx(this, 10), ApplicationClass.dpToPx(this, 63));

                final ListView lv = layout.findViewById(R.id.listView);
                lv.setAdapter(adapter);
                popup.showAsDropDown(view);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        popup.dismiss();
                        if (position == 0) {
//                            if (progressDialog != null) progressDialog.show();
                            progressBar.setVisibility(View.VISIBLE);
                            progressBar.setIndeterminate(true);
                            new GetContactTask().execute();
                        }
                    }
                });
                break;
            case R.id.cancelbtn:
                searchView.setText("");
                break;
        }
    }

    public boolean isValidPhoneNumber(CharSequence target) {
        if (target.length() < 7 || target.length() > 15) {
            return false;
        } else {
            return android.util.Patterns.PHONE.matcher(target).matches();
        }
    }

    public void saveMyContacts(List<String> contacts) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_CONTACTS, "" + contacts);
        Log.v(TAG, "saveMyContacts=" + contacts);
        Call<SaveMyContacts> call = apiInterface.saveMyContacts(GetSet.getToken(), map);
        call.enqueue(new Callback<SaveMyContacts>() {
            @Override
            public void onResponse(Call<SaveMyContacts> call, Response<SaveMyContacts> response) {
                Log.v(TAG, "saveMyContacts=" + new Gson().toJson(response));
                updatemycontacts(contacts);
            }

            @Override
            public void onFailure(Call<SaveMyContacts> call, Throwable t) {
                Log.e(TAG, "saveMyContacts: " + t.getMessage());
                call.cancel();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    void updatemycontacts(List<String> myContacts) {
        List<String> contacts = new ArrayList<>();
        contacts = dbhelper.getAllContactsNumber(this);
        for (String contact : contacts) {
            if (!myContacts.contains(contact)) {
                myContacts.add(contact.replaceAll("[^0-9]", ""));
            }
        }

        List<ContactsModel> contactsNum = new ArrayList<>();
        for (String contact : contacts) {
            if (!myContacts.contains(contact)) {
                ContactsModel contactsModel = new ContactsModel();
                contactsModel.setNumber(contact.replaceAll("[^0-9]", ""));
                contactsNum.add(contactsModel);
//                myContacts.add(contact.replaceAll("[^0-9]", ""));
            }
        }

//        HashMap<String, String> map = new HashMap<>();
//        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
//        map.put(Constants.TAG_PHONE_NUMBER, GetSet.getphonenumber());
//        map.put(Constants.TAG_CONTACTS, "" + myContacts);
//        Log.v("Login params:", "" + map);
        Gson gson = new Gson();
        String contact = gson.toJson(contactsNum);

//        Log.v(TAG, "updateMyContacts: " + map);
        Call<ContactsData> call3 = apiInterface.updatemycontacts(GetSet.getToken(), GetSet.getUserId(), GetSet.getphonenumber(), contact);
        call3.enqueue(new Callback<ContactsData>() {
            @Override
            public void onResponse(Call<ContactsData> call, Response<ContactsData> response) {
                try {
                    Log.v(TAG, "updateMyContacts=" + new Gson().toJson(response));
                    ContactsData data = response.body();
                    if (data.status.equals("true")) {
                        new UpdateContactTask(data).execute();
                    } else if (data.status.equals("false")) {
                        progressBar.setVisibility(View.GONE);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ContactsData> call, Throwable t) {
                Log.e(TAG, "updateMyContacts: " + t.getMessage());
                call.cancel();
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateContactTask extends AsyncTask<Void, Integer, Void> {
        ContactsData data = new ContactsData();

        public UpdateContactTask(ContactsData data) {
            this.data = data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (ContactsData.Result result : data.result) {
                dbhelper.addContactDetails(result.user_id, result.user_name, result.phone_no, result.country_code, result.user_image, result.privacy_about,
                        result.privacy_last_scene, result.privacy_profile_image, result.about, result.contactstatus);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            filteredList.clear();
            contactList.clear();
            contactList.addAll(dbhelper.getStoredContacts(SelectContact.this));
            Collections.sort(contactList, new Comparator<ContactsData.Result>() {
                public int compare(ContactsData.Result obj1, ContactsData.Result obj2) {
                    // ## Ascending order
                    return obj1.user_name.compareToIgnoreCase(obj2.user_name); // To compare string values
                }
            });
            filteredList.addAll(contactList);
            if (recyclerViewAdapter != null) {
                recyclerViewAdapter.notifyDataSetChanged();
                Collections.sort(contactList, new Comparator<ContactsData.Result>() {
                    public int compare(ContactsData.Result obj1, ContactsData.Result obj2) {
                        // ## Ascending order
                        return obj1.user_name.compareToIgnoreCase(obj2.user_name); // To compare string values
                    }
                });
            }

            nullText.setText(getString(R.string.no_contact));
            if (contactList.size() == 0) {
                nullLay.setVisibility(View.VISIBLE);
            } else {
                nullLay.setVisibility(View.GONE);
            }
            progressBar.setVisibility(View.GONE);
        }
    }

 @SuppressLint("StaticFieldLeak")
    private class GetContactTask extends AsyncTask<Void, Integer, Void> {
     List<String> contactsNum = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Uri uri = null;
            uri = ContactsContract.CommonDataKinds.Contactables.CONTENT_URI;
            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(uri, Constants.PROJECTION, Constants.SELECTION, Constants.SELECTION_ARGS, null);

            if (cur != null) {
                try {
                    final int nameIndex = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                    final int numberIndex = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    while (cur.moveToNext()) {
                        String phoneNo = cur.getString(numberIndex).replace(" ", "");
                        String name = cur.getString(nameIndex);
                        try {
                            PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                            Phonenumber.PhoneNumber numberProto = phoneUtil.parse(phoneNo, Locale.getDefault().getCountry());
                            if (phoneNo != null && !phoneNo.equals("") && phoneNo.length() > 6 && phoneUtil.isPossibleNumberForType(numberProto, PhoneNumberUtil.PhoneNumberType.MOBILE)) {
                                String tempNo = ("" + numberProto.getNationalNumber());
                                if(tempNo.startsWith("0")) {
                                    tempNo = tempNo.replaceFirst("^0+(?!$)", "");
                                }
                                contactsNum.add(tempNo.replaceAll("[^0-9]", ""));
//                                Log.v("Name", "name=" + name + " num="+tempNo.replaceAll("[^0-9]", ""));
                            }
                        } catch (NumberParseException e) {
                            if (isValidPhoneNumber(phoneNo)) {
//                                Log.v("Name", "excep name=" + name + " num="+phoneNo.replaceAll("[^0-9]", ""));
                                if(phoneNo.startsWith("0")) {
                                    phoneNo = phoneNo.replaceFirst("^0+(?!$)", "");
                                }
                                contactsNum.add(phoneNo.replaceAll("[^0-9]", ""));
                            }
                        }
                    }
                } finally {
                    cur.close();
                }
            }
            Log.e(TAG, "getContactList: " + contactsNum.size());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            saveMyContacts(contactsNum);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recyclerViewAdapter != null) {
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketConnection.getInstance(this).setSelectContactListener(null);
    }
}
