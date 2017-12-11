package com.example.android.modernartui.colors;


public class UniformColorSampler extends ColorSampler {

    @Override
    protected float sampleParameter(int index, float current, float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    public static final class Builder extends ColorSampler.ABuilder<UniformColorSampler, Builder> {

        public Builder() {}

        @Override
        protected UniformColorSampler getObject() {
            return new UniformColorSampler();
        }

        @Override
        protected Builder thisBuilder() {
            return this;
        }
    }
}
