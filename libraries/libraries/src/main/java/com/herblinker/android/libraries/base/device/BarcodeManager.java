package com.herblinker.android.libraries.base.device;

import android.view.KeyEvent;

import com.herblinker.android.libraries.base.exception.BarcodeInvalidDetectingException;
import com.herblinker.android.libraries.base.exception.BarcodeNotSupportException;

public interface BarcodeManager {
    /**
     * 바코드 스캔 및 그 결과에 따른 처리 loop는 스캔 반복 실행 여부
     * @param onBarcodeReadListener
     * @throws BarcodeInvalidDetectingException
     * @throws BarcodeNotSupportException
     */
    public void scanBarcode1D(OnBarcodeReadListener onBarcodeReadListener) throws BarcodeInvalidDetectingException, BarcodeNotSupportException;
    /**
     * 바코드 스캔 및 그 결과에 따른 처리 loop는 스캔 반복 실행 여부
     * @param onBarcodeReadListener
     * @throws BarcodeInvalidDetectingException
     * @throws BarcodeNotSupportException
     */
    public void scanBarcode2D(OnBarcodeReadListener onBarcodeReadListener) throws BarcodeInvalidDetectingException, BarcodeNotSupportException;
    public boolean keyDown(int keycode, KeyEvent event);
    public void resume();
    public void pause();
    public void cancelBarcode1D();
    public void cancelBarcode2D();
    public void close();
}
