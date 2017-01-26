package com.orcchg.vikstra.app.ui.group.list.listview.parent;

import com.orcchg.vikstra.app.ui.base.adapter.expandable.base.BaseParentItem;
import com.orcchg.vikstra.app.ui.group.list.listview.child.GroupChildItem;
import com.orcchg.vikstra.domain.model.Keyword;

import java.util.ArrayList;
import java.util.List;

public class GroupParentItem extends BaseParentItem<GroupChildItem> {

    private final Keyword keyword;
    private List<GroupChildItem> childItems;
    private int selectedCount;

    public GroupParentItem(Keyword keyword) {
        this(keyword, new ArrayList<>());
    }

    public GroupParentItem(Keyword keyword, List<GroupChildItem> childItems) {
        this.keyword = keyword;
        this.childItems = childItems;
    }

    public void setChildList(List<GroupChildItem> childItems) {
        this.childItems = childItems;
    }
    public void setSelectedCount(int selectedCount) {
        this.selectedCount = selectedCount;
    }
    public void incrementSelectedCount(int add) {
        this.selectedCount += add;
    }

    public Keyword getKeyword() {
        return keyword;
    }
    public String getName() {
        return keyword.keyword();
    }

    @Override
    public List<GroupChildItem> getChildList() {
        return childItems;
    }

    public int getChildCount() {
        if (childItems == null) return 0;
        return childItems.size();
    }

    public int getSelectedCount() {
        return selectedCount;
    }
}
