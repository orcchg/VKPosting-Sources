package com.orcchg.vikstra.app.ui.group.list.listview.parent;

import com.orcchg.vikstra.domain.model.Keyword;

import java.util.ArrayList;

public class AddNewKeywordParentItem extends GroupParentItem {

    public AddNewKeywordParentItem(Keyword keyword) {
        super(keyword, new ArrayList<>());
    }
}
