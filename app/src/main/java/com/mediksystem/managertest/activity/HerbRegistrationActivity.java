package com.mediksystem.managertest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mediksystem.managertest.R;
import com.mediksystem.managertest.databinding.ActivityHerbRegistrationBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class HerbRegistrationActivity extends AppCompatActivity {
    ActivityHerbRegistrationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_herb_registration);

        setToolbar();

        Intent intent = getIntent();
        String barcode = intent.getStringExtra("barcode");
        Log.e("-", barcode);

        if (CheckNumber(barcode)) {
            binding.herbRegistrationBarcode.setText(barcode);
        }

        binding.herbRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "등록하였습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


    }

    public boolean CheckNumber(String str){
        char check;

        if(str.equals(""))
        {
            //문자열이 공백인지 확인
            return false;
        }

        for(int i = 0; i<str.length(); i++){
            check = str.charAt(i);
            if( check < 48 || check > 58)
            {
                //해당 char값이 숫자가 아닐 경우
                return false;
            }

        }
        return true;
    }

    private void setToolbar() {
        setSupportActionBar(binding.herbRegisterToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(0);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("약재 등록");
    }
}
