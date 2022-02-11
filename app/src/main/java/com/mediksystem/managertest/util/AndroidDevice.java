package com.mediksystem.managertest.util;

import android.app.Activity;
import android.os.Build;


public enum AndroidDevice {
    OBM_A8(Manufacturer.MANUFACTURER_OBM_A8, "fars82_wet_v113_jb5", 17 ,true, false, true, false){
        @Override
        public BarcodeManager getBarcodeManager(Activity activity) {
            return new ObmA8BarcodeManager(activity);
        }
    };

    private String manufacturer;
    private String model;
    private int version;
    private boolean nfcSupport;
    private boolean nfcAndroidStandard;
    private boolean barcodeSupport;
    private boolean barcodeAndroidStandard;

    AndroidDevice(String manufacturer, String model, int version
            , boolean nfcSupport, boolean nfcAndroidStandard, boolean barcodeSupport, boolean barcodeAndroidStandard){
        this.manufacturer=manufacturer;
        this.model=model;
        this.version=version;
        this.nfcSupport=nfcSupport;
        this.nfcAndroidStandard=nfcAndroidStandard;
        this.barcodeSupport=barcodeSupport;
        this.barcodeAndroidStandard=barcodeAndroidStandard;
    }

    public static AndroidDevice getDevice(){
        AndroidDevice androidDevice;
        switch (Build.MANUFACTURER){
            case Manufacturer.MANUFACTURER_OBM_A8:
                androidDevice=OBM_A8;
                break;
            default: return OBM_A8;
        }
        if(androidDevice.model.equals(Build.MODEL))
            if(androidDevice.version==Build.VERSION.SDK_INT)
                return androidDevice;
        return  OBM_A8;
    }

    public abstract BarcodeManager getBarcodeManager(Activity activity);

    private class Manufacturer{
        private static final String MANUFACTURER_OBM_A8 = "alps";
    }
}

