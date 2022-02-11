package com.herblinker.android.libraries.base.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;

import com.herblinker.android.libraries.base.util.DeviceHelper;

public abstract class NFCReceiver extends BroadcastReceiver {
    public NFCReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR2){
            String action = intent.getAction();
            if(action.equals(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED)){
                int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE,
                        NfcAdapter.STATE_OFF);
                switch (state) {
                    case NfcAdapter.STATE_OFF:
                        onStateOff();
                        break;
                    case NfcAdapter.STATE_TURNING_OFF:
                        onStateTurningOff();
                        break;
                    case NfcAdapter.STATE_ON:
                        onStateOn();
                        break;
                    case NfcAdapter.STATE_TURNING_ON:
                        onStateTurningOn();
                        break;
                }
            }
        } else {
            DeviceHelper.Device device = DeviceHelper.getDevice();
            if(device==null){
                //호환 기종 없음
            } else {

            }
        }
    }

    /**
     * 설정에서 NFC 사용이 꺼지는 중일 때
     */
    public abstract void onStateTurningOff();
    /**
     * 설정에서 NFC 사용이 꺼졌을때
     */
    public abstract void onStateOff();
    /**
     * 설정에서 NFC 사용이 켜는 중일 때
     */
    public abstract void onStateTurningOn();
    /**
     * 설정에서 NFC 사용이 켜졌을때
     */
    public abstract void onStateOn();
}
