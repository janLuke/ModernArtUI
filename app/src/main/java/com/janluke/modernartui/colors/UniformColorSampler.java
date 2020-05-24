package com.janluke.modernartui.colors;


import android.graphics.Color;

import com.janluke.modernartui.Util;

import java.util.Random;


public class UniformColorSampler extends ConstrainedColorSampler<UniformColorSampler> {

    private Random random = new Random();
    private float[] hsb = {0f, 0f, 0f};

    private float sampleComponent(int c) {
        return (hasFixed(c)) ? minValueOf[c] : Util.randFloat(random, minValueOf[c], maxValueOf[c]);
    }

    @Override
    public int nextColor() {
        hsb[H] = sampleComponent(H);
        hsb[S] = sampleComponent(S);
        hsb[B] = sampleComponent(B);
        return Color.HSVToColor(hsb);
    }
}
