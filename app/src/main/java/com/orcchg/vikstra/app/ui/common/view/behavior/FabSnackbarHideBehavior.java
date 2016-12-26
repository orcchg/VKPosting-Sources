package com.orcchg.vikstra.app.ui.common.view.behavior;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.View;

public class FabSnackbarHideBehavior extends CoordinatorLayout.Behavior<View> {

    public FabSnackbarHideBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float scaleFactor = dependency.getTranslationY() / dependency.getHeight();
        child.setScaleX(scaleFactor);
        child.setScaleY(scaleFactor);
        return false;
    }
}
