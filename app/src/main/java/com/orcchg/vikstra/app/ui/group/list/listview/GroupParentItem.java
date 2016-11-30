package com.orcchg.vikstra.app.ui.group.list.listview;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import java.util.List;

public class GroupParentItem implements Parent<GroupChildItem> {

    private String name;
    private List<GroupChildItem> childItems;

    public GroupParentItem(String name) {
        this(name, null);
    }

    public GroupParentItem(String name, List<GroupChildItem> childItems) {
        this.name = name;
        this.childItems = childItems;
    }

    public void setChildList(List<GroupChildItem> childItems) {
        this.childItems = childItems;
    }

    public String getName() {
        return name;
    }

    @Override
    public List<GroupChildItem> getChildList() {
        return childItems;
    }

    public int getChildCount() {
        if (childItems == null) return 0;
        return childItems.size();
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
