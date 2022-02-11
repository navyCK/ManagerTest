package com.mediksystem.managertest.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public abstract class AbstractNestActivity {
    protected Activity activity;
    public Activity getActivity(){
        return activity;
    }
    protected void onInitBeforeOnCreate(Bundle savedInstanceState) {}
    protected void onInitAfterOnCreate(Bundle savedInstanceState) {}
    protected void onCreate(Bundle savedInstanceState) {}
    protected void onStart() {}
    protected void onRestart() {}
    protected void onResume() {}
    protected void onPause() {}
    protected void onStop() {}
    protected void onDestroy() {}
    protected void onNewIntent(Intent intent) {}
    protected boolean onKeyDown(int keyCode, KeyEvent event) {return true;}
}
