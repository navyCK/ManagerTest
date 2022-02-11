package com.mediksystem.managertest.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.mediksystem.managertest.R;
import com.mediksystem.managertest.databinding.ActivityEquipmentDetailBinding;
import com.mediksystem.managertest.item.EquipmentHistoryItem;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;


public class EquipmentDetailActivity extends AppCompatActivity {
    ActivityEquipmentDetailBinding binding;
    String id, user_id, title, body;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_equipment_detail);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        user_id = intent.getStringExtra("user_id");
        title = intent.getStringExtra("title");
        body = intent.getStringExtra("body");

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(0);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("장비 관리");

        binding.equipmentHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EquipmentDetailActivity.this, EquipmentHistoryActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.id.setText(id);
        binding.title.setText(title);
        binding.body.setText(body);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.equipment_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_search:
                setDialog();
                break;
//            case R.id.menu_account:
//                toast.setText("Select menu_account");
//                break;
//            case R.id.menu_logout:
//                toast.setText("Select menu_logout");
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setDialog() {
        String getTime = getNowTime();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("장비 ID : " + id);

        builder.setMessage(getTime + "\n위의 일시로 등록하시겠습니까?");

        builder.setPositiveButton("사용", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "사용 등록되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("세척", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "세척 등록되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNeutralButton("고장", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "고장 등록되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }


    @NonNull
    private String getNowTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String getTime = format.format(date);
        return getTime;
    }



}