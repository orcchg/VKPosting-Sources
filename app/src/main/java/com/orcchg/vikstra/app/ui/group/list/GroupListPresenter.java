package com.orcchg.vikstra.app.ui.group.list;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupChildItem;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupParentItem;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.keyword.AddKeywordToBundle;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundleById;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.vkontakte.MakeWallPostToGroups;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.model.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import hugo.weaving.DebugLog;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {

    private final GetPostById getPostByIdUseCase;
    private final GetKeywordBundleById getKeywordBundleByIdUseCase;
    private final AddKeywordToBundle addKeywordToBundleUseCase;
    private final VkontakteEndpoint vkontakteEndpoint;

    List<GroupParentItem> groupParentItems = new ArrayList<>();
    GroupListAdapter listAdapter;

    int totalSelectedGroups, totalGroups;
    KeywordBundle inputKeywordBundle;
    Post currentPost;

    final PostToSingleGridVoMapper postToSingleGridVoMapper;

    @Inject
    GroupListPresenter(GetPostById getPostByIdUseCase, GetKeywordBundleById getKeywordBundleByIdUseCase,
                       AddKeywordToBundle addKeywordToBundleUseCase, VkontakteEndpoint vkontakteEndpoint,
                       PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.listAdapter = createListAdapter(groupParentItems, createGroupClickCallback(), createAllGroupsSelectedCallback());
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.getKeywordBundleByIdUseCase = getKeywordBundleByIdUseCase;
        this.getKeywordBundleByIdUseCase.setPostExecuteCallback(createGetKeywordBundleByIdCallback());
        this.addKeywordToBundleUseCase = addKeywordToBundleUseCase;
        this.addKeywordToBundleUseCase.setPostExecuteCallback(createAddKeywordToBundleCallback());
        this.vkontakteEndpoint = vkontakteEndpoint;
        this.postToSingleGridVoMapper = postToSingleGridVoMapper;
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
            RecyclerView list = getView().getListView(GroupListFragment.RV_TAG);
            if (list.getAdapter() == null) {
                list.setAdapter(listAdapter);
            }
        }
        super.onStart();
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void addKeyword(Keyword keyword) {
        addKeywordToBundleUseCase.setParameters(new AddKeywordToBundle.Parameters(keyword));
        addKeywordToBundleUseCase.execute();
    }

    @Override
    public void postToGroups() {
        Set<Long> selectedGroupIds = new TreeSet<>();
        for (GroupParentItem parentItem : listAdapter.getParentList()) {
            for (GroupChildItem childItem : parentItem.getChildList()) {
                if (childItem.isSelected()) selectedGroupIds.add(childItem.getId());
            }
        }

        MakeWallPostToGroups.Parameters parameters = new MakeWallPostToGroups.Parameters.Builder()
                .setGroupIds(selectedGroupIds)
                .setPost(currentPost)
                .build();
        vkontakteEndpoint.makeWallPosts(parameters, createMakeWallPostCallback());
        // TODO: show progress dialog
    }

    @Override
    public void removeListItem(int position) {
        groupParentItems.remove(position);
        listAdapter.notifyParentRemoved(position);
    }

    @DebugLog @Override
    public void retry() {
        groupParentItems.clear();
        inputKeywordBundle = null;
        currentPost = null;
        totalSelectedGroups = 0;
        totalGroups = 0;
        listAdapter.clear();
        freshStart();
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    protected void freshStart() {
        getPostByIdUseCase.execute();
        getKeywordBundleByIdUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @Override
            public void onFinish(@Nullable Post post) {
                currentPost = post;
                if (isViewAttached()) getView().showPost(postToSingleGridVoMapper.map(post));
            }

            @Override
            public void onError(Throwable e) {
                // TODO: impl
            }
        };
    }

    private UseCase.OnPostExecuteCallback<KeywordBundle> createGetKeywordBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                // TODO: NPE handle - crash when BAD_ID or not found keywords
                inputKeywordBundle = bundle;
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

    private UseCase.OnPostExecuteCallback<Boolean> createAddKeywordToBundleCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @Override
            public void onFinish(@Nullable Boolean result) {
                if (result) retry();  // get newly added keyword from repository at next request
                // TODO: else result
            }

            @Override
            public void onError(Throwable e) {
                // TODO:
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
                    totalGroups += groups.size();
                    if (AppConfig.INSTANCE.isAllGroupsSortedByMembersCount()) Collections.sort(groups);
                    List<GroupChildItem> childItems = new ArrayList<>(groups.size());
                    for (Group group : groups) {
                        GroupChildItem childItem = new GroupChildItem(group);
                        childItems.add(childItem);
                        if (AppConfig.INSTANCE.isAllGroupsSelected()) {
                            childItem.setSelected(true);
                            ++totalSelectedGroups;
                        }
                    }
                    GroupParentItem parentItem = groupParentItems.get(index);
                    if (AppConfig.INSTANCE.isAllGroupsSelected()) parentItem.setSelectedCount(groups.size());
                    parentItem.setChildList(childItems);
                    ++index;
                }

                listAdapter.notifyParentDataSetChanged(false);
                if (isViewAttached()) {
                    getView().updateSelectedGroupsCounter(totalSelectedGroups, totalGroups);
                    getView().showGroups(splitGroups.isEmpty());
                }
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<List<GroupReport>> createMakeWallPostCallback() {
        return new UseCase.OnPostExecuteCallback<List<GroupReport>>() {
            @Override
            public void onFinish(@Nullable List<GroupReport> values) {
                if (isViewAttached()) getView().openReportScreen(getPostByIdUseCase.getPostId());
            }

            @Override
            public void onError(Throwable e) {
                // TODO:
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
            int total = model.getChildCount();
            int selected = model.getSelectedCount();
            int unselected = total - selected;
            model.setSelectedCount(isSelected ? total : 0);
            totalSelectedGroups += isSelected ? unselected : -selected;
            listAdapter.notifyParentChanged(position);
            if (isViewAttached()) getView().updateSelectedGroupsCounter(totalSelectedGroups, totalGroups);
        };
    }

    private GroupListAdapter.OnCheckedChangeListener createExternalChildItemSwitcherCallback() {
        return (data, isChecked) -> {
            totalSelectedGroups += isChecked ? 1 : -1;
            if (isViewAttached()) getView().updateSelectedGroupsCounter(totalSelectedGroups, totalGroups);
        };
    }
}
