package com.mediksystem.managertest.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;

import com.mediksystem.managertest.exception.BarcodeNotSupportException;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ObmA8BarcodeManager implements BarcodeManager {
    private static final String BROADCAST_START = "Start.Scan.BarCode";
    private static final String BROADCAST_STOP = "Stop.Scan.BarCode";
    private static final String BROADCAST_RECEIVE_DATA = "com.android.scancontext.inputmethod";
    private static final String BROADCAST_RECEIVE_DATA_EXTRA_NAME = "Scan_context_imputmethod";

    private static final int HARDWARE_KEY_BARCODE = 221;
    private Activity activity;
    private volatile OnBarcodeReadListener onBarcodeReadListener;
    private Lock getter;
    //private volatile boolean isScanning;

    private DetectTask detectTask;
    private IntentFilter scanResult;

    private static class DetectTask extends BroadcastReceiver {
        private String TAG = "ObmA8BarcodeManager";
        private ObmA8BarcodeManager barcodeManager;
        private DetectTask(ObmA8BarcodeManager barcodeManager){
            this.barcodeManager=barcodeManager;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive");
            OnBarcodeReadListener onBarcodeReadListener = barcodeManager.onBarcodeReadListener;
            if(onBarcodeReadListener!=null) {
                String result = intent.getStringExtra(BROADCAST_RECEIVE_DATA_EXTRA_NAME);
                String action = intent.getAction();
                if(action!=null)
                    switch (action) {
                        //데이터 읽은결과
                        case BROADCAST_RECEIVE_DATA:
                            if(result==null)
                                onBarcodeReadListener.onFail();
                            else
                                onBarcodeReadListener.onBarcodeReadListener(intent.getStringExtra(BROADCAST_RECEIVE_DATA_EXTRA_NAME));
                            break;
                    }
            }
        }
    }

    public ObmA8BarcodeManager(Activity activity){
        this.activity = activity;
        getter = new ReentrantLock(true);
        detectTask = new DetectTask(this);
        scanResult = new IntentFilter();
        scanResult.addAction(BROADCAST_RECEIVE_DATA);
    }

    @Override
    public boolean keyDown(int keycode, KeyEvent event) {
        if(keycode==HARDWARE_KEY_BARCODE){
            //scanBarcode1D(null);
            return false;
        }
        return true;
    }

    @Override
    public void scanBarcode1D(OnBarcodeReadListener onBarcodeReadListener){
        getter.lock();
        try{
            //if(isScanning)
            //    throw new BarcodeInvalidDetectingException();
            //isScanning=true;
            this.onBarcodeReadListener=onBarcodeReadListener;
            Log.e("CHECK", "scanBarcode1D");
            activity.sendBroadcast(new Intent(BROADCAST_START));
        } finally {
            getter.unlock();
        }
    }

    @Override
    public void scanBarcode2D(OnBarcodeReadListener onBarcodeReadListener) throws BarcodeNotSupportException {
        throw new BarcodeNotSupportException();
    }

    @Override
    public void resume() {
        activity.registerReceiver(detectTask, scanResult);
    }

    @Override
    public void pause() {
        activity.unregisterReceiver(detectTask);
    }

    @Override
    public void cancelBarcode1D() {
        activity.sendBroadcast(new Intent(BROADCAST_STOP));
    }

    @Override
    public void cancelBarcode2D() {

    }

    @Override
    public void close() {

    }
}
