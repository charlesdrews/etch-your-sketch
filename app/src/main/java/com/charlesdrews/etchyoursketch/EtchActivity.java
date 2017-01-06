package com.charlesdrews.etchyoursketch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.charlesdrews.etchyoursketch.colors.ColorDialog;
import com.charlesdrews.etchyoursketch.gallery.GalleryActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EtchActivity extends AppCompatActivity implements View.OnClickListener,
        ColorDialog.OnOptionsSelectedListener {


    public static final String FILE_PROVIDER_AUTHORITY = "com.charlesdrews.etchyoursketch.fileprovider";
    public static final int SHARE_REQUEST_CODE = 567;

    private static final String TAG = "EtchActivity";
    private static final String CURRENT_ETCHING_KEY = "currentEtching";

    private EtchView mEtchView;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private Bitmap mCurrentEtching;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_etch);

        // Restore current etching if in bundle
        if (savedInstanceState != null) {
            mCurrentEtching = savedInstanceState.getParcelable(CURRENT_ETCHING_KEY);
        }

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SHARE_REQUEST_CODE) {
            File cacheDir = new File(getCacheDir(), "images");
            for (File file : cacheDir.listFiles()) {
                file.delete();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCurrentEtching != null) {
            mEtchView.restoreEtching(mCurrentEtching);
        }

        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mEtchView.readyToEtch();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mCurrentEtching = mEtchView.getEtchBitmap();
        mSensorManager.unregisterListener(mShakeDetector);
        mEtchView.stopEtching();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        mCurrentEtching = mEtchView.getEtchBitmap();
        outState.putParcelable(CURRENT_ETCHING_KEY, mCurrentEtching);

        super.onSaveInstanceState(outState, outPersistentState);
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
                shareEtching();
                break;
        }
    }

    @Override
    public void onColorOptionSelected(int color) {
        mEtchView.setEtchColor(color);
    }

    public void saveEtching() {
        File file = saveBitmapToFile(false, mEtchView.getEtchBitmap());

        if (file != null) {
            Toast.makeText(this, R.string.save_success_msg, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.save_error_msg, Toast.LENGTH_SHORT).show();
        }
    }

    public void shareEtching() {
        File file = saveBitmapToFile(true, mEtchView.getEtchBitmap());
        if (file != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");

            Uri uri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));

            startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.share_chooser_title)),
                    SHARE_REQUEST_CODE);
        } else {
            Toast.makeText(this, R.string.share_fail_message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Saves the provided bitmap as a png file in the app's internal storage.
     *
     * @param temporary indicates whether to use the cache directory or regular directory.
     * @param bitmap    is the image to save.
     * @return saved file if successful, else null.
     */
    private File saveBitmapToFile(boolean temporary, Bitmap bitmap) {
        //TODO - move off UI thread?

        File path = getSaveDirectory(temporary);
        File file;
        try {
            // Create file, either in cache or in regular storage, as needed
            if (temporary) {
                file = File.createTempFile("tempEtch", ".png", path);
            } else {
                file = new File(path, "etch" + System.currentTimeMillis() + ".png");
                file.createNewFile();
            }

            // Write the current etching out to the file
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();
            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private File getSaveDirectory(boolean temporary) {
        File path = new File((temporary ? getCacheDir() : getFilesDir()), "images");
        if (!path.exists()) {
            if (!path.mkdir()) {
                Log.w(TAG, "saveBitmapToFile: Unable to create path " + path.getAbsolutePath());
                return null;
            }
        }
        return path;
    }
}