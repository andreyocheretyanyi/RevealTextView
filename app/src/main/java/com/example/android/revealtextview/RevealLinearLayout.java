package com.example.android.revealtextview;

import android.animation.Animator;
import android.animation.AnimatorSet;
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
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;


public class RevealLinearLayout extends LinearLayout {


    private final Xfermode mode = new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP);

    private int mAnimDuration = 1000;
    private float mCornerRadius;
    private boolean isFirstLayerVisible = true, isFirstInit = true, isAnimStart = false;
    private int mFirstBackColorColor, mSecondBackColorColor,
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
    private final List<Animator> animatorList = new ArrayList<>();
    private final TimeInterpolator interpolator = new LinearInterpolator();


    public RevealLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        initTools();
    }

    public RevealLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
        initTools();

    }


    public int getCurrentState() {
        return isFirstLayerVisible ? 0 : 1;
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RevealLinearLayout);
        mFirstBackColorColor = ta.getColor(R.styleable.RevealLinearLayout_backgroundColorFirst, Color.RED);
        mSecondBackColorColor = ta.getColor(R.styleable.RevealLinearLayout_backgroundColorSecond, Color.BLACK);
        mStartGradientColorFirst = ta.getColor(R.styleable.RevealLinearLayout_startColorFirst, -1);
        mEndGradientColorFirst = ta.getColor(R.styleable.RevealLinearLayout_endColorFirst, -1);
        mStartGradientColorSecond = ta.getColor(R.styleable.RevealLinearLayout_startColorSecond, -1);
        mEndGradientColorSecond = ta.getColor(R.styleable.RevealLinearLayout_endColorSecond, -1);
        mAnimDuration = ta.getInt(R.styleable.RevealLinearLayout_animDuration, 700);
        mCornerRadius = ta.getDimension(R.styleable.RevealLinearLayout_cornerRadius, convertDpToPixel(5));

        setWillNotDraw(false);

        ta.recycle();

        setLayerType(LAYER_TYPE_SOFTWARE, null);

    }


    public void addAnimation(List<Animator> animators) {
        animatorList.clear();
        animatorList.add(mValueAnimator);
        animatorList.addAll(animators);
    }


    public void restoreAnimation() {
        animatorList.clear();
        animatorList.add(mValueAnimator);
    }


    public void startAnim() {
        if (!isAnimStart) {
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setInterpolator(interpolator);
            animatorSet.setDuration(mAnimDuration);
            animatorSet.playTogether(animatorList);
            animatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    isAnimStart = true;
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
            animatorSet.start();

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
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (isFirstInit) {
            cx = getWidth() / 2;
            cy = getHeight() / 2;
            if (firstGradientExist()) {
                mFirstGradient = new LinearGradient(0, 0, getWidth(), getHeight(), mStartGradientColorFirst, mEndGradientColorFirst, Shader.TileMode.MIRROR);
            } else {
                mFirstGradient = new LinearGradient(0, 0, getWidth(), getHeight(), mFirstBackColorColor, mFirstBackColorColor, Shader.TileMode.MIRROR);
            }

            if (secondGradientExist()) {
                mSecondGradient = new LinearGradient(0, 0, getWidth(), getHeight(), mStartGradientColorSecond, mEndGradientColorSecond, Shader.TileMode.MIRROR);
            } else {
                mSecondGradient = new LinearGradient(0, 0, getWidth(), getHeight(), mSecondBackColorColor, mSecondBackColorColor, Shader.TileMode.MIRROR);
            }
            setUpAnimation();
            createBackground(0);
            isFirstInit = false;

        }
    }

    private void setUpAnimation() {
        mValueAnimator = ValueAnimator.ofFloat(0, getWidth() / 1.9f);
        mValueAnimator.setDuration(mAnimDuration);
        mValueAnimator.setInterpolator(interpolator);
        animatorList.add(mValueAnimator);

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                createBackground((Float) animation.getAnimatedValue());
            }
        });
    }


    private void createBackground(float radius) {

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
            drawLayer(cx, cy, mFirstGradient, getWidth() / 1.9f);

            //second layer
            drawLayer(cx, cy, mSecondGradient, maxRadius);

        } else {
            drawLayer(cx, cy, mSecondGradient, getWidth() / 1.9f);

            //first layer
            drawLayer(cx, cy, mFirstGradient, maxRadius);
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