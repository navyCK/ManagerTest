package com.herblinker.android.libraries.base.device;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import com.herblinker.android.libraries.base.device.obm_a8.ObmA8BarcodeManager;
import com.herblinker.android.libraries.base.device.obm_a8.ObmA8NFCManager;
import com.herblinker.android.libraries.base.exception.NFCNotSupportException;
import com.herblinker.android.libraries.base.view.compatible.AndroidCamera;
import com.herblinker.android.libraries.base.view.compatible.AndroidCamera1;
import com.herblinker.android.libraries.base.view.compatible.AndroidCamera2;

import androidx.annotation.RequiresApi;

public enum AndroidDevice {
    OBM_A8(Manufacturer.MANUFACTURER_OBM_A8, "fars82_wet_v113_jb5", 17 ,true, false, true, false){
        @Override
        public NFCManager getNFCManager(Activity activity, NFCJobs nfcJobs) throws NFCNotSupportException {
            return new ObmA8NFCManager(activity);
        }

        @Override
        public BarcodeManager getBarcodeManager(Activity activity) {
            return new ObmA8BarcodeManager(activity);
        }

        @Override
        public AndroidCamera getCamera(Context context) {
            return new AndroidCamera1(context, this);
        }

        @Override
        public AndroidCamera.CameraDataFormat getCameraDataFormat() {
            return AndroidCamera.CameraDataFormat.JPEG;
        }
    },
    IP_67(Manufacturer.MANUFACTURER_IP_67, "V1H", 22, true, false, true, false){
        @Override
        public NFCManager getNFCManager(Activity activity, NFCJobs nfcJobs) {
            return null;
        }

        @Override
        public BarcodeManager getBarcodeManager(Activity activity) {
            return null;
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public AndroidCamera getCamera(Context context) {
            return new AndroidCamera2(context, this);
        }

        @Override
        public AndroidCamera.CameraDataFormat getCameraDataFormat() {
            return AndroidCamera.CameraDataFormat.RGBA_8888;
        }
    },
    NV20A20INCH(Manufacturer.MANUFACTURER_NV20A20INCH, "DMTAB-NV20A", 19, true, true, false, false){
        @Override
        public NFCManager getNFCManager(Activity activity, NFCJobs nfcJobs) {
            return null;
        }

        @Override
        public BarcodeManager getBarcodeManager(Activity activity) {
            return null;
        }

        @Override
        public AndroidCamera getCamera(Context context) {
            return new AndroidCamera1(context, this);
        }

        @Override
        public AndroidCamera.CameraDataFormat getCameraDataFormat() {
            return AndroidCamera.CameraDataFormat.JPEG;
        }
    },
    ETC(Manufacturer.MANUFACTURER_ETC, null, 0, false, false, false, false){
        @Override
        public NFCManager getNFCManager(Activity activity, NFCJobs nfcJobs) throws NFCNotSupportException{
            return new AndroidNFCManager(activity, nfcJobs);
        }

        @Override
        public BarcodeManager getBarcodeManager(Activity activity) {
            return null;
        }

        @Override
        public AndroidCamera getCamera(Context context) {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
                return new AndroidCamera2(context, this);
            return new AndroidCamera1(context, this);
        }

        @Override
        public AndroidCamera.CameraDataFormat getCameraDataFormat() {
            return AndroidCamera.CameraDataFormat.JPEG;
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
            case Manufacturer.MANUFACTURER_IP_67:
                androidDevice=IP_67;
                break;
            default: return ETC;
        }
        if(androidDevice.model.equals(Build.MODEL))
            if(androidDevice.version==Build.VERSION.SDK_INT)
                return androidDevice;
        return  ETC;
    }

    public abstract NFCManager getNFCManager(Activity activity, NFCJobs nfcJobs) throws NFCNotSupportException;
    public abstract BarcodeManager getBarcodeManager(Activity activity);
    public abstract AndroidCamera getCamera(Context context);
    public abstract AndroidCamera.CameraDataFormat getCameraDataFormat();

    private class Manufacturer{
        private static final String MANUFACTURER_OBM_A8 = "alps";
        private static final String MANUFACTURER_IP_67 = "V1H";
        private static final String MANUFACTURER_NV20A20INCH = "FUHU";
        private static final String MANUFACTURER_ETC = "NOT_LISTED_ABOVE";
    }
}
