package com.herblinker.android.libraries.base.device;

public abstract class OnBarcodeReadListener {
    public abstract void onBarcodeReadListener(String barcode);
    public abstract void onFail();
}
