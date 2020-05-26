package com.janluke.modernartui;

import android.content.Context;
import android.util.DisplayMetrics;

import java.util.Random;


public class Util {

    public static int dpToPx(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static float pxToDp(Context context, int px) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return px * ((float) DisplayMetrics.DENSITY_DEFAULT / metrics.densityDpi);
    }

    public static float randFloat(Random random, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public static void checkArg(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
