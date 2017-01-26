package com.orcchg.vikstra.app.ui.group.list;

import com.orcchg.vikstra.app.ui.group.list.listview.parent.GroupParentItem;

public interface OnAllGroupsSelectedListener {
    void onAllGroupsSelected(GroupParentItem model, int position, boolean isSelected);
}
