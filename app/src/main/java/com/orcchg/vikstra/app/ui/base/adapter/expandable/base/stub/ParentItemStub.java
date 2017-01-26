package com.orcchg.vikstra.app.ui.base.adapter.expandable.base.stub;

import com.orcchg.vikstra.app.ui.base.adapter.expandable.base.BaseParentItem;

import java.util.ArrayList;
import java.util.List;

public class ParentItemStub extends BaseParentItem<ChildItemStub> {

    private List<ChildItemStub> childItems = new ArrayList<>();  // empty list

    @Override
    public List<ChildItemStub> getChildList() {
        return childItems;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }
}
