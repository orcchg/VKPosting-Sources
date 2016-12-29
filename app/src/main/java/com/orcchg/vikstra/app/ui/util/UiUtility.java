package com.orcchg.vikstra.app.ui.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;
import android.view.View;

public class UiUtility {

    public static int getAttributeColor(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        return context.getResources().getColor(typedValue.resourceId);
    }

    @Nullable
    public static Drawable getAttributeDrawable(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        return context.getResources().getDrawable(typedValue.resourceId);
    }

    public static float getAttributeDimension(Context context, int attributeId) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attributeId, typedValue, true);
        return context.getResources().getDimension(typedValue.resourceId);
    }

    public static Bitmap getScreenshot(View view) {
        view.setDrawingCacheEnabled(true);
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bmp;
    }

    public static boolean isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    public static void showSnackbar(Activity activity, @StringRes int resId) {
        Snackbar.make(activity.findViewById(android.R.id.content), resId, Snackbar.LENGTH_SHORT).show();
    }

    public static void showSnackbar(Activity activity, String text) {
        Snackbar.make(activity.findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show();
    }

    public static void showSnackbar(View view, @StringRes int resId) {
        Snackbar.make(view, resId, Snackbar.LENGTH_SHORT).show();
    }

    public static void showSnackbar(View view, String text) {
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show();
    }
}
