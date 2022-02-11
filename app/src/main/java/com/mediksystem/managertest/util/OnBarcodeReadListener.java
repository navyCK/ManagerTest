package com.mediksystem.managertest.util;

public abstract class OnBarcodeReadListener {
    public abstract void onBarcodeReadListener(String barcode);
    public abstract void onFail();
}
