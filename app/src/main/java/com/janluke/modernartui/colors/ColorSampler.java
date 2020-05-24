package com.janluke.modernartui.colors;


/**
 * Can be used to generate a sequence of colors.
 */
@FunctionalInterface
public interface ColorSampler {

    /**
     * Returns the next color in the sequence.
     * @return integer representation of a color (the same used by Android Color APIs)
     */
    int nextColor();
}
