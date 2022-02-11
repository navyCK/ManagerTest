package com.herblinker.android.libraries.base.view.compatible;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.TextureView;

import com.herblinker.android.libraries.base.exception.CameraNotSupportException;
import com.herblinker.android.libraries.base.view.CameraView;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

public interface AndroidCamera extends Closeable {
    public enum Facing{
        NONE(0),
        POSSIBLE(1),
        FRONT(2),
        BACK(3),
        EXTERNAL(4);
        private int value;
        Facing(int value){
            this.value=value;
        }
        public int getValue(){
            return value;
        }
    }
    public enum CameraDataFormat{
        JPEG,
        RGBA_8888,
        NOT_SUPPORT;
    }

    public interface Callback{
        public void onPictureTaken(byte[] data, Bitmap bitmap);
    }

    public TextureView.SurfaceTextureListener getSurfaceTextureListener(CameraView cameraView);
    public void takePicture(AndroidCamera.Callback callback, int width, int height, CameraView cameraView) throws CameraNotSupportException;
    public boolean startRecord(Activity activity, CameraView cameraView, File file, int width, int height, int bitRate, int frame, boolean recordSound) throws IOException, CameraNotSupportException;
    public boolean endRecord(CameraView cameraView) throws CameraNotSupportException;
    public boolean isUsed();
    public void init(Activity activity, Facing facing, CameraView cameraView) throws CameraNotSupportException;
    public void resume() throws CameraNotSupportException;
}
