package com.janluke.modernartui.colors;

import android.graphics.Color;
import android.util.Log;

import java.util.Arrays;
import java.util.Random;


public abstract class ColorSampler
{
    private static int HUE = 0;
    private static int SATURATION = 1;
    private static int BRIGHTNESS = 2;
    private static String[] HSV_STRINGS = {"hue", "saturation", "brightness"};
    private static String RANGE_ERROR_FORMAT
            = "The sampled %1$s is not in the range [min %1$s, max %1$s). " +
            "It should be in range: [%g, %g). It's: %g";

    private float[] hsv = new float[3];
    private float[] hsvNext = new float[3];
    protected float[] hsvMin = {0f, 0f, 0f};
    protected float[] hsvMax = {1f, 1f, 1f};
    protected boolean[] hsvVary = {true, true, true};

    protected Random random = new Random();

    protected ColorSampler() {
        hsvNext[HUE] = random.nextFloat();
        hsvNext[SATURATION] = random.nextFloat();
        hsvNext[BRIGHTNESS] = random.nextFloat();
    }

    protected abstract float sampleParameter(int index, float current, float min, float max);

    protected static float scale(float value, float min, float max) {
        return min + value * (max - min);
    }

    public int nextColor() {
        for (int i=0; i<=2; i++) {
            if (hsvVary[i]) {
                hsvNext[i] = sampleParameter(i, hsvNext[i], hsvMin[i], hsvMax[i]);
                checkSampledParameter(i, hsvNext[i]);
            }
            hsv[i] = hsvNext[i];
        }
        hsv[HUE] *= 360f;
        Log.v(getClass().getSimpleName(), Arrays.toString(hsv));
        return Color.HSVToColor(hsv);
    }

    private void checkSampledParameter(int index, float value) {
        if (value < hsvMin[index] || value >= hsvMax[index]) {
            String parameterName = HSV_STRINGS[index];
            float min = hsvMin[index];
            float max = hsvMax[index];
            throw new IllegalStateException(
                    String.format(RANGE_ERROR_FORMAT, parameterName, min, max, value)
            );
        }
    }

    public boolean isHueFixed() {
        return !hsvVary[HUE];
    }

    public boolean isSaturationFixed() {
        return !hsvVary[SATURATION];
    }

    public boolean isBrightnessFixed() {
        return !hsvVary[BRIGHTNESS];
    }

    public float getFixedHue() {
        return getFixedParameter(HUE);
    }

    public float getFixedSaturation() {
        return getFixedParameter(SATURATION);
    }

    public float getFixedBrightness() {
        return getFixedParameter(BRIGHTNESS);
    }

    public float getFixedParameter(int index) {
        return (!hsvVary[index]) ? hsvNext[index] : -1;
    }

    public void keepHueFixedTo(float value) {
        keepParameterFixedTo(HUE, value);
    }

    public void keepSaturationFixedTo(float value) {
        keepParameterFixedTo(SATURATION, value);
    }

    public void keepBrightnessFixedTo(float value) {
        keepParameterFixedTo(BRIGHTNESS, value);
    }

    public void keepParameterFixedTo(int index, float value) {
        checkFixedParameter(index, value);
        hsvVary[index] = false;
        hsvNext[index] = value;
    }

    private void checkFixedParameter(int index, float value) {
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException(
                    HSV_STRINGS[index] + " must be in range [0, 1]. It's: " + value
            );
        }
    }

    public void setParameterRange(int index, float min, float max) {
        if (min > max || min < 0 || min > 1 || max < 0 || max > 1) {
            throw new IllegalArgumentException(
                    String.format("Invalid range for %s: [%g, %g]. Both values must be in range [0, 1] and min <= max",
                            HSV_STRINGS[index], min, max)
            );
        }
        hsvVary[index] = true;
        hsvMin[index] = min;
        hsvMax[index] = max;
    }

    public void setHueRange(float min, float max) {
        setParameterRange(HUE, min, max);
    }

    public void setSaturationRange(float min, float max) {
        setParameterRange(SATURATION, min, max);
    }

    public void setBrightnessRange(float min, float max) {
        setParameterRange(BRIGHTNESS, min, max);
    }


    protected abstract static class ABuilder<T extends ColorSampler, B extends ABuilder<T,B>>
    {
        protected T object;
        protected B thisBuilder;
        /** 
         * Each concrete implementing subclass overrides this so that 
         * T becomes an object of the concrete subclass
         */
        protected abstract T getObject();
        /**
         * Each concrete implementing subclass builder overrides this for the same reason,
         * but for B for the builder
         */
        protected abstract B thisBuilder();

        public T build() {
            return object;
        }

        protected ABuilder() {
            object = getObject();
            thisBuilder = thisBuilder();
        }

        public B withFixedHue(float value) {
            object.keepHueFixedTo(value);
            return thisBuilder;
        }

        public B withFixedSaturation(float value) {
            object.keepSaturationFixedTo(value);
            return thisBuilder;
        }

        public B withFixedBrightness(float value) {
            object.keepBrightnessFixedTo(value);
            return thisBuilder;
        }

        public B withHueInRange(float rangeMin, float rangeMaxExcluded) {
            object.setHueRange(rangeMin, rangeMaxExcluded);
            return thisBuilder;
        }

        public B withSaturationInRange(float rangeMin, float rangeMaxExcluded) {
            object.setSaturationRange(rangeMin, rangeMaxExcluded);
            return thisBuilder;
        }

        public B withBrightnessInRange(float rangeMin, float rangeMaxExcluded) {
            object.setBrightnessRange(rangeMin, rangeMaxExcluded);
            return thisBuilder;
        }
    }

}
