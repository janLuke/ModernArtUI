package com.janluke.modernartui;

import android.content.Context;
import android.util.DisplayMetrics;


public class Util {

    public static int dpToPx(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * ((float) DisplayMetrics.DENSITY_DEFAULT / metrics.densityDpi));
    }

}
