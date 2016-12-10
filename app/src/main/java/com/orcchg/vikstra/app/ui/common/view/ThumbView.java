package com.orcchg.vikstra.app.ui.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.orcchg.vikstra.R;

import butterknife.ButterKnife;

public class ThumbView extends FrameLayout {

    public ThumbView(Context context) {
        this(context, null);
    }

    public ThumbView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ThumbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /* View */
    // --------------------------------------------------------------------------------------------
    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View rootView = inflater.inflate(R.layout.thumb_view, this, true);
        ButterKnife.bind(rootView);
    }
}
