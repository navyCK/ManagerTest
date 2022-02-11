package com.herblinker.android.libraries.base.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.herblinker.android.libraries.base.net.CookieData;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractDataCenter extends Application {
    private static final String KEY_STORED_DATA = "com.herblinker.android.member.KEY_STORED_DATA";
    private static final String KEY_STORED_DATA_DELIMETER = ",";
    private static final long DATA_STOREM_MAX_TRIAL = 10000L;
    private static String PROTOCOL = "https://";

    private static final String TAG = "CHECK";

    private CallbackForCookieData callbackForCookieData;
    private HashMap<Long, Object> dataSet = new HashMap<>();
    private long key;

    protected String host="v1.haniclinic.com";
    protected boolean debug;

    private Map<Activity, ActivityData> activityData = new HashMap<>();
    private long fileName;
    public CookieData getCookieData(){
        return callbackForCookieData.getCookieData();
    }

    //바로적용위해 적용함.
    public void setHost(String host) {
        this.host = "v1.haniclinic.com";
    }

    public boolean saveSingle(Intent intent, Object datum){
        return save(intent, datum);
    }

    public synchronized boolean save(Intent intent, Object... data){
        if(data==null)
            return true;
        if(data.length==0)
            return true;
        boolean isFail=false;
        LinkedList<Long> keys = new LinkedList<>();
        Long key=null;
        long trials;
        for(Object datum: data){
            if(data==null) {
                keys.add(null);
                continue;
            }
            trials=DATA_STOREM_MAX_TRIAL;
            do{
                if(trials<=0L){
                    isFail=true;
                    break;
                }
                key=new Long(this.key++);
            } while (dataSet.containsKey(key));
            if(isFail)
                break;
            dataSet.put(key, datum);
            keys.add(key);
        }
        if(isFail){
            for(Long fail: keys)
                dataSet.remove(fail);
            return false;
        }
        intent.putExtra(KEY_STORED_DATA, keys);
        return true;
    }
    public Object loadSingle(Activity activity){
        return loadSingle(activity.getIntent());
    }
    public Object loadSingle(Intent intent){
        Object[] data = load(intent);
        if(data==null)
            return null;
        if(data.length==0)
            return null;
        return data[0];
    }

    public Object[] load(Activity activity){
        return load(activity.getIntent());
    }
    public synchronized  Object[] load(Intent intent){
        LinkedList<Long> keys = (LinkedList<Long>)intent.getSerializableExtra(KEY_STORED_DATA);
        if(keys==null||keys.size()==0)
            return new Object[0];
        Object[] data = new Object[keys.size()];
        int index = 0;
        for(Long key: keys){
            if(key!=null)
                data[index] = dataSet.remove(key);
            index++;
        }
        return data;
    }

    /**
     * Log Level Error
     **/
    public final void e(String message) {
        if (debug)
            Log.e(TAG, buildLogMsg(message));
    }

    /**
     * Log Level Warning
     **/
    public final void w(String message) {
        if (debug)
            Log.w(TAG, buildLogMsg(message));
    }

    /**
     * Log Level Information
     **/
    public final void i(String message) {
        if (debug)
            Log.i(TAG, buildLogMsg(message));
    }

    /**
     * Log Level Debug
     **/
    public final void d(String message) {
        if (debug)
            Log.d(TAG, buildLogMsg(message));
    }

    /**
     * Log Level Verbose
     **/
    public final void v(String message) {
        if (debug)
            Log.v(TAG, buildLogMsg(message));
    }


    public static String buildLogMsg(String message) {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[4];
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(ste.getFileName().replace(".java", ""));
        sb.append("::");
        sb.append(ste.getMethodName());
        sb.append("]");
        sb.append(message);
        return sb.toString();
    }

    public static void deleteFileRecursive(File directory) {
        if(directory.exists()){
            for(File sub: directory.listFiles()){
                if(sub.isFile())
                    sub.delete();
                else if(sub.isDirectory()) {
                    deleteFileRecursive((sub));
                    sub.delete();
                }
            }
        }
    }
    private static final String TEMP_DIRECTORY_NAME = "HerbLinkerTempFiles";
    private File tempFileDirectory;
    public void setTempFiles(Context context){
        File fileDirectory = context.getFilesDir();
        tempFileDirectory = new File(fileDirectory, TEMP_DIRECTORY_NAME);
        if(!tempFileDirectory.exists())
            tempFileDirectory.mkdirs();
        deleteFileRecursive(tempFileDirectory);
    }

    public File getTempFile(Activity activity) {
        return getTempFile(activity, null);
    }
    public synchronized File getTempFile(Activity activity, String extensionName){
        ActivityData activityData = this.activityData.get(activity);
        String name=String.valueOf(this.fileName++);
        if(extensionName!=null)
            name+="."+extensionName;
        File newFile = new File(tempFileDirectory, name);
        if(newFile.exists())
            newFile.delete();
        newFile = new File(tempFileDirectory, name);
        activityData.files.add(newFile);
        return newFile;
    }
    protected void setNewCookieData(Context context){
        callbackForCookieData.setNewCookieData(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        callbackForCookieData = new AbstractDataCenter.CallbackForCookieData(this, new CookieData(this));
        registerActivityLifecycleCallbacks(callbackForCookieData);
    }

    protected static final class ActivityData{
        private List<File> files = new LinkedList<>();
        private Activity activity;
        private ActivityData(Activity activity){
            this.activity=activity;
        }
    }
    protected static final class CallbackForCookieData implements ActivityLifecycleCallbacks{
        private AbstractDataCenter dataCenter;
        private CookieData cookieData;
        private int activityCount;
        public CallbackForCookieData(AbstractDataCenter dataCenter, CookieData cookieData){
            this.dataCenter=dataCenter;
            this.cookieData=cookieData;
        }
        public CookieData setNewCookieData(Context context){
            this.cookieData=new CookieData();
            cookieData.saveToSharedPreferences(context);
            return cookieData;
        }
        public CookieData getCookieData(){
            return cookieData;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            Log.e("CHECK", "onActivityCreated: "+(++activityCount));
            ActivityData activityData;
            if(!dataCenter.activityData.containsKey(activity)){
                activityData = new ActivityData(activity);
                dataCenter.activityData.put(activity, activityData);
            } else
                activityData = dataCenter.activityData.get(activity);

        }

        @Override
        public void onActivityStarted(Activity activity) {
            Log.e("CHECK", "onActivityStarted");

        }

        @Override
        public void onActivityResumed(Activity activity) {
            Log.e("CHECK", "onActivityResumed");
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Log.e("CHECK", "onActivityPaused");
            if(cookieData!=null)
                cookieData.saveToSharedPreferences(activity);
        }

        @Override
        public void onActivityStopped(Activity activity) {
            Log.e("CHECK", "onActivityStopped");
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

        @Override
        public void onActivityDestroyed(Activity activity) {
            Log.e("CHECK", "onActivityCreated: "+(--activityCount));
            if(activityCount==0)
                if(cookieData!=null)
                    cookieData.saveToSharedPreferences(activity);
            ActivityData activityData = dataCenter.activityData.remove(activity);
            if(activityData!=null)
                for(File file: activityData.files)
                    file.delete();
        }
    }

    public String getURL(String path) {
        if(path==null)
            return null;
        int protocolIndex = path.indexOf("//");
        int pageIndex = path.indexOf("/");
        if(pageIndex<0)
            return PROTOCOL + path;
        if(protocolIndex==pageIndex)
            return path;
        if(path.startsWith("/"))
            return PROTOCOL + host + path;
        Log.e("CHECK", "PROTOCOL + path: "+PROTOCOL + path);
        return PROTOCOL + path;
    }
}
