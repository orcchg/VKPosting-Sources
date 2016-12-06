package com.orcchg.vikstra.app.ui.common.content;

public interface IScrollList {
    void retry();
    void onEmpty();
    void onScroll(int itemsLeftToEnd);
}
