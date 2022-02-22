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
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.mediksystem.managertest.R;
import com.mediksystem.managertest.activity.HerbDetailActivity;
import com.mediksystem.managertest.databinding.RecyclerviewHerbItemBinding;
import com.mediksystem.managertest.item.HerbItem;
import com.mediksystem.managertest.item.HerbPackageItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HerbAdapter extends RecyclerView.Adapter<HerbAdapter.ViewHolder> {
    ArrayList<HerbItem> herbItemArrayList;
    ArrayList<HerbPackageItem> herbPackageItemArrayList;


    public HerbAdapter(ArrayList<HerbItem> list) {
        herbItemArrayList = list;
    }

    @NonNull
    @Override
    public HerbAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        return new ViewHolder(RecyclerviewHerbItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerviewHerbItemBinding binding;
        boolean isCheck = true;

        @SuppressLint("ResourceType")
        public ViewHolder(RecyclerviewHerbItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.btnExpandToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isCheck) {
                        binding.btnExpandToggle.setImageResource(R.drawable.circle_minus);
                        isCheck = false;

                        setExpandView(View.VISIBLE);

                    } else {
                        binding.btnExpandToggle.setImageResource(R.drawable.circle_plus);
                        isCheck = true;

                        setExpandView(View.GONE);

                    }
                }

                private void setExpandView(int visible) {
                    Animation animation = new AlphaAnimation(0, 1);
                    animation.setDuration(500);
                    binding.expandLayout.setVisibility(visible);
                    binding.expandLayout.setAnimation(animation);
                }

            });

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
                .into(holder.binding.herbImage);

        holder.binding.herbBarcode.setText(item.getBarcode());
        holder.binding.herbName.setText(item.getName());
        holder.binding.herbCompany.setText(item.getCompany());
        holder.binding.herbCountry.setText(item.getCountry_of_origin());

        herbPackageItemArrayList = new ArrayList<>();

        for (int i = 0; i < item.getPackage_size().length; i++) {
            herbPackageItemArrayList.add(new HerbPackageItem(item.getPackage_size()[i], item.getPackage_quantity()[i], item.getPackage_barcode()[i]));
        }

        ListView listView = holder.binding.packageListview;
        HerbPackageAdapter adapter = new HerbPackageAdapter(listView.getContext(), herbPackageItemArrayList);
        listView.setAdapter(adapter);
//        setListViewHeightBasedOnChildren(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });


        double result = 0;

        for (int i = 0; i < item.getPackage_size().length; i++) {
            result += item.getPackage_size()[i] * item.getPackage_quantity()[i];
        }

        holder.binding.packageTotalWeight.setText(String.valueOf(result / 1000));

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

        herbItemArrayList.add(item);

    }

    @Override
    public int getItemCount() {
        if (herbItemArrayList != null) {
            return herbItemArrayList.size();
        }
        return 0;
    }

    // listview 높이 조정
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            //listItem.measure(0, 0);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight;
        listView.setLayoutParams(params);

        listView.requestLayout();
    }

}
