package com.orcchg.vikstra.app.ui.group.list.listview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.group.list.OnAllGroupsSelectedListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupParentViewHolder extends ParentViewHolder<GroupParentItem, GroupChildItem> {

//    boolean isAllSelected;

    @BindView(R.id.tv_title) TextView titleTextView;
    @BindView(R.id.tv_selected_count) TextView selectedCountTextView;
    @BindView(R.id.iv_arrow) ImageView arrowImageView;
    @OnClick(R.id.ibtn_select_all)
    void onSelectAllClick() {
//        isAllSelected = !isAllSelected;
//        GroupParentItem parentItem = getParent();
//        for (GroupChildItem childItem : parentItem.getChildList()) {
//            childItem.setSelected(isAllSelected);
//        }
//        if (onAllGroupsSelectedListener != null) {
//            onAllGroupsSelectedListener.onAllGroupsSelected(parentItem, getAdapterPosition(), isAllSelected);
//        }
    }

    private OnAllGroupsSelectedListener onAllGroupsSelectedListener;

    public GroupParentViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void setOnAllGroupsSelectedListener(OnAllGroupsSelectedListener listener) {
        onAllGroupsSelectedListener = listener;
    }

    public void bind(GroupParentItem model) {
//        /**
//         * Here {@link isAllSelected} is dropped on each re-bind of this
//         * {@link GroupParentViewHolder}, so this leads
//         */
//        isAllSelected = false;  // drop flag
//        int selectedChilds = 0;
//        List<GroupChildItem> childItems = getParent().getChildList();
//        for (GroupChildItem childItem : childItems) {
//            selectedChilds += childItem.isSelected() ? 1 : 0;
//        }
//        if (selectedChilds == childItems.size()) isAllSelected = true;
        titleTextView.setText(model.getName());
        updateCounter(model.getSelectedCount(), model.getChildCount());
    }

    @Override
    public void onExpansionToggled(boolean expanded) {
        super.onExpansionToggled(expanded);
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
