package com.janluke.modernartui.colors;


import com.janluke.modernartui.Util;

/**
 * Color sampler that allows to specify constraints on the HSB (aka HSV) components of the generated
 * colors. Each component can be set to a fixed value or constrained to be in a specified interval.
 * NOTE: the hue component takes value in the interval [0f, 360f], while the other two
 * components take value in the range [0f, 1f].
 */
public abstract class ConstrainedColorSampler<T extends ConstrainedColorSampler> implements ColorSampler {
    // Color component indexes
    static final int H = 0;
    static final int S = 1;
    static final int B = 2;
    static final float[] COMPONENT_LIMIT = {360f, 1f, 1f};

    protected float[] minValueOf = {0f, 0f, 0f};
    protected float[] maxValueOf = COMPONENT_LIMIT.clone();

    public T setComponentRange(int comp, float min, float max) {
        Util.checkArg(0f <= min && min <= max && max <= COMPONENT_LIMIT[comp]);
        minValueOf[comp] = min;
        maxValueOf[comp] = max;
        return (T) this;
    }

    public T setHueRange(float min, float max) {
        return setComponentRange(H, min, max);
    }

    public T setSaturationRange(float min, float max) {
        return setComponentRange(S, min, max);
    }

    public T setBrightnessRange(float min, float max) {
        return setComponentRange(B, min, max);
    }

    public T setComponent(int component, float value) {
        return setComponentRange(component, value, value);
    }

    public T setHue(float value) {
        return setComponent(H, value);
    }

    public T setSaturation(float value) {
        return setComponent(S, value);
    }

    public T setBrightness(float value) {
        return setComponent(B, value);
    }

    public boolean hasFixed(int component) {
        return minValueOf[component] == maxValueOf[component];
    }
}
