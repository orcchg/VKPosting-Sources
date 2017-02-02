package com.orcchg.vikstra.app.ui.common.showcase;

import android.graphics.Point;
import android.view.View;

import com.github.amlcurran.showcaseview.targets.ViewTarget;

public class PositionedViewTarget extends ViewTarget {

    private int[] location = new int[2];
    private int px, py;

    public PositionedViewTarget(View view, int px, int py) {
        super(view);
        view.getLocationInWindow(location);
        this.px = px;
        this.py = py;
    }

    @Override
    public Point getPoint() {
        int x = location[0] + px;
        int y = location[1] + py;
        return new Point(x, y);
    }
}
