package com.orcchg.vikstra.app.ui.group.list.listview;

import com.orcchg.vikstra.domain.model.Group;

public class GroupChildItem {

    private final Group group;

    public GroupChildItem(Group group) {
        this.group = group;
    }

    public String getName() {
        return group.name();
    }

    public int getCount() {
        return group.membersCount();
    }
}
