package com.charlesdrews.etchyoursketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * This view provides the etching surface and responds to the rotation of the dials.
 * <p>
 * Created by charlie on 1/3/17.
 */

public class EtchView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "EtchView";

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private SurfaceHolder mSurfaceHolder;
    private boolean mSurfaceReady = false, mReadyToDraw = false;
    private Bitmap mBitmap;
    private Canvas mBitmapCanvas;
    private Paint mEtchPaint, mPointerPaint;
    private int mWidth = 0, mHeight = 0;
    private float mEtchLineWidth, mEtchSegmentLength, mPointerSegmentLength;
    private float mX = 0f, mY = 0f;

    public EtchView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mHandlerThread = new HandlerThread("backgroundThread");
        mHandlerThread.start();
//        mHandler = new Handler(mHandlerThread.getLooper());

        // The UX is better using the UI thread - otherwise "etch jobs" queue up and keep executing
        // after the user has stopped touching the dials, which is unpleasant.
        mHandler = new Handler(Looper.getMainLooper());

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        mEtchPaint = new Paint();
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.etch_line_width, outValue, true);
        mEtchLineWidth = outValue.getFloat();
        mEtchPaint.setStrokeWidth(mEtchLineWidth);
        mEtchPaint.setColor(ContextCompat.getColor(context, R.color.etchLineColor));

        mPointerPaint = new Paint();
        getResources().getValue(R.dimen.pointer_line_width, outValue, true);
        mPointerPaint.setStrokeWidth(outValue.getFloat());
        mPointerPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

        getResources().getValue(R.dimen.pointer_segment_length, outValue, true);
        mPointerSegmentLength = outValue.getFloat();

        getResources().getValue(R.dimen.etch_segment_length, outValue, true);
        mEtchSegmentLength = outValue.getFloat();
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

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mBitmapCanvas = new Canvas(mBitmap);

        fillBackground(ContextCompat.getColor(getContext(), R.color.etchBackground));
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceReady = false;
    }

    public void etch(final float angleDelta, final int orientation) {
        if (mHandlerThread.isAlive() && mReadyToDraw) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    float distance = (angleDelta < 0) ? -mEtchSegmentLength : mEtchSegmentLength;

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
            return mEtchLineWidth / 2f;
        } else if (coordinate > max) {
            return max - (mEtchLineWidth / 2f);
        } else {
            return coordinate;
        }
    }

    private void fillBackground(final int color) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mReadyToDraw) {
                    mBitmapCanvas.drawColor(color);
                    drawBitmapToSurfaceCanvas();
                }
            }
        });
    }

    private void drawBitmapToSurfaceCanvas() {
        if (mSurfaceHolder.getSurface().isValid()) {
            Canvas canvas = mSurfaceHolder.lockCanvas();

            canvas.drawBitmap(mBitmap, 0, 0, null);

            // pointer
            canvas.drawLine(mX, mY, mX + mPointerSegmentLength, mY, mPointerPaint);
            canvas.drawLine(mX, mY, mX - mPointerSegmentLength, mY, mPointerPaint);
            canvas.drawLine(mX, mY, mX, mY + mPointerSegmentLength, mPointerPaint);
            canvas.drawLine(mX, mY, mX, mY - mPointerSegmentLength, mPointerPaint);

            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void stopThread() {
        mHandlerThread.quit();
        mReadyToDraw = false;
    }

    public void startThread() {
        if (!mHandlerThread.isAlive()) {
            mHandlerThread.start();
        }
        if (mSurfaceReady) {
            mReadyToDraw = true;
        }
    }
}
