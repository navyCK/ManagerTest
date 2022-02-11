package com.herblinker.android.libraries.base.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.nio.ByteBuffer;

public class BitmapHelper {
    /**
     * 이미지 저장 포맷에 맞는 정보 bytes를 비트맵으로 변환
     * @param bytes
     * @return
     */
    public static Bitmap bytesToBitmap(byte[] bytes){
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 이미지 Raw 정보 bytes를 비트맵으로 변환.
     * @param bytes
     * @param width
     * @param height
     * @return
     */
    public static Bitmap bytesToBitmap(byte[] bytes, int width, int height){
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return bytesToBitmap(byteBuffer, width, height);
    }

    /**
     * 이미지 Raw 정보 bytes를 비트맵으로 변환.
     * @param byteBuffer
     * @param width
     * @param height
     * @return
     */
    public static Bitmap bytesToBitmap(ByteBuffer byteBuffer, int width, int height){
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(byteBuffer);
        return bitmap;
    }

    /**
     * 이미지를 확대 또는 축소함
     * @param from
     * @param widthRatio 가로 확대비율
     * @param heightRatio 세로 확대비율
     * @param recycle
     * @return
     */
    public static Bitmap zoom(Bitmap from, double widthRatio, double heightRatio, boolean recycle){
        return zoom(from, (int)(from.getWidth()*widthRatio), (int)(from.getHeight()*heightRatio) , recycle);
    }
    /**
     * 이미지를 확대 또는 축소함
     * @param from
     * @param width 가로
     * @param height 세로
     * @param recycle
     * @return
     */
    public static Bitmap zoom(Bitmap from, int width, int height, boolean recycle){
        Bitmap to = Bitmap.createScaledBitmap(from, width, height, true);
        if(recycle)
            from.recycle();
        return to;
    }

    /**
     * 이미지를 확대 또는 축소함
     * @param from
     * @param ratio
     * @param recycle
     * @return
     */
    public static Bitmap zoom(Bitmap from, double ratio, boolean recycle){
        return zoom(from, (int)(from.getWidth()*ratio), (int)(from.getHeight()*ratio) , recycle);
    }

    /**
     * 가로에 맞게 확대 또는 축소함
     * @param from
     * @param width
     * @param recycle
     * @return
     */
    public static Bitmap zoomToWidth(Bitmap from, int width, boolean recycle){
        double ratio = (double)width / from.getWidth();
        return zoom(from, ratio, recycle);
    }
    /**
     * 세로에 맞게 확대 또는 축소함
     * @param from
     * @param height
     * @param recycle
     * @return
     */
    public static Bitmap zoomToHeight(Bitmap from, int height, boolean recycle){
        double ratio = (double)height / from.getHeight();
        return zoom(from, ratio, recycle);
    }

    public static Bitmap fitToFrameIn(Bitmap from, int width, int height, boolean recycle){
        int getWidth = from.getWidth();
        int getHeight = from.getHeight();
        double widthRatio = (double)width / getWidth;
        double heightRatio = (double)height / getHeight;
        if(widthRatio>heightRatio)
            return zoom(from, width, (int)(getHeight*widthRatio), recycle);
        else
            return zoom(from, (int)(getHeight*heightRatio), height, recycle);
    }
    public static Bitmap fitToFrameOut(Bitmap from, int width, int height, boolean recycle){
        int getWidth = from.getWidth();
        int getHeight = from.getHeight();
        double widthRatio = (double)width / getWidth;
        double heightRatio = (double)height / getHeight;
        if(widthRatio<heightRatio)
            return zoom(from, width, (int)(getHeight*widthRatio), recycle);
        else
            return zoom(from, (int)(getHeight*heightRatio), height, recycle);
    }
    public static Bitmap crop(Bitmap from, int width, int height, int widthOffset, int heightOffset, boolean recycle) {
        int croppedWidth = width-from.getWidth()-widthOffset;
        int croppedHeight = height-from.getWidth()-heightOffset;
        Bitmap to = Bitmap.createBitmap(from, width, height, croppedWidth,croppedHeight);
        if(recycle)
            from.recycle();
        return to;
    }
    public static Bitmap setInCenter(Bitmap from, int width, int height, double frameInnerWitdhOffsetRatio, int frameInnerHeightOffsetRatio, boolean recycle) {
        int getWidth=from.getWidth();
        int getHeight=from.getHeight();
        if(width==getWidth&&height==getHeight)
            return from;
        Bitmap to;
        if(width==getWidth)
            to = Bitmap.createBitmap(from, 0, (getHeight-height)/2, width, height);
        else
            to = Bitmap.createBitmap(from, (getWidth - width) / 2, 0, width, height);
        if(recycle)
            from.recycle();
        return to;
    }
}
