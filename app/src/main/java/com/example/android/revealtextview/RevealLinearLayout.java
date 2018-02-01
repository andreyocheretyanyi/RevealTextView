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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;


public class RevealLinearLayout extends LinearLayout {


    private int mAnimDuration;
    private float mCornerRadius;
    private boolean isFirstLayerVisible = true, isAnimStart = false;
    private int mFirstBackColorColor, mSecondBackColorColor,
            mStartGradientColorFirst, mStartGradientColorSecond, mEndGradientColorFirst, mEndGradientColorSecond;
    private float cx;
    private float cy;

    private LinearGradient mFirstGradient, mSecondGradient;
    private Bitmap mainViewBitmap;
    private Paint mainPaint;
    private Drawable backgroundStart, backgroundEnd;


    private ValueAnimator mValueAnimator;
    private final List<Animator> animatorList = new ArrayList<>();
    private final TimeInterpolator interpolator = new LinearInterpolator();
    private Drawable currentBackground;
    private Animator.AnimatorListener listener;


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
        mAnimDuration = ta.getInt(R.styleable.RevealLinearLayout_animDuration, 300);
        mCornerRadius = ta.getDimension(R.styleable.RevealLinearLayout_cornerRadius, convertDpToPixel(5));
        backgroundStart = ta.getDrawable(R.styleable.RevealLinearLayout_backgroundStart);
        backgroundEnd = ta.getDrawable(R.styleable.RevealLinearLayout_backgroundEnd);
        currentBackground = backgroundStart;

        setBackground(currentBackground);

        ta.recycle();


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

    private void setUpAnimation() {
        if (mValueAnimator != null)
            animatorList.remove(mValueAnimator);
        mValueAnimator = ValueAnimator.ofFloat(0, getWidth() / 2f);
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

    private void startAnim(int duration) {
        calculateSize();
        if (!isAnimStart) {

            setUpAnimation();
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setInterpolator(interpolator);
            animatorSet.setDuration(duration);
            animatorSet.playTogether(animatorList);
            animatorSet.addListener(listener);
            animatorSet.start();

        }
    }

    public void startAnim() {
        startAnim(mAnimDuration);
    }

    public void restoreStates(){
        startAnim(0);
    }


    private void initTools() {
        listener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimStart = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isFirstLayerVisible) {
                    currentBackground = backgroundEnd;
                } else {
                    currentBackground = backgroundStart;
                }
                setBackground(currentBackground);
                isAnimStart = false;
                isFirstLayerVisible = !isFirstLayerVisible;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainPaint.setAntiAlias(true);
        mainPaint.setStyle(Paint.Style.FILL);
        mainPaint.setFilterBitmap(true);
    }


    private void calculateSize() {
        cx = getWidth()/2;
        cy = getHeight()/2;
        createGradients();
    }


    private void createGradients() {
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
    }


    private void createBackground(float radius) {

        if (getWidth() > 0 && getHeight() > 0) {

            //draw mainLayers
            drawMainLayers(radius);

            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), mainViewBitmap);
            setBackground(bitmapDrawable);

        }
    }

    private void drawMainLayers(float maxRadius) {
        if (isFirstLayerVisible) {

            //secondLayer
//            drawLayer(cx, cy, mFirstGradient, mSecondGradient, sizeWidth / 2f);

            //first layer
            drawLayer(cx, cy, mSecondGradient, mFirstGradient, maxRadius);


        } else {
            //first layer
//            drawLayer(cx, cy, mSecondGradient, mFirstGradient, sizeWidth / 2f);

            //second layer
            drawLayer(cx, cy, mFirstGradient, mSecondGradient, maxRadius);
        }
    }


    private void drawLayer(float cx, float cy, LinearGradient first, LinearGradient second, float radius) {

        mainViewBitmap = Bitmap.createBitmap(
                getWidth(),
                getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas mainViewCanvas = new Canvas(mainViewBitmap);

        mainViewCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        currentBackground.draw(mainViewCanvas);
        mainPaint.setShader(second);
        mainViewCanvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), mCornerRadius, mCornerRadius, mainPaint);
        mainPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        mainPaint.setShader(first);
        mainViewCanvas.drawCircle(cx, cy, radius, mainPaint);
        mainPaint.setXfermode(null);


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