package com.mediksystem.managertest.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediksystem.managertest.R;
import com.mediksystem.managertest.adapter.EquipmentAdapter;
import com.mediksystem.managertest.databinding.ActivityEquipmentBinding;
import com.mediksystem.managertest.dialog.ProgressDialog;
import com.mediksystem.managertest.item.EquipmentItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EquipmentActivity extends AppCompatActivity {
    ActivityEquipmentBinding binding;
    private String str_response;
    JSONObject equipmentObject;
    Map<String, Object> map = null;

    ProgressDialog customProgressDialog;

    RecyclerView recyclerView = null;
    EquipmentAdapter adapter = null;
    ArrayList<EquipmentItem> equipmentItemArrayList = new ArrayList<>();
    ArrayList<EquipmentItem> filterList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_equipment);

        setSupportActionBar(binding.equipmentToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(0);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("장비 목록");

        OkHttpClient client = new OkHttpClient();
        String EQUIPMENT_URL = "https://jsonplaceholder.typicode.com/posts";
        Request request = new Request.Builder().url(EQUIPMENT_URL).build();
        convertJson(client, request);
        setProgressDialog();
        setTimer();

        binding.searchEquipment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchText = binding.searchEquipment.getText().toString();
                searchFilter(searchText);

                if (searchText.equals("")) {
                    recyclerView.scrollToPosition(equipmentItemArrayList.size() - 1);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.equipment_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_barcode:
                Intent intent = new Intent(EquipmentActivity.this, BarcodeSampleActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private void convertJson(OkHttpClient client, Request request) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                str_response = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(str_response);

                    for (int i=0; i<jsonArray.length(); i++) {
                        equipmentObject = jsonArray.getJSONObject(i);
                        map = getMapFromJsonObject(equipmentObject);

                        String id = map.put("id", map).toString();
                        String userId = map.put("userId", map).toString();
                        String title = map.put("title", map).toString();
                        String body = map.put("body", map).toString();

                        addItem(id, userId, title, body);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // json -> map
    public static Map<String, Object> getMapFromJsonObject(JSONObject jsonObj){
        Map<String, Object> objectMap = null;

        try {
            objectMap = new ObjectMapper().readValue(jsonObj.toString(), Map.class);
        } catch (JsonParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JsonMappingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return objectMap;
    }

    private void setTimer() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView = binding.todoRecyclerView;
                recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), 1));
                LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
                manager.setReverseLayout(true);
                manager.setStackFromEnd(true);
                recyclerView.setLayoutManager(manager);

                adapter = new EquipmentAdapter(equipmentItemArrayList);
                recyclerView.setAdapter(adapter);

                customProgressDialog.dismiss();
            }
        }, 2000);
    }

    private void setProgressDialog() {
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        customProgressDialog.setCancelable(false);
        customProgressDialog.show();
    }

    private void addItem(String id, String userId, String title, String body) {
        EquipmentItem item = new EquipmentItem();

        item.setId(id);
        item.setUserId(userId);
        item.setTitle(title);
        item.setBody(body);

        equipmentItemArrayList.add(item);
    }

    public void searchFilter(String searchText) {
        filterList.clear();


        for (int i=0; i < equipmentItemArrayList.size(); i++) {
            if (equipmentItemArrayList.get(i).getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                filterList.add(equipmentItemArrayList.get(i));
            }
        }
        adapter.filterList(filterList);
    }





}