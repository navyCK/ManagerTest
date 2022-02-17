package com.mediksystem.managertest.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mediksystem.managertest.R;
import com.mediksystem.managertest.activity.HerbActivity;
import com.mediksystem.managertest.activity.HerbDetailActivity;
import com.mediksystem.managertest.item.HerbItem;
import com.mediksystem.managertest.util.OnItemClick;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

public class HerbAdapter extends RecyclerView.Adapter<HerbAdapter.ViewHolder> {
    private ArrayList<HerbItem> herbItemArrayList = null;

    public static final int HEADER = 0;
    public static final int CHILD = 1;

    public HerbAdapter(ArrayList<HerbItem> list) {
        herbItemArrayList = list;
    }

    //    @Override
//    public HerbAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        Context context = parent.getContext() ;
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;
//
//        View view = inflater.inflate(R.layout.recyclerview_herb_item, parent, false);
//        HerbAdapter.ViewHolder vh = new HerbAdapter.ViewHolder(view);
//
//        return vh;
//    }


    @Override
    public HerbAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        Context context = parent.getContext();
        float dp = context.getResources().getDisplayMetrics().density;
        int subItemPaddingLeft = (int) (18 * dp);
        int subItemPaddingTopAndBottom = (int) (5 * dp);

        switch (viewType) {
            case HEADER:
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.recyclerview_herb_item, parent, false);

                HerbAdapter.ViewHolder vh = new HerbAdapter.ViewHolder(view);
                return vh;

            case CHILD:
                TextView itemTextView = new TextView(context);
                itemTextView.setPadding(subItemPaddingLeft, subItemPaddingTopAndBottom, 0, subItemPaddingTopAndBottom);
                itemTextView.setTextColor(0x88000000);
                itemTextView.setLayoutParams(
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                return new ViewHolder(itemTextView) {

                };
        }

        return null;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView barcode, name, company, country_of_origin, storage_period, expiration, memo,
                storage_location, total_weight_of_inventory_storage, purchase_price, sales_price;
        ImageView image;
        Button expandButton;
        HerbItem refferalItem;

        public ViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.herbImage);
            barcode = itemView.findViewById(R.id.herbBarcode);
            name = itemView.findViewById(R.id.herbName);
            company = itemView.findViewById(R.id.herbCompany);
            country_of_origin = itemView.findViewById(R.id.herbCountry);
            expandButton = itemView.findViewById(R.id.expandButton);
//            storage_period = itemView.findViewById(R.id.herbStoragePeriod);
//            expiration = itemView.findViewById(R.id.herbExpiration);
//            total_weight_of_inventory_storage = itemView.findViewById(R.id.herbTotalWeight);
//            purchase_price = itemView.findViewById(R.id.herbPurchasePrice);
//            sales_price = itemView.findViewById(R.id.herbSalesPrice);

//            storage_location = itemView.findViewById(R.id.herbStorage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        HerbItem item = herbItemArrayList.get(pos);

                        Log.e("입력된 position : ", String.valueOf(pos));
                        Log.d("getName : ", item.getName());
                        Log.d("getCompany : ", item.getCompany());



                        Context context = itemView.getContext();

//                        Intent posIntent = new Intent(context, HerbActivity.class);
//                        posIntent.putExtra("pos", posIntent);
//                        LocalBroadcastManager.getInstance(context).sendBroadcast(posIntent);

                        Intent intent = new Intent(context, HerbDetailActivity.class);

                        // String
                        intent.putExtra("image", item.getImage());
                        intent.putExtra("barcode", item.getBarcode());
                        intent.putExtra("name", item.getName());
                        intent.putExtra("company", item.getCompany());
                        intent.putExtra("country_of_origin", item.getCountry_of_origin());
                        intent.putExtra("storage_location", item.getStorage_location());
                        intent.putExtra("memo", item.getMemo());

                        // int
                        intent.putExtra("storage_period", item.getStorage_period());
                        intent.putExtra("expiration", item.getExpiration());
                        intent.putExtra("total_weight_of_inventory_storage", item.getTotal_weight_of_inventory_storage());

                        // double
                        intent.putExtra("purchase_price", item.getPurchase_price());
                        intent.putExtra("sales_price", item.getSales_price());

                        context.startActivity(intent);
                    }
                }
            });
        }
    }





    @Override
    public void onBindViewHolder(HerbAdapter.ViewHolder holder, int position) {
        HerbItem item = herbItemArrayList.get(position);

        switch (item.getType()) {
            case HEADER:


            case CHILD:



        }

        String imageUrl = item.getImage();
        Glide.with(holder.itemView)
                .load(imageUrl)
                .placeholder(R.drawable.icon_herbal)
                .error(R.drawable.icon_herbal)
                .into(holder.image);

        holder.barcode.setText(item.getBarcode());
        holder.name.setText(item.getName());
        holder.company.setText(item.getCompany());
        holder.country_of_origin.setText(item.getCountry_of_origin());
//        holder.storage_period.setText(String.valueOf(item.getStorage_period()));
//        holder.expiration.setText(String.valueOf(item.getExpiration()));
//        holder.storage_location.setText(item.getStorage_location());
//        holder.total_weight_of_inventory_storage.setText(String.valueOf(item.getTotal_weight_of_inventory_storage()));
//        holder.purchase_price.setText(String.valueOf(item.getPurchase_price()));
//        holder.sales_price.setText(String.valueOf(item.getSales_price()));


    }

    @Override
    public int getItemCount() {
        if (herbItemArrayList != null) {
            return herbItemArrayList.size();
        }
        return 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<HerbItem> arrayList) {
        herbItemArrayList = arrayList;
        notifyDataSetChanged();
    }

    public void addItem(int type, String image, String name, String barcode, String company, String country_of_origin, String memo, int expiration, int storage_period,
                        String storage_location, int total_weight_of_inventory_storage, double purchase_price, double sales_price) {
        HerbItem item = new HerbItem();

        item.setType(type);
        item.setImage(image);
        item.setName(name);
        item.setBarcode(barcode);
        item.setCompany(company);
        item.setCountry_of_origin(country_of_origin);
        item.setMemo(memo);
        item.setExpiration(expiration);
        item.setStorage_period(storage_period);
        item.setStorage_location(storage_location);
        item.setTotal_weight_of_inventory_storage(total_weight_of_inventory_storage);
        item.setPurchase_price(purchase_price);
        item.setSales_price(sales_price);

        herbItemArrayList.add(item);

    }


    private class ListHeaderViewHolder extends RecyclerView.ViewHolder{
        TextView name;
        Button expandButton;
        HerbItem refferalItem;

        public ListHeaderViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.herbName);
            expandButton = itemView.findViewById(R.id.expandButton);
        }
    }
}
