package com.janluke.modernartui;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * A piece of Artwork. Basically, it's a tree node with a double nature: at any moment it can behave:
 * - as a leaf, i.e. hiding its children
 * - or as an internal node, i.e. showing its children.
 */
public class ArtworkNode {
    // For shortness and readability
    private static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;

    private List<ArtworkNode> children = new ArrayList<>();

    ViewSwitcher viewSwitcher;
    View leafView;              // this is shown when the node works in "leaf mode"
    LinearLayout childrenView;   // children view are ViewSwitchers associated with of children ArtworkNodes

    private float[] viewColorHSB = new float[3];
    private int marginBetweenChildren;

    boolean isShowingChildren = true;

    private OnClickListener listener;

    public ArtworkNode(Context context, int color, int marginBetweenChildren) {
        // Create sub-views
        leafView = new View(context);
        childrenView = new LinearLayout(context);

        // Switcher
        viewSwitcher = new ViewSwitcher(context);
        viewSwitcher.addView(childrenView, MATCH_PARENT, MATCH_PARENT);
        viewSwitcher.addView(leafView, MATCH_PARENT, MATCH_PARENT);

        setColor(color);
        this.marginBetweenChildren = marginBetweenChildren;
    }

    public void addChild(ArtworkNode child, int weight) {
        children.add(child);
        View childView = child.getView();

        // Set layout parameters for the child view
        LinearLayout.LayoutParams params;
        int marginLeft = 0;
        int marginTop = 0;
        if (childrenView.getOrientation() == LinearLayout.HORIZONTAL) {
            params = new LinearLayout.LayoutParams(0, MATCH_PARENT, weight);
            marginLeft = marginBetweenChildren;
        } else {
            params = new LinearLayout.LayoutParams(MATCH_PARENT, 0, weight);
            marginTop = marginBetweenChildren;
        }

        if (children.size() > 1)
            params.setMargins(marginLeft, marginTop, 0, 0);

        childView.setLayoutParams(params);
        childrenView.addView(childView);
    }

    public Iterable<ArtworkNode> children() {
        return children;
    }

    public boolean isShowingChildren() {
        return isShowingChildren;
    }

    public void showChildren(boolean show) {
        if (isShowingChildren != show) {
            viewSwitcher.showNext();
            isShowingChildren = show;
        }
    }

    public int getColor() {
        return Color.HSVToColor(viewColorHSB);
    }

    public void getColorHSB(float[] hsv) {
        hsv[0] = this.viewColorHSB[0];
        hsv[1] = this.viewColorHSB[1];
        hsv[2] = this.viewColorHSB[2];
    }

    public float getHue() {
        return viewColorHSB[0];
    }

    public void setColor(int color) {
        Color.colorToHSV(color, viewColorHSB);
        leafView.setBackgroundColor(color);
    }

    public void setColorHSB(float[] hsb) {
        setColorHSB(hsb[0], hsb[1], hsb[2]);
    }

    public void setColorHSB(float h, float s, float b) {
        viewColorHSB[0] = h;
        viewColorHSB[1] = s;
        viewColorHSB[2] = b;
        leafView.setBackgroundColor(Color.HSVToColor(viewColorHSB));
    }

    public View getView() {
        return viewSwitcher;
    }

    public Iterator<ArtworkNode> childrenIterator() {
        return children.iterator();
    }

    public void setHue(float hue) {
        viewColorHSB[0] = hue;
        leafView.setBackgroundColor(Color.HSVToColor(viewColorHSB));
    }

    public void setSaturation(float saturation) {
        viewColorHSB[1] = saturation;
        leafView.setBackgroundColor(Color.HSVToColor(viewColorHSB));
    }

    public void setBrightness(float brightness) {
        viewColorHSB[2] = brightness;
        leafView.setBackgroundColor(Color.HSVToColor(viewColorHSB));
    }

    public void setMargins(int left, int top, int right, int bottom) {
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) viewSwitcher.getLayoutParams();
        params.setMargins(left, top, right, bottom);
        viewSwitcher.setLayoutParams(params);
    }

    public Context getContext() {
        return viewSwitcher.getContext();
    }

    public int getOrientation() {
        return childrenView.getOrientation();
    }

    public void setMarginBetweenChildren(int margin) {
        int numChildren = children.size();
        int left = 0;
        int top = 0;
        if (childrenView.getOrientation() == LinearLayout.HORIZONTAL)
            left = margin;
        else
            top = margin;

        for (int i = 1; i < numChildren; i++) {
            children.get(i).setMargins(left, top, 0, 0);
        }
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public interface OnClickListener {
        void onClick(ArtworkNode node);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
        leafView.setOnClickListener(view -> listener.onClick(this));
    }


    interface ArtworkNodeConsumer {
        void consume(ArtworkNode node);
    }


    public void traverseBreadthFirst(ArtworkNodeConsumer consumer) {
        traverseBreadthFirst(this, consumer);
    }

    public static void traverseBreadthFirst(ArtworkNode root, ArtworkNodeConsumer consumer) {
        Queue<ArtworkNode> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            ArtworkNode node = queue.poll();
            consumer.consume(node);
            Iterator<ArtworkNode> iterator = node.childrenIterator();
            while (iterator.hasNext())
                queue.add(iterator.next());
        }
    }

    public LevelsIterator levelsIterator() {
        return new LevelsIterator(this);
    }

    /**
     *  Visit artwork nodes level by level. Each level contains all nodes at the same depth.
     */
    public static class LevelsIterator implements Iterator<List<ArtworkNode>> {

        List<ArtworkNode> nextLevel;

        public LevelsIterator(ArtworkNode node) {
            nextLevel = new ArrayList<>();
            nextLevel.add(node);
        }

        @Override
        public boolean hasNext() {
            return !nextLevel.isEmpty();
        }

        @Override
        public List<ArtworkNode> next() {
            List<ArtworkNode> currentLevel = nextLevel;

            nextLevel = new ArrayList<>();
            for (ArtworkNode node : currentLevel) {
                nextLevel.addAll(node.children);
            }

            return currentLevel;
        }
    }
}
