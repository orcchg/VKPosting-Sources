package com.orcchg.vikstra.app.ui.common.screen;

public interface LceView {
    void showContent(int tag, boolean isEmpty);
    void showEmptyList(int tag);
    void showError(int tag);
    void showLoading(int tag);
}
