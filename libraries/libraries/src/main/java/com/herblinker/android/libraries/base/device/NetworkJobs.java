package com.herblinker.android.libraries.base.device;

public interface NetworkJobs {
    public void wifiOff();
    public void wifiTurningOff();
    public void wifiOn();
    public void wifiTurningOn();
    public void wifiUnknown();

    public void wifiScanned();

    public void wifiConnecting();
    public void wifiConnected();
    public void wifiDisconnecting();
    public void wifiDisconnected();

    public void mobileConnecting();
    public void mobileConnected();
    public void mobileDisconnecting();
    public void mobileDisconnected();
}
