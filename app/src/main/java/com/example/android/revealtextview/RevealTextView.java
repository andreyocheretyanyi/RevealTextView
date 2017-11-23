package com.example.android.revealtextview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

/**
 * Created by Android on 23.11.2017.
 */

public class RevealTextView extends AppCompatTextView {

    TextDrawable first, second;
    RectF boundsFirst, boundsSecond;
    float step = 0.1f;
    private boolean isFirstNeedRedraw = true, isSecondNeedRedraw = true;
    private boolean isFirstLaunch = true;

    public RevealTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RevealTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isFirstLaunch) {
            initRect();
            isFirstLaunch = false;
        }
        if (isFirstNeedRedraw) {
            first = new TextDrawable("111", Color.RED, Color.BLUE, new RectF());
            first.draw(canvas);
            isFirstNeedRedraw = false;
            canvas.save();
        }
        if (isSecondNeedRedraw) {
            second = new TextDrawable("222", Color.BLACK, Color.YELLOW, boundsFirst);
            second.draw(canvas);
        }


        postDelayed(new Runnable() {
            @Override
            public void run() {
                boundsFirst.set(boundsFirst.left - step, boundsFirst.top - step, boundsFirst.right - step, boundsFirst.bottom - step);
                invalidate();

            }
        }, 10);


    }


    private void initRect() {
        boundsFirst = new RectF(0, 0, getWidth(), getHeight());
        boundsSecond = new RectF(0, 0, getWidth(), getHeight());
    }


    private float convertDpToPixel(float dp) {
        Resources resources = getContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    class TextDrawable extends Drawable {

        private final String text;
        private final Paint textPaint;
        private final Paint backPaint;
        private final RectF rectF;

        public TextDrawable(String text, int textColor, int backColor, RectF rect) {

            this.rectF = rect;
            this.text = text;

            textPaint = new Paint();
            textPaint.setColor(textColor);
            textPaint.setTextSize(14f);
            textPaint.setAntiAlias(true);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setTextAlign(Paint.Align.CENTER);

            backPaint = new Paint();
            backPaint.setColor(backColor);
            backPaint.setAntiAlias(true);
            backPaint.setStyle(Paint.Style.FILL);

        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawRoundRect(rectF, 5, 5, backPaint);
            canvas.drawText(text, getWidth() / 2, getHeight() / 2, textPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            textPaint.setAlpha(alpha);
            backPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            textPaint.setColorFilter(cf);
            backPaint.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

}