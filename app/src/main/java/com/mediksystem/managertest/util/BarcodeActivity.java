package com.mediksystem.managertest.util;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

public class BarcodeActivity extends AbstractNestActivity{
    private String TAG = "BarcodeActivity";

    private BarcodeManager barcodeManager;

    public BarcodeActivity(Activity activity){
        this.activity=activity;
    }

    public BarcodeManager getBarcodeManager() {
        return barcodeManager;
    }

    @Override
    protected void onInitBeforeOnCreate(Bundle savedInstanceState) {
        AndroidDevice androidDevice = AndroidDevice.getDevice();
        if(androidDevice==null)
            return;
        barcodeManager = androidDevice.getBarcodeManager(activity);
    }

    @Override
    protected boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "onKeyDown: "+keyCode+"  /  "+event);
        if(barcodeManager!=null)
            return barcodeManager.keyDown(keyCode, event);
        return true;
    }

    @Override
    protected void onResume() {
        if(barcodeManager!=null)
            barcodeManager.resume();
    }

    @Override
    protected void onPause() {
        if(barcodeManager!=null)
            barcodeManager.pause();
    }

    @Override
    protected void onDestroy() {
        if(barcodeManager!=null)
            barcodeManager.close();
    }
}

