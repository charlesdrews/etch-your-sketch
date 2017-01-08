package com.charlesdrews.etchyoursketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.charlesdrews.etchyoursketch.options.weight.WeightDialog;

import java.util.Random;

/**
 * This view provides the etching surface and responds to the rotation of the dials.
 * <p>
 * Created by charlie on 1/3/17.
 */

public class EtchView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "EtchView";

    private static final float ETCH_SEGMENT_LENGTH = 3.0f;
    private static final float POINTER_LINE_WIDTH = 2.0f;
    private static final float POINTER_SEGMENT_LENGTH = 20.0f;

    private static final int PARTIAL_ERASE_MIN_SHAKES = 3;
    private static final int PARTIAL_ERASE_CIRCLE_COUNT = 50;
    private static final float PARTIAL_ERASE_CIRCLE_RADIUS = 50f;
    private static final int FULL_ERASE_SHAKE_THRESHOLD = 10;

//    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private SurfaceHolder mSurfaceHolder;
    private boolean mSurfaceReady = false, mReadyToDraw = false;
    private Bitmap mBitmap;
    private Canvas mBitmapCanvas;
    private Paint mEtchPaint, mPointerPaint;
    private int mWidth = 0, mHeight = 0, mBackgroundColor;
    private float mX = 0f, mY = 0f;
    private Random mEraseRandom = new Random(System.currentTimeMillis());

    public EtchView(Context context, AttributeSet attrs) {
        super(context, attrs);

//        mHandlerThread = new HandlerThread("backgroundThread");
//        mHandlerThread.start();
//        mHandler = new Handler(mHandlerThread.getLooper());

        // The UX is better using the UI thread - otherwise "etch jobs" queue up and keep executing
        // after the user has stopped touching the dials, which is unpleasant.
        mHandler = new Handler(Looper.getMainLooper());

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        mEtchPaint = new Paint();
        mEtchPaint.setStrokeWidth(WeightDialog.ETCH_LINE_WEIGHTS[0]); // default weight
        mEtchPaint.setColor(ContextCompat.getColor(context, R.color.etchBlack));

        mPointerPaint = new Paint();
        mPointerPaint.setStrokeWidth(POINTER_LINE_WIDTH);
        mPointerPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

        mBackgroundColor = ContextCompat.getColor(context, R.color.etchBackground);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceReady = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth = width;
        mHeight = height;
        mX = width / 2f;
        mY = height / 2f;
        mReadyToDraw = true;

        if (mBitmap == null) {
            // Brand new instance of EtchView, so initialize the bitmap
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mBitmapCanvas = new Canvas(mBitmap);
            fillBackground(mBackgroundColor);
        } else {
            // Otherwise, use the existing, restored bitmap
            mBitmapCanvas = new Canvas(mBitmap);
            drawBitmapToSurfaceCanvas();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceReady = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mX = event.getX();
                mY = event.getY();
                drawBitmapToSurfaceCanvas();
                break;
        }
        return true;
    }

    public void etch(final float angleDelta, final int orientation) {
//        if (mHandlerThread.isAlive() && mReadyToDraw) {
        if (mReadyToDraw) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    float distance = (angleDelta < 0) ? -ETCH_SEGMENT_LENGTH : ETCH_SEGMENT_LENGTH;

                    switch (orientation) {
                        case Dial.HORIZONTAL:
                            float newX = normalizeCoordinate(mX + distance, mWidth);
                            mBitmapCanvas.drawLine(mX, mY, newX, mY, mEtchPaint);
                            mX = newX;
                            break;

                        case Dial.VERTICAL:
                            // use the opposite of distance since + is down in canvas coordinates
                            float newY = normalizeCoordinate(mY - distance, mHeight);
                            mBitmapCanvas.drawLine(mX, mY, mX, newY, mEtchPaint);
                            mY = newY;
                            break;
                    }
                    drawBitmapToSurfaceCanvas();
                }
            });
        }
    }

    private float normalizeCoordinate(float coordinate, float max) {
        if (coordinate < 0) {
            return mEtchPaint.getStrokeWidth() / 2f;
        } else if (coordinate > max) {
            return max - (mEtchPaint.getStrokeWidth() / 2f);
        } else {
            return coordinate;
        }
    }

    private void fillBackground(final int color) {
//        if (mHandlerThread.isAlive() && mReadyToDraw) {
        if (mReadyToDraw) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBitmapCanvas.drawColor(color);
                    drawBitmapToSurfaceCanvas();
                }
            });
        }
    }

    public void erasePartial(final int shakeCount) {
        if (shakeCount > FULL_ERASE_SHAKE_THRESHOLD) {
            fillBackground(mBackgroundColor);
        } else if (shakeCount > PARTIAL_ERASE_MIN_SHAKES) {
//            if (mHandlerThread.isAlive() && mReadyToDraw) {
            if (mReadyToDraw) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Paint paint = new Paint();
                        paint.setColor(mBackgroundColor);
                        for (int i = 0; i < PARTIAL_ERASE_CIRCLE_COUNT; i++) {
                            mBitmapCanvas.drawCircle(mEraseRandom.nextInt(mWidth),
                                    mEraseRandom.nextInt(mHeight), PARTIAL_ERASE_CIRCLE_RADIUS,
                                    paint);
                            drawBitmapToSurfaceCanvas();
                        }
                    }
                });
            }
        }
    }

    private void drawBitmapToSurfaceCanvas() {
        if (mSurfaceHolder.getSurface().isValid()) {
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

    public void setEtchColor(int color) {
        mEtchPaint.setColor(color);
    }

    public int getEtchColor() {
        return mEtchPaint.getColor();
    }

    public float getEtchLineWeight() {
        return mEtchPaint.getStrokeWidth();
    }

    public void setEtchLineWeight(float etchLineWeight) {
        mEtchPaint.setStrokeWidth(etchLineWeight);
    }

    public Bitmap getEtchBitmap() {
        return mBitmap;
    }

    public void stopEtching() {
//        mHandlerThread.quit();
        mReadyToDraw = false;
    }

    public void readyToEtch() {
//        if (!mHandlerThread.isAlive()) {
//            mHandlerThread.start();
//        }
        if (mSurfaceReady) {
            mReadyToDraw = true;
        }
    }

    public void restoreEtching(Bitmap etching) {
        Log.d(TAG, "restoreEtching: ");
        mBitmap = etching;
        drawBitmapToSurfaceCanvas();
    }

    //TODO: Remote display? https://developers.google.com/cast/docs/remote
}
