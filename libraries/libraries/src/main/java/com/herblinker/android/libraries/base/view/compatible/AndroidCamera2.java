package com.herblinker.android.libraries.base.view.compatible;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import com.herblinker.android.libraries.base.device.AndroidDevice;
import com.herblinker.android.libraries.base.exception.CameraNotSupportException;
import com.herblinker.android.libraries.base.view.CameraView;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class AndroidCamera2 implements AndroidCamera {
    private static final String TAG = "CHECK";
    private Context context;
    private AndroidDevice androidDevice;
    private Facing facing;
    private Integer foundFacing;
    private CameraDevice camera;
    private CaptureRequest.Builder previewBuilder;
    private Size previewSize;
    private CameraCaptureSession previewSession;
    private HandlerThread handlerThread;
    private Handler handler;
    private MediaRecorder mediaRecorder;
    private File file;
    private boolean beingUsed;

    public AndroidCamera2(Context context, AndroidDevice androidDevice){
        this.context=context;
        this.androidDevice=androidDevice;
    }

    @Override
    public TextureView.SurfaceTextureListener getSurfaceTextureListener(final CameraView cameraView) {
        return new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(final SurfaceTexture surfaceTexture, int width, int height) {
                Log.e(TAG, "onSurfaceTextureAvailable: "+facing);
                CameraManager manager=(CameraManager)context.getSystemService(Context.CAMERA_SERVICE);

                if(manager==null)
                    return;
                try{
                    String camerId = null;
                    String[] cameraIdList=manager.getCameraIdList();

                    if (facing == Facing.POSSIBLE) {
                        if(cameraIdList.length>0)
                            camerId=cameraIdList[0];
                    } else if(facing==Facing.NONE){
                        camerId=null;
                    } else{
                        String id=null;
                        if (facing == Facing.FRONT)
                            id = String.valueOf(Camera.CameraInfo.CAMERA_FACING_FRONT);
                        else if (facing == Facing.BACK)
                            id = String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK);
                        if(id!=null)
                            for (int i = 0; i < cameraIdList.length; i++) {
                                if(cameraIdList[i].equals(id)) {
                                    camerId = cameraIdList[i];
                                    break;
                                }
                            }
                    }
                    if(camerId==null)
                        return;
                    CameraCharacteristics characteristics=manager.getCameraCharacteristics(camerId);
                    foundFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                    cameraView.transformImage(width, height);
                    StreamConfigurationMap map=characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    if(map==null)
                        return;
                    previewSize=map.getOutputSizes(SurfaceTexture.class)[0];
                    manager.openCamera(camerId, new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(@NonNull CameraDevice camera) {
                            Log.e(TAG, "onOpened");
                            AndroidCamera2.this.camera=camera;
                            start(surfaceTexture);
                        }

                        @Override
                        public void onDisconnected(@NonNull CameraDevice camera) {
                            Log.e(TAG, "onDisconnected");

                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera, int error) {
                            Log.e(TAG, "onError: "+error);
                            close();
                        }
                    },null);
                } catch (CameraAccessException e){
                    e.printStackTrace();
                } catch (SecurityException e){
                    e.printStackTrace();
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
    public void takePicture(final Callback callback, final int width, final int height, final CameraView cameraView) throws CameraNotSupportException {
        if(beingUsed)
            return;
        if(camera==null)
            throw new CameraNotSupportException();
        beingUsed=true;

        CameraManager manager=(CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics=manager.getCameraCharacteristics(camera.getId());
            if(characteristics==null)
                return;
            Size bestSize = null;
            int widthDiff;
            int heightDiff;
            //Size[] jpegSizes=characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            Size[] jpegSizes=characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            //TODO 추후 여기 크기 고치고 bitmap에서 수정해서 들어가야할거도같음
            if(jpegSizes!=null)
                for(Size size: jpegSizes){
                    if(size.getWidth()>=width&&size.getHeight()>=height){
                        if(bestSize==null){
                            bestSize=size;
                        } else {
                            widthDiff=bestSize.getWidth()-size.getWidth();
                            heightDiff=bestSize.getHeight()-size.getHeight();
                            if(widthDiff>0){
                                if(heightDiff>0)
                                    bestSize=size;
                                else if(bestSize.getWidth()*bestSize.getHeight()>size.getWidth()*size.getHeight())
                                    bestSize=size;
                            } else {
                                if(heightDiff>0)
                                    if(bestSize.getWidth()*bestSize.getHeight()>size.getWidth()*size.getHeight())
                                        bestSize=size;
                            }
                        }
                    }
                }
            if(bestSize==null){
                if(jpegSizes!=null&&jpegSizes.length>0)
                    bestSize=jpegSizes[jpegSizes.length-1];
            }
            ImageReader reader;
            List<Surface> outputSurfaces=new ArrayList<>();
            switch (androidDevice.getCameraDataFormat()){
                case JPEG:
                    if(bestSize==null)
                        reader=ImageReader.newInstance(width, height, ImageFormat.JPEG,1);
                    else
                        reader=ImageReader.newInstance(bestSize.getWidth(), bestSize.getHeight(), ImageFormat.JPEG,1);
                    break;
                case RGBA_8888:
                    if(bestSize==null)
                        reader=ImageReader.newInstance(width, height, PixelFormat.RGBA_8888,1);
                    else
                        reader=ImageReader.newInstance(bestSize.getWidth(), bestSize.getHeight(), PixelFormat.RGBA_8888,1);
                    break;
                default :
                    throw new CameraNotSupportException();
            }
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(cameraView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder=camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            int rotationAdjust = 360 - cameraView.getDisplayRotation() + sensorOrientation;
            rotationAdjust %= 360;
            final int finalRotationAdjust = rotationAdjust;

            Log.e("CHECK", "sensorOrientation: "+sensorOrientation);
            Log.e("CHECK", "cameraView.getDisplayRotation(): "+cameraView.getDisplayRotation());
            Log.e("CHECK", "finalRotationAdjust1: "+finalRotationAdjust);

            ImageReader.OnImageAvailableListener imageAvailableListener = null;
            switch (androidDevice.getCameraDataFormat()){
                case JPEG:
                    Log.e("CHECK", "finalRotationAdjust2: "+finalRotationAdjust);
                    captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, finalRotationAdjust);
                    imageAvailableListener=new ImageReader.OnImageAvailableListener() {
                        @Override
                        public void onImageAvailable(ImageReader reader) {
                            Log.e("CHECK", "onImageAvailable BB");
                            Image image = null;
                            try {
                                image = reader.acquireLatestImage();
                                Log.e("CHECK", "acquireLatestImage: "+image);
                                if(image==null)
                                    return;
                                Log.e("CHECK", "onImageAvailable: "+image);
                                Rect rect = image.getCropRect();
                                Log.e("CHECK", "onImageAvailable: "+rect);
                                int getWidth = rect.width();
                                int getHeight = rect.height();
                                Log.e("CHECK", "onImageAvailable: "+getWidth);
                                Log.e("CHECK", "onImageAvailable: "+getHeight);
                                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                final byte[] bytes = new byte[buffer.capacity()];
                                buffer.get(bytes);
                                Log.e("CHECK", "onImageAvailable: "+bytes.length);
                                Bitmap rawBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                Log.e("CHECK", "onImageAvailable: "+rawBitmap);

                                //회전
                                Bitmap rotatedBitmap;
                                Log.e("CHECK", "finalRotationAdjust3: "+finalRotationAdjust);
                                if(finalRotationAdjust==0){
                                    rotatedBitmap = rawBitmap;
                                } else {
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(finalRotationAdjust);
                                    rotatedBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.getWidth(), rawBitmap.getHeight(), matrix, true);
                                    rawBitmap.recycle();
                                }
                                Log.e("CHECK", "onImageAvailable: "+rotatedBitmap);

                                //확대
                                getWidth=rotatedBitmap.getWidth();
                                getHeight=rotatedBitmap.getHeight();
                                Bitmap zoomedBitmap;
                                if(width==getWidth&&height==getHeight){
                                    zoomedBitmap=rotatedBitmap;
                                } else {
                                    double widthRatio = (double)width / getWidth;
                                    double heightRatio = (double)height/ getHeight;
                                    if(widthRatio>heightRatio){
                                        zoomedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, width, (int)(getHeight*widthRatio), true);
                                    } else {
                                        zoomedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, (int)(getHeight*heightRatio), height, true);
                                    }
                                    rotatedBitmap.recycle();
                                }
                                Log.e("CHECK", "onImageAvailable: "+zoomedBitmap);
                                //자르기
                                getWidth=zoomedBitmap.getWidth();
                                getHeight=zoomedBitmap.getHeight();
                                Bitmap croppedBitmap;
                                if(width==getWidth&&height==getHeight){
                                    croppedBitmap=zoomedBitmap;
                                } else {
                                    if(width==getWidth){
                                        croppedBitmap = Bitmap.createBitmap(zoomedBitmap, 0, (getHeight-height)/2, width, height);
                                    } else {
                                        croppedBitmap = Bitmap.createBitmap(zoomedBitmap, (getWidth-width)/2, 0, width, height);
                                    }
                                    zoomedBitmap.recycle();
                                }
                                Log.e("CHECK", "onImageAvailable: "+croppedBitmap);
                                final Bitmap bitmap = croppedBitmap;
                                if(context instanceof Activity){
                                    Activity activity = (Activity)context;
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            callback.onPictureTaken(bytes, bitmap);
                                        }
                                    });
                                } else {
                                    new Handler(){
                                        @Override
                                        public void handleMessage(Message msg) {
                                            super.handleMessage(msg);
                                            callback.onPictureTaken(bytes, bitmap);
                                        }
                                    }.handleMessage(null);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            finally {
                                if(image!=null)
                                    image.close();
                            }
                        }
                    };
                    break;
                case RGBA_8888:
                    imageAvailableListener = new ImageReader.OnImageAvailableListener() {
                        @Override
                        public void onImageAvailable(ImageReader reader) {
                            Log.e("CHECK", "onImageAvailable AA");
                            Image image = null;
                            try {
                                image = reader.acquireLatestImage();
                                Log.e("CHECK", "acquireLatestImage: "+image);
                                if(image==null)
                                    return;
                                Log.e("CHECK", "onImageAvailable: "+image);
                                Rect rect = image.getCropRect();
                                Log.e("CHECK", "onImageAvailable: "+rect);
                                int getWidth = rect.width();
                                int getHeight = rect.height();
                                Log.e("CHECK", "onImageAvailable: "+getWidth);
                                Log.e("CHECK", "onImageAvailable: "+getHeight);
                                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                final byte[] bytes = new byte[buffer.capacity()];
                                buffer.get(bytes);
                                Log.e("CHECK", "onImageAvailable: "+bytes.length);
                                Bitmap rawBitmap = Bitmap.createBitmap(getWidth, getHeight, Bitmap.Config.ARGB_8888);
                                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                                rawBitmap.copyPixelsFromBuffer(byteBuffer);
                                Log.e("CHECK", "finalRotationAdjust: "+rawBitmap);
                                //회전
                                Bitmap rotatedBitmap;
                                Log.e("CHECK", "finalRotationAdjust3: "+finalRotationAdjust);
                                if(finalRotationAdjust==0){
                                    rotatedBitmap = rawBitmap;
                                } else {
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(finalRotationAdjust);
                                    rotatedBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.getWidth(), rawBitmap.getHeight(), matrix, true);
                                    rawBitmap.recycle();
                                }
                                Log.e("CHECK", "onImageAvailable: "+rotatedBitmap);
                                //확대
                                getWidth=rotatedBitmap.getWidth();
                                getHeight=rotatedBitmap.getHeight();
                                Log.e("CHECK", "onImageAvailable: "+width+"  ,  "+height+"  /   "+getWidth+"   ,  "+getHeight);
                                Bitmap zoomedBitmap;
                                if(width==getWidth&&height==getHeight){
                                    zoomedBitmap=rotatedBitmap;
                                } else {
                                    double widthRatio = (double)width / getWidth;
                                    double heightRatio = (double)height/ getHeight;
                                    if(widthRatio>heightRatio){
                                        zoomedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, width, (int)(getHeight*widthRatio), true);
                                    } else {
                                        zoomedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, (int)(getHeight*heightRatio), height, true);
                                    }
                                    rotatedBitmap.recycle();
                                }
                                Log.e("CHECK", "onImageAvailable: "+zoomedBitmap);

                                //자르기
                                getWidth=zoomedBitmap.getWidth();
                                getHeight=zoomedBitmap.getHeight();
                                Bitmap croppedBitmap;
                                Log.e("CHECK", "onImageAvailable: "+width+"  ,  "+height+"  /   "+getWidth+"   ,  "+getHeight);
                                if(width==getWidth&&height==getHeight){
                                    croppedBitmap=zoomedBitmap;
                                } else {
                                    Log.e("CHECK", "onImageAvailable: "+width+"  ,  "+height+"  /   "+getWidth+"   ,  "+getHeight);
                                    if(width==getWidth){
                                        croppedBitmap = Bitmap.createBitmap(zoomedBitmap, 0, (getHeight-height)/2, width, height);
                                    } else {
                                        croppedBitmap = Bitmap.createBitmap(zoomedBitmap, (getWidth-width)/2, 0, width, height);
                                    }
                                    zoomedBitmap.recycle();
                                }
                                Log.e("CHECK", "onImageAvailable: "+croppedBitmap);
                                final Bitmap bitmap = croppedBitmap;
                                new Handler(){
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        callback.onPictureTaken(bytes, bitmap);
                                    }
                                }.handleMessage(null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            finally {
                                if(image!=null)
                                    image.close();
                            }
                        }
                    };
                    break;
            }
            reader.setOnImageAvailableListener(imageAvailableListener, handler);

            final CameraCaptureSession.CaptureCallback previewSession=new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                    Log.e("CHECK", "onCaptureStarted");
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Log.e("CHECK", "onCaptureCompleted");
                    start(cameraView.getSurfaceTexture());
                }
            };

            camera.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.e("CHECK", "onConfigured");
                    try {
                        AndroidCamera2.this.previewSession=session;
                        session.capture(captureBuilder.build(),previewSession, handler);
                    } catch (Exception e) {
                        Log.e("CHECK", "Exception 1");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e("CHECK", "onConfigureFailed");

                }
            },handler);
        } catch (Exception e) {
            beingUsed=false;
            Log.e("CHECK", "Exception 2");
            e.printStackTrace();
            throw new CameraNotSupportException();
        }
    }

    @TargetApi(23)
    @Override
    public synchronized boolean startRecord(final Activity activity, CameraView cameraView, final File file, int width, int height, int bitRate, int frame, boolean recordSound) throws IOException, CameraNotSupportException {
        Log.e("CHECK", "startRecord1: "+beingUsed);
        if(beingUsed)
            return false;
        Log.e("CHECK", "startRecord1: "+activity);
        if(activity==null)
            return false;
        Log.e("CHECK", "startRecord1: "+camera);
        if(camera==null)
            throw new CameraNotSupportException();
        beingUsed=true;
        try {
            if(previewSession!=null){
                previewSession.close();
                previewSession = null;
            }
            mediaRecorder = new MediaRecorder();
            if(recordSound)
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            if(recordSound)
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setVideoSize(width, height);
            mediaRecorder.setOutputFile(file.getPath());
            if(foundFacing!=null&&foundFacing.intValue()==CameraCharacteristics.LENS_FACING_FRONT){
                Log.e("CHECK", "facing==Facing.FRONT");
                //TODO 좌우 변환 처리 - 확인필요
            }
            mediaRecorder.setVideoEncodingBitRate(bitRate);
            mediaRecorder.setVideoFrameRate(frame);
            mediaRecorder.prepare();

            SurfaceTexture surfaceTexture = cameraView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(cameraView.getWidth(), cameraView.getHeight());
            previewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);

            List<Surface> surfaces = new ArrayList<>();

            Surface surface = new Surface(surfaceTexture);
            surfaces.add(surface);
            previewBuilder.addTarget(surface);

            Surface surfaceForRecord = mediaRecorder.getSurface();
            surfaces.add(surfaceForRecord);
            previewBuilder.addTarget(surfaceForRecord);

            camera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {

                    Log.e(TAG, "onConfigured RRRR");
                    previewSession=session;
                    previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                    try {
                        previewSession.setRepeatingRequest(previewBuilder.build(), null, handler);
                    } catch (IllegalArgumentException e){
                        e.printStackTrace();
                        Log.e(TAG, "If the request references no Surfaces or references Surfaces that are not currently configured as outputs; or the request is a reprocess capture request; or the capture targets a Surface in the middle of being prepared; or the handler is null, the listener is not null, and the calling thread has no looper; or no requests were passed in");
                    } catch (CameraAccessException e){
                        e.printStackTrace();
                        Log.e(TAG, "if the camera device is no longer connected or has encountered a fatal error");
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                        Log.e(TAG, "if this session is no longer active, either because the session was explicitly closed, a new session has been created or the camera device has been closed.");
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AndroidCamera2.this.file=file;
                            mediaRecorder.start();
                        }
                    });
                    Log.e("CHECK", "startRecord1 ssss");

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e("CHECK", "startRecord1 ffff");
                }
            }, handler);
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
        Log.e("CHECK", "endRecord1");
        if(!beingUsed)
            return false;
        if(mediaRecorder==null)
            return false;
        if(camera==null)
            throw new CameraNotSupportException();
        try{
            previewSession.stopRepeating();
            previewSession.abortCaptures();
            boolean hasProblem = false;
            while(true) {
                try {
                    mediaRecorder.stop();
                    mediaRecorder.reset();
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
        } catch (Exception e){
            e.printStackTrace();
        }
        mediaRecorder=null;
        beingUsed=false;
        start(cameraView.getSurfaceTexture());
        Log.e("CHECK", "endRecord2");
        return true;
    }

    @Override
    public boolean isUsed() {
        return beingUsed;
    }

    @Override
    public void init(Activity activity, Facing facing, CameraView cameraView) {
        if(facing==null)
            facing=Facing.POSSIBLE;
        this.facing=facing;
    }

    public void resume()throws CameraNotSupportException{
        handlerThread = new HandlerThread("CameraBackground");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    @Override
    public void close() {
        if(handlerThread!=null){
            handlerThread.quitSafely();
            try {
                handlerThread.join();
                handlerThread = null;
                handler = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(beingUsed)
            if(mediaRecorder!=null)
                if(camera!=null){
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
                }
        try{
            if(camera!=null)
                camera.close();
            camera=null;
        } catch (Exception e){
            e.printStackTrace();
        }




    }

    public void start(SurfaceTexture surfaceTexture){
        if(previewSize==null||surfaceTexture==null){
            beingUsed = false;
        }
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Surface surface = new Surface(surfaceTexture);

        try{
            //A
            previewBuilder=camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //TODO 플래시 끄기 previewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            previewBuilder.addTarget(surface);
            //B
            camera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "onConfigured A");
                    previewSession=session;
                    previewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    try {
                        previewSession.setRepeatingRequest(previewBuilder.build(), null, handler);
                    } catch (IllegalArgumentException e){
                        e.printStackTrace();
                        Log.e(TAG, "If the request references no Surfaces or references Surfaces that are not currently configured as outputs; or the request is a reprocess capture request; or the capture targets a Surface in the middle of being prepared; or the handler is null, the listener is not null, and the calling thread has no looper; or no requests were passed in");
                    } catch (CameraAccessException e){
                        e.printStackTrace();
                        Log.e(TAG, "if the camera device is no longer connected or has encountered a fatal error");
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                        Log.e(TAG, "if this session is no longer active, either because the session was explicitly closed, a new session has been created or the camera device has been closed.");
                    } finally {
                        beingUsed = false;
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull  CameraCaptureSession session) {
                    beingUsed = false;
                    Log.e(TAG, "onConfigureFailed A");

                }
            },null);
        } catch (IllegalArgumentException e){
            beingUsed = false;
            e.printStackTrace();
            //A
            Log.e(TAG, "if the templateType is not supported by this device.");
            //B
            Log.e(TAG, "if the set of output Surfaces do not meet the requirements, the callback is null, or the handler is null but the current thread has no looper.");
        } catch (CameraAccessException e){
            beingUsed = false;
            e.printStackTrace();
            Log.e(TAG, "if the camera device is no longer connected or has encountered a fatal error");
        } catch (IllegalStateException e){
            beingUsed = false;
            e.printStackTrace();
            Log.e(TAG, "if the camera device has been closed");
        }
    }
}
