package com.example.android.modernartui.colors;


public class OffsetColorSampler extends ColorSampler {

    float offset;

    @Override
    protected float sampleParameter(int index, float current, float min, float max) {
        current += offset;
        current -= (int) current;
        return scale(current, min, max);
    }

    public static final class Builder extends ABuilder<OffsetColorSampler, Builder> {

        public Builder(float offset) {
            object.offset = offset;
        }

        @Override
        protected OffsetColorSampler getObject() {
            return new OffsetColorSampler();
        }

        @Override
        protected Builder thisBuilder() {
            return this;
        }
    }

}
