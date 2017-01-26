package com.orcchg.vikstra.app.ui.group.list.listview.parent;

import android.view.View;

public class AddNewKeywordParentViewHolder extends GroupParentViewHolder {

    public AddNewKeywordParentViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void bind(GroupParentItem model) {
        titleTextView.setText(model.getKeyword().keyword());
    }
}
