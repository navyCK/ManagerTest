package com.mediksystem.managertest.activity;

import android.os.Bundle;

import com.mediksystem.managertest.R;
import com.mediksystem.managertest.databinding.ActivityTemperatureBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class TemperatureActivity extends AppCompatActivity {
    ActivityTemperatureBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_temperature);


    }
}