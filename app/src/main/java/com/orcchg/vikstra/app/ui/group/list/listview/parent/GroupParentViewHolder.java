package com.orcchg.vikstra.app.ui.group.list.listview.parent;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.expandable.base.BaseParentViewHolder;
import com.orcchg.vikstra.app.ui.group.list.OnAllGroupsSelectedListener;
import com.orcchg.vikstra.app.ui.group.list.listview.child.GroupChildItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class GroupParentViewHolder extends BaseParentViewHolder<GroupParentItem, GroupChildItem> {

    @BindView(R.id.tv_title) TextView titleTextView;
    @Nullable @BindView(R.id.tv_selected_count) TextView selectedCountTextView;
    @Nullable @BindView(R.id.iv_arrow) ImageView arrowImageView;
    @Optional @OnClick(R.id.ibtn_select_all)
    void onSelectAllClick() {
        GroupParentItem parentItem = getParent();
        boolean isNotAllSelected = parentItem.getChildCount() != parentItem.getSelectedCount();

        for (GroupChildItem childItem : parentItem.getChildList()) {
            childItem.setSelected(isNotAllSelected);
        }
        if (onAllGroupsSelectedListener != null) {
            onAllGroupsSelectedListener.onAllGroupsSelected(parentItem, getAdapterPosition(), isNotAllSelected);
        }
    }

    private OnAllGroupsSelectedListener onAllGroupsSelectedListener;

    public GroupParentViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setOnAllGroupsSelectedListener(OnAllGroupsSelectedListener listener) {
        onAllGroupsSelectedListener = listener;
    }

    @Override
    public void bind(GroupParentItem model) {
        titleTextView.setText(model.getName());
        updateCounter(model.getSelectedCount(), model.getChildCount());
    }

    @Override
    public void onExpansionToggled(boolean expanded) {
        super.onExpansionToggled(expanded);
        if (arrowImageView == null) return;
        if (expanded) {
            arrowImageView.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
        } else {
            arrowImageView.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
        }
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private void updateCounter(int selectedCount, int totalCount) {
        if (selectedCountTextView != null) {
            String text = new StringBuilder(Integer.toString(selectedCount)).append('/').append(totalCount).toString();
            selectedCountTextView.setText(text);
        }
    }
}
