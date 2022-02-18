package com.mediksystem.managertest.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mediksystem.managertest.R;
import com.mediksystem.managertest.activity.HerbDetailActivity;
import com.mediksystem.managertest.item.HerbItem;
import com.mediksystem.managertest.item.HerbPackageItem;

import java.util.ArrayList;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HerbAdapter extends RecyclerView.Adapter<HerbAdapter.ViewHolder> {
    private ArrayList<HerbItem> herbItemArrayList = null;
    private ArrayList<HerbPackageItem> packageItems = null;

    RecyclerView packageRecyclerView = null;
    HerbPackageAdapter adapter = null;

    public HerbAdapter(ArrayList<HerbItem> list) {
        herbItemArrayList = list;
    }

    @Override
    public HerbAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;


        View view = inflater.inflate(R.layout.recyclerview_herb_item, parent, false);
        HerbAdapter.ViewHolder vh = new HerbAdapter.ViewHolder(view);

        return vh;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView barcode, name, company, country_of_origin, storage_period, expiration, memo,
                storage_location, total_weight_of_inventory_storage, purchase_price, sales_price;
        ImageView image;
        ImageView btn_expand_toggle;
        LinearLayout expand_layout;
        TextView totalWeight, barcode1, barcode2, barcode3, size1, size2, size3, quantity1, quantity2, quantity3;
        RecyclerView recyclerView;

        boolean isCheck = true;

        @SuppressLint("ResourceType")
        public ViewHolder(View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.herbImage);
            barcode = itemView.findViewById(R.id.herbBarcode);
            name = itemView.findViewById(R.id.herbName);
            company = itemView.findViewById(R.id.herbCompany);
            country_of_origin = itemView.findViewById(R.id.herbCountry);

            btn_expand_toggle = itemView.findViewById(R.id.btn_expand_toggle);

            expand_layout = itemView.findViewById(R.id.expandLayout);

            barcode1 = itemView.findViewById(R.id.barcode1);
            barcode2 = itemView.findViewById(R.id.barcode2);
            barcode3 = itemView.findViewById(R.id.barcode3);
            size1 = itemView.findViewById(R.id.size1);
            size2 = itemView.findViewById(R.id.size2);
            size3 = itemView.findViewById(R.id.size3);
            quantity1 = itemView.findViewById(R.id.quantity1);
            quantity2 = itemView.findViewById(R.id.quantity2);
            quantity3 = itemView.findViewById(R.id.quantity3);

            totalWeight = itemView.findViewById(R.id.package_total_weight);

            recyclerView = itemView.findViewById(R.id.package_recyclerview);

//            storage_period = itemView.findViewById(R.id.herbStoragePeriod);
//            expiration = itemView.findViewById(R.id.herbExpiration);
//            total_weight_of_inventory_storage = itemView.findViewById(R.id.herbTotalWeight);
//            purchase_price = itemView.findViewById(R.id.herbPurchasePrice);
//            sales_price = itemView.findViewById(R.id.herbSalesPrice);

//            storage_location = itemView.findViewById(R.id.herbStorage);

            btn_expand_toggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isCheck) {
                        btn_expand_toggle.setImageResource(R.drawable.circle_minus);
                        isCheck = false;

                        setExpandView(View.VISIBLE);

                    } else {
                        btn_expand_toggle.setImageResource(R.drawable.circle_plus);
                        isCheck = true;

                        setExpandView(View.GONE);

                    }
                }

                private void setExpandView(int visible) {
                    Animation animation = new AlphaAnimation(0, 1);
                    animation.setDuration(500);
                    expand_layout.setVisibility(visible);
                    expand_layout.setAnimation(animation);
                }

            });

            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), 1));
            LinearLayoutManager manager = new LinearLayoutManager(recyclerView.getContext());
            manager.setReverseLayout(false);
            manager.setStackFromEnd(true);
            recyclerView.setLayoutManager(manager);

            adapter = new HerbPackageAdapter(herbItemArrayList);
            recyclerView.setAdapter(adapter);



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

        holder.size1.setText(String.valueOf(item.getPackage_size()[0]));
        holder.size2.setText(String.valueOf(item.getPackage_size()[1]));
        holder.size3.setText(String.valueOf(item.getPackage_size()[2]));
        holder.quantity1.setText(String.valueOf(item.getPackage_quantity()[0]));
        holder.quantity2.setText(String.valueOf(item.getPackage_quantity()[1]));
        holder.quantity3.setText(String.valueOf(item.getPackage_quantity()[2]));
        holder.barcode1.setText(item.getPackage_barcode()[0]);
        holder.barcode2.setText(item.getPackage_barcode()[1]);
        holder.barcode3.setText(item.getPackage_barcode()[2]);

        double result = item.getPackage_size()[0]*item.getPackage_quantity()[0]
                    + item.getPackage_size()[1]*item.getPackage_quantity()[1]
                    + item.getPackage_size()[2]*item.getPackage_quantity()[2];
        holder.totalWeight.setText(String.valueOf(result/1000));


//        holder.storage_period.setText(String.valueOf(item.getStorage_period()));
//        holder.expiration.setText(String.valueOf(item.getExpiration()));
//        holder.storage_location.setText(item.getStorage_location());
//        holder.total_weight_of_inventory_storage.setText(String.valueOf(item.getTotal_weight_of_inventory_storage()));
//        holder.purchase_price.setText(String.valueOf(item.getPurchase_price()));
//        holder.sales_price.setText(String.valueOf(item.getSales_price()));


    }



    @SuppressLint("NotifyDataSetChanged")
    public void filterList(ArrayList<HerbItem> arrayList) {
        herbItemArrayList = arrayList;
        notifyDataSetChanged();
    }

    public void addItem(String image, String name, String barcode, String company, String country_of_origin, String memo, int expiration, int storage_period,
                        String storage_location, int total_weight_of_inventory_storage, double purchase_price, double sales_price,
                        int[] package_size, int[] package_quantity, String[] package_barcode) {
        HerbItem item = new HerbItem();

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

        item.setPackage_size(package_size);
        item.setPackage_quantity(package_quantity);
        item.setPackage_barcode(package_barcode);

        if (package_size != null) {
            for (int i=0; i<package_size.length; i++) {
                addPackageItem(package_size[i], package_quantity[i], package_barcode[i]);
            }
            Log.e("패키지", packageItems.toString());
        }


        herbItemArrayList.add(item);

    }

    public void addPackageItem(int size, int quantity, String barcode) {
        HerbPackageItem item = new HerbPackageItem();

        item.setSize(size);
        item.setQuantity(quantity);
        item.setBarcode(barcode);

        packageItems.add(item);
    }

    @Override
    public int getItemCount() {
        if (herbItemArrayList != null) {
            return herbItemArrayList.size();
        }
        return 0;
    }



}
