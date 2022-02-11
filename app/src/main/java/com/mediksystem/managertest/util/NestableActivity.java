package com.mediksystem.managertest.util;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;

import com.mediksystem.managertest.exception.InvalidActivityRequestException;
import com.mediksystem.managertest.exception.InvalidPermissionRequestException;

import java.util.Iterator;
import java.util.LinkedList;

import androidx.appcompat.app.AppCompatActivity;

public class NestableActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1588;
    private static final int ACTIVITY_REQUEST_IMAGE_CAPTURE = 1577;
    private static final int ACTIVITY_REQUEST_VIDEO_CAPTURE = 1566;
    private static final int ACTIVITY_REQUEST_URI = 1544;
    private LinkedList<AbstractNestActivity> activities;
    private Iterator<AbstractNestActivity> iterator;
    private boolean initBeforeOnCreate;
    private boolean initAfterOnCreate;
    private volatile PermissionRequestCallback permissionRequestCallback;
    private volatile CameraPictureRequestCallback cameraPictureRequestCallback;
    private volatile UriCallback cameraVideoRequestCallback;
    private volatile UriCallback uriCallback;

    public static interface CameraPictureRequestCallback{
        public void callback(Bitmap bitmap);
        public void fail();
    }
    public static interface UriCallback{
        public void callback(Uri uri);
        public void fail();
    }
    public static interface PermissionRequestCallback{
        public void callback(String[] requestedPermissions, boolean[] isGranted);
    }
    {
        activities = new LinkedList<>();
        initBeforeOnCreate=true;
        initAfterOnCreate=true;
    }

    public final void pushActivity(AbstractNestActivity abstractNestActivity){
        abstractNestActivity.activity=this;
        activities.add(abstractNestActivity);
    }

    //최초실행 관련
    public void onInitBeforeOnCreate(Bundle savedInstanceState){}

    public void onInitAfterOnCreate(Bundle savedInstanceState){}
    private void onInitBeforeOnCreateForce(Bundle savedInstanceState){
        if(iterator.hasNext())
            try{
                iterator.next().onInitBeforeOnCreate(savedInstanceState);
            } finally {
                onInitBeforeOnCreateForce(savedInstanceState);
            }
    }
    private void onCreateForce(Bundle savedInstanceState){
        if(iterator.hasNext())
            try{
                iterator.next().onCreate(savedInstanceState);
            } finally {
                onCreateForce(savedInstanceState);
            }
    }
    private void onInitAfterOnCreateForce(Bundle savedInstanceState){
        if(iterator.hasNext())
            try{
                iterator.next().onInitAfterOnCreate(savedInstanceState);
            } finally {
                onInitAfterOnCreateForce(savedInstanceState);
            }
    }
    @Override
    protected final void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try{
            if(initBeforeOnCreate) {
                initBeforeOnCreate=false;
                iterator = activities.iterator();
                try{
                    onInitBeforeOnCreateForce(savedInstanceState);
                } finally {
                    onInitBeforeOnCreate(savedInstanceState);
                }
            }
        } finally {
            try{
                iterator = activities.iterator();
                try{
                    onCreateForce(savedInstanceState);
                } finally {
                    onCreateForce(savedInstanceState);
                }
            } finally {
                if(initAfterOnCreate) {
                    initAfterOnCreate=false;
                    iterator = activities.iterator();
                    try{
                        onInitAfterOnCreateForce(savedInstanceState);
                    } finally {
                        onInitAfterOnCreate(savedInstanceState);
                    }
                }
            }
        }
    }

    private void onStartForce(){
        if(iterator.hasNext())
            try{
                iterator.next().onStart();
            } finally {
                onStartForce();
            }
    }
    @Override
    protected final void onStart(){
        super.onStart();
        iterator = activities.iterator();
        onStartForce();
    }

    private void onRestartForce(){
        if(iterator.hasNext())
            try{
                iterator.next().onRestart();
            } finally {
                onRestartForce();
            }
    }
    @Override
    protected final void onRestart(){
        super.onRestart();
        iterator = activities.iterator();
        onRestartForce();
    }

    private void onResumeForce(){
        if(iterator.hasNext())
            try{
                iterator.next().onResume();
            } finally {
                onResumeForce();
            }
    }
    @Override
    protected final void onResume(){
        super.onResume();
        iterator = activities.iterator();
        onResumeForce();
    }

    private void onPauseForce(){
        if(iterator.hasNext())
            try{
                iterator.next().onPause();
            } finally {
                onPauseForce();
            }
    }
    @Override
    protected final void onPause(){
        super.onPause();
        iterator = activities.iterator();
        onPauseForce();
    }

    private void onStopForce(){
        if(iterator.hasNext())
            try{
                iterator.next().onStop();
            } finally {
                onStopForce();
            }
    }
    @Override
    protected final void onStop(){
        super.onStop();
        iterator = activities.iterator();
        onStopForce();
    }

    private void onDestroyForce(){
        if(iterator.hasNext())
            try{
                iterator.next().onDestroy();
            } finally {
                onDestroyForce();
            }
    }
    @Override
    protected final void onDestroy(){
        super.onDestroy();
        iterator = activities.iterator();
        onDestroyForce();
    }


    private void onNewIntentForce(Intent intent){
        if(iterator.hasNext())
            try{
                iterator.next().onNewIntent(intent);
            } finally {
                onNewIntentForce(intent);
            }
    }
    @Override
    protected final void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        iterator = activities.iterator();
        onNewIntentForce(intent);
    }

    private boolean onKeyDownForce(boolean result, int keyCode, KeyEvent event){
        if(result)
            if(iterator.hasNext())
                try{
                    result = iterator.next().onKeyDown(keyCode, event);
                } finally {
                    return onKeyDownForce(result, keyCode, event);
                }
        return result;
    }

    @Override
    public final boolean onKeyDown(int keyCode, KeyEvent event) {
        iterator = activities.iterator();
        boolean result = true;
        try {
            result = onKeyDownForce(true, keyCode, event);
        } finally {
            if(result)
                return super.onKeyDown(keyCode, event);
            return result;
        }
    }

    /**
     *
     * @param permissions
     * @return true=need request, false = already has
     */
    public final boolean[] checkPermission(String permissions[], PermissionRequestCallback permissionRequestCallback) throws InvalidPermissionRequestException {
        if(this.permissionRequestCallback!=null)
            throw new InvalidPermissionRequestException();
        this.permissionRequestCallback=permissionRequestCallback;
        boolean[] hasPermissions = new boolean[permissions.length];
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LinkedList<String> requests = new LinkedList<>();
            for (int i = 0; i < permissions.length; ++i) {
                if (checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED){
                    requests.add(permissions[i]);
                    hasPermissions[i]=false;
                } else {
                    hasPermissions[i]=true;
                }
            }
            if(requests.size()>0){
                String[] targets = new String[requests.size()];
                requests.toArray(targets);
                requestPermissions(targets, PERMISSION_REQUEST_CODE);
            } else {
                NestableActivity.this.permissionRequestCallback=null;
            }
        } else {
            NestableActivity.this.permissionRequestCallback=null;
            for(int i=0;i<hasPermissions.length;++i)
                hasPermissions[i]=true;
        }
        return hasPermissions;
    }

    @Override
    public final void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (permissionRequestCallback != null) {
                    boolean[] results = new boolean[grantResults.length];
                    for (int i = 0; i < results.length; ++i)
                        results[i] = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                    permissionRequestCallback.callback(permissions, results);
                    permissionRequestCallback = null;
                }
                break;
        }
    }

    public final void capturePictureByAnother(CameraPictureRequestCallback cameraPictureRequestCallback) throws InvalidActivityRequestException {
        if(this.cameraPictureRequestCallback!=null)
            throw new InvalidActivityRequestException();
        this.cameraPictureRequestCallback=cameraPictureRequestCallback;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) == null)
            throw new InvalidActivityRequestException();
        else
            startActivityForResult(takePictureIntent, ACTIVITY_REQUEST_IMAGE_CAPTURE);
    }
    public final void captureVideoByAnother(UriCallback cameraVideoRequestCallback) throws InvalidActivityRequestException {
        if(this.cameraVideoRequestCallback!=null)
            throw new InvalidActivityRequestException();
        this.cameraVideoRequestCallback=cameraVideoRequestCallback;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) == null)
            throw new InvalidActivityRequestException();
        else
            startActivityForResult(takePictureIntent, ACTIVITY_REQUEST_VIDEO_CAPTURE);
    }
    public final void getUriByAnother(String type, UriCallback uriCallback) throws InvalidActivityRequestException {
        if(this.uriCallback!=null)
            throw new InvalidActivityRequestException();
        this.uriCallback=uriCallback;
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(type);
        if (intent.resolveActivity(getPackageManager()) == null)
            throw new InvalidActivityRequestException();
        else
            startActivityForResult(intent, ACTIVITY_REQUEST_URI);
    }
    @Override
    protected final void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case ACTIVITY_REQUEST_IMAGE_CAPTURE:
                if (resultCode == Activity.RESULT_OK)
                    cameraPictureRequestCallback.callback((Bitmap) intent.getExtras().get("data"));
                else
                    cameraPictureRequestCallback.fail();
                cameraPictureRequestCallback = null;
                break;
            case ACTIVITY_REQUEST_VIDEO_CAPTURE:
                if (resultCode == Activity.RESULT_OK)
                    cameraVideoRequestCallback.callback(intent.getData());
                else
                    cameraVideoRequestCallback.fail();
                cameraVideoRequestCallback = null;
                break;
            case ACTIVITY_REQUEST_URI:
                if (resultCode == Activity.RESULT_OK)
                    uriCallback.callback(intent.getData());
                else
                    uriCallback.fail();
                uriCallback = null;
                break;
        }
    }
}

