package com.herblinker.android.libraries.base.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by RedEye on 2018-11.
 */
public class Signature extends View {
    private static final float TOUCH_TOLERANCE = 3;
    public Bitmap forUpdateBitmap;
    private boolean isUsed;
    private int width, height;
    private Bitmap bitmap;
    private Canvas canvas;
    private Path path;
    private Paint bitmapPaint;
    private Paint paint;
    private float pX, pY;

    public Signature(Context c, AttributeSet attrs){
        super(c, attrs);
        path = new Path();
        bitmapPaint = new Paint();
        bitmapPaint.setDither(true);
        bitmapPaint.setColor(Color.BLACK);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        super.onSizeChanged(w, h, oldw, oldh);
        width=w;
        height=h;
        if(forUpdateBitmap!=null){
            Bitmap temp = forUpdateBitmap;
            forUpdateBitmap=null;
            temp = Bitmap.createScaledBitmap(temp, width, height, true);
            bitmap = temp.copy(Bitmap.Config.ARGB_8888 ,true);
            canvas = new Canvas(bitmap);
            isUsed=true;
            invalidate();
        } else {
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        //canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
        canvas.drawPath(path, paint);
    }

    private void TouchStart(float x, float y)
    {
        path.reset();
        path.moveTo(x, y);
        pX = x;
        pY = y;
    }

    private void TouchMove(float x, float y){
        float dx = Math.abs(x - pX);
        float dy = Math.abs(y - pY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            path.quadTo(pX, pY, (x + pX) / 2, (y + pY) / 2);
            pX = x;
            pY = y;
        }
    }

    private void TouchUp(){
        path.lineTo(pX, pY);
        canvas.drawPath(path, paint);
        path.reset();
        isUsed=true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e){
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()){
            case MotionEvent.ACTION_DOWN:
                TouchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                TouchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                TouchUp();
                invalidate();
                break;
        }
        return true;
    }

    public void clearCanvas(){
        if(bitmap!=null)
            try {
                bitmap.recycle();
            }catch (Exception e){
                e.printStackTrace();
            }
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        isUsed=false;
        invalidate();
    }

    public Bitmap getCanvasBitmap(){
        return bitmap;
    }
    public boolean isSigned(){
        return isUsed;
    }
}