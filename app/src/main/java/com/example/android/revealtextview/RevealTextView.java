package com.example.android.revealtextview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;


public class RevealTextView extends View {


    private static final int mFps = 30;
    private final Xfermode mode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);

    private int mAnimDuration = 1000;
    private float mCornerRadius;
    private int mCurrentInvalidateCount;
    private int mMaxInvalidateCount;
    private float mChangedRadius;
    private boolean isFirstLayerVisible = true, isFirstInit = true, isNeedShowAnim = false, isAnimStart = false;
    private int mHighAlpha = 255;
    private int mLowAlpha = 0;
    private int mFirstTextColor, mSecondTextColor, mFirstBackColorColor, mSecondBackColorColor,
            mStartGradientColorFirst, mStartGradientColorSecond, mEndGradientColorFirst, mEndGradientColorSecond;
    private float mTextSize = 20;
    private String mText;
    private float cx;
    private float cy;
    private float step;
    private int alphaStep;

    private LinearGradient mFirstGradient, mSecondGradient;
    private Canvas roundedBitmapCanvas, mainViewCanvas;
    private Bitmap roundedBitmap, mainViewBitmap;
    private Path mPath;
    private Path textPath;
    private Paint textPaint;
    private Paint pathPaint;
    private Paint roundedPaint;
    private Paint mainPaint;


    public RevealTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        initTools();
    }

    public RevealTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initTools();

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
        mTextSize = ta.getDimension(R.styleable.RevealTextView_textSize, 20);
        mText = ta.getString(R.styleable.RevealTextView_text);
        mAnimDuration = ta.getInt(R.styleable.RevealTextView_animDuration, 1000);
        mCornerRadius = ta.getDimension(R.styleable.RevealTextView_cornerRadius, convertDpToPixel(5));

        ta.recycle();

        setLayerType(LAYER_TYPE_SOFTWARE, null);

    }

    public void startAnim() {
        if (!isAnimStart) {
            isAnimStart = true;
            isNeedShowAnim = true;
            invalidate();
        }
    }

    private void initTools() {
        mMaxInvalidateCount = (int) (mAnimDuration / mFps);
        mPath = new Path();
        alphaStep = (int) (255f / mMaxInvalidateCount);

        textPath = new Path();
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(mTextSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setAntiAlias(true);
        pathPaint.setStyle(Paint.Style.FILL);
        pathPaint.setFilterBitmap(true);

        roundedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        roundedPaint.setFilterBitmap(true);

        mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isFirstInit) {
            cx = getWidth() / 2;
            cy = getHeight() / 2;
            step = (getWidth() / 1.5f) / mMaxInvalidateCount;
            isFirstInit = false;
            mFirstGradient = new LinearGradient(0, 0, getWidth(), getHeight(), mStartGradientColorFirst, mEndGradientColorFirst, Shader.TileMode.MIRROR);
            mSecondGradient = new LinearGradient(0, 0, getWidth(), getHeight(), mStartGradientColorSecond, mEndGradientColorSecond, Shader.TileMode.MIRROR);
        }

        //drawRoundCorners
        clipPath();

        //draw mainLayers
        drawMainLayers();


        canvas.drawBitmap(roundedBitmap, 0, 0, mainPaint);
        mainPaint.setXfermode(mode);
        canvas.drawBitmap(mainViewBitmap, 0, 0, mainPaint);
        mainPaint.setXfermode(null);

        mHighAlpha -= alphaStep;
        mLowAlpha += alphaStep;
        mChangedRadius += step;


        if (isNeedShowAnim)
            if (mCurrentInvalidateCount <= mMaxInvalidateCount) {
                mCurrentInvalidateCount++;
                invalidate();

            } else {
                isAnimStart = false;
                isFirstLayerVisible = !isFirstLayerVisible;
                mChangedRadius = 0;
                mCurrentInvalidateCount = 0;
                mHighAlpha = 255;
                mLowAlpha = 0;
            }


    }

    private void drawMainLayers() {
        if (isFirstLayerVisible) {
            //first layer
            if (!firstGradientExist())
                drawLayer(cx, cy, mFirstBackColorColor, step * mMaxInvalidateCount, mFirstTextColor, mHighAlpha);
            else {
                drawLayer(cx, cy, mFirstGradient, step * mMaxInvalidateCount, mFirstTextColor, mHighAlpha);
            }

            //second layer
            if (!secondGradientExist()) {
                drawLayer(cx, cy, mSecondBackColorColor, mChangedRadius, mSecondTextColor, mLowAlpha);
            } else {
                drawLayer(cx, cy, mSecondGradient, mChangedRadius, mSecondTextColor, mLowAlpha);
            }

        } else {
            //second layer
            if (!secondGradientExist())
                drawLayer(cx, cy, mSecondBackColorColor, step * mMaxInvalidateCount, mSecondTextColor, mHighAlpha);
            else {
                drawLayer(cx, cy, mSecondGradient, step * mMaxInvalidateCount, mSecondTextColor, mHighAlpha);
            }

            //first layer
            if (!firstGradientExist())
                drawLayer(cx, cy, mFirstBackColorColor, mChangedRadius, mFirstTextColor, mLowAlpha);
            else {
                drawLayer(cx, cy, mFirstGradient, mChangedRadius, mFirstTextColor, mLowAlpha);
            }
        }
    }


    private void drawLayer(float cx, float cy, int backgroundColor, float radius, int textColor, int textAlpha) {
        if (mainViewBitmap == null) {
            mainViewBitmap = Bitmap.createBitmap(getWidth(),
                    getHeight(),
                    Bitmap.Config.ARGB_8888);
            mainViewCanvas = new Canvas(mainViewBitmap);
            mainViewCanvas.drawColor(
                    Color.TRANSPARENT,
                    PorterDuff.Mode.CLEAR);
        }

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textColor);
        textPaint.setAlpha(textAlpha);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPath.moveTo(0, cy);
        textPath.lineTo(getWidth(), cy);


        mPath.addCircle(cx, cy, radius, Path.Direction.CW);


        mPath.addCircle(cx, cy, radius, Path.Direction.CW);
        pathPaint.setColor(backgroundColor);
        mPath.addPath(textPath);
        mainViewCanvas.drawPath(mPath, pathPaint);
        if (mText != null)
            mainViewCanvas.drawTextOnPath(mText, textPath, 0, 0, textPaint);

        mPath.reset();
    }

    private void drawLayer(float cx, float cy, LinearGradient linearGradient, float radius, int textColor, int textAlpha) {
        if (mainViewBitmap == null) {
            mainViewBitmap = Bitmap.createBitmap(getWidth(),
                    getHeight(),
                    Bitmap.Config.ARGB_8888);
            mainViewCanvas = new Canvas(mainViewBitmap);
            mainViewCanvas.drawColor(
                    Color.TRANSPARENT,
                    PorterDuff.Mode.CLEAR);
        }

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textColor);
        textPaint.setAlpha(textAlpha);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPath.moveTo(0, cy);
        textPath.lineTo(getWidth(), cy);


        mPath.addCircle(cx, cy, radius, Path.Direction.CW);


        mPath.addCircle(cx, cy, radius, Path.Direction.CW);
        pathPaint.setShader(linearGradient);
        mPath.addPath(textPath);
        mainViewCanvas.drawPath(mPath, pathPaint);
        if (mText != null)
            mainViewCanvas.drawTextOnPath(mText, textPath, 0, 0, textPaint);

        mPath.reset();
    }


    private void clipPath() {
        if (roundedBitmap == null) {
            roundedBitmap = Bitmap.createBitmap(getWidth(),
                    getHeight(),
                    Bitmap.Config.ARGB_8888);
            roundedBitmapCanvas = new Canvas(roundedBitmap);
        }
        roundedBitmapCanvas.drawColor(
                Color.TRANSPARENT,
                PorterDuff.Mode.CLEAR);

        Path path = new Path();
        path.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), mCornerRadius, mCornerRadius, Path.Direction.CW);
        roundedBitmapCanvas.drawPath(path, roundedPaint);

    }


    private boolean firstGradientExist() {
        return mStartGradientColorFirst != -1 && mEndGradientColorFirst != -1;
    }

    private boolean secondGradientExist() {
        return mStartGradientColorSecond != -1 && mEndGradientColorSecond != -1;
    }


    private float convertDpToPixel(float dp) {
        Resources resources = getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }


}