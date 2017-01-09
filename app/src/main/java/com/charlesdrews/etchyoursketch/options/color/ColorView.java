package com.charlesdrews.etchyoursketch.options.color;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.charlesdrews.etchyoursketch.R;
import com.charlesdrews.etchyoursketch.options.BaseOptionView;

/**
 * Created by charlie on 1/5/17.
 */

public class ColorView extends BaseOptionView {

    private int mColor;

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mColor = ContextCompat.getColor(context, R.color.etchBlack);
        init();
    }

    public ColorView(Context context, AttributeSet attrs, int color) {
        super(context, attrs);
        mColor = color;
        init();
    }

    public ColorView(Context context, AttributeSet attrs, int color, int sizeDp, int marginDp) {
        super(context, attrs, sizeDp, marginDp);
        mColor = color;
        init();
    }

    private void init() {
        if (mColor == Color.WHITE) {
            setBackground(ContextCompat.getDrawable(getContext(),
                    R.drawable.white_circle_black_border));
        } else {
            ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
            drawable.getPaint().setColor(mColor);
            setBackground(drawable);
        }
    }

    public int getColor() {
        return mColor;
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);

        if (selected && mColor == Color.WHITE) {
            setColorFilter(Color.BLACK);
        }
    }
}
