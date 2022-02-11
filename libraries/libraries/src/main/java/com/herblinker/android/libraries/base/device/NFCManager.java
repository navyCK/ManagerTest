package com.herblinker.android.libraries.base.device;

import com.herblinker.android.libraries.base.exception.NFCInvalidDetectingException;
import com.herblinker.libraries.base.data.BytesEncoding;

public interface NFCManager {
    public static final byte[] APDU_GET_UID = BytesEncoding.HEXA.decode("FFCA000007");
    public static final byte[] HCE_REQUEST_HEADER = BytesEncoding.HEXA.decode("00A4040005");
    public static final byte[] AID_CHECK = BytesEncoding.HEXA.decode("F20AC48B11");
    public static final byte[] AID_ACCOUNT = BytesEncoding.HEXA.decode("F20AC48B12");
    public static final byte[] AID_PATIENT = BytesEncoding.HEXA.decode("F20AC48B13");
    public static final byte[] AID_ETC = BytesEncoding.HEXA.decode("F20AC48B16");
    public static final int RESULT_CODE_SUCESS = 00;
    public static final int RESULT_CODE_FAIL = 01;

    public enum NFCState{
        NOT_SUPPORT,
        TURNED_OFF,
        READ_SUPPORT,
        WRITE_SUPPORT,
        READ_WRITE_SUPPORT;
    }

    public NFCState getCurrentNFCState();
    public void detect(OnNFCReadListener onNFCReadListener, OnNFCWriteListener onNFCWriteListener) throws NFCInvalidDetectingException;
    public void onRestart();
    public void detectResume();
    public void detectPause();
    public boolean isDetecting();
    public void cancel();
    public void execute();
    public void close();
}
