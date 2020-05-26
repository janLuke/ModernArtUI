package com.janluke.modernartui;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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

    private boolean forceWhiteNodes = true;

    private Random rand = new Random();


    public Artwork generateArtwork(Context context, int width, int height) {

        ArtworkNode root = generateArtworkTree(context, 0, width, height);
        Artwork artwork = new Artwork(root, forceWhiteNodes);
        artwork.setStrokeWidth(strokeWidthInDp);
        if (colorSampler != null)
            artwork.recolor(colorSampler);

        return artwork;
    }

    private ArtworkNode generateArtworkTree(Context context, int depthLevel, int width, int height) {
        ArtworkNode node = new ArtworkNode(context, Color.WHITE);

        if (depthLevel == maxDepth)  {
            node.showChildren(false);
            return node;
        }

        int orientation = (depthLevel == 0)
            ? randOrientation()
            : (width >= height)
                ? LinearLayout.HORIZONTAL
                : LinearLayout.VERTICAL;
        node.childrenView.setOrientation(orientation);

        // The maximum number of children decreases with depth
        float depthProgress = depthLevel / (float) maxDepth;
        int maxExtraChildren = Math.round((1F - depthProgress) * (maxNumChildren - minNumChildren));
        int numChildren = minNumChildren + rand.nextInt(maxExtraChildren + 1);

        // Generate layout weights
        int deltaWeight = maxLayoutWeight - minLayoutWeight;
        int[] weights = new int[numChildren];
        int total = 0;
        for (int i = 0; i < numChildren; i++) {
            weights[i] = minLayoutWeight + rand.nextInt(deltaWeight);
            total += weights[i];
        }

        // Generate children subtrees
        int childWidth = width;
        int childHeight = height;
        for (int i = 0; i < numChildren; i++) {

            if (orientation == LinearLayout.HORIZONTAL)
                childWidth = Math.round(width * (weights[i] / (float) total));
            else
                childHeight = Math.round(height * (weights[i] / (float) total));

            ArtworkNode child = generateArtworkTree(context, depthLevel + 1, childWidth, childHeight);
            node.addChild(child, weights[i]);
        }

        return node;
    }

    private int randOrientation() {
        return (rand.nextBoolean()) ? LinearLayout.VERTICAL : LinearLayout.HORIZONTAL;
    }

    public void setColorSampler(ColorSampler colorSampler) {
        this.colorSampler = colorSampler;
    }

    public void setStrokeWidthInDp(int strokeWidthInDp) {
        this.strokeWidthInDp = strokeWidthInDp;
    }

}
