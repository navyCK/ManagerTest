package com.mediksystem.managertest.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;

import com.mediksystem.managertest.R;
import com.mediksystem.managertest.adapter.EquipmentHistoryAdapter;
import com.mediksystem.managertest.databinding.ActivityEquipmentHistoryBinding;
import com.mediksystem.managertest.dialog.ProgressDialog;
import com.mediksystem.managertest.item.EquipmentHistoryItem;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EquipmentHistoryActivity extends AppCompatActivity {
    ActivityEquipmentHistoryBinding binding;
    RecyclerView recyclerView = null;
    EquipmentHistoryAdapter adapter = null;
    ArrayList<EquipmentHistoryItem> equipmentHistoryItemArrayList = new ArrayList<>();

    ProgressDialog customProgressDialog;

    String id;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_equipment_history);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");

        setSupportActionBar(binding.equipmentHistoryToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(0);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(id + "의 장비 이력");

        setProgressDialog();
        setTimer();
    }

    private void setProgressDialog() {
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        customProgressDialog.setCancelable(false);
        customProgressDialog.show();
    }

    public void setTimer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView = binding.equipmentHistoryRecyclerView;
                recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), 1));
                LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
                manager.setReverseLayout(true);
                manager.setStackFromEnd(true);
                recyclerView.setLayoutManager(manager);

                adapter = new EquipmentHistoryAdapter(equipmentHistoryItemArrayList);
                recyclerView.setAdapter(adapter);


                for (int i=11; i<31; i++) {
                    if (i % 3 == 0) {
                        adapter.addItem("세척", "2022-01-" + i + " 10:30");
                    } else if (i % 10 == 0) {
                        adapter.addItem("고장", "2022-01-" + i + " 09:20");
                    } else {
                        adapter.addItem("사용", "2022-01-" + i + " 13:40");
                    }
                }

                customProgressDialog.dismiss();
            }
        }, 1000);
    }


}
