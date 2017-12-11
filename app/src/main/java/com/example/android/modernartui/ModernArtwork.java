package com.example.android.modernartui;

import android.view.View;

import com.example.android.modernartui.colors.ColorSampler;
import com.example.android.modernartui.utils.LayoutHelper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;


public class ModernArtwork {
    private static final String TAG = ModernArtwork.class.getSimpleName();
    private static final int WHITE = 0xFFFFFFFF;

    private ArtworkNode root;
    private boolean forceWhiteNodes;
    private Set<ArtworkNode> blockedWhiteNodes = new HashSet<>();


    public ModernArtwork(ArtworkNode root) {
        this(root, false);
    }

    public ModernArtwork(ArtworkNode root, boolean forceWhiteNodes) {
        this.root = root;
        this.forceWhiteNodes = forceWhiteNodes;
    }

    public ArtworkNode getRoot() { return root; }


    public void recolor(ColorSampler sampler) {
        blockedWhiteNodes.clear();
        if (forceWhiteNodes) {
            Random random = new Random();

            ArtworkNode.LevelsIterator levelsIterator = root.levelsIterator();
            while (levelsIterator.hasNext()) {
                List<ArtworkNode> level = levelsIterator.next();
                for (ArtworkNode node : level) {
                    node.setColor(sampler.nextColor());
                }
                int whiteNodeIndex = random.nextInt(level.size());
                ArtworkNode whiteNode = level.get(whiteNodeIndex);
                whiteNode.setColor(WHITE);
                blockedWhiteNodes.add(whiteNode);
            }
        }
        else {
            root.traverseBreadthFirst(node ->
                    node.setColor(sampler.nextColor()));
        }
    }

    public void setStrokeWidth(float strokeWidthInDp) {
        int strokeWidthInPx = LayoutHelper.dpToPx(root.getContext(), strokeWidthInDp);
        root.traverseBreadthFirst(node ->
                node.setMarginBetweenChildren(strokeWidthInPx));
    }

    public void setSaturation(float saturation) {
        root.traverseBreadthFirst(node -> {
            if (!blockedWhiteNodes.contains(node))
                node.setSaturation(saturation);
        });
    }

    public void setBrightness(float brightness) {
        root.traverseBreadthFirst(node -> {
            if (!blockedWhiteNodes.contains(node))
                node.setBrightness(brightness);
        });
    }

    public void setMinLayoutSize(float sizeInDp) {
        int sizeInPx = LayoutHelper.dpToPx(root.getContext(), sizeInDp);
        root.traverseBreadthFirst(node -> {
            if (!node.isLeaf())
                node.showChildren(node.view.getWidth() >= sizeInPx
                                    && node.view.getHeight() >= sizeInPx);
        });
    }

    public void setDepthLimit(int depthLimit) {
        setDepthLimit(root, depthLimit);
    }

    private static void setDepthLimit(ArtworkNode node, int depthLimit) {
        if (depthLimit <= 0)
            node.showChildren(false);
        else if (!node.isLeaf()) {
            node.showChildren(true);
            Iterator<ArtworkNode> iterator = node.childrenIterator();
            depthLimit--;
            while (iterator.hasNext())
                setDepthLimit(iterator.next(), depthLimit);
        }
    }


    public void setOnNodesClickListener(ArtworkNode.OnClickListener listener) {
        root.traverseBreadthFirst(node ->
            node.setOnClickListener(listener));
    }
}
