package com.mediksystem.managertest.activity;

import android.os.Bundle;

import com.mediksystem.managertest.R;
import com.mediksystem.managertest.databinding.ActivityMemberBinding;
import com.mediksystem.managertest.databinding.ActivityTemperatureBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class MemberActivity extends AppCompatActivity {
    ActivityMemberBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_member);


    }
}