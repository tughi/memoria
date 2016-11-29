package com.tughi.memoria;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class AnswerKeyboardLayout extends ViewGroup {

    private int padding;

    public AnswerKeyboardLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        padding = context.getResources().getDimensionPixelSize(R.dimen.answer_keyboard_padding);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (BuildConfig.DEBUG) {
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            if (widthMode != MeasureSpec.EXACTLY) {
                throw new IllegalStateException("Unexpected width mode");
            }
        }
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        View deleteKey = getChildAt(0);
        deleteKey.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

        if (getChildCount() > 1) {
            View key = getChildAt(1);
            key.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

            int keyHeight = key.getMeasuredHeight();
            int keysPerRow = (widthSize - deleteKey.getMeasuredWidth()) / (keyHeight + padding);
            int keys = getChildCount() - 1;
            int rows;
            if (keys > 0) {
                rows = keys / keysPerRow;
                if (keys % keysPerRow != 0) {
                    rows++;
                }
            } else {
                rows = 1;
            }

            setMeasuredDimension(widthSize, (keyHeight + padding) * rows);
        } else {
            setMeasuredDimension(widthSize, 0);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() > 1) {
            View deleteKey = getChildAt(0);
            int deleteKeyWidth = deleteKey.getMeasuredWidth();

            View key = getChildAt(1);
            int keyHeight = key.getMeasuredHeight();

            deleteKey.layout(right - left - deleteKeyWidth, 0, right - left, keyHeight);

            int keyLeft = 0;
            int keyTop = 0;
            for (int index = 1; index < getChildCount(); index++) {
                key = getChildAt(index);
                key.layout(keyLeft, keyTop, keyLeft + keyHeight, keyTop + keyHeight);

                keyLeft += keyHeight + padding;
                if (keyLeft > right - left - deleteKeyWidth - padding - keyHeight) {
                    keyLeft = 0;
                    keyTop += keyHeight + padding;
                }
            }
        }
    }

}
