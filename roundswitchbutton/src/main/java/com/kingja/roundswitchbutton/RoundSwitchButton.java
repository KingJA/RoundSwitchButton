package com.kingja.roundswitchbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
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

    private static final float CORNERRADIUS = 50;
    private static final int BACKGROUNDCOLOR = 0x77ffffff;
    private static final int SELECTED_BACKGROUNDCOLOR = 0xFF42B1F6;
    private static final int TEXTCOLOR = 0xFF000000;
    private static final int SELECTEDTEXTCOLOR = 0xFFffffff;
    private static final float TEXTSIZE = 36;
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
    private int backgroundColor;
    private int selectedBackgroundColor;
    private int textColor;
    private int selectedTextColor;
    private float textSize;
    private float cornerRadius;
    private int mSwitchTabsResId;
    private int mSelectedTab;
    private int borderWidth = 0;

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

    private void initRoundSwitchButton(AttributeSet attrs) {
        initAttrs(attrs);
        initPaint();
    }

    private void initPaint() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setStrokeWidth(borderWidth);
        bgPaint.setColor(backgroundColor);

        selPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        selPaint.setStyle(Paint.Style.FILL);
        selPaint.setColor(selectedBackgroundColor);

        // selected text paint
        mSelectedTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mSelectedTextPaint.setTextSize(textSize);
        mSelectedTextPaint.setColor(selectedTextColor);
        // unselected text paint
        mUnselectedTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mUnselectedTextPaint.setTextSize(textSize);
        mUnselectedTextPaint.setColor(textColor);
        mFontMetrics = mSelectedTextPaint.getFontMetrics();
        mTextHeightOffset = -(mSelectedTextPaint.ascent() + mSelectedTextPaint.descent()) * 0.5f;
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RoundSwitchButton);
        backgroundColor = typedArray.getColor(R.styleable.RoundSwitchButton_backgroundColor, BACKGROUNDCOLOR);
        selectedBackgroundColor = typedArray.getColor(R.styleable.RoundSwitchButton_selectedBackgroundColor,
                SELECTED_BACKGROUNDCOLOR);
        textColor = typedArray.getColor(R.styleable.RoundSwitchButton_textColor, TEXTCOLOR);
        selectedTextColor = typedArray.getColor(R.styleable.RoundSwitchButton_selectedTextColor, SELECTEDTEXTCOLOR);
        textSize = typedArray.getDimension(R.styleable.RoundSwitchButton_textSize, TEXTSIZE);
        cornerRadius = typedArray.getDimension(R.styleable.RoundSwitchButton_cornerRadius, CORNERRADIUS);
        currentTab = mSelectedTab = typedArray.getInteger(R.styleable.RoundSwitchButton_selectedTab, 0);
        mSwitchTabsResId = typedArray.getResourceId(R.styleable.RoundSwitchButton_switchTabs, 0);
        if (mSwitchTabsResId != 0) {
            mTabTexts = getResources().getStringArray(mSwitchTabsResId);
            mTabNum = mTabTexts.length;
        }
        typedArray.recycle();
    }



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
        canvas.drawRoundRect(bgRectf, cornerRadius, cornerRadius, bgPaint);
        if (mSelectedTab != currentTab) {
            canvas.drawRoundRect(new RectF(perWidth * currentTab + currentAnimationValue, 0, perWidth * currentTab +
                    currentAnimationValue + perWidth, mHeight), cornerRadius, cornerRadius, selPaint);
        } else {
            canvas.drawRoundRect(new RectF(perWidth * currentTab, 0, perWidth * currentTab + perWidth, mHeight),
                    cornerRadius, cornerRadius, selPaint);
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

    private String[] mTabTexts = {"AAA", "BBB", "CCC"};
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
                    setSelectedTab(i);
                }
            }

        }
        return true;
    }

    public void setSelectedTab(int mSelectedTab) {
        if (currentTab == mSelectedTab) {
            return;
        }
        this.mSelectedTab = mSelectedTab;
        startAnimator();
        if (onSwitchListener != null) {
            onSwitchListener.onSwitch(mSelectedTab, mTabTexts[mSelectedTab]);
        }
        return;
    }

    private boolean isAnimating;

    private void startAnimator() {
        float distance = Math.abs(mSelectedTab - currentTab) * perWidth;
        if (mSelectedTab < currentTab) {
            distance = 0 - distance;
        }

        ValueAnimator anim = ValueAnimator.ofFloat(0, distance);
        anim.setDuration(300);
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
                Log.d("TAG", "over" + currentTab);
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
