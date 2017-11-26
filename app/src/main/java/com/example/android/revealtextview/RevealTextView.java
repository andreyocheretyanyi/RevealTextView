package com.example.android.revealtextview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by Android on 23.11.2017.
 */

public class RevealTextView extends View {


    public static final int mFps = 60;

    private int animationTime = 2000;
    private int mCurrentInvalidateCount;
    private int mMaxInvalidateCount;
    private Path mPath;
    private float mChangedRadius;
    private boolean isFirstLayerVisible = false;
    private int mHighAlpha = 255;
    private int mLowAlpha = 0;
    private int mFirstTextColor = Color.RED, mSecondTextColor = Color.BLACK, mFirstBackColorColor = Color.BLACK, mSecondBackColorColor = Color.RED;
    private int mTextSize = 20;

    public RevealTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RevealTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    private void initAttrs(AttributeSet attrs) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        initRect(canvas);

    }

    public void startAnim() {
        invalidate();
    }


    private void initRect(Canvas canvas) {
        mMaxInvalidateCount = (int) (animationTime / mFps);
        mPath = new Path();
        float cx = getWidth() / 2;
        float cy = getHeight() / 2;
        float step = (getWidth() / 1.5f) / mMaxInvalidateCount;
        int alphaStep = (int) (255f / mMaxInvalidateCount);
        mHighAlpha -= alphaStep;
        mLowAlpha += alphaStep;
        mChangedRadius += step;

        clipPath(canvas);
        if (isFirstLayerVisible) {
            //first layer
            drawLayer(canvas, cx, cy, mFirstBackColorColor, step * mMaxInvalidateCount, mTextSize, mFirstTextColor, mHighAlpha);

            //second layer
            drawLayer(canvas, cx, cy, mSecondBackColorColor, mChangedRadius, mTextSize, mSecondTextColor, mLowAlpha);

        } else {
            //second layer
            drawLayer(canvas, cx, cy, mSecondBackColorColor, step * mMaxInvalidateCount, mTextSize, mSecondTextColor, mHighAlpha);

            //first layer
            drawLayer(canvas, cx, cy, mFirstBackColorColor, mChangedRadius, mTextSize, mFirstTextColor, mLowAlpha);

        }


        if (mCurrentInvalidateCount <= mMaxInvalidateCount) {
            mCurrentInvalidateCount++;
            invalidate();

        } else {
            isFirstLayerVisible = !isFirstLayerVisible;
            mChangedRadius = 0;
            mCurrentInvalidateCount = 0;
            mHighAlpha = 255;
            mLowAlpha = 0;
        }


    }


    private void drawLayer(Canvas canvas, float cx, float cy, int backgroundColor, float radius, int textSize, int textColor, int textAlpha) {
        Path textPath = new Path();
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textColor);
        textPaint.setAlpha(textAlpha);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPath.moveTo(0, cy);
        textPath.lineTo(getWidth(), cy);

        mPath.addCircle(cx, cy, radius, Path.Direction.CW);
        Paint pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStyle(Paint.Style.FILL);
        pathPaint.setColor(backgroundColor);
        mPath.addPath(textPath);
        canvas.drawPath(mPath, pathPaint);
        canvas.drawTextOnPath("11111", textPath, 0, 0, textPaint);

        mPath.reset();
    }


    private void clipPath(Canvas canvas) {
        Path path = new Path();
        path.addRoundRect(new RectF(4, 4, getWidth() - 4, getHeight() - 4), convertDpToPixel(5), convertDpToPixel(5), Path.Direction.CW);
        canvas.clipPath(path);

    }


    private float convertDpToPixel(float dp) {
        Resources resources = getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }


}