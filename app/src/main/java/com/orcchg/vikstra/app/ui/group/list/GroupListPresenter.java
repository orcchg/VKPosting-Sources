package com.orcchg.vikstra.app.ui.group.list;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupChildItem;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupParentItem;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundleById;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {

    List<GroupParentItem> groupParentItems = new ArrayList<>();
//    Set<Long> selectedGroupIds = new TreeSet<>();
    GroupListAdapter listAdapter;

    final GetKeywordBundleById getKeywordBundleByIdUseCase;
    final VkontakteEndpoint vkontakteEndpoint;

//    private int totalSelectedGroups;

    @Inject
    GroupListPresenter(GetKeywordBundleById getKeywordBundleByIdUseCase, VkontakteEndpoint vkontakteEndpoint) {
        this.listAdapter = createListAdapter(groupParentItems, createGroupClickCallback(), createAllGroupsSelectedCallback());
        this.getKeywordBundleByIdUseCase = getKeywordBundleByIdUseCase;
        this.getKeywordBundleByIdUseCase.setPostExecuteCallback(createGetKeywordBundleByIdCallback());
        this.vkontakteEndpoint = vkontakteEndpoint;
    }

    private GroupListAdapter createListAdapter(List<GroupParentItem> items, OnGroupClickListener listener,
                                               OnAllGroupsSelectedListener listener2) {
        GroupListAdapter adapter = new GroupListAdapter(items, listener, listener2);
        adapter.setExternalChildItemSwitcherListener(createExternalChildItemSwitcherCallback());
        return adapter;
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    public void onStart() {
        if (isViewAttached()) {  // TODO: try to use BaseListPresenter
            RecyclerView list = getView().getListView();
            if (list.getAdapter() == null) {
                list.setAdapter(listAdapter);
            }
        }
        super.onStart();
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void postToGroups() {
        // TODO: post selected groups
    }

    @DebugLog @Override
    public void retry() {
        groupParentItems.clear();
        selectedGroupIds.clear();
        totalSelectedGroups = 0;
        listAdapter.clear();
        freshStart();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    protected void freshStart() {
        getKeywordBundleByIdUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<KeywordBundle> createGetKeywordBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                // TODO: NPE handle - crash when BAD_ID or not found keywords
                for (Keyword keyword : bundle) {
                    GroupParentItem item = new GroupParentItem(keyword.keyword());
                    groupParentItems.add(item);
                }
                vkontakteEndpoint.getGroupsByKeywordsSplit(bundle.keywords(), createGetGroupsByKeywordsListCallback());
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<List<List<Group>>> createGetGroupsByKeywordsListCallback() {
        return new UseCase.OnPostExecuteCallback<List<List<Group>>>() {
            @Override
            public void onFinish(@Nullable List<List<Group>> splitGroups) {
                // TODO: NPE
                // TODO: only 20 groups by single keyword by default
                int index = 0;
                for (List<Group> groups : splitGroups) {
                    if (AppConfig.INSTANCE.isAllGroupsSortedByMembersCount()) Collections.sort(groups);
                    List<GroupChildItem> childItems = new ArrayList<>(groups.size());
                    for (Group group : groups) {
                        GroupChildItem childItem = new GroupChildItem(group);
                        childItems.add(childItem);
                        if (AppConfig.INSTANCE.isAllGroupsSelected()) {
                            childItem.setSelected(true);
                            ++totalSelectedGroups;
                            selectedGroupIds.add(group.id());
                        }
                    }
                    GroupParentItem parentItem = groupParentItems.get(index);
                    if (AppConfig.INSTANCE.isAllGroupsSelected()) parentItem.setSelectedCount(groups.size());
                    parentItem.setChildList(childItems);
                    ++index;
                }

                listAdapter.notifyParentDataSetChanged(false);
                if (isViewAttached()) {
                    getView().updateSelectedGroupsCounter(totalSelectedGroups);
                    getView().showGroups(splitGroups.isEmpty());
                }
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }

    // ------------------------------------------
    private OnGroupClickListener createGroupClickCallback() {
        return (groupId) -> {
            if (isViewAttached()) getView().openGroupDetailScreen(groupId);
        };
    }

    private OnAllGroupsSelectedListener createAllGroupsSelectedCallback() {
        return (model, position, isSelected) -> {
//            int total = model.getChildCount();
//            int selected = model.getSelectedCount();
//            int unselected = total - selected;
//            model.setSelectedCount(isSelected ? total : 0);
//            totalSelectedGroups += isSelected ? unselected : -selected;
//            // TODO: add ids to set
//            // TODO: fix counter overflow
//            listAdapter.notifyParentChanged(position);
//            if (isViewAttached()) getView().updateSelectedGroupsCounter(totalSelectedGroups);
        };
    }

    private GroupListAdapter.OnCheckedChangeListener createExternalChildItemSwitcherCallback() {
        return (data, isChecked) -> {
            totalSelectedGroups += isChecked ? 1 : -1;
            if (isChecked) {
                selectedGroupIds.add(data.getId());
            } else {
                selectedGroupIds.remove(data.getId());
            }
            if (isViewAttached()) getView().updateSelectedGroupsCounter(totalSelectedGroups);
        };
    }
}
