package com.example.android.revealtextview;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
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
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.animation.LinearInterpolator;


public class RevealTextView extends android.support.v7.widget.AppCompatTextView {


    private final Xfermode mode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);

    private int mAnimDuration = 300;
    private float mCornerRadius;
    private boolean isFirstLayerVisible = true, isFirstInit = true, isAnimStart = false;
    private int mFirstTextColor, mSecondTextColor, mFirstBackColorColor, mSecondBackColorColor,
            mStartGradientColorFirst, mStartGradientColorSecond, mEndGradientColorFirst, mEndGradientColorSecond;

    private float cx;
    private float cy;

    private LinearGradient mFirstGradient, mSecondGradient;
    private Canvas roundedBitmapCanvas, mainViewCanvas;
    private Bitmap roundedBitmap, mainViewBitmap;
    private Path mPath;
    private Paint pathPaint;
    private Paint roundedPaint;
    private Paint mainPaint;

    private ValueAnimator mValueAnimator;
    private final TimeInterpolator interpolator = new LinearInterpolator();


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
        mAnimDuration = ta.getInt(R.styleable.RevealTextView_animDuration, 300);
        mCornerRadius = ta.getDimension(R.styleable.RevealTextView_cornerRadius, convertDpToPixel(5));

        ta.recycle();

        setLayerType(LAYER_TYPE_SOFTWARE, null);

    }

    public void startAnim() {
        if (!isAnimStart) {
            isAnimStart = true;
            mValueAnimator.start();
        }
    }

    private void initTools() {
        mPath = new Path();
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
        super.onDraw(canvas);
        setUpAnimation();

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (isFirstInit) {
            createBackground(getWidth() / 1.5f);
            isFirstInit = false;
            isFirstLayerVisible = !isFirstLayerVisible;
        }
    }

    private void setUpAnimation() {
        mValueAnimator = ValueAnimator.ofFloat(0, getWidth() / 1.5f);
        mValueAnimator.setDuration(mAnimDuration);
        mValueAnimator.setInterpolator(interpolator);
        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimStart = false;
                isFirstLayerVisible = !isFirstLayerVisible;

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                createBackground((Float) animation.getAnimatedValue());
            }
        });
    }


    private void createBackground(float radius) {
        cx = getWidth() / 2;
        cy = getHeight() / 2;
        mFirstGradient = new LinearGradient(0, 0, getWidth(), getHeight(), mStartGradientColorFirst, mEndGradientColorFirst, Shader.TileMode.MIRROR);
        mSecondGradient = new LinearGradient(0, 0, getWidth(), getHeight(), mStartGradientColorSecond, mEndGradientColorSecond, Shader.TileMode.MIRROR);

        Bitmap background = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        //drawRoundCorners
        clipPath();

        //draw mainLayers
        drawMainLayers(radius);


        canvas.drawBitmap(roundedBitmap, 0, 0, mainPaint);
        mainPaint.setXfermode(mode);
        canvas.drawBitmap(mainViewBitmap, 0, 0, mainPaint);
        mainPaint.setXfermode(null);


        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), background);
        setBackground(bitmapDrawable);
    }

    private void drawMainLayers(float maxRadius) {
        if (isFirstLayerVisible) {
            //first layer
            if (!firstGradientExist()) {
                drawLayer(cx, cy, mFirstBackColorColor, getWidth() / 1.5f);
            } else {
                drawLayer(cx, cy, mFirstGradient, getWidth() / 1.5f);
            }

            //second layer
            if (!secondGradientExist()) {
                drawLayer(cx, cy, mSecondBackColorColor, maxRadius);
            } else {
                drawLayer(cx, cy, mSecondGradient, maxRadius);
            }

        } else {
            //second layer
            if (!secondGradientExist())
                drawLayer(cx, cy, mSecondBackColorColor, getWidth() / 1.5f);
            else {
                drawLayer(cx, cy, mSecondGradient, getWidth() / 1.5f);
            }

            //first layer
            if (!firstGradientExist())
                drawLayer(cx, cy, mFirstBackColorColor, maxRadius);
            else {
                drawLayer(cx, cy, mFirstGradient, maxRadius);
            }
        }
    }

    private void drawLayer(float cx, float cy, int backgroundColor, float radius) {
        if (mainViewBitmap == null) {
            mainViewBitmap = Bitmap.createBitmap(getWidth(),
                    getHeight(),
                    Bitmap.Config.ARGB_8888);
            mainViewCanvas = new Canvas(mainViewBitmap);
            mainViewCanvas.drawColor(
                    Color.TRANSPARENT,
                    PorterDuff.Mode.CLEAR);
        }

        mPath.addCircle(cx, cy, radius, Path.Direction.CW);
        pathPaint.setColor(backgroundColor);
        mainViewCanvas.drawPath(mPath, pathPaint);

        mPath.reset();
    }


    private void drawLayer(float cx, float cy, LinearGradient linearGradient, float radius) {
        if (mainViewBitmap == null) {
            mainViewBitmap = Bitmap.createBitmap(getWidth(),
                    getHeight(),
                    Bitmap.Config.ARGB_8888);
            mainViewCanvas = new Canvas(mainViewBitmap);
            mainViewCanvas.drawColor(
                    Color.TRANSPARENT,
                    PorterDuff.Mode.CLEAR);
        }


        mPath.addCircle(cx, cy, radius, Path.Direction.CW);
        pathPaint.setShader(linearGradient);
        mainViewCanvas.drawPath(mPath, pathPaint);

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