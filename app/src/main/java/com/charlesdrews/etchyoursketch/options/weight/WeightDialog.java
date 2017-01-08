package com.charlesdrews.etchyoursketch.options.weight;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.LinearLayout;

import com.charlesdrews.etchyoursketch.R;

/**
 * Created by charlie on 1/8/17.
 */

public class WeightDialog extends DialogFragment {

    public static final float[] ETCH_LINE_WEIGHTS = {4f, 8f, 16f, 32f};
    private static final int HORIZ_PAD_DP = 20;
    private static final int VERT_PAD_DP = 10;
    private static final String CURRENT_WEIGHT_KEY = "currentWeight";

    private OnWeightSelectedListener mListener;

    public static WeightDialog newInstance(float currentWeight) {
        WeightDialog weightDialog = new WeightDialog();

        Bundle args = new Bundle();
        args.putFloat(CURRENT_WEIGHT_KEY, currentWeight);
        weightDialog.setArguments(args);

        return weightDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        float currentWeight = getArguments().getFloat(CURRENT_WEIGHT_KEY, -1);

        // Set up WeightViews
        LinearLayout linearLayout = new LinearLayout(getContext());
        int horizPad = dpToPx(HORIZ_PAD_DP);
        int vertPad = dpToPx(VERT_PAD_DP);
        linearLayout.setPadding(horizPad, vertPad, horizPad, vertPad);

        for (float weight : ETCH_LINE_WEIGHTS) {
            final WeightView weightView = new WeightView(getContext(), null, weight);
            weightView.setSelected(weight == currentWeight);
            weightView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onWeightSelected(weightView.getWeight());
                    dismiss();
                }
            });
            linearLayout.addView(weightView);
        }

        // Set up dialog and populate with WeightViews
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(getString(R.string.weight_dialog_title))
                .setNegativeButton(R.string.dialog_negative_button, null)
                .setView(linearLayout);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnWeightSelectedListener) {
            mListener = (OnWeightSelectedListener) context;
        } else {
            throw new ClassCastException(context.getClass().getName()
                    + " must implement " + OnWeightSelectedListener.class.getName());
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().scaledDensity);
    }

    public interface OnWeightSelectedListener {
        void onWeightSelected(float etchWeightPx);
    }
}
