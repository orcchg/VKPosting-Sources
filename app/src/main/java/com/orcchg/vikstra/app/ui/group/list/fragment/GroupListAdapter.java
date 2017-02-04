package com.orcchg.vikstra.app.ui.group.list.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.ui.base.adapter.expandable.BaseExpandableAdapter;
import com.orcchg.vikstra.app.ui.group.list.OnAllGroupsSelectedListener;
import com.orcchg.vikstra.app.ui.group.list.OnGroupClickListener;
import com.orcchg.vikstra.app.ui.group.list.listview.child.GroupChildItem;
import com.orcchg.vikstra.app.ui.group.list.listview.child.GroupChildViewHolder;
import com.orcchg.vikstra.app.ui.group.list.listview.parent.AddNewKeywordParentViewHolder;
import com.orcchg.vikstra.app.ui.group.list.listview.parent.GroupParentItem;
import com.orcchg.vikstra.app.ui.group.list.listview.parent.GroupParentViewHolder;
import com.orcchg.vikstra.domain.model.Keyword;

import java.util.List;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GroupListAdapter extends BaseExpandableAdapter<GroupParentItem, GroupChildItem, GroupParentViewHolder, GroupChildViewHolder> {

    private static final int TYPE_ADD_NEW = TYPE_FIRST_USER;

    public interface OnCheckedChangeListener {
        void onCheckedChange(GroupChildItem data, boolean isChecked);
    }

    private OnGroupClickListener onGroupClickListener;
    private OnAllGroupsSelectedListener onAllGroupsSelectedListener;
    private CompoundButton.OnCheckedChangeListener childItemSwitcherListener;
    private OnCheckedChangeListener externalChildItemSwitcherListener;

    private boolean isAddingNewItem;

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

    @DebugLog
    public void setAddingNewItem(boolean isAddingNewItem, Keyword keyword) {
        boolean oldValue = this.isAddingNewItem;
        this.isAddingNewItem = isAddingNewItem;
        if (isAddingNewItem) {
            GroupParentItem item = new GroupParentItem(keyword);
            getParentList().add(0, item);
            notifyParentInserted(0);
        } else if (oldValue) {  // ignore if it wasn't previously set to TRUE
            getParentList().remove(0);
            notifyParentRemoved(0);
        }
    }

    @NonNull @Override
    public GroupParentViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        Context context = parentViewGroup.getContext();
        GroupParentViewHolder viewHolder;
        switch (viewType) {
            case TYPE_ADD_NEW:
                View itemView1 = LayoutInflater.from(context).inflate(R.layout.group_list_add_new_keyword_item, parentViewGroup, false);
                viewHolder = new AddNewKeywordParentViewHolder(itemView1);
                break;
            case TYPE_PARENT:
            default:
                View itemView2 = LayoutInflater.from(context).inflate(R.layout.group_list_parent_item, parentViewGroup, false);
                viewHolder = new GroupParentViewHolder(itemView2);
                viewHolder.setOnAllGroupsSelectedListener(onAllGroupsSelectedListener);
                break;
        }
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
    public void onBindParentViewHolder(@NonNull GroupParentViewHolder parentViewHolder,
                                       int parentPosition, @NonNull GroupParentItem parent) {
        parentViewHolder.bind(parent);
    }

    @Override
    public void onBindChildViewHolder(@NonNull GroupChildViewHolder childViewHolder,
                                      int parentPosition, int childPosition, @NonNull GroupChildItem child) {
        childViewHolder.bind(child);
    }

    /* Data access */
    // --------------------------------------------------------------------------------------------
    public void clear() {
        if (!getParentList().isEmpty()) {
            getParentList().clear();
            notifyParentDataSetChanged(false);
        }
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private CompoundButton.OnCheckedChangeListener createChildItemSwitcherListener() {
        return (buttonView, isChecked) -> {
            GroupChildViewHolder.SupplyData data = (GroupChildViewHolder.SupplyData) buttonView.getTag();
            int affectedParentItemPosition = data.getParentAdapterPosition();
            GroupParentItem parentItem = getParentList().get(affectedParentItemPosition);
            GroupChildItem childItem = data.getModel();
            try {
                childItem.setSelected(isChecked);
                parentItem.incrementSelectedCount(isChecked ? 1 : -1);
                notifyParentChanged(affectedParentItemPosition);  // re-bind parent and all it's children
                if (externalChildItemSwitcherListener != null) {
                    externalChildItemSwitcherListener.onCheckedChange(childItem, isChecked);
                }
            } catch (IndexOutOfBoundsException e) {
                /**
                 * Temporary guarding workaround - when user is touching list items while the list
                 * is changing (for example, new Parent item with the set of child items is being added)
                 * then {@link com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter#notifyParentChanged(int)}
                 * might produce {@link IndexOutOfBoundsException}, so we catch it here and revert
                 * all previously made changes under the list items.
                 */
                Timber.e("Index out of bounds - touching list item while list is changing - revert action");
                Switch switcher = (Switch) buttonView;
                switcher.setOnCheckedChangeListener(null);
                switcher.setChecked(!isChecked);
                switcher.setOnCheckedChangeListener(data.getSwitcherListener());
                childItem.setSelected(!isChecked);
                parentItem.incrementSelectedCount(!isChecked ? 1 : -1);
            }
        };
    }

    // ------------------------------------------
    @Override
    public int getParentViewType(int parentPosition) {
        if (isAddingNewItem && parentPosition == 0) return TYPE_ADD_NEW;
        return super.getParentViewType(parentPosition);
    }

    @Override
    public boolean isParentViewType(int viewType) {
        return super.isParentViewType(viewType) || viewType == TYPE_ADD_NEW;
    }
}
