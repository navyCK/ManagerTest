package com.herblinker.android.libraries.base.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.herblinker.android.libraries.base.net.CookieData;

public class NetworkActivity extends AbstractNestActivity{
    private static final String EXTRA_COOKIE_NAME = "com.herblinker.cookieData";
    protected CookieData cookieData;
    public NetworkActivity(Activity activity){
        this.activity=activity;
    }

    @Override
    protected void onInitBeforeOnCreate(Bundle savedInstanceState) {
        Intent intent = activity.getIntent();
        cookieData = new CookieData(intent.getStringExtra(EXTRA_COOKIE_NAME));
    }
    public CookieData getCookieData(){
        return cookieData;
    }
    public void setCookieData(Intent intent){
        intent.putExtra(EXTRA_COOKIE_NAME, cookieData.toString());
    }
    public void initCookieDate(Context context){
        cookieData=new CookieData(context);
    }
    public void initCookieDate(String string){
        cookieData=new CookieData(string);
    }
    public String saveCookieDateToString(){
        return cookieData.toString();
    }
    public void saveCookieDateToSharedPreference(Context context){
        cookieData.saveToSharedPreferences(context);
    }
}
