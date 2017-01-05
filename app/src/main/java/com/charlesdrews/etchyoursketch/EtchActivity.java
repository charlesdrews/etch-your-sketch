package com.charlesdrews.etchyoursketch;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.charlesdrews.etchyoursketch.options.ColorDialog;

public class EtchActivity extends AppCompatActivity implements View.OnClickListener,
        ColorDialog.OnOptionsSelectedListener {

    private EtchView mEtchView;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etch);

        // Set up views
        Dial leftDial = (Dial) findViewById(R.id.left_dial);
        Dial rightDial = (Dial) findViewById(R.id.right_dial);
        mEtchView = (EtchView) findViewById(R.id.etch_view);

        leftDial.setEtchViewAndOrientation(mEtchView, Dial.HORIZONTAL);
        rightDial.setEtchViewAndOrientation(mEtchView, Dial.VERTICAL);

        // Set up shake detection
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                mEtchView.erasePartial(count);
            }
        });

        // Set up options menu
        findViewById(R.id.ic_color).setOnClickListener(this);
        findViewById(R.id.ic_weight).setOnClickListener(this);
        findViewById(R.id.ic_erase).setOnClickListener(this);
        findViewById(R.id.ic_share).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mEtchView.startThread();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(mShakeDetector);
        mEtchView.stopThread();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.ic_color:
                DialogFragment optionsDialog = ColorDialog.newInstance(mEtchView.getEtchColor());
                optionsDialog.show(getSupportFragmentManager(), "optionsDialog");
                break;

            case R.id.ic_weight:
                break;

            case R.id.ic_erase:
                break;

            case R.id.ic_share:
                break;
        }

    }

    @Override
    public void onColorOptionSelected(int color) {
        mEtchView.setEtchColor(color);
    }
}
