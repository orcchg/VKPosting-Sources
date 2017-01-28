package com.orcchg.vikstra.app.ui.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
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

    // {@see http://stackoverflow.com/questions/15055458/detect-7-inch-and-10-inch-tablet-programmatically}
    public static float getSmallestWidth(Activity context) {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        int heightPixels = metrics.heightPixels;
        float scaleFactor = metrics.density;
        float widthDp = widthPixels / scaleFactor;
        float heightDp = heightPixels / scaleFactor;
        float smallestWidth = Math.min(widthDp, heightDp);
        return smallestWidth;
    }

    public static Bitmap getScreenshot(View view) {
        view.setDrawingCacheEnabled(true);
        Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bmp;
    }

    /**
     * Retrieves {@link Bitmap} from file specified with {@param path}, resized to adopt
     * the {@param targetWidth} and {@param targetHeight}.
     *
     * {@see https://developer.android.com/training/camera/photobasics.html}
     */
    public static Bitmap getBitmapFromFile(String path, int targetWidth, int targetHeight) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetWidth, photoH / targetHeight);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        return BitmapFactory.decodeFile(path, bmOptions);
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
