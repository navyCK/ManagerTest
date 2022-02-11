package com.mediksystem.managertest.activity;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.mediksystem.managertest.R;
import com.mediksystem.managertest.databinding.ActivityHerbDetailBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class HerbDetailActivity extends AppCompatActivity {
    ActivityHerbDetailBinding binding;

    String image, barcode, name, company, country_of_origin, storage_location;
    int storage_period, expiration, total_weight_of_inventory_storage;
    double purchase_price, sales_price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_herb_detail);

        Intent intent = getIntent();
        image = intent.getStringExtra("image");
        barcode = intent.getStringExtra("barcode");
        name = intent.getStringExtra("name");
        company = intent.getStringExtra("company");
        country_of_origin = intent.getStringExtra("country_of_origin");
        storage_location = intent.getStringExtra("storage_location");
        storage_period = intent.getIntExtra("storage_period", 0);
        expiration = intent.getIntExtra("expiration", 0);
        total_weight_of_inventory_storage = intent.getIntExtra("total_weight_of_inventory_storage", 0);
        purchase_price = intent.getDoubleExtra("purchase_price", 0);
        sales_price = intent.getDoubleExtra("sales_price", 0);

        Glide.with(this).load(image).into(binding.herbDetailImage);
        binding.herbDetailName.setText(name);
        binding.herbDetailTotalWeight.setText(String.valueOf(total_weight_of_inventory_storage));
        binding.herbDetailExpiration.setText(String.valueOf(expiration));
        binding.herbDetailStoragePeriod.setText(String.valueOf(storage_period));
        binding.herbDetailPurchasePrice.setText(String.valueOf(purchase_price));
        binding.herbDetailSalesPrice.setText(String.valueOf(sales_price));

    }
}