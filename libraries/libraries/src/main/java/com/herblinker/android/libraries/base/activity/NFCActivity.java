package com.herblinker.android.libraries.base.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.herblinker.android.libraries.base.device.AndroidDevice;
import com.herblinker.android.libraries.base.device.NFCJobs;
import com.herblinker.android.libraries.base.device.NFCManager;
import com.herblinker.android.libraries.base.exception.NFCNotSupportException;

public class NFCActivity extends AbstractNestActivity{
    private NFCManager nfcManager;
    private NFCJobs nfcJobs;

    public NFCActivity(Activity activity, NFCJobs nfcJobs){
        this.activity=activity;
        this.nfcJobs=nfcJobs;
    }

    public NFCManager getNFCManager() {
        return nfcManager;
    }

    @Override
    protected void onInitBeforeOnCreate(Bundle savedInstanceState) {
        AndroidDevice androidDevice = AndroidDevice.getDevice();
        if(androidDevice==null)
            return;
        try{
            nfcManager = androidDevice.getNFCManager(activity, nfcJobs);
        } catch (NFCNotSupportException e){
            Toast.makeText(activity, "NFC 미지원 기기입니다.", Toast.LENGTH_LONG);
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        if(nfcManager==null)
            return;
        Log.e("CHECK-NFC", "nfcManager.onRestart();");
        nfcManager.onRestart();
    }

    @Override
    protected void onResume() {
        if(nfcManager==null)
            return;
        Log.e("CHECK-NFC", "nfcManager.detectResume();");
        nfcManager.detectResume();
    }

    @Override
    protected void onPause() {
        if(nfcManager==null)
            return;
        Log.e("CHECK-NFC", "nfcManager.detectPause();");
        nfcManager.detectPause();
    }

    @Override
    protected void onDestroy() {
        if(nfcManager==null)
            return;
        Log.e("CHECK-NFC", "nfcManager.close();");
        nfcManager.close();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(intent==null)
            return;
        String action = intent.getAction();
        if(action==null)
            return;
        if(action.equals("android.nfc.action.TAG_DISCOVERED")){
            if(nfcManager==null)
                return;
            Log.e("CHECK-NFC", "nfcManager.execute();");
            nfcManager.execute();
        }
    }
}
