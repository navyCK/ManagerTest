package com.mediksystem.managertest.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.mediksystem.managertest.R;
import com.mediksystem.managertest.databinding.ActivityBarcodeDetailBinding;
import com.mediksystem.managertest.exception.BarcodeInvalidDetectingException;
import com.mediksystem.managertest.exception.BarcodeNotSupportException;
import com.mediksystem.managertest.util.AbstractNestActivity;
import com.mediksystem.managertest.util.BarcodeActivity;
import com.mediksystem.managertest.util.BarcodeManager;
import com.mediksystem.managertest.util.NestableActivity;
import com.mediksystem.managertest.util.OnBarcodeReadListener;

import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

public class BarcodeSampleActivity extends NestableActivity {
    ActivityBarcodeDetailBinding binding;
    BarcodeActivity barcodeActivity;
    BarcodeSampleContentActivity barcodeSampleContentActivity;
    {
        barcodeActivity = new BarcodeActivity(this);
        barcodeSampleContentActivity = new BarcodeSampleContentActivity();
        pushActivity(barcodeActivity);
        pushActivity(barcodeSampleContentActivity);
    }

    private class BarcodeSampleContentActivity extends AbstractNestActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            binding = DataBindingUtil.setContentView(BarcodeSampleActivity.this, R.layout.activity_barcode_detail);

            binding.barcodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BarcodeManager barcodeManager = barcodeActivity.getBarcodeManager();
                    if (barcodeManager == null) {
                        Toast.makeText(getApplicationContext(), "Barcode가 지원 되지않거나 일시적 오류입니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            barcodeManager.scanBarcode1D(new OnBarcodeReadListener() {
                                @Override
                                public void onBarcodeReadListener(String barcode) {
                                    binding.barcodeText.setText(barcode);
                                    Toast.makeText(getApplicationContext(), "Barcode1D read 성공: "+barcode, Toast.LENGTH_SHORT).show();
                                    setDialog(barcode);
                                }

                                @Override
                                public void onFail() {
                                    Toast.makeText(getApplicationContext(), "Barcode1D read 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (BarcodeInvalidDetectingException e) {
                            Toast.makeText(getApplicationContext(), "이미 바코드 스캔중 입니다.", Toast.LENGTH_SHORT).show();
                        } catch (BarcodeNotSupportException e){
                            Toast.makeText(getApplicationContext(), "1D 바코드 스캔을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getApplicationContext(), "읽기 대기중입니다. 스캔해주세요.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
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

            AlertDialog.Builder builder = new AlertDialog.Builder(BarcodeSampleActivity.this);

            builder.setTitle("장비 ID : " + id);

            builder.setMessage(getTime + "\n작업할 내용을 선택해주세요.");

            builder.setPositiveButton("출고", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), "출고", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("입고", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), "입고", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNeutralButton("조회", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(getApplicationContext(), "조회", Toast.LENGTH_SHORT).show();
                }
            });

            builder.show();
        }
    }


}
