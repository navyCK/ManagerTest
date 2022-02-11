package com.herblinker.android.libraries.base.device;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

public class AndroidNetworkManager implements NetworkManager {
    private Activity activity;
    private NetworkJobs networkJobs;
    private ConnectivityManager connectivityManager;
    private WifiManager wifiManager;
    private BroadcastReceiver broadcastReceiver;

    public AndroidNetworkManager(Activity activity, NetworkJobs networkJobs){
        this.activity=activity;
        this.networkJobs = networkJobs;
        this.connectivityManager=(ConnectivityManager)activity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        this.wifiManager=(WifiManager)activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.broadcastReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action!=null)
                    if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                        NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                        if(networkInfo.getType()==ConnectivityManager.TYPE_WIFI){
                            switch (networkInfo.getState()){
                                case CONNECTING:
                                    AndroidNetworkManager.this.networkJobs.wifiConnecting();
                                    break;
                                case CONNECTED:
                                    AndroidNetworkManager.this.networkJobs.wifiConnected();
                                    break;
                                case DISCONNECTING:
                                    AndroidNetworkManager.this.networkJobs.wifiDisconnecting();
                                    break;
                                case DISCONNECTED:
                                    AndroidNetworkManager.this.networkJobs.wifiDisconnected();
                                    break;
                            }
                        } else if(networkInfo.getType()==ConnectivityManager.TYPE_MOBILE) {
                            switch (networkInfo.getState()){
                                case CONNECTING:
                                    AndroidNetworkManager.this.networkJobs.mobileConnecting();
                                    break;
                                case CONNECTED:
                                    AndroidNetworkManager.this.networkJobs.mobileConnected();
                                    break;
                                case DISCONNECTING:
                                    AndroidNetworkManager.this.networkJobs.mobileDisconnecting();
                                    break;
                                case DISCONNECTED:
                                    AndroidNetworkManager.this.networkJobs.mobileDisconnected();
                                    break;
                            }
                        }
                    } else if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
                        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                        switch(wifiState){
                            case WifiManager.WIFI_STATE_DISABLING:
                                AndroidNetworkManager.this.networkJobs.wifiTurningOff();
                                break;
                            case WifiManager.WIFI_STATE_DISABLED:
                                AndroidNetworkManager.this.networkJobs.wifiOff();
                                break;
                            case WifiManager.WIFI_STATE_ENABLING:
                                AndroidNetworkManager.this.networkJobs.wifiTurningOn();
                                break;
                            case WifiManager.WIFI_STATE_ENABLED:
                                AndroidNetworkManager.this.networkJobs.wifiOn();
                                break;
                            case WifiManager.WIFI_STATE_UNKNOWN:
                            default:
                                AndroidNetworkManager.this.networkJobs.wifiUnknown();
                                break;
                        }
                    } else if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                        AndroidNetworkManager.this.networkJobs.wifiScanned();
                    }
            }
        };
    }

    @Override
    public boolean startScan(){
        return wifiManager.startScan();
    }

    @Override
    public WifiConnectionFailType connectWifi(ScanResult scanResult, String password){
        return connectWifi(scanResult.SSID, scanResult.BSSID, password);
    }
    @Override
    public WifiConnectionFailType connectWifi(String ssid, String bssid, String password){
        if(getCurrentWifiState()!=WifiState.TURNED_ON)
            return WifiConnectionFailType.NEED_TURNED_ON;
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('"');
        stringBuilder.append(ssid);
        stringBuilder.append('"');
        wifiConfiguration.SSID = stringBuilder.toString();
        stringBuilder = new StringBuilder();
        stringBuilder.append('"');
        stringBuilder.append(password);
        stringBuilder.append('"');
        wifiConfiguration.preSharedKey = stringBuilder.toString();
        if(bssid!=null)
            wifiConfiguration.BSSID=bssid;
        List<WifiConfiguration> olds = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration old : olds)
            if (old.SSID.equals(wifiConfiguration.SSID)||
                    old.SSID.equals(ssid)) {
                wifiManager.disableNetwork(old.networkId);
                if(!wifiManager.removeNetwork(old.networkId))
                    return WifiConnectionFailType.CANNOT_REMOVABLE;
            }

        int networkId = wifiManager.addNetwork(wifiConfiguration);
        if(networkId<0)
            return WifiConnectionFailType.REGISTER_FAIL;
        wifiManager.disconnect();
        if(wifiManager.enableNetwork(networkId, true))
            if(wifiManager.reconnect())
                return WifiConnectionFailType.SUCCESS;
        return WifiConnectionFailType.UNKNOWN;
    }
    @Override
    public boolean isConnected(){
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo==null?false:networkInfo.isConnected();
    }

    @Override
    public WifiState getCurrentWifiState() {
        int wifiState = wifiManager.getWifiState();
        switch(wifiState){
            case WifiManager.WIFI_STATE_DISABLING:
                return WifiState.TURNNING_OFF;
            case WifiManager.WIFI_STATE_DISABLED:
                return WifiState.TURNED_OFF;
            case WifiManager.WIFI_STATE_ENABLING:
                return WifiState.TURNNING_ON;
            case WifiManager.WIFI_STATE_ENABLED:
                return WifiState.TURNED_ON;
            case WifiManager.WIFI_STATE_UNKNOWN:
            default:
                return WifiState.UNKNOWN;
        }
    }

    @Override
    public List<ScanResult> getScanResults(){
        return wifiManager.getScanResults();
    }
    @Override
    public WifiInfo getConnectionInfo(){
        return wifiManager.getConnectionInfo();
    }
    @Override
    public DhcpInfo getDhcpInfo(){
        return wifiManager.getDhcpInfo();
    }
    @Override
    public boolean isWifiEnabled(){
        return wifiManager.isWifiEnabled();
    }

    @Override
    public void setWifiPower(boolean onOff) {
        wifiManager.setWifiEnabled(onOff);
    }

    @Override
    public void detectResume() {
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        activity.registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void detectPause() {
        activity.unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void close() {

    }
}
