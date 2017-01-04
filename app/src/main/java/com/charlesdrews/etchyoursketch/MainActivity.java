package com.charlesdrews.etchyoursketch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private EtchView mEtchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Dial leftDial = (Dial) findViewById(R.id.left_dial);
        Dial rightDial = (Dial) findViewById(R.id.right_dial);
        mEtchView = (EtchView) findViewById(R.id.etch_view);

        leftDial.setEtchViewAndOrientation(mEtchView, Dial.HORIZONTAL);
        rightDial.setEtchViewAndOrientation(mEtchView, Dial.VERTICAL);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mEtchView.stopThread();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mEtchView.startThread();
    }
}
