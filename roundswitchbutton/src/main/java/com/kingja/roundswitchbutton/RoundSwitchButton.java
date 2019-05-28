package com.kingja.roundswitchbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Description:TODO
 * Create Time:2019/5/28 0028 上午 11:06
 * Author:KingJA
 * Email:kingjavip@gmail.com
 */
public class RoundSwitchButton extends View {

    private RectF bgRectf;
    private Paint bgPaint;
    private Paint mSelectedTextPaint;
    private Paint mUnselectedTextPaint;
    private int perWidth;
    private int mHeight;
    private float mTextHeightOffset;
    private Paint selPaint;
    private float currentAnimationValue;
    private Paint.FontMetrics mFontMetrics;

    public RoundSwitchButton(Context context) {
        this(context, null);
    }

    public RoundSwitchButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundSwitchButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initRoundSwitchButton(attrs);
    }

    private int bgColor = 0x77ffffff;
    private int selColor = 0xFF42B1F6;
    private int mTextSize = 16;
    private int selTextColor = 0xFFffffff;
    private int unselTextColor = 0xFF000000;

    private void initRoundSwitchButton(AttributeSet attrs) {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setStrokeWidth(borderWidth);
        bgPaint.setColor(bgColor);

        selPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        selPaint.setStyle(Paint.Style.FILL);
        selPaint.setColor(selColor);

        // selected text paint
        mSelectedTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mSelectedTextPaint.setTextSize(mTextSize);
        mSelectedTextPaint.setColor(selTextColor);
        // unselected text paint
        mUnselectedTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mUnselectedTextPaint.setTextSize(mTextSize);
        mUnselectedTextPaint.setColor(unselTextColor);

        mFontMetrics = mSelectedTextPaint.getFontMetrics();

        mTextHeightOffset = -(mSelectedTextPaint.ascent() + mSelectedTextPaint.descent()) * 0.5f;
    }

    private int borderWidth = 0;
    private int radius = 40;
    private int mSelectedTab = 0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mHeight = getMeasuredHeight();
        int measuredWidth = getMeasuredWidth();
        bgRectf = new RectF(borderWidth, borderWidth, measuredWidth - borderWidth, mHeight -
                borderWidth);
        perWidth = measuredWidth / mTabNum;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制背景
        canvas.drawRoundRect(bgRectf, radius, radius, bgPaint);
        //绘制选中tab
        if (mSelectedTab != currentTab) {
            canvas.drawRoundRect(new RectF(perWidth * currentTab + currentAnimationValue, 0, perWidth * currentTab +
                    currentAnimationValue + perWidth, mHeight), radius, radius, selPaint);
        } else {
            canvas.drawRoundRect(new RectF(perWidth * currentTab, 0, perWidth * currentTab + perWidth, mHeight),
                    radius, radius, selPaint);
        }
        //draw tab and line
        drawText(canvas);
        drawTextColor(canvas);
    }

    private void drawText(Canvas canvas) {
        for (int i = 0; i < mTabNum; i++) {
            String tabText = mTabTexts[i];
            float tabTextWidth = mSelectedTextPaint.measureText(tabText);
            canvas.drawText(tabText, 0.5f * perWidth * (2 * i + 1) - 0.5f * tabTextWidth, mHeight * 0.5f +
                    mTextHeightOffset, mUnselectedTextPaint);
        }
    }

    private void drawTextColor(Canvas canvas) {
        canvas.save();
        if (mSelectedTab != currentTab) {
            canvas.clipRect(perWidth * currentTab + currentAnimationValue, 0, perWidth * currentTab +
                    currentAnimationValue + perWidth, getMeasuredHeight());
        } else {
            canvas.clipRect(perWidth * currentTab, 0, perWidth * currentTab + perWidth, getMeasuredHeight());
        }
        for (int i = 0; i < mTabNum; i++) {
            String tabText = mTabTexts[i];
            float tabTextWidth = mSelectedTextPaint.measureText(tabText);
            canvas.drawText(tabText, 0.5f * perWidth * (2 * i + 1) - 0.5f * tabTextWidth, mHeight * 0.5f +
                    mTextHeightOffset, mSelectedTextPaint);
        }
        canvas.restore();
    }

    private String[] mTabTexts = {"视频", "图像", "VR"};
    private int mTabNum = mTabTexts.length;

    public RoundSwitchButton setText(String... tagTexts) {
        if (tagTexts.length > 1) {
            this.mTabTexts = tagTexts;
            mTabNum = tagTexts.length;
            requestLayout();
            return this;
        } else {
            throw new IllegalArgumentException("the size of tagTexts should greater then 1");
        }
    }

    private boolean mEnable = true;
    private OnSwitchListener onSwitchListener;

    public interface OnSwitchListener {
        void onSwitch(int position, String tabText);
    }

    public RoundSwitchButton setOnSwitchListener(OnSwitchListener onSwitchListener) {
        this.onSwitchListener = onSwitchListener;
        return this;
    }

    private int currentTab;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return true;
        }
        if (isAnimating) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            for (int i = 0; i < mTabNum; i++) {
                if (x > perWidth * i && x < perWidth * (i + 1)) {
                    if (mSelectedTab == i) {
                        return true;
                    }
                    mSelectedTab = i;
                    //todo 开启动画
                    startAnimator();
                    if (onSwitchListener != null) {
                        onSwitchListener.onSwitch(i, mTabTexts[i]);
                    }
                }
            }
            invalidate();
        }
        return true;
    }

    private boolean isAnimating;

    private void startAnimator() {
        float distance = Math.abs(mSelectedTab - currentTab) * perWidth;
        if (mSelectedTab < currentTab) {
            distance = 0 - distance;
        }

        ValueAnimator anim = ValueAnimator.ofFloat(0, distance);
        anim.setDuration(500);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                isAnimating = true;
                currentAnimationValue = (float) animation.getAnimatedValue();
                Log.d("TAG", "cuurent value is " + currentAnimationValue);
                invalidate();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                currentTab = mSelectedTab;
                Log.d("TAG", "结束" + currentTab);
            }
        });
        anim.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultWidth = getDefaultWidth();
        int defaultHeight = getDefaultHeight();
        setMeasuredDimension(getExpectSize(defaultWidth, widthMeasureSpec), getExpectSize(defaultHeight,
                heightMeasureSpec));
    }

    private int getExpectSize(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(size, specSize);
                break;
            default:
                break;
        }
        return result;
    }

    private int getDefaultWidth() {
        float tabTextWidth = 0f;
        int tabs = mTabTexts.length;
        for (int i = 0; i < tabs; i++) {
            tabTextWidth = Math.max(tabTextWidth, mSelectedTextPaint.measureText(mTabTexts[i]));
        }
        float totalTextWidth = tabTextWidth * tabs;
        float totalStrokeWidth = (borderWidth * tabs);
        int totalPadding = (getPaddingRight() + getPaddingLeft()) * tabs;
        return (int) (totalTextWidth + totalStrokeWidth + totalPadding);
    }

    private int getDefaultHeight() {
        return (int) (mFontMetrics.bottom - mFontMetrics.top) + getPaddingTop() + getPaddingBottom();
    }
}
