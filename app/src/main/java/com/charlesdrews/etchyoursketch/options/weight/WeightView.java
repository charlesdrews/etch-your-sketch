package com.charlesdrews.etchyoursketch.options.weight;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.charlesdrews.etchyoursketch.options.BaseOptionView;

/**
 * Created by charlie on 1/8/17.
 */

public class WeightView extends BaseOptionView {

    private float mWeight;
    private Paint mPaint;

    public WeightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mWeight = WeightDialog.ETCH_LINE_WEIGHTS[0]; // default weight
        init();
    }

    public WeightView(Context context, AttributeSet attrs, float weight) {
        super(context, attrs);
        mWeight = weight;
        init();
    }

    public WeightView(Context context, AttributeSet attrs, float weight, int sizeDp, int marginDp) {
        super(context, attrs, sizeDp, marginDp);
        mWeight = weight;
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStrokeWidth(mWeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float y = canvas.getHeight() / 2f;
        canvas.drawLine(0f, y, canvas.getWidth(), y, mPaint);
    }

    public float getWeight() {
        return mWeight;
    }
}
