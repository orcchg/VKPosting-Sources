package com.orcchg.vikstra.app.ui.common;

import android.content.Context;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.domain.model.Keyword;

import java.util.List;

import timber.log.Timber;

public class KeywordsFlowLayout extends AbstractFlowLayout {

    public KeywordsFlowLayout(Context context) {
        this(context, null);
    }

    public KeywordsFlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeywordsFlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDotsView = LayoutInflater.from(context).inflate(R.layout.keywords_blob_more, null, false);
        mDotsView.setOnClickListener(mDotsViewClickListener);
        setUpLayoutChangeListener();
    }

    /* Layout changes listener */
    // --------------------------------------------------------------------------------------------
    private void setUpLayoutChangeListener() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    switch (mCurrentState) {
                        case STATE_COLLAPSED:
                            ((TextView) mDotsView).setText(String.format(getResources().getString(R.string.keywords_flow_layout_more), mRestItems));
                            break;
                        case STATE_EXPANDED:
                            ((TextView) mDotsView).setText(getResources().getString(R.string.keywords_flow_layout_more_collapse));
                            break;
                    }
                } catch (Exception e) {
                    Timber.e(TAG, "Preventing from crash...");
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    // ------------------------------------------
    @Override
    protected void onDotsViewClicked() {
        super.onDotsViewClicked();
        setUpLayoutChangeListener();
    }

    public void setKeywords(@NonNull List<Keyword> keywords, boolean isEditable) {
        removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (Keyword keyword : keywords) {
            // TODO: use passive view, if need
            @LayoutRes int resId = isEditable ? R.layout.keywords_blob_active_edit : R.layout.keywords_blob_active;
            TextView blob = (TextView) inflater.inflate(resId, null, false);
            blob.setText(keyword.keyword());
            addView(blob);
        }
    }
}
