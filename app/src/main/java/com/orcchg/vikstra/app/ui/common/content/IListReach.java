package com.orcchg.vikstra.app.ui.common.content;

/**
 * Scrolling events to top and to bottom and the ability to detect
 * whether the topmost or bottommost positions were reached.
 */
public interface IListReach {
    void hasReachedTop(boolean reached);
    void hasReachedBottom(boolean reached);
}
