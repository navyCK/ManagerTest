package com.herblinker.android.libraries.base.device;

import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;

import java.util.List;

public interface NetworkManager {
    public enum WifiState{
        TURNNING_ON,
        TURNED_ON,
        TURNNING_OFF,
        TURNED_OFF,
        UNKNOWN;
    }
    public enum WifiConnectionFailType{
        SUCCESS,
        NEED_TURNED_ON,
        CANNOT_REMOVABLE,
        REGISTER_FAIL,
        UNKNOWN;
    }

    public boolean isConnected();
    public NetworkManager.WifiState getCurrentWifiState();
    public boolean isWifiEnabled();
    public List<ScanResult> getScanResults();
    public WifiInfo getConnectionInfo();
    public DhcpInfo getDhcpInfo();
    public void setWifiPower(boolean onOff);
    public boolean startScan();
    public WifiConnectionFailType connectWifi(ScanResult scanResult, String password);
    public WifiConnectionFailType connectWifi(String ssid, String bssid, String password);
    public void detectResume();
    public void detectPause();
    public void close();
}
