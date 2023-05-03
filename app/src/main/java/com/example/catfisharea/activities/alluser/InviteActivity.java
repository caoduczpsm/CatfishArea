package com.example.catfisharea.activities.alluser;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityInviteBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.network.ApiClient;
import com.example.catfisharea.network.ApiService;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InviteActivity extends BaseActivity implements MultipleListener {
    private ActivityInviteBinding mBinding;
    private PreferenceManager preferenceManager;
    private MultipleUserSelectionAdapter usersAdapter;
    private List<User> users;
    private FirebaseFirestore database;
    private String inviterToken;
    private String meetingRoom = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityInviteBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        init();
        setListener();
    }

    private void init() {
        preferenceManager = new PreferenceManager(this);
        database = FirebaseFirestore.getInstance();
        users = new ArrayList<>();
        usersAdapter = new MultipleUserSelectionAdapter(users, this);
        mBinding.recyclerviewInvite.setAdapter(usersAdapter);
        getUsers();

    }

    @SuppressLint("SetTextI18n")
    private void setListener() {
        mBinding.toolbarInvite.setOnClickListener(view -> onBackPressed());

        mBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                usersAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String inputSearch) {
                usersAdapter.getFilter().filter(inputSearch);
                return true;
            }
        });
        mBinding.imageFilter.setOnClickListener(view -> openFilterSearchDialog());

        // Chọn tất cả hoặc bỏ chọn tất cả tài khoản
        mBinding.cbAllAccount.setOnClickListener(view -> {
            if (mBinding.cbAllAccount.getText().toString().equals("Tất Cả")) {
                usersAdapter.setAllSelected(users);
                mBinding.cbAllAccount.setText("Bỏ Chọn");
            } else {
                usersAdapter.setAllUnSelected(users);
                mBinding.cbAllAccount.setText("Tất Cả");
            }
        });

        mBinding.invite.setOnClickListener(view -> {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    inviterToken = task.getResult().getToken();
                }
            });
            List<User> mUsers = usersAdapter.getSelectedUser();
            if (mUsers != null && mUsers.size() > 0) {
                for (User user : mUsers) {
                    //Log.d("Calltest", user.name);
                    initiateConference(user.token);
                }
                try {
                    JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
                    userInfo.setDisplayName(preferenceManager.getString(Constants.KEY_NAME));

                    URL serverURL = new URL("https://meet.jit.si");
                    JitsiMeetConferenceOptions conferenceOptions = new JitsiMeetConferenceOptions.Builder()
                            .setServerURL(serverURL)
                            .setUserInfo(userInfo)
                            .setRoom(meetingRoom)
                            .build();
                    JitsiMeetActivity.launch(this, conferenceOptions);
                    finish();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void initiateConference(String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);
            //Log.d("Calltest", receiverToken);
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, "video");
            data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            meetingRoom = preferenceManager.getString(Constants.KEY_USER_ID) + "_" +
                    UUID.randomUUID().toString().substring(0, 5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);

        } catch (Exception ignored) {

        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    if (Objects.equals(type, Constants.REMOTE_MSG_INVITATION)) {
                        Toast.makeText(InviteActivity.this, "Invitation sent successfully", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(InviteActivity.this, response.message(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void openFilterSearchDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_filter_search);
        assert dialog != null;

        //Button trong dialog
        AppCompatButton no_btn = dialog.findViewById(R.id.btnClose);
        AppCompatButton btnApply = dialog.findViewById(R.id.btnApply);

        //CheckBox
        CheckBox cbAllAccount, cbAdmin, cbAccountant, cbRegionalChief, cbDirector, cbWorker;

        cbAllAccount = dialog.findViewById(R.id.cbAllAccount);
        cbAdmin = dialog.findViewById(R.id.cbAdmin);
        cbAccountant = dialog.findViewById(R.id.cbAccountant);
        cbRegionalChief = dialog.findViewById(R.id.cbRegionalChief);
        cbDirector = dialog.findViewById(R.id.cbDirector);
        cbWorker = dialog.findViewById(R.id.cbWorker);

        cbAdmin.setOnClickListener(view -> {
            if (cbAccountant.isChecked() && cbRegionalChief.isChecked() &&
                    cbDirector.isChecked() && cbWorker.isChecked()) {
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()) {
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbAccountant.setOnClickListener(view -> {
            if (cbAdmin.isChecked() && cbRegionalChief.isChecked() &&
                    cbDirector.isChecked() && cbWorker.isChecked()) {
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()) {
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbRegionalChief.setOnClickListener(view -> {
            if (cbAdmin.isChecked() && cbAccountant.isChecked() &&
                    cbDirector.isChecked() && cbWorker.isChecked()) {
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()) {
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbDirector.setOnClickListener(view -> {
            if (cbAdmin.isChecked() && cbAccountant.isChecked() &&
                    cbRegionalChief.isChecked() && cbWorker.isChecked()) {
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()) {
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbWorker.setOnClickListener(view -> {
            if (cbAdmin.isChecked() && cbAccountant.isChecked() &&
                    cbRegionalChief.isChecked() && cbDirector.isChecked()) {
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()) {
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        // Chọn tất cả loại tài khoản
        cbAllAccount.setOnClickListener(view -> {
            if (cbAllAccount.getText().toString().equals("Tất Cả")) {
                cbAdmin.setChecked(true);
                cbAccountant.setChecked(true);
                cbRegionalChief.setChecked(true);
                cbDirector.setChecked(true);
                cbWorker.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            } else {
                cbAdmin.setChecked(false);
                cbAccountant.setChecked(false);
                cbRegionalChief.setChecked(false);
                cbDirector.setChecked(false);
                cbWorker.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }

        });

        // Bấm áp dụng
        btnApply.setOnClickListener(view -> {
            // Nếu checkbox tất cả tài khoản hoặc tất cả vùng được chọn thì hiển thị ra tất cả người dùng
            if (cbAllAccount.isChecked()) {
                users.clear();
                getUsers();
                dialog.dismiss();
            } else {
                users.clear();
                // Ngược lại thì truyền loại tài khoản người dùng vào hàm bộ lọc
                if (cbAdmin.isChecked()) {
                    getFilterUser(Constants.KEY_ADMIN);
                }

                if (cbAccountant.isChecked()) {
                    getFilterUser(Constants.KEY_ACCOUNTANT);
                }

                if (cbRegionalChief.isChecked()) {
                    getFilterUser(Constants.KEY_REGIONAL_CHIEF);
                }

                if (cbDirector.isChecked()) {
                    getFilterUser(Constants.KEY_DIRECTOR);
                }

                if (cbWorker.isChecked()) {
                    getFilterUser(Constants.KEY_WORKER);
                }

                dialog.dismiss();
            }

        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getFilterUser(String type) {
        // Kiểm tra nếu tài khoản có cùng loại với bộ lọc thì mới hiển thị
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, type)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                            usersAdapter.notifyDataSetChanged();

                        }

                    }

                });
    }

    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        return dialog;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getUsers() {

        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        users.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);

                        }
                        usersAdapter.notifyDataSetChanged();

                    }
                });

    }

    @Override
    public void onMultipleUserSelection(Boolean isSelected) {

    }

    @Override
    public void onChangeTeamLeadClicker(User user) {

    }

    @Override
    public void onTaskClicker(Task task) {

    }

    @Override
    public void onTaskSelectedClicker(Boolean isSelected, Boolean isMultipleSelection) {

    }

}