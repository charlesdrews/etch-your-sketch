package com.charlesdrews.etchyoursketch.options.color;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.GridLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by charlie on 1/5/17.
 */

public class ColorGrid extends GridLayout {

    private static final int DEFAULT_COLUMN_COUNT = 4;
    private static final int DEFAULT_HORIZ_PAD_DP = 20;
    private static final int DEFAULT_VERT_PAD_DP = 10;

    private int mColumns, mHorizPadPx, mVertPadPx;
    private List<ColorView> mColorViews;

    public ColorGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        mColumns = DEFAULT_COLUMN_COUNT;
        mHorizPadPx = dpToPx(DEFAULT_HORIZ_PAD_DP);
        mVertPadPx = dpToPx(DEFAULT_VERT_PAD_DP);
        init();
    }

    public ColorGrid(Context context, AttributeSet attrs, int columns, int horizPadDp, int vertPadDp) {
        super(context, attrs);
        mColumns = columns;
        mHorizPadPx = dpToPx(horizPadDp);
        mVertPadPx = dpToPx(vertPadDp);
        init();
    }

    private void init() {
        mColorViews = new ArrayList<>();
        setColumnCount(mColumns);
        setPadding(mHorizPadPx, mVertPadPx, mHorizPadPx, mVertPadPx);
        for (ColorView colorView : mColorViews) {
            addView(colorView);
        }
    }

    public void addColorViews(@NonNull List<ColorView> colorViews) {
        for (ColorView colorView : colorViews) {
            addColorView(colorView);
        }
    }

    public void addColorView(@NonNull ColorView colorView) {
        mColorViews.add(colorView);
        addView(colorView);
    }

    public void selectColor(ColorView colorView) {
        for (ColorView cv : mColorViews) {
            setSelected(cv.equals(colorView));
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().scaledDensity);
    }
}
