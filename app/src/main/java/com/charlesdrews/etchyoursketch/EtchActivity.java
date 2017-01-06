package com.charlesdrews.etchyoursketch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.charlesdrews.etchyoursketch.gallery.GalleryActivity;
import com.charlesdrews.etchyoursketch.colors.ColorDialog;

import java.io.FileOutputStream;
import java.io.IOException;

public class EtchActivity extends AppCompatActivity implements View.OnClickListener,
        ColorDialog.OnOptionsSelectedListener {

    private static final String TAG = "EtchActivity";

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
        findViewById(R.id.menu_color).setOnClickListener(this);
        findViewById(R.id.menu_weight).setOnClickListener(this);
        findViewById(R.id.menu_erase).setOnClickListener(this);
        findViewById(R.id.menu_save).setOnClickListener(this);
        findViewById(R.id.menu_gallery).setOnClickListener(this);
        findViewById(R.id.menu_share).setOnClickListener(this);
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
            case R.id.menu_color:
                ColorDialog.newInstance(mEtchView.getEtchColor())
                        .show(getSupportFragmentManager(), "colorDialog");
                break;
            case R.id.menu_weight:
                break;
            case R.id.menu_erase:
                break;
            case R.id.menu_save:
                saveEtching();
                break;
            case R.id.menu_gallery:
                startActivity(new Intent(this, GalleryActivity.class));
                break;
            case R.id.menu_share:
                break;
        }
    }

    @Override
    public void onColorOptionSelected(int color) {
        mEtchView.setEtchColor(color);
    }

    public void saveEtching() {
        //TODO - move off UI thread?
        try {
            FileOutputStream outputStream = openFileOutput(
                    "etch" + System.currentTimeMillis() + ".png", MODE_PRIVATE);
            mEtchView.getEtchBitmap()
                    .compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            Toast.makeText(this, R.string.save_success_msg, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, R.string.save_error_msg, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
