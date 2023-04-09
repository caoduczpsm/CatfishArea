package com.example.catfisharea.activities.alluser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityConferenceBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;

public class ConferenceActivity extends BaseActivity {
    private ActivityConferenceBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityConferenceBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        Animatoo.animateSlideLeft(this);
        setListener();
    }

    private void setListener() {
        mBinding.toolbarConference.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        mBinding.newMeeting.setOnClickListener(view -> {
            startActivity(new Intent(this, InviteActivity.class));
        });
    }
}