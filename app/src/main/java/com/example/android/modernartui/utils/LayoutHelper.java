package com.example.android.modernartui.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class LayoutHelper {

    public final static int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;
    public final static int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;

    /**
     * Margin values are in pixels.
     */
    public static void setMargin(View view, int left, int top, int right, int bottom) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        params.setMargins(left, top, right, bottom);
        view.setLayoutParams(params);
    }

    /**
     * Margin values are in pixels.
     */
    public static void setMargin(View view, int margin) {
        setMargin(view, margin, margin, margin, margin);
    }

    public static float pxToDp(Context context, int px){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static int dpToPx(Context context, float dp){
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * ((float) DisplayMetrics.DENSITY_DEFAULT / metrics.densityDpi));
    }

    public static int getPerpendicularOrientation(int orientation) {
        if (orientation == LinearLayout.HORIZONTAL)
            return LinearLayout.VERTICAL;
        return LinearLayout.HORIZONTAL;
    }

}
