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
import android.view.inputmethod.InputMethodManager;

import com.orcchg.vikstra.R;

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
        showSnackbar(activity, resId, Snackbar.LENGTH_SHORT);
    }

    public static void showSnackbar(Activity activity, @StringRes int resId, int duration) {
        Snackbar.make(activity.findViewById(android.R.id.content), resId, duration).show();
    }

    public static void showSnackbar(Activity activity, @StringRes int resId, int duration,
                                    @StringRes int actionResId, View.OnClickListener listener) {
        Snackbar.make(activity.findViewById(android.R.id.content), resId, duration)
                .setActionTextColor(UiUtility.getAttributeColor(activity, R.attr.colorAccent))
                .setAction(actionResId, listener)
                .show();
    }

    public static void showSnackbar(Activity activity, String text) {
        showSnackbar(activity, text, Snackbar.LENGTH_SHORT);
    }

    public static void showSnackbar(Activity activity, String text, int duration) {
        Snackbar.make(activity.findViewById(android.R.id.content), text, duration).show();
    }

    public static void showSnackbar(Activity activity, String text, int duration,
                                    @StringRes int actionResId, View.OnClickListener listener) {
        Snackbar.make(activity.findViewById(android.R.id.content), text, duration)
                .setActionTextColor(UiUtility.getAttributeColor(activity, R.attr.colorAccent))
                .setAction(actionResId, listener)
                .show();
    }

    public static void showSnackbar(View view, @StringRes int resId) {
        showSnackbar(view, resId, Snackbar.LENGTH_SHORT);
    }

    public static void showSnackbar(View view, @StringRes int resId, int duration) {
        Snackbar.make(view, resId, duration).show();
    }

    public static void showSnackbar(View view, @StringRes int resId, int duration,
                                    @StringRes int actionResId, View.OnClickListener listener) {
        Snackbar.make(view, resId, duration)
                .setActionTextColor(view.getResources().getColor(R.color.accent))
                .setAction(actionResId, listener)
                .show();
    }

    public static void showSnackbar(View view, String text) {
        showSnackbar(view, text, Snackbar.LENGTH_SHORT);
    }

    public static void showSnackbar(View view, String text, int duration) {
        Snackbar.make(view, text, duration).show();
    }

    public static void showSnackbar(View view, String text, int duration,
                                    @StringRes int actionResId, View.OnClickListener listener) {
        Snackbar.make(view, text, duration)
                .setActionTextColor(view.getResources().getColor(R.color.accent))
                .setAction(actionResId, listener)
                .show();
    }

    public static void showSoftKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
}
