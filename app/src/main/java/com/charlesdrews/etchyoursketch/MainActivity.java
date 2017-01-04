package com.charlesdrews.etchyoursketch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Dial leftDial = (Dial) findViewById(R.id.left_dial);
        Dial rightDial = (Dial) findViewById(R.id.right_dial);
        EtchView etchView = (EtchView) findViewById(R.id.etch_view);

        leftDial.setEtchViewAndOrientation(etchView, Dial.HORIZONTAL);
        rightDial.setEtchViewAndOrientation(etchView, Dial.VERTICAL);
    }
}
