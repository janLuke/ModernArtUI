package com.janluke.modernartui;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.janluke.modernartui.colors.ColorSampler;
import com.janluke.modernartui.utils.LayoutHelper;

import java.util.Random;

public class ModernArtworkGenerator {

    private static final String TAG = ModernArtworkGenerator.class.getSimpleName();

    public static final int DEFAULT_MAX_DEPTH = 5;

    public static final int DEFAULT_MIN_NUM_CHILDREN = 2;
    public static final int DEFAULT_MAX_NUM_CHILDREN = 6;

    public static final int DEFAULT_MIN_LAYOUT_WEIGHT = 10;
    public static final int DEFAULT_MAX_LAYOUT_WEIGHT = 20;
    public static final float DEFAULT_STROKE_WIDTH_IN_DP = 5;

    private int maxDepth = DEFAULT_MAX_DEPTH;
    private int minNumChildren = DEFAULT_MIN_NUM_CHILDREN;
    private int maxNumChildren = DEFAULT_MAX_NUM_CHILDREN;
    private int minLayoutWeight = DEFAULT_MIN_LAYOUT_WEIGHT;
    private int maxLayoutWeight = DEFAULT_MAX_LAYOUT_WEIGHT;

    private float strokeWidthInDp = DEFAULT_STROKE_WIDTH_IN_DP;
    private int strokeWidthInPx;

    private boolean forceWhiteNodes = true;

    private Random random = new Random();


    public ModernArtwork generateArtwork(ViewGroup parent, ColorSampler colorSampler) {
        // Clean up
        parent.removeAllViews();

        strokeWidthInPx = LayoutHelper.dpToPx(parent.getContext(), strokeWidthInDp);

        // Create root node
        ArtworkNode root = new ArtworkNode(parent.getContext(), 0, strokeWidthInPx);
        root.layout.setOrientation(sampleOrientation());
        parent.addView(root.getView(), LayoutHelper.MATCH_PARENT,
                                       LayoutHelper.MATCH_PARENT);

        generateArtwork(root, 0);
        ModernArtwork artwork = new ModernArtwork(root, forceWhiteNodes);
        artwork.recolor(colorSampler);
        return artwork;
    }

    void generateArtwork(ArtworkNode parent, int depth) {
        Context context = parent.getContext();
        int childrenOrientation = LayoutHelper.getPerpendicularOrientation(parent.getOrientation());

        // Probability for a child to be a leaf in the tree
        float probabilityChildIsLeaf = 1f - (float) Math.sqrt((maxDepth - depth) / (float) maxDepth);

        // Sample the number of children and the corresponding layout_weight's
        // The maximum number of children is a function of depth (decrease with depth) and it's
        // linearly interpolated from minNumChildren and maxNumChildren
        int maxExtraChildren = maxNumChildren - minNumChildren;
        float depthMultiplier = (DEFAULT_MAX_DEPTH - depth) / (float) DEFAULT_MAX_DEPTH;
        int maxExtraChildrenForDepth = Math.round(maxExtraChildren * depthMultiplier);
        Log.v(TAG, "Max number of extra children: " + maxExtraChildrenForDepth);

        int numChildren = minNumChildren;
        if (maxExtraChildrenForDepth > 0)
            numChildren += random.nextInt(maxExtraChildrenForDepth); // nextInt(n) wants n>0

        for (int i=0; i<numChildren; i++) {
            ArtworkNode childNode = new ArtworkNode(context, 0, strokeWidthInPx);
            childNode.layout.setOrientation(childrenOrientation);

            int weight = minLayoutWeight + random.nextInt(maxLayoutWeight - minLayoutWeight);
            parent.addChild(childNode, weight);

            boolean childIsLeaf = (random.nextFloat() < probabilityChildIsLeaf);
            if (!childIsLeaf)
                generateArtwork(childNode, depth + 1);        // recursive call
            else
                childNode.showChildren(false);
        }
    }

    int sampleOrientation() {
        return (random.nextBoolean()) ? LinearLayout.VERTICAL :
                                        LinearLayout.HORIZONTAL;
    }

    public float getStrokeWidthInDp() {
        return strokeWidthInDp;
    }

    public void setStrokeWidthInDp(int strokeWidthInDp) {
        this.strokeWidthInDp = strokeWidthInDp;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getMinNumChildren() {
        return minNumChildren;
    }

    public void setMinNumChildren(int minNumChildren) {
        if (minNumChildren <= 0)
            throw new IllegalArgumentException("minNumChildren must be >= 1: It's: " + minNumChildren);
        if (minNumChildren > maxNumChildren)
            throw new IllegalArgumentException("minNumChildren must be <= maxNumChildren");
        this.minNumChildren = minNumChildren;
    }

    public int getMaxNumChildren() {
        return maxNumChildren;
    }

    public void setMaxNumChildren(int maxNumChildren) {
        if (maxNumChildren <= 0)
            throw new IllegalArgumentException("maxNumChildren must be >= 1: It's: " + maxNumChildren);
        if (maxNumChildren < minNumChildren)
            throw new IllegalArgumentException("maxNumChildren must be >= minNumChildren");
        this.maxNumChildren = maxNumChildren;
    }

    public double getMinLayoutWeight() {
        return minLayoutWeight;
    }

    public void setMinLayoutWeight(int minLayoutWeight) {
        this.minLayoutWeight = minLayoutWeight;
    }

    public double getMaxLayoutWeight() {
        return maxLayoutWeight;
    }

    public void setMaxLayoutWeight(int maxLayoutWeight) {
        this.maxLayoutWeight = maxLayoutWeight;
    }

}
