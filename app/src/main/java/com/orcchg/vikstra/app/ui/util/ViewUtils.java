package com.orcchg.vikstra.app.ui.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.TypedValue;

public class ViewUtils {

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

    public static void showSnackbar(Activity activity, String text) {
        Snackbar.make(activity.findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show();
    }
}
