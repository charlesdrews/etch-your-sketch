package com.charlesdrews.etchyoursketch;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * This view provides dial functionality and notifies EtchView when the dials are turned.
 * <p>
 * Created by charlie on 1/3/17.
 */

public class Dial extends View {
    private static final String TAG = "Dial";

    public static final int HORIZONTAL = 123;
    public static final int VERTICAL = 456;

    private EtchView mEtchView;
    private int mOrientation, mActivePointerId;
    private float mCenterX = 0f, mCenterY = 0f, mStartAngle = 0f;

    public Dial(Context context, AttributeSet attrs) {
        super(context, attrs);

        setBackgroundResource(R.drawable.dial);

        mActivePointerId = MotionEvent.INVALID_POINTER_ID;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width < height ? width : height;
        setMeasuredDimension(size, size); // force to be square using smaller dimension

        mCenterX = size / 2f;
        mCenterY = size / 2f;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int index = MotionEventCompat.getActionIndex(event);
        int id = event.getPointerId(index);
        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {

            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 1) {
                    // If this is the only pointer, use it, otherwise ignore add'l pointers
                    mActivePointerId = id;
                    mStartAngle = getAngleFromCenterPoint(event.getX(), event.getY());
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId != MotionEvent.INVALID_POINTER_ID) {
                    float newAngle = getAngleFromCenterPoint(
                            event.getX(event.findPointerIndex(mActivePointerId)),
                            event.getY(event.findPointerIndex(mActivePointerId)));
                    float delta = newAngle - mStartAngle;
                    setRotation(getRotation() + delta);

                    if (mEtchView != null) {
                        mEtchView.etch(delta, mOrientation);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (id == mActivePointerId) {
                    mActivePointerId = MotionEvent.INVALID_POINTER_ID;
                    setRotation(getRotation() % 360f); // don't want thousands of degrees of rotation
                }
                break;
        }
        return true;
    }

    float getAngleFromCenterPoint(double x, double y) {
        double angleInRadians = Math.atan2(y - mCenterY, x - mCenterX);
        return (float) Math.toDegrees(angleInRadians); // float type is needed for setRotation()
    }

    void setEtchViewAndOrientation(EtchView etchView, int orientation) {
        mEtchView = etchView;
        mOrientation = orientation;
    }
}
