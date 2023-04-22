package com.lesa_humdet.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class LineView extends View {

    private Paint mPaint;
    private float mStartX, mStartY, mEndX, mEndY;

    public LineView(Context context) {
        super(context);
        init();
    }

    public LineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(10f);
    }

    public void setLineCoords(float startX, float startY, float endX, float endY) {
        mStartX = startX;
        mStartY = startY;
        mEndX = endX;
        mEndY = endY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(mStartX, mStartY, mEndX, mEndY, mPaint);
    }
}
