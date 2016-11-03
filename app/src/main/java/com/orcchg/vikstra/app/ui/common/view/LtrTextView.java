package com.orcchg.vikstra.app.ui.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class LtrTextView extends TextView {

    public LtrTextView(Context context) {
        super(context);
    }

    public LtrTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LtrTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        // override
    }
}
