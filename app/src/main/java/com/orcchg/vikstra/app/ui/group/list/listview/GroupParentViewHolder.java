package com.orcchg.vikstra.app.ui.group.list.listview;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;
import com.orcchg.vikstra.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupParentViewHolder extends ParentViewHolder<GroupParentItem, GroupChildItem> {

    @BindView(R.id.tv_title) TextView titleTextView;
    @BindView(R.id.tv_selected_count) TextView selectedCountTextView;
    @BindView(R.id.iv_arrow) ImageView arrowImageView;
    @OnClick(R.id.ibtn_select_all)
    public void onSelectAllClick() {
        // TODO: select all child items
    }

    public GroupParentViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(GroupParentItem model) {
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
