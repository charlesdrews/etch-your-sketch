package com.charlesdrews.etchyoursketch;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Detect and quantify shakes so they can be translated into erasing the etchings.
 * <p>
 * Created by charlie on 1/4/17.
 */

public class ShakeDetector implements SensorEventListener {

    private static final float SHAKE_THRESHOLD_G_FORCE = 2.0f;
    private static final int MS_TO_WAIT_BEFORE_CONSIDERING_NEXT_SHAKE = 100;
    private static final int MS_TO_WAIT_BEFORE_RESETTING_SHAKE_COUNT = 3000;

    private OnShakeListener mListener;
    private long mLastShakeTime = 0;
    private int mShakeCount = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mListener != null) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD_G_FORCE) {
                final long time = System.currentTimeMillis();

                // ignore shake events too close to each other (500ms)
                if (mLastShakeTime + MS_TO_WAIT_BEFORE_CONSIDERING_NEXT_SHAKE > time) {
                    return;
                }

                // reset the shake count after 3 seconds of no shakes
                if (mLastShakeTime + MS_TO_WAIT_BEFORE_RESETTING_SHAKE_COUNT < time) {
                    mShakeCount = 0;
                }

                mLastShakeTime = time;
                mShakeCount++;

                mListener.onShake(mShakeCount);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void setOnShakeListener(OnShakeListener listener) {
        mListener = listener;
    }

    public interface OnShakeListener {
        void onShake(int count);
    }
}
