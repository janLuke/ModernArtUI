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

    protected float relativeOffset;
    protected float offset;
    protected Random random = new Random();
    private float[] color = {0f, 0f, 0f};

    /**
     * @param relativeOffset: A number between 0 and 1 (inclusive) that expresses the hue increment
     *                      as a percentage of the hue range width: the absolute increment varies
     *                      in function of the range you set using setHueRange(minHue, maxHue).
     */
    public HueOffsetColorSampler(float relativeOffset) {
        assert 0 <= relativeOffset && relativeOffset <= 1;
        this.relativeOffset = relativeOffset;
        this.offset = relativeOffset * HUE_LIMIT;
        color[H] = random.nextFloat() * HUE_LIMIT;
    }

    public static HueOffsetColorSampler withGoldenRatioOffset() {
        return new HueOffsetColorSampler(GOLDEN_RATIO_CONJUGATE);
    }

    @Override
    public HueOffsetColorSampler setHueRange(float min, float max) {
        super.setHueRange(min, max);
        offset = (max - min) * relativeOffset;
        color[H] = Math.min(max, color[H]);
        return this;
    }

    public HueOffsetColorSampler startFromHue(float initialHue) {
        assert 0 <= initialHue && initialHue <= HUE_LIMIT;
        color[H] = initialHue;
        return this;
    }

    protected float sampleComponent(int c) {
        return (hasFixed(c)) ? minValueOf[c] : Util.randFloat(random, minValueOf[c], maxValueOf[c]);
    }

    protected float nextHue() {
        float hue = color[H] + offset;
        if (hue > maxValueOf[H])
            return minValueOf[H] + hue - maxValueOf[H];
        return hue;
    }

    @Override
    public int nextColor() {
        color[H] = nextHue();
        color[S] = sampleComponent(S);
        color[B] = sampleComponent(B);
        return Color.HSVToColor(color);
    }
}
