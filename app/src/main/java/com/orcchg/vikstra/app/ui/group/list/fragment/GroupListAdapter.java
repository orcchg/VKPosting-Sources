package com.orcchg.vikstra.app.ui.group.list.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.group.list.OnAllGroupsSelectedListener;
import com.orcchg.vikstra.app.ui.group.list.OnGroupClickListener;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupChildItem;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupChildViewHolder;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupParentItem;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupParentViewHolder;

import java.util.List;

public class GroupListAdapter extends ExpandableRecyclerAdapter<GroupParentItem, GroupChildItem, GroupParentViewHolder, GroupChildViewHolder> {

    public interface OnCheckedChangeListener {
        void onCheckedChange(GroupChildItem data, boolean isChecked);
    }

    private OnGroupClickListener onGroupClickListener;
    private OnAllGroupsSelectedListener onAllGroupsSelectedListener;
    private CompoundButton.OnCheckedChangeListener childItemSwitcherListener;
    private OnCheckedChangeListener externalChildItemSwitcherListener;

    public GroupListAdapter(@NonNull List<GroupParentItem> parentItems,
                            OnGroupClickListener onGroupClickListener,
                            OnAllGroupsSelectedListener onAllGroupsSelectedListener) {
        super(parentItems);
        this.onGroupClickListener = onGroupClickListener;
        this.onAllGroupsSelectedListener = onAllGroupsSelectedListener;
        this.childItemSwitcherListener = createChildItemSwitcherListener();
    }

    public void setExternalChildItemSwitcherListener(OnCheckedChangeListener listener) {
        externalChildItemSwitcherListener = listener;
    }

    @NonNull @Override
    public GroupParentViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        Context context = parentViewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.group_list_parent_item, parentViewGroup, false);
        GroupParentViewHolder viewHolder = new GroupParentViewHolder(itemView);
        viewHolder.setOnAllGroupsSelectedListener(onAllGroupsSelectedListener);
        return viewHolder;
    }

    @NonNull @Override
    public GroupChildViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        Context context = childViewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.group_list_child_item, childViewGroup, false);
        GroupChildViewHolder viewHolder = new GroupChildViewHolder(itemView);
        viewHolder.setOnGroupClickListener(onGroupClickListener);
        viewHolder.setSwitcherListener(childItemSwitcherListener);
        return viewHolder;
    }

    @Override
    public void onBindParentViewHolder(@NonNull GroupParentViewHolder parentViewHolder, int parentPosition, @NonNull GroupParentItem parent) {
        parentViewHolder.bind(parent);
    }

    @Override
    public void onBindChildViewHolder(@NonNull GroupChildViewHolder childViewHolder, int parentPosition, int childPosition, @NonNull GroupChildItem child) {
        childViewHolder.bind(child);
    }

    /* Data access */
    // --------------------------------------------------------------------------------------------
    public void clear() {
        getParentList().clear();
        notifyParentDataSetChanged(false);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private CompoundButton.OnCheckedChangeListener createChildItemSwitcherListener() {
        return (buttonView, isChecked) -> {
            GroupChildViewHolder.SupplyData data = (GroupChildViewHolder.SupplyData) buttonView.getTag();
            int affectedParentItemPosition = data.getParentAdapterPosition();
            GroupChildItem childItem = data.getModel();
            GroupParentItem parentItem = getParentList().get(affectedParentItemPosition);
            childItem.setSelected(isChecked);
            parentItem.incrementSelectedCount(isChecked ? 1 : -1);
            notifyParentChanged(affectedParentItemPosition);  // re-bind parent and all it's childs
            // TODO: keep checked group id to use further
            if (externalChildItemSwitcherListener != null) {
                externalChildItemSwitcherListener.onCheckedChange(childItem, isChecked);
            }
        };
    }
}
