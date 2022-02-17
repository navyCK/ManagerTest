package com.mediksystem.managertest.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.mediksystem.managertest.R;
import com.mediksystem.managertest.databinding.ActivityHerbDetailBinding;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

public class HerbDetailActivity extends AppCompatActivity {
    ActivityHerbDetailBinding binding;

    String image, barcode, name, company, country_of_origin, storage_location, memo;
    int storage_period, expiration, total_weight_of_inventory_storage;
    double purchase_price, sales_price;

    int herb_value = 0, result = 0;

    String history_type="", history_weight="", history_date="";

    boolean isUpdated = false;

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
        memo = intent.getStringExtra("memo");
        storage_location = intent.getStringExtra("storage_location");
        storage_period = intent.getIntExtra("storage_period", 0);
        expiration = intent.getIntExtra("expiration", 0);
        total_weight_of_inventory_storage = intent.getIntExtra("total_weight_of_inventory_storage", 0);
        purchase_price = intent.getDoubleExtra("purchase_price", 0);
        sales_price = intent.getDoubleExtra("sales_price", 0);

        result = total_weight_of_inventory_storage;

        setToolbar(name);

        Glide.with(this)
                .load(image)
                .placeholder(R.drawable.icon_herbal)
                .error(R.drawable.icon_herbal)
                .into(binding.herbDetailImage);

        binding.herbDetailBarcode.setText(barcode);
        binding.herbDetailName.setText(name);
        binding.herbDetailTotalWeight.setText(String.valueOf(total_weight_of_inventory_storage));
        binding.herbDetailExpiration.setText(String.valueOf(expiration));
        binding.herbDetailStoragePeriod.setText(String.valueOf(storage_period));
        binding.herbDetailPurchasePrice.setText(String.valueOf(purchase_price));
        binding.herbDetailSalesPrice.setText(String.valueOf(sales_price));
        binding.herbDetailSpecialNote.setText(String.valueOf(memo));

        binding.herbInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                setDialog("입고");
                showDialog("입고");
            }
        });
        binding.herbOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                setDialog("출고");
                showDialog("출고");
            }
        });
        binding.herbDetailImageApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Glide.with(HerbDetailActivity.this)
                        .load(binding.herbDetailImageEdit.getText().toString())
                        .placeholder(R.drawable.icon_herbal)
                        .error(R.drawable.icon_herbal)
                        .into(binding.herbDetailUpdateImage);
            }
        });
        binding.herbDetailUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO 약재 수정
                isUpdated = true;
                binding.herbDetailScroll.smoothScrollTo(0,0);

                binding.herbDetailUpDelLayout.setVisibility(View.GONE);
                binding.herbDetailUpdateConfirmLayout.setVisibility(View.VISIBLE);

                binding.herbDetailImage.setVisibility(View.GONE);
                binding.herbDetailImageTextLayout.setVisibility(View.VISIBLE);
                binding.herbDetailImageEdit.setText(image);
                Glide.with(HerbDetailActivity.this)
                        .load(image)
                        .placeholder(R.drawable.icon_herbal)
                        .error(R.drawable.icon_herbal)
                        .into(binding.herbDetailUpdateImage);

                binding.herbDetailInOutLayout.setVisibility(View.GONE);
                binding.herbHistory.setVisibility(View.GONE);

                binding.herbDetailBarcode.setVisibility(View.GONE);
                binding.herbDetailBarcodeEdit.setVisibility(View.VISIBLE);
                binding.herbDetailBarcodeEdit.setText(binding.herbDetailBarcode.getText().toString());

                binding.herbDetailName.setVisibility(View.GONE);
                binding.herbDetailNameEdit.setVisibility(View.VISIBLE);
                binding.herbDetailNameEdit.setText(binding.herbDetailName.getText().toString());

                binding.herbDetailTotalWeightTextLayout.setVisibility(View.GONE);
                binding.herbDetailTotalWeightEditLayout.setVisibility(View.VISIBLE);
                binding.herbDetailTotalWeightEdit.setText(binding.herbDetailTotalWeight.getText().toString());

                binding.herbDetailExpirationTextLayout.setVisibility(View.GONE);
                binding.herbDetailExpirationEditLayout.setVisibility(View.VISIBLE);
                binding.herbDetailExpirationEdit.setText(binding.herbDetailExpiration.getText().toString());

                binding.herbDetailStoragePeriodTextLayout.setVisibility(View.GONE);
                binding.herbDetailStoragePeriodEditLayout.setVisibility(View.VISIBLE);
                binding.herbDetailStoragePeriodEdit.setText(binding.herbDetailStoragePeriod.getText().toString());

                binding.herbDetailPurchasePriceTextLayout.setVisibility(View.GONE);
                binding.herbDetailPurchasePriceEditLayout.setVisibility(View.VISIBLE);
                binding.herbDetailPurchasePriceEdit.setText(binding.herbDetailPurchasePrice.getText().toString());

                binding.herbDetailSalesPriceTextLayout.setVisibility(View.GONE);
                binding.herbDetailSalesPriceEditLayout.setVisibility(View.VISIBLE);
                binding.herbDetailSalesPriceEdit.setText(binding.herbDetailSalesPrice.getText().toString());

                binding.herbDetailSpecialNote.setVisibility(View.GONE);
                binding.herbDetailSpecialNoteEdit.setVisibility(View.VISIBLE);
                binding.herbDetailSpecialNoteEdit.setText(binding.herbDetailSpecialNote.getText().toString());

            }
        });
        binding.herbDetailDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO 약재 삭제
                AlertDialog.Builder dialog = new AlertDialog.Builder(HerbDetailActivity.this);
                dialog.setMessage("약재를 삭제하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                Toast.makeText(HerbDetailActivity.this, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(HerbDetailActivity.this, "취소", Toast.LENGTH_SHORT).show();
                            }
                        }).create().show();

            }
        });
        binding.herbDetailUpdateConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO 약재 수정 완료 버튼
                isUpdated = false;
                binding.herbDetailScroll.smoothScrollTo(0,0);

                binding.herbDetailUpDelLayout.setVisibility(View.VISIBLE);
                binding.herbDetailUpdateConfirmLayout.setVisibility(View.GONE);

                binding.herbDetailImage.setVisibility(View.VISIBLE);
                binding.herbDetailImageTextLayout.setVisibility(View.GONE);

                binding.herbDetailInOutLayout.setVisibility(View.VISIBLE);
                binding.herbHistory.setVisibility(View.VISIBLE);

                binding.herbDetailBarcode.setVisibility(View.VISIBLE);
                binding.herbDetailBarcodeEdit.setVisibility(View.GONE);

                binding.herbDetailName.setVisibility(View.VISIBLE);
                binding.herbDetailNameEdit.setVisibility(View.GONE);

                binding.herbDetailTotalWeightTextLayout.setVisibility(View.VISIBLE);
                binding.herbDetailTotalWeightEditLayout.setVisibility(View.GONE);

                binding.herbDetailExpirationTextLayout.setVisibility(View.VISIBLE);
                binding.herbDetailExpirationEditLayout.setVisibility(View.GONE);

                binding.herbDetailStoragePeriodTextLayout.setVisibility(View.VISIBLE);
                binding.herbDetailStoragePeriodEditLayout.setVisibility(View.GONE);

                binding.herbDetailPurchasePriceTextLayout.setVisibility(View.VISIBLE);
                binding.herbDetailPurchasePriceEditLayout.setVisibility(View.GONE);

                binding.herbDetailSalesPriceTextLayout.setVisibility(View.VISIBLE);
                binding.herbDetailSalesPriceEditLayout.setVisibility(View.GONE);

                binding.herbDetailSpecialNote.setVisibility(View.VISIBLE);
                binding.herbDetailSpecialNoteEdit.setVisibility(View.GONE);

                setToolbar(binding.herbDetailNameEdit.getText().toString());
                Glide.with(HerbDetailActivity.this)
                        .load(binding.herbDetailImageEdit.getText().toString())
                        .placeholder(R.drawable.icon_herbal)
                        .error(R.drawable.icon_herbal)
                        .into(binding.herbDetailImage);

                binding.herbDetailBarcode.setText(binding.herbDetailBarcodeEdit.getText().toString());
                binding.herbDetailName.setText(binding.herbDetailNameEdit.getText().toString());
                binding.herbDetailTotalWeight.setText(binding.herbDetailTotalWeightEdit.getText().toString());
                binding.herbDetailExpiration.setText(binding.herbDetailExpirationEdit.getText().toString());
                binding.herbDetailStoragePeriod.setText(binding.herbDetailStoragePeriodEdit.getText().toString());
                binding.herbDetailPurchasePrice.setText(binding.herbDetailPurchasePriceEdit.getText().toString());
                binding.herbDetailSalesPrice.setText(binding.herbDetailSalesPriceEdit.getText().toString());
                binding.herbDetailSpecialNote.setText(binding.herbDetailSpecialNoteEdit.getText().toString());

            Toast.makeText(getApplicationContext(), "수정되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        binding.herbDetailCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO 약재 수정 취소 버튼
                isUpdated = false;
                binding.herbDetailScroll.smoothScrollTo(0,0);

                binding.herbDetailUpDelLayout.setVisibility(View.VISIBLE);
                binding.herbDetailUpdateConfirmLayout.setVisibility(View.GONE);

                binding.herbDetailImage.setVisibility(View.VISIBLE);
                binding.herbDetailImageTextLayout.setVisibility(View.GONE);

                binding.herbDetailInOutLayout.setVisibility(View.VISIBLE);
                binding.herbHistory.setVisibility(View.VISIBLE);

                binding.herbDetailBarcode.setVisibility(View.VISIBLE);
                binding.herbDetailBarcodeEdit.setVisibility(View.GONE);

                binding.herbDetailName.setVisibility(View.VISIBLE);
                binding.herbDetailNameEdit.setVisibility(View.GONE);

                binding.herbDetailTotalWeightTextLayout.setVisibility(View.VISIBLE);
                binding.herbDetailTotalWeightEditLayout.setVisibility(View.GONE);

                binding.herbDetailExpirationTextLayout.setVisibility(View.VISIBLE);
                binding.herbDetailExpirationEditLayout.setVisibility(View.GONE);

                binding.herbDetailStoragePeriodTextLayout.setVisibility(View.VISIBLE);
                binding.herbDetailStoragePeriodEditLayout.setVisibility(View.GONE);

                binding.herbDetailPurchasePriceTextLayout.setVisibility(View.VISIBLE);
                binding.herbDetailPurchasePriceEditLayout.setVisibility(View.GONE);

                binding.herbDetailSalesPriceTextLayout.setVisibility(View.VISIBLE);
                binding.herbDetailSalesPriceEditLayout.setVisibility(View.GONE);

                binding.herbDetailSpecialNote.setVisibility(View.VISIBLE);
                binding.herbDetailSpecialNoteEdit.setVisibility(View.GONE);
            }
        });
        binding.herbDetailImageEditClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.herbDetailImageEdit.setText("");
            }
        });

        binding.herbHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "입/출고 이력", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HerbDetailActivity.this, HerbHistoryActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("type", history_type);
                intent.putExtra("weight", history_weight);
                intent.putExtra("date", history_date);

                if (history_type != null) {
                    Log.e("name", name);
                    Log.e("type", history_type);
                    Log.e("weight", history_weight);
                    Log.e("date", history_date);
                }

                startActivity(intent);
            }
        });
    }

    private void setToolbar(String title) {
        setSupportActionBar(binding.herbDetailToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(0);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(title);
    }

    @NonNull
    private String getNowTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String getTime = format.format(date);
        return getTime;
    }

    private void setDialog(String id) {
        String getTime = getNowTime();
        EditText editText = new EditText(getApplicationContext());
        editText.setHint("중량을 입력하세요.");
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder builder = new AlertDialog.Builder(HerbDetailActivity.this, R.style.MyAlertDialogStyle);
        builder.setTitle(id);
        builder.setMessage(getTime);
        builder.setView(editText);

        builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton("등록", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                herb_value = Integer.parseInt(editText.getText().toString());

                if (id.equals("입고")) {
                    result += herb_value;
                    history_type += "입고" + "\n";
                    //                    herb_history.append("입고" + "\t\t" + editText.getText().toString() + "\t\t" + getTime + "\n");
                } else {
                    result -= herb_value;
                    history_type += "출고" + "\n";
                    //                    herb_history.append("출고" + "\t\t" + editText.getText().toString() + "\t\t" + getTime + "\n");
                }
                history_weight += editText.getText().toString() + "\n";
                history_date += getTime + "\n";
                binding.herbDetailTotalWeight.setText(String.valueOf(result));
                Toast.makeText(getApplicationContext(), "등록되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

    public void showDialog(String selectType) {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.dialog_custom, null);
        final EditText herbWeight = alertLayout.findViewById(R.id.etHerbWeight);
        final Spinner herbInType = alertLayout.findViewById(R.id.spHerbInType);
        final Spinner herbOutType = alertLayout.findViewById(R.id.spHerbOutType);
        final TextView herbDate = alertLayout.findViewById(R.id.txHerbDate);
        final LinearLayout inLayout = alertLayout.findViewById(R.id.herbInLayout);
        final LinearLayout outLayout = alertLayout.findViewById(R.id.herbOutLayout);

        Log.e("selectType", selectType);
        if (selectType.equals("입고")) {
            inLayout.setVisibility(View.VISIBLE);
            outLayout.setVisibility(View.GONE);
        } else {
            inLayout.setVisibility(View.GONE);
            outLayout.setVisibility(View.VISIBLE);
        }

        String getTime = getNowTime();
        herbDate.setText(getTime);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(selectType);
        alert.setView(alertLayout);
        alert.setCancelable(true);
        alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getBaseContext(), "Cancel clicked", Toast.LENGTH_SHORT).show();
            }
        });

        alert.setPositiveButton("등록", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (herbWeight.getText().toString().equals("")) {
                    Toast.makeText(getBaseContext(), "중량이 입력되지 않았습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    herb_value = Integer.parseInt(herbWeight.getText().toString());

                    String weight = herbWeight.getText().toString();
                    String type;

                    if (selectType.equals("입고")) {
                        type = String.valueOf(herbInType.getSelectedItem());
                        result += herb_value;
                        history_type += "입고" + "(" + type + ")" + "\n";
                    } else {
                        type = String.valueOf(herbOutType.getSelectedItem());
                        result -= herb_value;
                        history_type += "출고" + "(" + type + ")" + "\n";
                    }
                    history_weight += herbWeight.getText().toString() + "\n";
                    history_date += getTime + "\n";
                    binding.herbDetailTotalWeight.setText(String.valueOf(result));
                    Toast.makeText(getBaseContext(), "중량: " + weight + "\n구분: " + type, Toast.LENGTH_SHORT).show();
                }
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    // 키보드 내리기
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

    @Override
    public void onBackPressed() {
        if (isUpdated) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(HerbDetailActivity.this);
            dialog.setMessage("수정을 취소하시겠습니까?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(HerbDetailActivity.this, "취소", Toast.LENGTH_SHORT).show();
                        }
                    }).create().show();
        } else {
            super.onBackPressed();
        }

    }
}