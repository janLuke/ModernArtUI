package com.janluke.modernartui;

import android.graphics.Color;
import android.view.View;

import com.janluke.modernartui.colors.ColorSampler;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;


/**
 * An artwork composed by colored tiles.
 * Assignment constraint: at least one of the tiles must be white and its color can't be changed.
 */
public class Artwork {
    private ArtworkNode root;
    private boolean forceWhiteNodes;
    private Set<ArtworkNode> lockedWhiteNodes = new HashSet<>();

    /**
     * @param root            The root ArtworkNode
     * @param forceWhiteNodes Require at least a node (a tile) to be white and unmodifiable
     */
    public Artwork(ArtworkNode root, boolean forceWhiteNodes) {
        this.root = root;
        this.forceWhiteNodes = forceWhiteNodes;
    }

    public Artwork(ArtworkNode root) {
        this(root, false);
    }

    public ArtworkNode getRoot() {
        return root;
    }

    public View getView() {
        return root.getView();
    }

    public void recolor(ColorSampler sampler) {
        lockedWhiteNodes.clear();
        if (forceWhiteNodes) {
            // Ensure there's at least one white node at each level of the tree
            Random random = new Random();

            ArtworkNode.LevelsIterator levelsIterator = root.levelsIterator();
            while (levelsIterator.hasNext()) {
                List<ArtworkNode> level = levelsIterator.next();
                for (ArtworkNode node : level) {
                    node.setColor(sampler.nextColor());
                }
                int whiteNodeIndex = random.nextInt(level.size());
                ArtworkNode whiteNode = level.get(whiteNodeIndex);
                whiteNode.setColor(Color.WHITE);
                lockedWhiteNodes.add(whiteNode);
            }
        } else {
            root.traverseBreadthFirst(node ->
                    node.setColor(sampler.nextColor()));
        }
    }

    public void setStrokeWidth(float strokeWidthInDp) {
        int strokeWidthInPx = Util.dpToPx(root.getContext(), strokeWidthInDp);
        root.traverseBreadthFirst(node ->
                node.setMarginBetweenChildren(strokeWidthInPx));
    }

    public void setSaturation(float saturation) {
        root.traverseBreadthFirst(node -> {
            if (!lockedWhiteNodes.contains(node))
                node.setSaturation(saturation);
        });
    }

    public void setBrightness(float brightness) {
        root.traverseBreadthFirst(node -> {
            if (!lockedWhiteNodes.contains(node))
                node.setBrightness(brightness);
        });
    }

    public void setMinLayoutSize(float sizeInDp) {
        int sizeInPx = Util.dpToPx(root.getContext(), sizeInDp);
        root.traverseBreadthFirst(node -> {
            if (!node.isLeaf())
                node.showChildren(node.leafView.getWidth() >= sizeInPx
                        && node.leafView.getHeight() >= sizeInPx);
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
