package com.mediksystem.managertest.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mediksystem.managertest.R;
import com.mediksystem.managertest.item.EquipmentHistoryItem;
import com.mediksystem.managertest.item.HerbItem;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class HerbPackageAdapter extends RecyclerView.Adapter<HerbPackageAdapter.ViewHolder> {
    private ArrayList<HerbItem> herbPackageItemArrayList = null;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView packageSize, packageQuantity, packageBarcode;

        public ViewHolder(View itemView) {
            super(itemView);

            packageSize = itemView.findViewById(R.id.package_size);
            packageQuantity = itemView.findViewById(R.id.package_quantity);
            packageBarcode = itemView.findViewById(R.id.package_barcode);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        HerbItem item = herbPackageItemArrayList.get(pos);
                        Log.e("입력된 position : ", String.valueOf(pos));

                        Log.e("getPackage_size : ", item.getPackage_size()[0] + "\n" + item.getPackage_size()[1] + "\n" + item.getPackage_size()[2]);
                        Log.d("getPackage_quantity : ", String.valueOf(item.getPackage_quantity()[pos]));
                        Log.d("getPackage_barcode : ", String.valueOf(item.getPackage_barcode()[pos]));

                    }
                }
            });

        }
    }

    public HerbPackageAdapter(ArrayList<HerbItem> list) {
        herbPackageItemArrayList = list;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_herb_package_item, parent, false);
        HerbPackageAdapter.ViewHolder vh = new HerbPackageAdapter.ViewHolder(view);
        return vh;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(HerbPackageAdapter.ViewHolder holder, int position) {
        HerbItem item = herbPackageItemArrayList.get(position);

        holder.packageSize.setText(item.getPackage_size()[0] + "\n" + item.getPackage_size()[1] + "\n" + item.getPackage_size()[2]);
        holder.packageQuantity.setText(item.getPackage_quantity()[0] + "\n" + item.getPackage_quantity()[1] + "\n" + item.getPackage_quantity()[2]);
        holder.packageBarcode.setText(item.getPackage_barcode()[0] + "\n" + item.getPackage_barcode()[1] + "\n" + item.getPackage_barcode()[2]);

    }

    @Override
    public int getItemCount() {
        if (herbPackageItemArrayList != null) {
            return herbPackageItemArrayList.size();
        }
        return 0;

    }

    public void addPackageItem(int[] size, int[] quantity, String[] barcode) {
        HerbItem item = new HerbItem();

        item.setPackage_size(size);
        item.setPackage_quantity(quantity);
        item.setPackage_barcode(barcode);

        herbPackageItemArrayList.add(item);

    }


}
