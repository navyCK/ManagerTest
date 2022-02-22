package com.mediksystem.managertest.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mediksystem.managertest.R;
import com.mediksystem.managertest.item.HerbPackageItem;

import java.util.ArrayList;

public class HerbPackageAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater mLayoutInflater;
    ArrayList<HerbPackageItem> sample;

    public HerbPackageAdapter(Context context, ArrayList<HerbPackageItem> data) {
        mContext = context;
        sample = data;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return sample.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public HerbPackageItem getItem(int position) {
        return sample.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") View view = mLayoutInflater.inflate(R.layout.recyclerview_herb_package_item, null);

        TextView packageSize = view.findViewById(R.id.package_size);
        TextView packageQuantity = view.findViewById(R.id.package_quantity);
        TextView packageBarcode = view.findViewById(R.id.package_barcode);


        packageSize.setText(String.valueOf(sample.get(position).getSize()));
        packageQuantity.setText(String.valueOf(sample.get(position).getQuantity()));
        packageBarcode.setText(String.valueOf(sample.get(position).getBarcode()));

        return view;
    }
}