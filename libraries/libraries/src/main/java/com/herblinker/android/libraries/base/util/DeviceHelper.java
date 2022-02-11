package com.herblinker.android.libraries.base.util;

import android.os.Build;

public class DeviceHelper {
    public static int NONE = 0;
    public static int NATIVE_SUPPORT = 1;
    public static int ANDROID_SUPPRT = 2;
    public enum Device{
        OBM_A8_SDK_17("alps", "fars82_wet_v113_jb5", 17, NATIVE_SUPPORT, NATIVE_SUPPORT, ANDROID_SUPPRT, ANDROID_SUPPRT),
        IP_68_SDK_22("V1H", "V1H", 22, ANDROID_SUPPRT, NATIVE_SUPPORT, ANDROID_SUPPRT, ANDROID_SUPPRT);
        public String manufacturer;
        public String model;
        public int sdk;
        public int nfcSupport;
        public int barcodeSupport;
        public int frontCameraSupport;
        public int rearCameraSupport;
        Device(String manufacturer, String model, int sdk, int nfcSupport, int barcodeSupport, int frontCameraSupport, int rearCameraSupport){
            this.manufacturer=manufacturer;
            this.model=model;
            this.sdk=sdk;
            this.nfcSupport=nfcSupport;
            this.barcodeSupport=barcodeSupport;
            this.frontCameraSupport=frontCameraSupport;
            this.rearCameraSupport=rearCameraSupport;
        }
    }
    public static Device getDevice(){
        Device device = null;
        int jobNumber = 0;
        while(true){
            switch (jobNumber){
                case 0:
                    //MANUFACTURER 검사
                    if(Device.OBM_A8_SDK_17.manufacturer.equals(Build.MANUFACTURER))
                        device = Device.OBM_A8_SDK_17;
                    else if(Device.IP_68_SDK_22.manufacturer.equals(Build.MANUFACTURER))
                        device = Device.IP_68_SDK_22;
                    else
                        device = null;
                    if(device!=null)
                        jobNumber++;
                    else
                        return null;
                    break;
                case 1:
                    //MODEL 검사
                    if(device.model.equals(Build.MODEL))
                        jobNumber++;
                    else
                        return null;
                    break;
                case 2:
                    //SDK 검사
                    if(device.sdk==Build.VERSION.SDK_INT)
                        return device;
                    else
                        return null;
            }
        }
    }
}
