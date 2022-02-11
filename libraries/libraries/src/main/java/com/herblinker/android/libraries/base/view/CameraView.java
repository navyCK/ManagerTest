package com.herblinker.android.libraries.base.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import com.herblinker.android.libraries.R;
import com.herblinker.android.libraries.base.device.AndroidDevice;
import com.herblinker.android.libraries.base.exception.CameraNotSupportException;
import com.herblinker.android.libraries.base.view.compatible.AndroidCamera;

import java.io.File;
import java.io.IOException;

import androidx.annotation.RequiresApi;

//TODO 추후 정리 재수정해야함
//이미지 처리에서 순차적으로 정보를 모으고 이를 토대로 순차적인 카메라 캡처 처리를 해야함
//이미지 캡처 reader를 다중으로 돌리지말고 순차적으로 하는게 좋을듯
public class CameraView extends TextureView {
    private static final String TAG = "CHECK";
    private static final int DEFAULT_SCREEN_WIDTH=640;
    private static final int DEFAULT_SCREEN_HEIGHT=480;

    private AndroidDevice androidDevice;
    private AndroidCamera camera;
    private AndroidCamera.Facing facing;
    private Context context;


    public CameraView(Context context) {
        super(context);
        this.context=context;
        androidDevice = AndroidDevice.getDevice();
        camera = androidDevice.getCamera(context);
        setSurfaceTextureListener(camera.getSurfaceTextureListener(this));
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        setAttrs(context, attrs);
        androidDevice = AndroidDevice.getDevice();
        camera = androidDevice.getCamera(context);
        setSurfaceTextureListener(camera.getSurfaceTextureListener(this));
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        setAttrs(context, attrs);
        androidDevice = AndroidDevice.getDevice();
        camera = androidDevice.getCamera(context);
        setSurfaceTextureListener(camera.getSurfaceTextureListener(this));
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context=context;
        setAttrs(context, attrs);
        androidDevice = AndroidDevice.getDevice();
        camera = androidDevice.getCamera(context);
        setSurfaceTextureListener(camera.getSurfaceTextureListener(this));
    }

    private void setAttrs(Context context, AttributeSet attrs){
        int facingType = context.obtainStyledAttributes(attrs, R.styleable.CameraView).getInt(R.styleable.CameraView_facing, AndroidCamera.Facing.NONE.getValue());
        if(facingType==AndroidCamera.Facing.POSSIBLE.getValue())
            facing=AndroidCamera.Facing.POSSIBLE;
        else if(facingType==AndroidCamera.Facing.FRONT.getValue())
            facing=AndroidCamera.Facing.FRONT;
        else if(facingType==AndroidCamera.Facing.BACK.getValue())
            facing=AndroidCamera.Facing.BACK;
        else if(facingType==AndroidCamera.Facing.EXTERNAL.getValue())
            facing=AndroidCamera.Facing.EXTERNAL;
        else
            facing=AndroidCamera.Facing.NONE;
    }

    public void takePicture(AndroidCamera.Callback callback) throws CameraNotSupportException{
        takePicture(callback, DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT);
    }

    public synchronized void takePicture(final AndroidCamera.Callback callback, final int width, final int height) throws CameraNotSupportException{
        Log.e(TAG, "takePicture");
        camera.takePicture(callback, width, height, this);
    }

    public synchronized boolean startRecord(Activity activity, File file, int width, int height, int bitRate, int frame, boolean recordSound) throws IOException, CameraNotSupportException{
        return camera.startRecord(activity, this, file, width, height, bitRate, frame, recordSound);
    }

    public synchronized boolean endRecord() throws CameraNotSupportException{
        return camera.endRecord(this);
    }
    public synchronized boolean isUsed(){
        return camera.isUsed();
    }
    public void init(Activity activity) throws CameraNotSupportException{
        init(activity, this.facing);
    }
    public void init(Activity activity, AndroidCamera.Facing facing) throws CameraNotSupportException{
        camera.init(activity, facing, this);
    }

    public void resume() throws CameraNotSupportException{
        camera.resume();
    }
    //TODO 갑자기 획 돌릴 경우 실행되지 않는다. 그래서 DisplayManager에 registerDisplayListener를 이용해서 처리하는게 좋다.
    public void transformImage(int width, int height){
        Matrix matrix = new Matrix();
        int rotationAdjust = 360 - getDisplayRotation();
        rotationAdjust%=360;
        if(rotationAdjust!=0){
            RectF camera = new RectF(0, 0, width, height);
            RectF preview = new RectF(0, 0, getHeight(), getWidth());
            float cX = camera.centerX();
            float cY = camera.centerY();
            preview.offset(cX-preview.centerX(), cY-preview.centerY());
            matrix.setRectToRect(camera, preview, Matrix.ScaleToFit.FILL);
            float scale = Math.max(((float)width)/getWidth(), ((float)height)/getHeight());
            matrix.postScale(scale, scale, cX, cY);
            matrix.postRotate(rotationAdjust, cX, cY);
        }
        setTransform(matrix);
    }
    public int getDisplayRotation(){
        Object systemService = context.getSystemService(Context.WINDOW_SERVICE);
        if(systemService instanceof WindowManager) {
            WindowManager windowManager = (WindowManager) systemService;
            int rotation = windowManager.getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                    return 0;
                case Surface.ROTATION_90:
                    return 90;
                case Surface.ROTATION_180:
                    return 180;
                case Surface.ROTATION_270:
                    return 270;
            }
        }
        return 0;
    }

    public void pause(){
        try{
            camera.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}