package com.janluke.modernartui.colors;


public class GoldenRatioColorSampler extends OffsetColorSampler {

    private static final float GOLDEN_RATIO_CONJUGATE = 0.618033988749895f;

    private GoldenRatioColorSampler() {
        offset = GOLDEN_RATIO_CONJUGATE;
    }

    public static final class Builder extends ABuilder<GoldenRatioColorSampler, Builder> {

        @Override
        protected GoldenRatioColorSampler getObject() {
            return new GoldenRatioColorSampler();
        }

        @Override
        protected Builder thisBuilder() {
            return this;
        }
    }
}
