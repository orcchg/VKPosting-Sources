package com.orcchg.vikstra.app.ui.common.content;

public interface ISelectableModel {
    long id();
    boolean getSelection();
    void setSelection(boolean isSelected);
}
