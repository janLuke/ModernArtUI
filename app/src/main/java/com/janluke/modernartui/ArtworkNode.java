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


public class ArtworkNode {
    // For convenience and readability
    private static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;

    private List<ArtworkNode> children = new ArrayList<>();

    View view;
    LinearLayout layout;
    ViewSwitcher viewSwitcher;

    private float[] viewColorHSV = new float[3];
    private int marginBetweenChildren;

    boolean isShowingChildren = true;

    private OnClickListener listener;


    public ArtworkNode(Context context, int color, int marginBetweenChildren) {
        // Create sub-views
        view = new View(context);
        layout = new LinearLayout(context);

        // Switcher
        viewSwitcher = new ViewSwitcher(context);
        viewSwitcher.addView(layout, MATCH_PARENT, MATCH_PARENT);
        viewSwitcher.addView(view, MATCH_PARENT, MATCH_PARENT);

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
        if (layout.getOrientation() == LinearLayout.HORIZONTAL) {
            params = new LinearLayout.LayoutParams(0, MATCH_PARENT, weight);
            marginLeft = marginBetweenChildren;
        }
        else {
            params = new LinearLayout.LayoutParams(MATCH_PARENT, 0, weight);
            marginTop = marginBetweenChildren;
        }

        if (children.size() > 1)
            params.setMargins(marginLeft, marginTop, 0, 0);

        childView.setLayoutParams(params);
        layout.addView(childView);
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
        return Color.HSVToColor(viewColorHSV);
    }

    public void getColorHSV(float[] hsv) {
        hsv[0] = this.viewColorHSV[0];
        hsv[1] = this.viewColorHSV[1];
        hsv[2] = this.viewColorHSV[2];
    }

    public float getHue() { return viewColorHSV[0]; }
    public float getSaturation() { return viewColorHSV[1]; }
    public float getBrightness() { return viewColorHSV[2]; }


    public void setColor(int color) {
        Color.colorToHSV(color, viewColorHSV);
        view.setBackgroundColor(color);
    }

    public void setColorHSV(float[] hsv) {
        setColorHSV(hsv[0], hsv[1], hsv[2]);
    }

    public void setColorHSV(float h, float s, float v) {
        viewColorHSV[0] = h;
        viewColorHSV[1] = s;
        viewColorHSV[2] = v;
        view.setBackgroundColor(Color.HSVToColor(viewColorHSV));
    }

    public View getView() {
        return viewSwitcher;
    }

    public Iterator<ArtworkNode> childrenIterator() {
        return children.iterator();
    }

    public void setHue(float hue) {
        viewColorHSV[0] = hue;
        view.setBackgroundColor(Color.HSVToColor(viewColorHSV));
    }

    public void setSaturation(float saturation) {
        viewColorHSV[1] = saturation;
        view.setBackgroundColor(Color.HSVToColor(viewColorHSV));
    }

    public void setBrightness(float brightness) {
        viewColorHSV[2] = brightness;
        view.setBackgroundColor(Color.HSVToColor(viewColorHSV));
    }

    public void setMargins(int left, int top, int right, int bottom) {
        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) viewSwitcher.getLayoutParams();
        params.setMargins(left, top, right, bottom);
        viewSwitcher.setLayoutParams(params);
    }

    public void setMargin(int margin) {
        setMargins(margin, margin, margin, margin);
    }

    public Context getContext() {
        return viewSwitcher.getContext();
    }

    public int getOrientation() {
        return layout.getOrientation();
    }

//    public void setOrientation(int orientation) {
//        int oldOrientation = layout.getOrientation();
//        layout.setOrientation(orientation);
//        if (oldOrientation!= orientation) {
//            // TODO update children layout parameters and propagate the change of orientation
//        }
//    }


    public void setMarginBetweenChildren(int margin) {
        int numChildren = children.size();
        int left = 0;
        int top = 0;
        if (layout.getOrientation() == LinearLayout.HORIZONTAL)
            left = margin;
        else
            top = margin;

        for (int i=1; i<numChildren; i++) {
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
        view.setOnClickListener(view -> listener.onClick(this));
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
            for (ArtworkNode node : currentLevel){
                nextLevel.addAll(node.children);
            }

            return currentLevel;
        }
    }
}
