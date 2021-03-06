package com.mediksystem.managertest.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mediksystem.managertest.R;
import com.mediksystem.managertest.adapter.HerbAdapter;
import com.mediksystem.managertest.databinding.ActivityHerbBinding;
import com.mediksystem.managertest.databinding.RecyclerviewHerbPackageItemBinding;
import com.mediksystem.managertest.dialog.ProgressDialog;
import com.mediksystem.managertest.exception.BarcodeInvalidDetectingException;
import com.mediksystem.managertest.exception.BarcodeNotSupportException;
import com.mediksystem.managertest.item.HerbItem;
import com.mediksystem.managertest.item.HerbPackageItem;
import com.mediksystem.managertest.util.AbstractNestActivity;
import com.mediksystem.managertest.util.BarcodeActivity;
import com.mediksystem.managertest.util.BarcodeManager;
import com.mediksystem.managertest.util.NestableActivity;
import com.mediksystem.managertest.util.OnBarcodeReadListener;
import com.mediksystem.managertest.util.OnItemClick;


import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class HerbActivity extends NestableActivity {
    ActivityHerbBinding binding;

//    LayoutInflater inflater;
//    LinearLayout listLayout;
//    View header;
//    TextView textView;

    ProgressDialog customProgressDialog;

    RecyclerView recyclerView = null;
    HerbAdapter adapter = null;
    ArrayList<HerbItem> herbItemArrayList = new ArrayList<>();
    ArrayList<HerbItem> filterList = new ArrayList<>();

    BarcodeActivity barcodeActivity;
    HerbContentActivity herbContentActivity;
    {
        barcodeActivity = new BarcodeActivity(this);
        herbContentActivity = new HerbContentActivity();
        pushActivity(barcodeActivity);
        pushActivity(herbContentActivity);
    }

    private class HerbContentActivity extends AbstractNestActivity {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding = DataBindingUtil.setContentView(HerbActivity.this, R.layout.activity_herb);

            setToolbar("?????? ??????");
            setProgressDialog();
            setAdapter();

            binding.searchHerb.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String searchText = binding.searchHerb.getText().toString();
                    searchFilter(searchText);

                    if (searchText.equals("")) {
                        recyclerView.scrollToPosition(herbItemArrayList.size() - 1);
                    }
                }
            });

            binding.herbRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(HerbActivity.this, HerbRegistrationActivity.class);
                    intent.putExtra("barcode", binding.searchHerb.getText().toString());
                    startActivity(intent);
                }
            });
        }

        @Override
        protected void onStart() {
            super.onStart();


        }

    }




    private void setToolbar(String title) {
        setSupportActionBar(binding.herbToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(0);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(title);
    }

    public void showDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_herb_register, null);
        final EditText editBarcode = alertLayout.findViewById(R.id.herbRegisterBarcode);
        final EditText editName = alertLayout.findViewById(R.id.herbRegisterName);
        final EditText editWeight = alertLayout.findViewById(R.id.herbRegisterWeight);
        final EditText editMemo = alertLayout.findViewById(R.id.herbRegisterMemo);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("?????? ?????? ??????");
        alert.setView(alertLayout);
        alert.setCancelable(true);
        alert.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show();
            }
        });
        alert.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getBaseContext(),
                        "????????? : " + editBarcode.getText().toString() + "\n" +
                            "?????? : " + editName.getText().toString() + "\n" +
                            "?????? : " + editWeight.getText().toString() + "\n" +
                            "???????????? : " + editMemo.getText().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.herb_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_register:
                showDialog();
                break;
            case R.id.menu_barcode:
                BarcodeManager barcodeManager = barcodeActivity.getBarcodeManager();

                if (barcodeManager == null) {
                    Toast.makeText(getApplicationContext(), "Barcode??? ?????? ??????????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        barcodeManager.scanBarcode1D(new OnBarcodeReadListener() {
                            @Override
                            public void onBarcodeReadListener(String barcode) {
                                Toast.makeText(getApplicationContext(), "Barcode1D read ??????: "+barcode, Toast.LENGTH_SHORT).show();
                                setProcessDialog(barcode);
                            }

                            @Override
                            public void onFail() {
                                Toast.makeText(getApplicationContext(), "Barcode1D read ??????", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (BarcodeInvalidDetectingException e) {
                        Toast.makeText(getApplicationContext(), "?????? ????????? ????????? ?????????.", Toast.LENGTH_SHORT).show();
                    } catch (BarcodeNotSupportException e){
                        Toast.makeText(getApplicationContext(), "1D ????????? ????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getApplicationContext(), "?????? ??????????????????. ??????????????????.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private String getNowTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String getTime = format.format(date);
        return getTime;
    }

    private void setProcessDialog(String id) {
        String getTime = getNowTime();

        AlertDialog.Builder builder = new AlertDialog.Builder(HerbActivity.this);

        builder.setTitle("?????? ID : " + id);

        builder.setMessage(getTime + "\n????????? ????????? ??????????????????.");

        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "??????", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "??????", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNeutralButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "??????", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }


    private void setAdapter() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                herbItemArrayList.clear();
                recyclerView = binding.herbRecyclerView;
                recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), 1));
                LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
                manager.setReverseLayout(true);
                manager.setStackFromEnd(true);
                recyclerView.setLayoutManager(manager);

                adapter = new HerbAdapter(herbItemArrayList);
                recyclerView.setAdapter(adapter);


                adapter.addItem("http://img.danawa.com/prod_img/500000/171/893/img/5893171_1.jpg?shrink=330:330&_v=20180213155042",
                        "??????", "834533215", "????????????", "?????????", "???????????? ??????.",
                        30, 365, "?????????", 600, 50, 60,
                        new int[]{500, 600, 1000}, new int[]{10, 5, 1}, new String[]{"8800617000132", "56158156156", "16188661"});
                adapter.addItem("data:image/jpeg;base64,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",
                        "??????", "8813151244156", "????????????", "?????????", "???????????? ??????.",
                        20, 200, "????????????", 700, 60, 80,
                        new int[]{500, 700, 1000}, new int[]{11, 4, 2}, new String[]{"1864864868", "2356158156156", "1426188661"});
                adapter.addItem("data:image/jpeg;base64,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",
                        "??????", "881124156", "????????????", "?????????", "???????????? ??????.",
                        30, 300, "????????????", 1000, 20, 25,
                        new int[]{200, 300, 500}, new int[]{9, 3, 2}, new String[]{"128648", "618156156", "2618661"});
                adapter.addItem("data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAoHCBUVEhgVFRUYGBgaGhgaGBoYGhgcGBgdGBgZGRwYGRkcIy4lHB4tHxgYJjgmKzAxNTU1GiQ7QDszPy40NTEBDAwMEA8QHxISHzQsJSM0NDQ0MTE0NDQ0NDQ0NDQ0NDExNDQxNDQ0NDQ0NDQ0NDQ0NDQ0NDQ/MT80Pz80NDQxMf/AABEIALwBDAMBIgACEQEDEQH/xAAbAAACAwEBAQAAAAAAAAAAAAAABAECBQMGB//EADgQAAICAQIEBAQDBwUAAwAAAAECABEDBCEFEjFBIlFhcRMygZEGQqEjUmKxwdHwFBVy4fEkM4L/xAAYAQEBAQEBAAAAAAAAAAAAAAAAAQIDBP/EACMRAQEBAQADAAICAgMAAAAAAAABAhEDITFBURJhInETMpH/2gAMAwEAAhEDEQA/APs0IQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEIQgEISCYEwnNMgPQ37S8nRMJECZRMJRHBFiXgEIQgEIQgEISIEwhCAQkXC4BCEIEwhCAQhCAQhCAQhCAQhIuBMJFwuAQlGygdTM7NxAk8qDz3Pt1nPXkzlqZtaGZ+VSfIRUBQOZ2Bbvua9gIknMQeZyT5X4R7xfLqgg2PMfM7hfUmcNef38bmGy2ahsAB6kD9POUGrOwoWfI2PvENVrOUY9/C+xaromiPYGLvxBVblZdhdcvQg+hkvlpMdbPxR3cetTjmycy0rCj1O9gd/rM1NegIAQ2dul0fLba52OusDlcC9/l6j0ox/y9X+Fh3BlVWKsQB+W23N9RRjOXUBRe59hc83qzfi5i3betvMjvG2WkTlsrV2CSTt6dt/0iea85C4jQ/wB1TvY9wR1nTDrUY7Eeky9O7N4SW/r6bnpJ1eVcaHxEt+6pG36Szya+9S4nxu80AwnmcWqLAePr5k7bdBHtMT1DVv7j613nSebv4S442oRXG7HyPsZcZvOdJrrHDEiEibQSIQgEtIkiQTCEIEQlC484odcC1LuO57CZ1vOfqzNp6EVxaxWHXf0lMvEsa9WFyXyZn5P409KO9C4oNXzLdUPXv7TE1nGg5KJZ7Guv0mN+aT41nFrYy6prpa+429+8rk04rmbK/vzAAfSYi4coa2ZMa9epL1Xl5+8Y/wB2UDlVXykHcmq/8nGeX9t3H6OHVclD41jp4gP5y2TWBTRYlq6DoL6X5GKLmfq4xY17AeJif7+044tV+0LKD/EzJyKdvXcmZ1u38rMn3xs4tvAp6m2DV73KtnTElKvTz6n3J3MWPEMfPZfmI6gfIO+57fWZ+t1yvys7BEvYA7v6TOtyT+2s4t+mF1mXJzEAUNlA7seu522H84jn1pAZAAzk77ULHcmu07Fi+JVxbda5Qbs2KPlsOs76fENNiLZfG+78qizf9ff1nPl032R1y4myadAWCGlLWD0og0PtIfEnIAqlyBfMW5enqKmDx7V5cq41VuR3IJXmqh5Cu+4msNMSiMzcqItOljdhvzMw3IM3KlnB8RFKs1oR2VuawNyCe1+85Zs6MylOVAeo/MPUAd6imPPj1bnCAeVFLll+TbYIx6+v0j2n4dgOG2RiMZK9SGvv0PTyEnureR2XGK+flroxZb63sBLJxRA5RW5wN2IPy9tqi7JpygYoVH5bDFzfcg7gRdNXiVfAti9wqW32Hf3i3hJ360Med0JJYFDfK3Q12385CadQOZRzk7l3IK2T0oRVdS98oQox3QEbdOh7XuY4iUvKrAb2/lbdth07TU9s306qim2dxQA5goFD28oxhdNgpNdm7/eIppjv18v/AD094ziw11tR6/5tN5ZrTVX2HNY7+dRoIPKKaI2Ls/Ux6p68T04UXC5W4XNotC5EIFpMXyapFNFhfl3ies4kB4UNsfLepz15M5ambT+TOq/MQIhqNfYPKaHnRv6TNd6PMx9/+zMnWcbVRS23lV0P7zy789vqOufE0s2rPME6liBuTuSfIHp9ZicY4oqZFx425VUnnC78zWQQfSW/Duo+JmtuViOYqbAYNW3hO9dYnqRpWcgamnqiqqgN8246Dv8AWcb2zrrJJeO2p4u6gdVvcdd9tio7iaXCNLkf9rqPAnVVPVu/M37o9I3wvTYvhBm5nA3U5QLHL+YCth/OZ/EmzalgiWmMdXJC856e/LEnPdS2X1GhqtUuW/GEx+9X239PadNJQT9kFA7sFYk/UCeY1DabTuFyZHyv8oRBQB8vOPafW5cg5MIdAvzNzJtv0JNx/L37X+Pr0020G5fKxNmgPO/Q9JXLgIAAz/DUHsEJP03ivEdSNPiVSTld3LAOeboOovt0i+m4fmy+PI4RBuUXsPWZtJPTvj4jp8ZqsmfIDYYgcxPTw+U0tNxdH2yIyDsXZD/I2J5PUax2ycmIKgugRuzURvXl7zawfhIuAc2Zl/hWr69S3S5rNt+Gs5k9rcXy6fG3hfloVyLRF/vcombptG+Snx4XY9FfIfuReyj2Fz0OLR6fTfIqFu7OwZz7307zqnEFJ5sjIp/LuaHqb/tLczvtJrXPTljrS4+UFS5G5P1P2smIaXx5mLEOzL4gGrYG6I6171FdTws5HLtqgwLX4UZtvKwdugjmDNpkTkDkFjuwHjf084v6P7Nvlxgf/WHaiPCK28uY71OeB2KFsiDHzAqEBulI+du30iGt1GLSguiu56gGz63XntEMPEdTndQMFoaYuSVBB6g2Nqjq8M6N1I+HgsILD5DZZgOp285rva6UKoYM7CidjVgBiDv0jCBNOoYimvZfWv8AqLajGdQQzORTDlC7bjfe+ssnImr2snOjh/GeWiGUsbvegCPOcGTL8dvh4ncEix0pT+6+wIvtc19TxXTLkLMQz/KO4PLsaHnOWXjiuBWZUBGyFSGr0Mzyflf5X8RCaQovPmzOg6cpcFvY1sfYTrotTi64w7cwNM2ytXXYRVExruxDg+Ih7Ymz1HevcTRw6xQFCIgUChyj5PQTWfd9Jo8hegxRV8ySfsD1M7ZEDnpY263/ACieDEzNZJPvNPHpfNjO+c2uWqviYjblNfSNhpzXCP8ACZ35Z6JOOVqlwE5Z8nKpbrUxtXxnk2LUfIRrcyuc2tnU6pEHiPXoPOYmu4q374G3yr6Hu0ytViyahgSCij87bH6L1MU4jkTEDytzPRok0Bsavz69J5N+XWnfPjkabM1W55Ad+1ke3U+5nLV8RTHj8O+389rJmGPxWgxqGpslU3X267Tz+r17ZiQpBPZQRv7+c4WOkjT13G+duQPyr09PYmKO+QEMArC9jzAj63Oei/DuoerUKD0LVv8Ac/0m/wAK4KMAdnyBmNKSg2S99iehNDcRyRv/AEtp9cw+A+RBj5XXxGgHLeEUOpsEia68KRszl1PKr7E7Xe9BurLvI0qJkYJjQGq58htm26W53uMcU1btgdcI5sijddgxA2sXLLLHOltXxFHyDEWpF3atuauigeX9pm8c4pzOq4iAN7+gNfrMDTaDUOxLAk+SmkQ/xP3PoLnq+E8BZOvjNgltqH8IuX2ckV03C0zudRy/tWRbY2aoAFgDsDO2NSzjT4Ry4k3yONzfdfVzNTVarFp1CsbJukXqT1sntOeFnyY7VlxJ2CUfu9VftM8/Z0hrseRsnOUTGqildyLRB+6o3v7SM2swrgOK3yCgXYHl5v8Akeqj0EnUf6NN8rnI3luwJ9htKjjiBf2WEf8A6rlH2EHFeGIQebFiCHb5V5ievV2uo/qceRuUM4QX4rcbj2Heef1fHXY0zEWaCpYv0FbzqicmRLJ5yRSKAxv+IsaHeSXjdy3F0WmVgWyKd+5Fek7ZNDjducBMhFBQzUoPoBPI/iDVhGBXTK7kkVzMEHqQD+krodXq3JCadK2ulBr9f1mmf43n16TLptSTRKIl0FQXY/T7mXGjdWAQoD3LE838oto9FnC3kxgN2HMa+o5qjuj0mNAXYoHY25UbfdjLzqd4S1HDs77HUKu5PgBsffrHcJXCvLzl37s5vfua7AeUR1/EkB5VyFze1A19xOOn1CZeZcgK10W1JdR1FDpcvOHvjhr9U7ozoC5F0w3AN1QXz9ek78A4c4Q58vMX5XGJHJ8JYVzV0F9I42vwYvCqhemw8R/rEdRq8uQgISqgnciy47X5SydS30UHBQlPndE8wgJY9zv3NntGMXDUdw6LkNACmFBvUk9Jo6DhLMeZzv8A55z0eDCFFCdceHv1z15OMrT8H5h4+UeiijXkW6mauDRIgoKIwonQLO+cTPxzurVESdAsAJYCbkZ6AJepAk3NIVyqCpB6EVMXJj0+Al2tn8zuR7eU2WnleK6fK55UXvRJoCvOz/ScPNeTsjpidY/GeMlEJQ8w3G5nkv8AUPnyBRdk1/xnrH/DILc2TKFX91P7npD4Wm0yMMKUxG7sbY/U9p469Usnxnaf8GBPHlyCidwt7+hY9Paa2l0enwg8iCx0J3MQ/wB0fOvIi2goUlkkjeyxFCK/7brMrFQqoo68zj+SkkzOu0/2f1vFUT89v5eVb/b+8rwXU6jU5KxMVUfO+xUD90A7E+knT/hNBT58hygdEQcgP1u6vvHtbxjFpsZRWVFHRRVLtuBXzNGcyFvfUbWt1q4U5MdX3bpvVEmYWm1QVwwJd+a1C7WT6Dt7zC0nEtRrMnJpsYKjZnceBQe59Z6LPqU0SLjVVfOwpnCBevYAb1c1YzznptaoIMTZGQuFAZkH5a3seYiGl46z4jmVRyi1xoOpra9uu8T0vCcuL/5GbOuIk2QSWLA7hWB289pp8P1+F1Y4AOYflNqD3PL5CLCf+sVMbJefWKpcm0QbvX8VfTaGdsmoFs3IgF1VBR5ek3FzajclcKH6k/epQ4czjZwT581D7AbyWQlYOn0HxVLueTCOjHZsnqv7q+vUzmuoTKx0+JH5R15KAUebk7119TGtToNeX5D8J0q/HQr6dbHpNvhejxYcbLsl7vV7/VtzJxr+TNHDk06BsGE5MzbIRfN52WbZR6nyjP4e4O6O2fUMpyHZQptMY70fzMfOd8/F2duXChI6A1Q+/aLOhsfGy82/yL/1uZZOM22wxnGlOQtyfEcHqbaqHYDapTPxFx4VUij4VAC39B2nVmVF3rGoG7EAX7Dz3mTkznMHGLmQWAPylgdiSx69ppIs/GHtlrxsdyvbzFG7iTnIU/aUzsSeRqKqK6ED77+Udw6NMa+NwD0oGyfr1/SNac4WWuRvUjmH63cn+VX/ABhHT4sYFOip7bf+RzS59NivkUEnyBJPuY1p+F4CQfg9t7Z7vsQSekaw8NQHbGB5b3NZxfzGbqUiderEcmFifoP6TSwPlIFYSB7qJo4NMB+UD0jKrO+fF325XZbGjmuYBfS7P6RpRJ5ZPLO+c8c7erASblQssBNIkNJBkVIgXuWlBLyhRphcZyMNwam+4nnvxC5UdLsTl5f+rePrzGoyM4YF97peUbD1JP8ASI6bgwyZCMrsUC7iyCe3Qdo1h0bud2KIOpP9B3M75tYmIcuJSTXXdmY9v1ni49Xz4rn1b415Ai4wopQu3KDfX+Kq+8RxcYK7ImR2Nc3KAR9SdhGDoFI+Jnd3ZqIU7AenKN/vLadHc7DlQeEMar226mYqxQZ8zqVI+FZ9GY+ft+snhn4OTJkL5SWANtZNdegmvpdDbCrO9s3YDyB85sZsTsgTHSqAbJ63/aXPU1efDHDcOJARjCKi7AL0uvmM8hpuGHLq3yO1hWNEHpRsC/aa+i0TIhDOXtro7AC/5CXbRgKVDcvMd6rv5S6ts4zPVYH4i1qDIAW52qls2Bv/AJvPN5MmoDKVD8zE8qILYm628ht1M9W3AdMmRWclmIJPM/Ugjeh2mhqOP6fTgkcgNUD1LEdh3Jkk/bfefHP8MY9b8Nv9UaJI5AwXYVuNtyfeNf6jHiYkqvqQzUPZW2v0E85x38SZkxhgeXm2RT8xv0mN+H9LqNRk5jzOfXot/oJeflOft67W8cD38PKoejyo/hHsK/7mNptLq8jXldAT0CUx+lf1m1pvwtpsbfE1B53BAIvw2fIHrU7avjeFDWFBS7E7Ab+p/pLYnZPjph4LzLTuyjy5jfrYBofrLtmw6ZSuMBfMk2Sa7k7zDz8fyZPlUADvuL/qf0iuPSu7W25/zpLnKW/tpuy5m5y6uR0DNyqvsvWdM+BiLbIu/RUMNLwQkdZr6Tg4Ub7zrnxsXcZWj0C2DU39NgUDpGMeiAjK6YTrnHHK665qq+U7Y2Fy3wBJTFRudJEdVlhIUSwE0ykSakCXgRUmFwgVhLQlAJaQBJgcXWJ6zHzCqmgwnF1ks6srzmo4WDuf+pgrifHkLMNqIFfzAntdShIqZGvwKgsiyTV+U4eTE47Z1esV+FK7c+TIQp3Cp8247k9JOp41psI5Qq2o2DEbV6ec58S1v7IhCB0vfxbTyGpwO1tVm78/tPNI6/7exw8TysDl5By1aqt7igQSOxltBxHVZwXUBBZrmvcDa68py4fjyHT4mqncgUbNooosR22uba50VCqiuWrvpQHSZ7yreceI/EOu1OBb5xyk0DVizdj0mPo+N5y1u5YHoDsfcVN5GbLkzKabCyspsWecnwso7Ue/0kcO4QiMoQF39RsPX0ElvI1n+1dRkVcZyZuYEihzNsL9Ks+0R/CvCGz5vjOh+Gm2NCN3brZE9Fk4NjOTm1eSwASqr4VFdrbvVSX/ABKiFcWmQegUHr6n6RL6S/0ZfgGN8nxtUygAeFL2Ud7jg1q0BgQKu4Vq5VP/ABA6+8z9Lw5shOXUsb/LjvwC+hI/NDjPFQoCIRzDuO3t6x9+M1x1mfxEO3Mw+Zr6DyUdjFtUEegt0Oo8z6mTg0FjmCnr/wB3NLR8PurE64z36xrUhXSaIsek39FoAO0Z0ukAmhjxz0Zzxx1rquLFU7qksqy1TrxgKJ0AlBLAyquBJqAMmEQBLVC4XAJaVuFyi0JW5aASQJAlhAAJMJMCCJzdZ1lWEBNxEtdgDrR+k03WK5EmbGpXkNZoFB6AmcNJwwFgTVX09POeg1OntpxzLyA0Oor1nDeeTsds6L5dWnMR0VfAD2oDtPP8Y1LKCmO3LDw+57X5RvigGPGBdtuxI7MdyZl8Dw/EzDmPiPic/uoOw9Tt955PtdZHbhPB89W7qiVvy7s3pZG3vNHVa1cQ+HgUFj0A+Yn1bqZPHuIqAUTYAbH60B+v6RbhWkCYzmT9plIokkWB5RD3Sp/D+oyuHzuN9+WyQt+k19Lw7TabmYC3bqxNm/ft7CIa3iOQZmVWsbAAWKHe673F20Luw528PXb+39THKV31fFnyMcaGtt2N1Xv0EppdMaoDnrdmIBXp2Y+/lOmTSqlBnsdkFGv88zHNJjd/Ag5E/X7zec2/GNWQxoMJY8poVXQTbwaYCcuH6Dk73dTTRJ6sY5Hn1rtRjSp2VYKJ0AnRlAEvywAlgJpVeWHLL1CoRFQEmoVAiSIVJqURJk1JqBWpIEmpYCBAloSYBCEIBCEIHNxF8ixszk6wM98czterCqFjr2mw6RPWL4Zz1nsbzXj+LY3dqUbdTfp2nXSYBgxsWoMws31rymly9T37TN1WjL319Se9dvaeXXj/AE9E0yU4eXPxGBCXzLf5zVA12UDp9YNqnUBEpdyznvfko/vNEadlHKCa8u3tJw8NsjaM+L9l2w10zsSdxvfvHtPw5z1s35mep0/DgB0j+LSqO07TxOd8jC0PBh1IAm9ptKFFARhMc7Kk65zI5XVqqpLqssqywWaZAEuFkqskCAKJNSahUcEwhCoBCFSalESQJIEmoFak1JqTAiEmEAhCEAhCEAhCEAlWEtIMDg6xbLiuOtOTScVkvpd95R9KCKE1XUSoQTPF6xRoBfSN49IB2mhyCSBEhaXXFOq452US4muI4hJcJOokwjmElgsvLQKBZIEtCURUKloQK1CpaECtSakwgRUmEIBCEIBCEIBCEIBCEIH/2Q==",
                        "??????", "81281124156", "????????????", "?????????", "???????????? ??????.",
                        50, 500, "????????????", 900, 30, 45,
                        new int[]{400, 600, 900}, new int[]{9, 3, 2}, new String[]{"12128648", "5618156156", "12618661"});
                adapter.addItem("data:image/jpeg;base64,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",
                        "????????????", "8800617000132", "????????????", "?????????", "???????????? ??????.",
                        40, 200, "????????????", 1200, 20, 45,
                        new int[]{300, 500, 0}, new int[]{5, 4, 0}, new String[]{"286142548", "15615456", "-"});


                adapter.addItem("data:image/jpeg;base64,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",
                        "????????? 1", "1100617000132", "???????????????", "?????????", "???????????? ??????.",
                        50, 200, "????????????", 3200, 20, 45,
                        new int[]{100, 500, 800}, new int[]{6, 2, 3}, new String[]{"286142548", "15615456", "51415387"});
                adapter.addItem("data:image/jpeg;base64,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",
                        "????????? 2", "3800617000132", "???????????????", "?????????", "???????????? ??????.",
                        40, 200, "????????????", 1200, 20, 45,
                        new int[]{500, 600, 1000}, new int[]{21, 3, 0}, new String[]{"24242548", "744635456", "56128586"});




                customProgressDialog.dismiss();
            }
        }, 200);
    }

    private void setProgressDialog() {
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        customProgressDialog.setCancelable(false);
        customProgressDialog.show();
    }


    public void searchFilter(String searchText) {
        filterList.clear();

//        inflater = getLayoutInflater();
//        header = inflater.inflate(R.layout.recyclerview_herb_package_item, null);
//        listLayout = header.findViewById(R.id.listLayout);
//        textView = header.findViewById(R.id.herbName);



        // TODO ?????? 1 : ????????? 2??? ??????, 4??? ????????? ??? ?????? ?(????????? ????????? ????????? 3?????? ???)
        // TODO ?????? 2 : ?????? ??? HighLight(?????? ??????????????? ?????? listview item ?????? ??????)
        // TODO ?????? 3 : ?????? ??? gone -> visible ???????????????



        for (int i=0; i < herbItemArrayList.size(); i++) {
            Log.e(i+"??????", Arrays.toString(herbItemArrayList.get(i).getPackage_barcode()));

            Log.e(i+"??? ??????", String.valueOf(herbItemArrayList.get(i).getPackage_barcode().length));

            if (herbItemArrayList.get(i).getName().toLowerCase().contains(searchText.toLowerCase())
                    || herbItemArrayList.get(i).getBarcode().contains(searchText)

                    || herbItemArrayList.get(i).getPackage_barcode()[0].contains(searchText)
                    || herbItemArrayList.get(i).getPackage_barcode()[1].contains(searchText)
                    || herbItemArrayList.get(i).getPackage_barcode()[2].contains(searchText)

//                    || Arrays.asList(herbItemArrayList.get(i).getPackage_barcode()).contains(searchText)


                    ) {

                filterList.add(herbItemArrayList.get(i));





//                textView.setBackgroundColor(Color.parseColor("#FAFAD2"));
//                binding.herbRegister.setVisibility(View.GONE);
            } else {
//                textView.setBackgroundColor(Color.parseColor("#000000"));
//                binding.herbRegister.setVisibility(View.VISIBLE);
            }
        }
        adapter.filterList(filterList);
    }

    // ????????? ?????????
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            Rect rect = new Rect();
            focusView.getGlobalVisibleRect(rect);
            int x = (int) ev.getX(), y = (int) ev.getY();
            if (!rect.contains(x, y)) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
                focusView.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }


}
