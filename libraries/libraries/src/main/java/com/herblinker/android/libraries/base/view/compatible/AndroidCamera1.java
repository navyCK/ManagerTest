package com.herblinker.android.libraries.base.view.compatible;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import com.herblinker.android.libraries.base.device.AndroidDevice;
import com.herblinker.android.libraries.base.exception.CameraNotSupportException;
import com.herblinker.android.libraries.base.view.CameraView;

import java.io.File;
import java.io.IOException;

public class AndroidCamera1 implements AndroidCamera {
    private static final String TAG = "CHECK";
    private Context context;
    private AndroidDevice androidDevice;
    private Camera camera;
    private CameraView cameraView;
    private MediaRecorder mediaRecorder;
    private File file;
    private boolean beingUsed;
    private Facing facing;

    private Integer foundFacing;
    public AndroidCamera1(Context context, AndroidDevice androidDevice){
        this.context=context;
        this.androidDevice=androidDevice;
    }
    @Override
    public TextureView.SurfaceTextureListener getSurfaceTextureListener(final CameraView cameraView) {
        return new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.e(TAG, "onSurfaceTextureAvailable");
                if(camera!=null)
                    try{
                        int rotation = 360 - cameraView.getDisplayRotation();
                        rotation%=360;
                        camera.setDisplayOrientation(rotation);
                        camera.setPreviewTexture(surface);
                        camera.startPreview();

                        beingUsed=false;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Error setting camera preview: " + e.getMessage());
                    }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.e(TAG, "onSurfaceTextureSizeChanged");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.e(TAG, "onSurfaceTextureDestroyed");
                close();
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                //화면 프레임 바뀔때 마다 계속 뜨니까 무시해 놓은거임
                //Log.e(TAG, "onSurfaceTextureUpdated");
            }
        };
    }

    @Override
    public synchronized void takePicture(final  Callback callback, int width, int height, CameraView cameraView) throws CameraNotSupportException {
        Log.e(TAG, "takePicture: "+beingUsed);
        if(beingUsed)
            return;
        if(camera==null)
            throw new CameraNotSupportException();
        beingUsed=true;
        try {
            camera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {
                    Log.e("CHECK", "takePicture: shutter");
                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Log.e("CHECK", "takePicture: raw: "+data);
                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Log.e("CHECK", "takePicture: postView: "+data);
                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    Log.e("CHECK", "takePicture: jpeg: "+data);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    Log.e("CHECK", "takePicture: foundFacing: "+foundFacing);
                    if(foundFacing!=null)
                        if(foundFacing.intValue()==Camera.CameraInfo.CAMERA_FACING_FRONT){
                            Bitmap old = bitmap;
                            Matrix sideInversion = new Matrix();
                            sideInversion.setScale(-1, 1);
                            bitmap = Bitmap.createBitmap(old, 0, 0, old.getWidth(), old.getHeight(), sideInversion, false);
                            old.recycle();
                        }
                    callback.onPictureTaken(data, bitmap);
                    if(camera!=null)
                        camera.startPreview();
                    beingUsed=false;
                }
            });
        } catch (RuntimeException e){
            beingUsed=false;
            e.printStackTrace();
            Log.e("CHECK", "log-level 문제 or camera자원을 다른데서 잡고있어서(release()를 안해줘서) or 카메라 로드실패 등");
        }
    }

    @Override
    public synchronized boolean startRecord(Activity activity, CameraView cameraView, File file, int width, int height, int bitRate, int frame, boolean recordSound) throws IOException, CameraNotSupportException {
        Log.e("CHECK", "startRecord s1: "+beingUsed);
        if(beingUsed)
            return false;
        Log.e("CHECK", "startRecord s1: "+activity);
        if(activity==null)
            return false;
        Log.e("CHECK", "startRecord s1: "+camera);
        if(camera==null)
            throw new CameraNotSupportException();
        beingUsed=true;
        try {
            camera.unlock();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setCamera(camera);
            if(recordSound)
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            if(recordSound)
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setVideoSize(width, height);
            mediaRecorder.setOutputFile(file.getPath());
            if(foundFacing!=null&&foundFacing.intValue()==Camera.CameraInfo.CAMERA_FACING_FRONT){
                Log.e("CHECK", "facing==Facing.FRONT");
                //TODO 좌우 변환 처리 - 확인필요
            }
            mediaRecorder.setVideoEncodingBitRate(512 * 1024);
            mediaRecorder.setVideoFrameRate(24);
            mediaRecorder.prepare();
            this.file=file;
            mediaRecorder.start();
            return true;
        } catch (Exception e){
            if(mediaRecorder!=null)
                mediaRecorder.release();
            mediaRecorder=null;
            beingUsed=false;
            throw new IOException(e);
        }
    }

    @Override
    public synchronized boolean endRecord(CameraView cameraView) throws CameraNotSupportException {
        Log.e("CHECK", "endRecorddddddd1");
        if(!beingUsed)
            return false;
        if(mediaRecorder==null)
            return false;
        if(camera==null)
            throw new CameraNotSupportException();
        camera.lock();
        try{
            boolean hasProblem = false;
            while(true) {
                try {
                    mediaRecorder.stop();
                    break;
                } catch (IllegalStateException ei) {
                    hasProblem=true;
                    break;
                } catch (RuntimeException er) {
                    hasProblem=true;
                    try{
                        Thread.sleep(100);
                    } catch (Exception es){
                        es.printStackTrace();
                    }
                }
            }
            if(hasProblem)
                if(file.exists())
                    file.delete();
            mediaRecorder.release();
        } catch (Exception e){
            e.printStackTrace();
        }
        mediaRecorder=null;
        beingUsed=false;
        Log.e("CHECK", "endRecord2");
        return true;
    }

    @Override
    public boolean isUsed() {
        return beingUsed;
    }

    @Override
    public void init(Activity activity, Facing facing, CameraView cameraView) throws CameraNotSupportException{
        Log.e(TAG, "Camera1: init: "+facing);
        this.cameraView=cameraView;
        if(facing==null)
            facing=Facing.POSSIBLE;
        this.facing=facing;
        try {
            int cameraCount = Camera.getNumberOfCameras();
            if (facing == Facing.POSSIBLE) {
                if(cameraCount>0) {
                    camera = Camera.open(0);
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    Camera.getCameraInfo(0, cameraInfo);
                    foundFacing = new Integer(cameraInfo.facing);
                } else
                    throw new CameraNotSupportException();
            } else if(facing==Facing.NONE){
                camera=null;
            } else{
                int id;
                if (facing == Facing.FRONT)
                    id = Camera.CameraInfo.CAMERA_FACING_FRONT;
                else if (facing == Facing.BACK)
                    id = Camera.CameraInfo.CAMERA_FACING_BACK;
                else
                    id = Integer.MIN_VALUE;
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Log.e(TAG, "Camera1: cameraCount: "+cameraCount);
                for (int i = 0; i < cameraCount; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == id) {
                        camera = Camera.open(i);
                        foundFacing = new Integer(cameraInfo.facing);
                        break;
                    }
                }
                Log.e(TAG, "Camera1: camera: "+camera);
                if(camera==null)
                    throw new CameraNotSupportException();
            }
        }catch (RuntimeException e){
            e.printStackTrace();
            Log.e(TAG,"제조사 카메라 Low-Level 프로그램 불량 or 다른 프로세스에서 사용중 or 운영프로세스 사용 비승인(1)");
            throw new CameraNotSupportException();
        }
    }

    @Override
    public synchronized void resume()throws CameraNotSupportException{
        Log.e(TAG, "Camera1: resume: "+camera);
        if(camera==null)
            init(null, facing, cameraView);
        if(camera==null)
            throw new CameraNotSupportException();
        try {
            Camera.Parameters parameters = camera.getParameters();
            Log.e(TAG, "Camera1: resume rotation: "+cameraView.getDisplayRotation());
            parameters.setRotation(cameraView.getDisplayRotation());
            camera.setParameters(parameters);
            camera.setPreviewTexture(cameraView.getSurfaceTexture());
            camera.startPreview();
            beingUsed=false;
        }catch (RuntimeException e){
            e.printStackTrace();
            Log.e(TAG,"제조사 카메라 Low-Level 프로그램 불량 or 다른 프로세스에서 사용중 or 운영프로세스 사용 비승인(2)");
            throw new CameraNotSupportException();
        }catch (IOException e){
            e.printStackTrace();
            Log.e(TAG,"뷰 처리 불량");
            throw new CameraNotSupportException();
        }
    }

    @Override
    public synchronized void close() {
        Log.e(TAG, "Camera1: close: "+camera);

        if(beingUsed)
            if(mediaRecorder!=null)
                if(camera!=null){
                    try{
                        camera.lock();
                        boolean hasProblem = false;
                        while(true) {
                            try {
                                mediaRecorder.stop();
                                break;
                            } catch (IllegalStateException ei) {
                                hasProblem=true;
                                break;
                            } catch (RuntimeException er) {
                                hasProblem=true;
                                try{
                                    Thread.sleep(100);
                                } catch (Exception es){
                                    es.printStackTrace();
                                }
                            }
                        }
                        if(hasProblem)
                            if(file.exists())
                                file.delete();
                        mediaRecorder.release();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    mediaRecorder=null;
                    beingUsed=false;
                }


        try {
            if(camera!=null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
                beingUsed = false;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
