package com.mediksystem.managertest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.mediksystem.managertest.R;
import com.mediksystem.managertest.databinding.ActivityHerbHistoryBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class HerbHistoryActivity extends AppCompatActivity {
    ActivityHerbHistoryBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_herb_history);
        setToolbar();

        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        String weight = intent.getStringExtra("weight");
        String date = intent.getStringExtra("date");

        if (!TextUtils.isEmpty(type)) {
            binding.herbHistoryType.setText(type);
            binding.herbHistoryWeight.setText(weight);
            binding.herbHistoryDate.setText(date);

            binding.herbHistoryEmptyText.setVisibility(View.GONE);
            binding.herbHistoryInfo.setVisibility(View.VISIBLE);
        } else {
            binding.herbHistoryEmptyText.setVisibility(View.VISIBLE);
            binding.herbHistoryInfo.setVisibility(View.GONE);
        }
    }

    private void setToolbar() {
        setSupportActionBar(binding.herbHistoryToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(0);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("약재 이력");
    }
}
