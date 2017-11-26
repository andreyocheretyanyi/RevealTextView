package com.example.android.revealtextview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;


public class RevealTextView extends View {


    public static final int mFps = 60;
    private static final String TAG = "TAG";

    private int animationTime = 2000;
    private int mCurrentInvalidateCount;
    private int mMaxInvalidateCount;
    private Path mPath;
    private float mChangedRadius;
    private boolean isFirstLayerVisible = false, isFirstInit = true;
    private int mHighAlpha = 255;
    private int mLowAlpha = 0;
    private int mFirstTextColor, mSecondTextColor, mFirstBackColorColor, mSecondBackColorColor,
            mStartGradientColorFirst, mStartGradientColorSecond, mEndGradientColorFirst, mEndGradientColorSecond;
    private LinearGradient linearGradient;
    private int mTextSize = 20;
    float cx;
    float cy;
    float step;
    int alphaStep;

    public RevealTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public RevealTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);

    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RevealTextView);
        mFirstBackColorColor = ta.getColor(R.styleable.RevealTextView_backgroundColorFirst, Color.RED);
        mSecondBackColorColor = ta.getColor(R.styleable.RevealTextView_backgroundColorSecond, Color.BLACK);
        mFirstTextColor = ta.getColor(R.styleable.RevealTextView_textColorFirst, Color.BLACK);
        mSecondTextColor = ta.getColor(R.styleable.RevealTextView_textColorSecond, Color.RED);
        mStartGradientColorFirst = ta.getColor(R.styleable.RevealTextView_startColorFirst, -1);
        mEndGradientColorFirst = ta.getColor(R.styleable.RevealTextView_endColorFirst, -1);
        mStartGradientColorSecond = ta.getColor(R.styleable.RevealTextView_startColorSecond, -1);
        mEndGradientColorSecond = ta.getColor(R.styleable.RevealTextView_endColorSecond, -1);
        ta.recycle();

    }

    private boolean firstGradientExist() {
        return mStartGradientColorFirst != -1 && mEndGradientColorFirst != -1;
    }

    private boolean secondGradientExist() {
        return mStartGradientColorSecond != -1 && mEndGradientColorSecond != -1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        initRect(canvas);

    }

    public void startAnim() {
        invalidate();
    }


    private void initRect(Canvas canvas) {
        if (isFirstInit) {
            mMaxInvalidateCount = (int) (animationTime / mFps);
            mPath = new Path();
            cx = getWidth() / 2;
            cy = getHeight() / 2;
            step = (getWidth() / 1.5f) / mMaxInvalidateCount;
            alphaStep = (int) (255f / mMaxInvalidateCount);
            isFirstInit = false;
        }
        mHighAlpha -= alphaStep;
        mLowAlpha += alphaStep;
        mChangedRadius += step;

        clipPath(canvas);
        if (isFirstLayerVisible) {
            //first layer
            if (!firstGradientExist())
                drawLayer(canvas, cx, cy, mFirstBackColorColor, step * mMaxInvalidateCount, mTextSize, mFirstTextColor, mHighAlpha);
            else {
                linearGradient = new LinearGradient(0, 0, getWidth(), getHeight(), mStartGradientColorFirst, mEndGradientColorFirst, Shader.TileMode.MIRROR);
                drawLayer(canvas, cx, cy, linearGradient, step * mMaxInvalidateCount, mTextSize, mFirstTextColor, mHighAlpha);
            }

            //second layer
            if (!secondGradientExist()) {
                drawLayer(canvas, cx, cy, mSecondBackColorColor, mChangedRadius, mTextSize, mSecondTextColor, mLowAlpha);
            } else {
                linearGradient = new LinearGradient(0, 0, getWidth(), getHeight(), mStartGradientColorSecond, mEndGradientColorSecond, Shader.TileMode.MIRROR);
                drawLayer(canvas, cx, cy, linearGradient, mChangedRadius, mTextSize, mSecondTextColor, mLowAlpha);
            }

        } else {
            //second layer
            if (!secondGradientExist())
                drawLayer(canvas, cx, cy, mSecondBackColorColor, step * mMaxInvalidateCount, mTextSize, mSecondTextColor, mHighAlpha);
            else {
                linearGradient = new LinearGradient(0, 0, getWidth(), getHeight(), mStartGradientColorSecond, mEndGradientColorSecond, Shader.TileMode.MIRROR);
                drawLayer(canvas, cx, cy, linearGradient, step * mMaxInvalidateCount, mTextSize, mSecondTextColor, mHighAlpha);
            }

            //first layer
            if (!firstGradientExist())
                drawLayer(canvas, cx, cy, mFirstBackColorColor, mChangedRadius, mTextSize, mFirstTextColor, mLowAlpha);
            else {
                linearGradient = new LinearGradient(0, 0, getWidth(), getHeight(), mStartGradientColorFirst, mEndGradientColorFirst, Shader.TileMode.MIRROR);
                drawLayer(canvas, cx, cy, linearGradient, mChangedRadius, mTextSize, mFirstTextColor, mLowAlpha);
            }

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

    private void drawLayer(Canvas canvas, float cx, float cy, LinearGradient linearGradient, float radius, int textSize, int textColor, int textAlpha) {
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
        pathPaint.setShader(linearGradient);
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