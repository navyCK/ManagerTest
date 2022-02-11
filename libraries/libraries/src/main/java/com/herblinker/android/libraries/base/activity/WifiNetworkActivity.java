package com.herblinker.android.libraries.base.activity;

import android.app.Activity;
import android.os.Bundle;

import com.herblinker.android.libraries.base.device.AndroidNetworkManager;
import com.herblinker.android.libraries.base.device.NetworkJobs;
import com.herblinker.android.libraries.base.device.NetworkManager;

public class WifiNetworkActivity extends AbstractNestActivity{
    private NetworkManager networkManager;
    private NetworkJobs networkJobs;

    public WifiNetworkActivity(Activity activity, NetworkJobs networkJobs){
        this.activity=activity;
        this.networkJobs = networkJobs;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    @Override
    protected void onInitBeforeOnCreate(Bundle savedInstanceState) {
        networkManager = new AndroidNetworkManager(activity, networkJobs);
    }

    @Override
    protected void onResume() {
        if(networkManager==null)
            return;
        networkManager.detectResume();
    }

    @Override
    protected void onPause() {
        if(networkManager==null)
            return;
        networkManager.detectPause();
    }

    @Override
    protected void onDestroy() {
        if(networkManager==null)
            return;
        networkManager.close();
    }
}
