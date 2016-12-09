package com.orcchg.vikstra.app.ui.group.list;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupChildItem;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupChildViewHolder;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupParentItem;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupParentViewHolder;

import java.util.List;

public class GroupListAdapter extends ExpandableRecyclerAdapter<GroupParentItem, GroupChildItem, GroupParentViewHolder, GroupChildViewHolder> {

    public interface OnCheckedChangeListener {
        void onCheckedChange(GroupChildItem data, boolean isChecked);
    }

    private CompoundButton.OnCheckedChangeListener childItemSwitcherListener;
    private OnCheckedChangeListener externalChildItemSwitcherListener;

    public GroupListAdapter(@NonNull List<GroupParentItem> parentItems) {
        super(parentItems);
        this.childItemSwitcherListener = createChildItemSwitcherListener();
    }

    public void setExternalChildItemSwitcherListener(OnCheckedChangeListener listener) {
        externalChildItemSwitcherListener = listener;
    }

    @NonNull @Override
    public GroupParentViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        Context context = parentViewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.group_list_parent_item, parentViewGroup, false);
        return new GroupParentViewHolder(itemView);
    }

    @NonNull @Override
    public GroupChildViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        Context context = childViewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.group_list_child_item, childViewGroup, false);
        GroupChildViewHolder viewHolder = new GroupChildViewHolder(itemView);
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

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private CompoundButton.OnCheckedChangeListener createChildItemSwitcherListener() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
            }
        };
    }
}
