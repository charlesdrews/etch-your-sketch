package com.charlesdrews.etchyoursketch.colors;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.charlesdrews.etchyoursketch.R;

import java.util.ArrayList;

/**
 * Dialog which allows the user to select different etching options
 * <p>
 * Created by charlie on 1/5/17.
 */

public class ColorDialog extends DialogFragment {

    private static final String CURRENT_COLOR_KEY = "currentColor";

    private OnOptionsSelectedListener mListener;

    public static ColorDialog newInstance(int currentColor) {
        ColorDialog colorDialog = new ColorDialog();

        Bundle args = new Bundle();
        args.putInt(CURRENT_COLOR_KEY, currentColor);
        colorDialog.setArguments(args);

        return colorDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create grid layout to hold color views
        final ColorGrid colorGrid = new ColorGrid(getContext(), null);

        // Generate color views to fill grid layout
        int[] colors = getResources().getIntArray(R.array.etchColors);
        int currentColor = getArguments().getInt(CURRENT_COLOR_KEY, -1);

        ArrayList<ColorView> colorViews = new ArrayList<>(colors.length);

        for (int color : colors) {
            final ColorView colorView = new ColorView(getContext(), null, color);
            colorView.setSelected(color == currentColor);
            colorView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    colorGrid.selectColor(colorView);
                    mListener.onColorOptionSelected(colorView.getColor());
                    dismiss();
                }
            });
            colorViews.add(colorView);
        }

        colorGrid.addColorViews(colorViews);

        // Set up dialog and populate with color grid
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(getString(R.string.color_dialog_title))
                .setNegativeButton(R.string.dialog_negative_button, null)
                .setView(colorGrid);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnOptionsSelectedListener) {
            mListener = (OnOptionsSelectedListener) context;
        } else {
            throw new ClassCastException(context.getClass().getName()
                    + " must implement " + OnOptionsSelectedListener.class.getName());
        }
    }

    public interface OnOptionsSelectedListener {
        void onColorOptionSelected(int color);
    }
}
