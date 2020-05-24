package com.janluke.modernartui.colors;

import android.graphics.Color;

import com.janluke.modernartui.Util;

import java.util.Random;


/**
 * Samples the hue component summing an offset to the previous hue value. The other two components
 * are sampled uniformly in their corresponding valid ranges.
 *
 * A good value of the offset (e.g. the golden ratio conjugate) guarantees that two consecutive
 * colors will be reasonably different.
 */
public class HueOffsetColorSampler extends ConstrainedColorSampler<HueOffsetColorSampler> {

    static final float HUE_LIMIT = COMPONENT_LIMIT[H];
    static final float GOLDEN_RATIO_CONJUGATE = 0.618033988749895f;

    protected float offset;
    protected Random random = new Random();
    private float[] color = {0f, 0f, 0f};

    public HueOffsetColorSampler(float offset) {
        assert 0 <= offset && offset <= 1;
        this.offset = offset;
        color[H] = random.nextFloat() * HUE_LIMIT;
    }

    public static HueOffsetColorSampler withGoldenRatioOffset() {
        return new HueOffsetColorSampler(GOLDEN_RATIO_CONJUGATE * HUE_LIMIT);
    }

    public HueOffsetColorSampler startFromHue(float initialHue) {
        assert 0 <= initialHue && initialHue <= HUE_LIMIT;
        color[H] = initialHue;
        return this;
    }

    protected float sampleComponent(int c) {
        return (hasFixed(c)) ? minValueOf[c] : Util.randFloat(random, minValueOf[c], maxValueOf[c]);
    }

    @Override
    public int nextColor() {
        color[H] = (color[H] + offset) % HUE_LIMIT;
        color[S] = sampleComponent(S);
        color[B] = sampleComponent(B);
        return Color.HSVToColor(color);
    }
}
