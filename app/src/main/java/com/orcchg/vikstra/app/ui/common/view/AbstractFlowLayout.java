package com.orcchg.vikstra.app.ui.common.view;

import android.animation.LayoutTransition;
import android.animation.LayoutTransition.TransitionListener;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.orcchg.vikstra.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

public class AbstractFlowLayout extends ViewGroup {
    protected final String TAG = this.getClass().getSimpleName();
    private static final int mDotsViewId = 1000;

    private Set<WeakReference<View>> mLastRowViews;

    public static final int STATE_COLLAPSED = 0;
    public static final int STATE_EXPANDED = 1;
    @IntDef({STATE_COLLAPSED, STATE_EXPANDED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LayoutState {}

    protected final int VERTICAL_SPACING;
    protected final int HORIZONTAL_SPACING;
    protected final int ALLOWED_ROWS_COUNT;
    protected final int INITIAL_STATE;

    protected int mHeight, mExpandedHeight;
    protected int mRestItems = 0;
    protected @LayoutState int mCurrentState = STATE_COLLAPSED;
    protected boolean mIsAnimated = false;
    protected boolean mShouldDeferMeasure = false;
    protected boolean mIsEditable = false;

    protected View mDotsView;
    protected LayoutTransition mLayoutTransition;

    public interface OnExpandChangedListener {
        void onExpandChanged(@LayoutState int newState);
    }

    protected OnExpandChangedListener mOnExpandChangedListener;

    public void setOnExpandChangedListener(OnExpandChangedListener listener) {
        mOnExpandChangedListener = listener;
    }

    public void setEditable(boolean isEditable) {
        mIsEditable = isEditable;
    }

    public boolean isEditable() {
        return mIsEditable;
    }

    /* Init */
    // --------------------------------------------------------------------------------------------
    public AbstractFlowLayout(Context context) {
        this(context, null);
    }

    public AbstractFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbstractFlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Resources resources = context.getResources();
        int defVerticalSpacing = resources.getDimensionPixelSize(R.dimen.flow_layout_vertical_spacing);
        int defHorizontalSpacing = resources.getDimensionPixelSize(R.dimen.flow_layout_horizontal_spacing);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout, defStyle, 0);
        mIsEditable = a.getBoolean(R.styleable.FlowLayout_editable, false);
        ALLOWED_ROWS_COUNT = a.getInt(R.styleable.FlowLayout_rowsCount, 0);
        VERTICAL_SPACING = a.getDimensionPixelSize(R.styleable.FlowLayout_verticalSpacing, defVerticalSpacing);
        HORIZONTAL_SPACING = a.getDimensionPixelSize(R.styleable.FlowLayout_horizontalSpacing, defHorizontalSpacing);
        INITIAL_STATE = a.getInt(R.styleable.FlowLayout_state, 0);
        a.recycle();

        mCurrentState = INITIAL_STATE == 0 ? STATE_COLLAPSED : STATE_EXPANDED;

        mLastRowViews = new HashSet<>();
    }

    public void enableLayoutTransition(boolean isEnabled) {
        mIsAnimated = isEnabled;
        if (isEnabled && mLayoutTransition == null) {
            mLayoutTransition = new LayoutTransition();
            mLayoutTransition.addTransitionListener(new TransitionListener() {
                @Override
                public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                    // no-op
                }

                @Override
                public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
                    if (view == mDotsView) {
                        switch (transitionType) {
                            case LayoutTransition.CHANGE_APPEARING:
                                // expand ended
                                break;
                            case LayoutTransition.CHANGE_DISAPPEARING:
                                // collapse ended
                                mShouldDeferMeasure = false;
                                requestLayout();
                                break;
                        }
                    }
                }
            });
        }
        LayoutTransition layoutTransition = isEnabled ? mLayoutTransition : null;
        setLayoutTransition(layoutTransition);
    }

    /* Listener */
    // --------------------------------------------------------------------------------------------
    protected View.OnClickListener mDotsViewClickListener = (view) -> onDotsViewClicked();

    protected void onDotsViewClicked() {
        @LayoutState int oldState = mCurrentState;
        mCurrentState = mCurrentState == STATE_COLLAPSED ? STATE_EXPANDED : STATE_COLLAPSED;

        if (mIsAnimated && oldState == STATE_EXPANDED) {
            // wait till animation finished - then measure
            mShouldDeferMeasure = true;
        }
        performLayoutRequest();
    }

    private void performLayoutRequest() {
        requestLayout();
        invalidateLastRowViews();
        if (mOnExpandChangedListener != null) {
            mOnExpandChangedListener.onExpandChanged(mCurrentState);
        }
    }

    /* Measuring */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int currentRow = 1;
        int width = View.MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = View.MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        int count = getChildCount();
        int xpos = getPaddingLeft();
        int ypos = getPaddingTop();
        int childHeightMeasureSpec;
        int widthAtMostSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST);

        if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST) {
            childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);
        } else {
            childHeightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        }
        mHeight = 0;
        for (int i = 0; i < count; ++i) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                child.measure(widthAtMostSpec, childHeightMeasureSpec);
                int childw = child.getMeasuredWidth();
                mHeight = Math.max(mHeight, child.getMeasuredHeight() + VERTICAL_SPACING);

                if (xpos + childw > width) {
                    ypos += mHeight;
                    if (mCurrentState == STATE_COLLAPSED &&
                            ALLOWED_ROWS_COUNT > 0 &&
                            mDotsView != null && mDotsView.getVisibility() != View.GONE &&
                            currentRow == ALLOWED_ROWS_COUNT) {
                        // measure dots view
                        mDotsView.measure(widthAtMostSpec, childHeightMeasureSpec);
                        int dotsViewWidth = mDotsView.getMeasuredWidth();
                        if (xpos + dotsViewWidth <= width) {  // same line
                            ypos -= mHeight;
                        }
                        break;
                    }

                    xpos = getPaddingLeft();
                    ++currentRow;
                }
                xpos += childw + HORIZONTAL_SPACING;
            }
        }

        if (mCurrentState == STATE_EXPANDED) {
            mExpandedHeight = mHeight * currentRow;
        }

        if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.UNSPECIFIED) {
            height = ypos + mHeight;
        } else if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.AT_MOST) {
            if (ypos + mHeight < height) {
                height = ypos + mHeight;
            }
        }

        if (mShouldDeferMeasure) {
            height = mExpandedHeight;
        }

        setMeasuredDimension(width, height);
    }

    /* Layout */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int currentRow = 1;
        int width = r - l;
        int xpos = getPaddingLeft();
        int ypos = getPaddingTop();
        int count = getChildCount();

        for (int i = 0; i < count; ++i) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                int childw = child.getMeasuredWidth();
                int childh = child.getMeasuredHeight();

                if (xpos + childw > width) {
                    if (mCurrentState == STATE_COLLAPSED &&
                            ALLOWED_ROWS_COUNT > 0 &&
                            mDotsView != null && mDotsView.getVisibility() != View.GONE &&
                            currentRow == ALLOWED_ROWS_COUNT) {
                        // layout dots view
                        int dotsViewWidth = mDotsView.getMeasuredWidth();
                        int dotsXpos = getPaddingLeft();
                        int dotsYpos = ypos + mHeight;
                        // Add dots view at the end of flow
                        mDotsView.setId(mDotsViewId);
                        if (findViewById(mDotsViewId) == null) {
                            addView(mDotsView);
                        }
                        if (xpos + dotsViewWidth <= width) {  // same line
                            dotsXpos = xpos;
                            dotsYpos = ypos;
                        }

                        mDotsView.layout(dotsXpos, dotsYpos, dotsXpos + dotsViewWidth, dotsYpos + childh);
                        if (mRestItems == 0) {
                            mRestItems = count - i;
                        }
                        break;
                    }

                    xpos = getPaddingLeft();
                    ypos += mHeight;
                    ++currentRow;
                }

                if (mCurrentState == STATE_EXPANDED &&
                        ALLOWED_ROWS_COUNT > 0 &&
                        currentRow >= ALLOWED_ROWS_COUNT + 1) {  // last row only: ==
                    WeakReference<View> viewRef = new WeakReference<>(child);
                    mLastRowViews.add(viewRef);
                }

                child.layout(xpos, ypos, xpos + childw, ypos + childh);
                xpos += childw + HORIZONTAL_SPACING;
            }
        }
    }

    /* Utility */
    // --------------------------------------------------------------------------------------------
    private void invalidateLastRowViews() {
        int visibility = mCurrentState == STATE_COLLAPSED ? View.GONE : View.VISIBLE;
        for (WeakReference<View> viewRef : mLastRowViews) {
            View child = viewRef.get();
            if (child != null && child != mDotsView) {
                child.setVisibility(visibility);
            }
        }
    }
}
