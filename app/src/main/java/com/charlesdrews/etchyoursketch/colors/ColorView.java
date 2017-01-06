package com.charlesdrews.etchyoursketch.colors;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.charlesdrews.etchyoursketch.R;

/**
 * Created by charlie on 1/5/17.
 */

public class ColorView extends ImageView {

    private static final int DEFAULT_SIZE_DP = 48;
    private static final int DEFAULT_MARGIN_DP = 6;

    private int mSizePx, mMarginPx, mColor;
    private boolean mSelected = false;

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSizePx = dpToPx(DEFAULT_SIZE_DP);
        mMarginPx = dpToPx(DEFAULT_MARGIN_DP);
        mColor = ContextCompat.getColor(context, R.color.etchBlack);
        init();
    }

    public ColorView(Context context, AttributeSet attrs, int color) {
        super(context, attrs);
        mSizePx = dpToPx(DEFAULT_SIZE_DP);
        mMarginPx = dpToPx(DEFAULT_MARGIN_DP);
        mColor = color;
        init();
    }

    public ColorView(Context context, AttributeSet attrs, int color, int sizeDp, int marginDp) {
        super(context, attrs);
        mSizePx = dpToPx((sizeDp > 0) ? sizeDp : DEFAULT_SIZE_DP);
        mMarginPx = dpToPx((marginDp >= 0) ? marginDp : DEFAULT_MARGIN_DP);
        mColor = color;
        init();
    }

    private void init() {
        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(mColor);
        setBackground(drawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = resolveSizeAndState(mSizePx, widthMeasureSpec, 0);
        int height = resolveSizeAndState(mSizePx, heightMeasureSpec, 0);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        ViewGroup.MarginLayoutParams margins = (ViewGroup.MarginLayoutParams) getLayoutParams();
        margins.topMargin = mMarginPx;
        margins.rightMargin = mMarginPx;
        margins.bottomMargin = mMarginPx;
        margins.leftMargin = mMarginPx;
        setLayoutParams(margins);
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
        if (mSelected) {
            setImageResource(R.drawable.checkmark);
        } else {
            setImageResource(0);
        }
    }

    public boolean isSelected() {
        return mSelected;
    }

    public int getColor() {
        return mColor;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().scaledDensity);
    }
}