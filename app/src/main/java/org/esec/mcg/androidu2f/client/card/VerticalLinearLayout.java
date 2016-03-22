package org.esec.mcg.androidu2f.client.card;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yz on 2016/3/22.
 */
public class VerticalLinearLayout extends ViewGroup {

    int mScreenWidth;
    int mScreenHeight;

    public VerticalLinearLayout(Context context) {
        this(context, null, 0);
    }

    public VerticalLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VerticalLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = context.getResources().getDisplayMetrics().heightPixels;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = 0;
        int height = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                // 丈量child的尺寸
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                // 这里的LayoutParams就是我们自定义的LayoutParams
                LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
                if (widthSpecMode == MeasureSpec.AT_MOST) {
                    width = Math.max(width, child.getMeasuredWidth() + layoutParams.leftMargin + layoutParams.rightMargin);
                }
                height += child.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
            }
        }

        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingBottom() + getPaddingTop();

        width = Math.min(width, mScreenWidth);
        height = Math.min(height, mScreenHeight);

        // 如果都是宽高设置为wrap_content，那么都使用文本的宽高
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, height);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            // 如果只是宽度是wrap_content，那么宽度使用文本的宽度，高度使用heightMeasureSpec中的高度
            setMeasuredDimension(width, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, height);
        }

        Log.d("VerticalLinearLayout", "### vertical, width = " + getMeasuredWidth() + ", height = " + getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int top = getPaddingTop();

        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                // 对child进行布局
                childView.layout(left, top, left + childView.getMeasuredWidth(), top + childView.getMeasuredHeight());

                Log.d("VerticalLinearLayout", "### left = " + left + ", top = " + top + ", right = " + (left + childView.getMeasuredWidth()));

                top += childView.getMeasuredHeight();
            }
        }
    }

    @Override
    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new VerticalLinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new VerticalLinearLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new VerticalLinearLayout.LayoutParams(p);
    }



    /**
     * 自定义LayoutParams，从而使你的Layout支持Margin等
     */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }
    }
}
