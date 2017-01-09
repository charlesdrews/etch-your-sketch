package com.charlesdrews.etchyoursketch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.charlesdrews.etchyoursketch.options.color.ColorDialog;
import com.charlesdrews.etchyoursketch.gallery.GalleryActivity;
import com.charlesdrews.etchyoursketch.options.weight.WeightDialog;
import com.charlesdrews.etchyoursketch.options.weight.WeightView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class EtchActivity extends AppCompatActivity implements View.OnClickListener,
        ColorDialog.OnColorSelectedListener, WeightDialog.OnWeightSelectedListener,
        EtchView.OnEraseFinishedListener {


    public static final String FILE_PROVIDER_AUTHORITY = "com.charlesdrews.etchyoursketch.fileprovider";
    public static final int SHARE_REQUEST_CODE = 567;

    private static final String TAG = "EtchActivity";
    private static final String CURRENT_ETCHING_KEY = "currentEtching";
    private static final String SHOW_ERASE_MESSAGE_PREF_KEY = "showEraseMessage";

    private EtchView mEtchView;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private Bitmap mCurrentEtching;
    private ImageView mEraseIcon;

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
        mEtchView.setOnEraseFinishedListener(this);

        leftDial.setEtchViewAndOrientation(mEtchView, Dial.HORIZONTAL);
        rightDial.setEtchViewAndOrientation(mEtchView, Dial.VERTICAL);

        mEraseIcon = (ImageView) findViewById(R.id.menu_erase);

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

        mSensorManager.registerListener(mShakeDetector, mAccelerometer,
                SensorManager.SENSOR_DELAY_UI);
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
                WeightDialog.newInstance(mEtchView.getEtchLineWeight())
                        .show(getSupportFragmentManager(), "weightDialog");
                break;

            case R.id.menu_erase:
                erase();
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
    public void onColorSelected(int color) {
        mEtchView.setEtchColor(color);
    }

    @Override
    public void onWeightSelected(float etchWeightPx) {
        mEtchView.setEtchLineWeight(etchWeightPx);
    }

    @Override
    public void onEraseFinished() {
        ((ImageView) findViewById(R.id.menu_erase)).setColorFilter(
                ContextCompat.getColor(this, R.color.menu_icons));
    }

    private void erase() {
        if (mEtchView.isErasing()) {
            // if already in erase mode, exit erase mode
            onEraseFinished();
            mEtchView.setErasing(false);
        } else {
            // otherwise, enter erase mode
            mEraseIcon.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent));
            mEtchView.setErasing(true);
            showEraseMessage();
        }
    }

    private void showEraseMessage() {
        final SharedPreferences prefs = getSharedPreferences(
                getString(R.string.shared_prefs_key), MODE_PRIVATE);
        boolean showMsg = prefs.getBoolean(SHOW_ERASE_MESSAGE_PREF_KEY, true);

        if (showMsg) {
            Spanned message;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                message = Html.fromHtml(getString(R.string.erase_dialog_message), Html.FROM_HTML_MODE_LEGACY);
            } else {
                message = Html.fromHtml(getString(R.string.erase_dialog_message));
            }

            new AlertDialog.Builder(this)
                    .setTitle(R.string.erase_dialog_title)
                    .setMessage(message)
                    .setPositiveButton(R.string.erase_dialog_pos_button, null)
                    .setNeutralButton(R.string.erase_dialog_neutral_button, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean(SHOW_ERASE_MESSAGE_PREF_KEY, false);
                            editor.apply();
                        }
                    })
                    .show();
        }
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