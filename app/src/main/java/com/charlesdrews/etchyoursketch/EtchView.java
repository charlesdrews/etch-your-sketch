package com.charlesdrews.etchyoursketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by charlie on 1/3/17.
 */

public class EtchView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "EtchView";

    public static final float POINTER_SEGMENT_LENGTH = 20f;

//    private HandlerThread mHandlerThread;
//    private Handler mHandler;
    private SurfaceHolder mSurfaceHolder;
    private boolean mSurfaceReady = false;
    private Bitmap mBitmap;
    private Canvas mBitmapCanvas;
    private Paint mEtchPaint, mPointerPaint;
    private int mWidth = 0, mHeight = 0;
    private float mX = 0f, mY = 0f;

    public EtchView(Context context, AttributeSet attrs) {
        super(context, attrs);

//        mHandlerThread = new HandlerThread("backgroundThread");
//        mHandlerThread.start();
//        mHandler = new Handler(mHandlerThread.getLooper());

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        mEtchPaint = new Paint();
        mEtchPaint.setStrokeWidth(getResources().getDimension(R.dimen.etch_line_width));
        mEtchPaint.setColor(ContextCompat.getColor(context, R.color.etchLineColor));

        mPointerPaint = new Paint();
        mPointerPaint.setStrokeWidth(getResources().getDimension(R.dimen.pointer_line_width));
        mPointerPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
        mX = width / 2f;
        mY = height / 2f;
        mSurfaceReady = true;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmap);

        fillBackground(ContextCompat.getColor(getContext(), R.color.etchBackground));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceReady = false;
    }

    public void etch(float deltaX, float deltaY) {
        if (mSurfaceReady && mSurfaceHolder.getSurface().isValid()) {
            float newX = mX + deltaX;
            float newY = mY + deltaY;

            mBitmapCanvas.drawLine(mX, mY, newX, newY, mEtchPaint);

            mX = newX;
            mY = newY;

            drawBitmapToSurfaceCanvas();
        }
    }

    private void fillBackground(int color) {
        if (mSurfaceReady && mSurfaceHolder.getSurface().isValid()) {
            mBitmapCanvas.drawColor(color);
            drawBitmapToSurfaceCanvas();
        }
    }

    private void drawBitmapToSurfaceCanvas() {
        Canvas canvas = mSurfaceHolder.lockCanvas();

        canvas.drawBitmap(mBitmap, 0, 0, null);

        // pointer
        canvas.drawLine(mX, mY, mX + POINTER_SEGMENT_LENGTH, mY, mPointerPaint);
        canvas.drawLine(mX, mY, mX - POINTER_SEGMENT_LENGTH, mY, mPointerPaint);
        canvas.drawLine(mX, mY, mX, mY + POINTER_SEGMENT_LENGTH, mPointerPaint);
        canvas.drawLine(mX, mY, mX, mY - POINTER_SEGMENT_LENGTH, mPointerPaint);

        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }
}
