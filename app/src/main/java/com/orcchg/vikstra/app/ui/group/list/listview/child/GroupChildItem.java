package com.orcchg.vikstra.app.ui.group.list.listview.child;

import com.orcchg.vikstra.app.ui.base.adapter.expandable.base.BaseChildItem;
import com.orcchg.vikstra.domain.model.Group;

public class GroupChildItem extends BaseChildItem {

    private final Group group;

    public GroupChildItem(Group group) {
        this.group = group;
    }

    public long getId() {
        return group.id();
    }
    public String getName() {
        return group.name();
    }
    public int getCount() {
        return group.membersCount();
    }

    public Group getGroup() {
        return group;
    }

    public boolean isSelected() {
        return group.isSelected();
    }
    public void setSelected(boolean isSelected) {
        group.setSelected(isSelected);
    }
}
