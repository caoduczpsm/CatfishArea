package com.example.catfisharea.activities.alluser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityIncomingConferenceBinding;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;

import java.net.MalformedURLException;
import java.net.URL;

public class IncomingConferenceActivity extends AppCompatActivity {
    private ActivityIncomingConferenceBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityIncomingConferenceBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        setInfo();
        setListener();
    }

    private void setListener() {
        mBinding.acceptBtn.setOnClickListener(view -> {
            try {
                JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
                userInfo.setDisplayName(preferenceManager.getString(Constants.KEY_NAME));
                URL serverURL = new URL("https://meet.jit.si");
                JitsiMeetConferenceOptions conferenceOptions = new JitsiMeetConferenceOptions.Builder()
                        .setServerURL(serverURL)
                        .setUserInfo(userInfo)
                        .setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM))
                        .build();
                JitsiMeetActivity.launch(this, conferenceOptions);
                finish();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        });

        mBinding.rejectBtn.setOnClickListener(view -> {
            finish();
        });
    }

    private void setInfo() {
        database.collection(Constants.KEY_COLLECTION_USER).document(getIntent().getStringExtra(Constants.KEY_USER_ID))
                .get().addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString(Constants.KEY_NAME);
                    String avt = documentSnapshot.getString(Constants.KEY_IMAGE);
                    String position = documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);

                    mBinding.nameInviter.setText(name);
                    mBinding.imageInviter.setImageBitmap(User.getUserImage(avt));
                    mBinding.positionInviter.setText(User.getNamePosition(position));

                });
    }
}