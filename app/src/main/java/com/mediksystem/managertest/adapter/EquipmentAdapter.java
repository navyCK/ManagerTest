package com.mediksystem.managertest.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mediksystem.managertest.R;
import com.mediksystem.managertest.activity.EquipmentDetailActivity;
import com.mediksystem.managertest.item.EquipmentHistoryItem;
import com.mediksystem.managertest.item.EquipmentItem;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.ViewHolder> {
    private ArrayList<EquipmentItem> equipmentItemArrayList = null ;
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);


    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView id, userId, title, body;

        ViewHolder(View itemView) {
            super(itemView) ;


            // 뷰 객체에 대한 참조. (hold strong reference)
            id = itemView.findViewById(R.id.idListItem) ;
            userId = itemView.findViewById(R.id.userIdListItem) ;
            title = itemView.findViewById(R.id.titleListItem) ;
            body = itemView.findViewById(R.id.bodyListItem) ;


            // 아이템 클릭 이벤트 처리
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        EquipmentItem item = equipmentItemArrayList.get(pos);

                        Log.e("입력된 position : ", String.valueOf(pos));

                        Log.d("해당 id data : ", item.getId());
                        Log.d("해당 user id data : ", item.getUserId());
                        Log.d("해당 title data : ", item.getTitle());
                        Log.d("해당 body data : ", item.getBody());

//                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//                        builder.setTitle(item.getTitle());
//                        builder.setMessage(item.getBody());
//                        builder.show();

                        Context context = itemView.getContext();
                        Intent intent = new Intent(context, EquipmentDetailActivity.class);
                        intent.putExtra("id", item.getId());
                        intent.putExtra("user_id", item.getUserId());
                        intent.putExtra("title", item.getTitle());
                        intent.putExtra("body", item.getBody());
                        context.startActivity(intent);



                    }
                }
            });

            // 길게 눌렀을 때
//            itemView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    int pos = getAdapterPosition();
//                    if (pos != RecyclerView.NO_POSITION) {
//                        EquipmentItem item = equipmentItemArrayList.get(pos);
//
//                        if (mSelectedItems.get(pos, false)) {
//                            mSelectedItems.put(pos, false);
//                            itemView.setBackgroundColor(Color.WHITE);
//                        } else {
//                            mSelectedItems.put(pos, true);
//                            itemView.setBackgroundColor(Color.YELLOW);
//                        }
//
//
//                    }
//                    return true;
//                }
//            });



        }

    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public EquipmentAdapter(ArrayList<EquipmentItem> list) {
        equipmentItemArrayList = list ;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public EquipmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout
                .recyclerview_equipment_item, parent, false) ;
        EquipmentAdapter.ViewHolder vh = new EquipmentAdapter.ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position 에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(EquipmentAdapter.ViewHolder holder, int position) {
        EquipmentItem item = equipmentItemArrayList.get(position) ;
        holder.id.setText(item.getId()) ;
        holder.userId.setText(item.getUserId());
        holder.title.setText(item.getTitle());
        holder.body.setText(item.getBody());

        if (mSelectedItems.get(position, false)) {
            holder.itemView.setBackgroundColor(Color.YELLOW);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return equipmentItemArrayList.size() ;
    }

    public void filterList(ArrayList<EquipmentItem> arrayList) {
        equipmentItemArrayList = arrayList;
        notifyDataSetChanged();
    }


}

