package com.mediksystem.managertest.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mediksystem.managertest.R;
import com.mediksystem.managertest.item.EquipmentHistoryItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class EquipmentHistoryAdapter extends RecyclerView.Adapter<EquipmentHistoryAdapter.ViewHolder> {
    private ArrayList<EquipmentHistoryItem> equipmentHistoryItemArrayList = null;
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);



    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView code, date;
        public ViewHolder(View itemView) {
            super(itemView);

            code = itemView.findViewById(R.id.equipmentHistoryCode);
            date = itemView.findViewById(R.id.equipmentHistoryDate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        EquipmentHistoryItem item = equipmentHistoryItemArrayList.get(pos);
                        Log.e("입력된 position : ", String.valueOf(pos));

                        Log.d("getCode : ", item.getCode());
                        Log.d("getDate : ", item.getDate());

                    }
                }
            });

        }
    }

    public EquipmentHistoryAdapter(ArrayList<EquipmentHistoryItem> list) {
        equipmentHistoryItemArrayList = list;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recyclerview_equipment_history, parent, false);
        EquipmentHistoryAdapter.ViewHolder vh = new EquipmentHistoryAdapter.ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(EquipmentHistoryAdapter.ViewHolder holder, int position) {
        EquipmentHistoryItem item = equipmentHistoryItemArrayList.get(position);
        holder.code.setText(item.getCode());
        holder.date.setText(item.getDate());

        if (item.getCode().equals("세척")) {
            holder.code.setTextColor(Color.BLUE);
        } else if (item.getCode().equals("고장")) {
            holder.code.setTextColor(Color.RED);
        } else {
            holder.code.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        if (equipmentHistoryItemArrayList != null) {
            return equipmentHistoryItemArrayList.size();
        }
        return 0;

    }

    public void addItem(String code, String date) {
        EquipmentHistoryItem item = new EquipmentHistoryItem();

        item.setCode(code);
        item.setDate(date);

        equipmentHistoryItemArrayList.add(item);

    }


}
