package com.janluke.modernartui;

import android.content.Context;
import android.graphics.Color;
import android.widget.LinearLayout;

import com.janluke.modernartui.colors.ColorSampler;

import java.util.Random;


public class ArtworkGenerator {

    public static final int DEFAULT_MAX_DEPTH = 5;

    private ColorSampler colorSampler;
    private int maxDepth = DEFAULT_MAX_DEPTH;
    private int minNumChildren = 2;
    private int maxNumChildren = 5;
    private int minLayoutWeight = 10;
    private int maxLayoutWeight = 20;

    private float strokeWidthInDp = 5;
    private int strokeWidthInPx;

    private boolean forceWhiteNodes = true;

    private Random random = new Random();


    public Artwork generateArtwork(Context context) {

        strokeWidthInPx = Util.dpToPx(context, strokeWidthInDp);

        ArtworkNode root = new ArtworkNode(context, Color.BLACK, strokeWidthInPx);
        root.childrenView.setOrientation(randOrientation());
        growArtworkTreeRecursively(root, 0);

        Artwork artwork = new Artwork(root, forceWhiteNodes);

        if (colorSampler != null)
            artwork.recolor(colorSampler);
        return artwork;
    }

    private void growArtworkTreeRecursively(ArtworkNode parent, int depthLevel) {
        Context context = parent.getContext();

        int childrenOrientation = perpendicularTo(parent.getOrientation());

        // The probability for a child to be a leaf increases with depth
        float probabilityToBeLeaf = 1F - (float) Math.sqrt((maxDepth - depthLevel) / (float) maxDepth);

        // The maximum number of children decreases with depth
        float depthRatio = depthLevel / (float) maxDepth;
        int maxExtraChildren = Math.round((1F - depthRatio) * (maxNumChildren - minNumChildren));
        int numChildren = minNumChildren + random.nextInt(maxExtraChildren + 1);

        for (int i = 0; i < numChildren; i++) {
            ArtworkNode childNode = new ArtworkNode(context, Color.BLACK, strokeWidthInPx);
            childNode.childrenView.setOrientation(childrenOrientation);

            int weight = minLayoutWeight + random.nextInt(maxLayoutWeight - minLayoutWeight);
            parent.addChild(childNode, weight);

            boolean childIsLeaf = (random.nextFloat() < probabilityToBeLeaf);
            if (childIsLeaf)
                childNode.showChildren(false);
            else
                growArtworkTreeRecursively(childNode, depthLevel + 1);
        }
    }

    private static int perpendicularTo(int orientation) {
        return (orientation == LinearLayout.HORIZONTAL)
                ? LinearLayout.VERTICAL
                : LinearLayout.HORIZONTAL;
    }

    private int randOrientation() {
        return (random.nextBoolean()) ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL;
    }

    public void setColorSampler(ColorSampler colorSampler) {
        this.colorSampler = colorSampler;
    }

    public void setStrokeWidthInDp(int strokeWidthInDp) {
        this.strokeWidthInDp = strokeWidthInDp;
    }

}
