package com.orcchg.vikstra.app.ui.group.list.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.group.list.OnAllGroupsSelectedListener;
import com.orcchg.vikstra.app.ui.group.list.OnGroupClickListener;
import com.orcchg.vikstra.app.ui.group.list.injection.DaggerGroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorModule;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupChildItem;
import com.orcchg.vikstra.app.ui.group.list.listview.GroupParentItem;
import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.keyword.AddKeywordToBundle;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundleById;
import com.orcchg.vikstra.domain.interactor.keyword.PostKeywordBundle;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
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
import timber.log.Timber;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {

    private final GetPostById getPostByIdUseCase;
    private final GetKeywordBundleById getKeywordBundleByIdUseCase;
    private final AddKeywordToBundle addKeywordToBundleUseCase;
    private final PostKeywordBundle postKeywordBundleUseCase;
    private final VkontakteEndpoint vkontakteEndpoint;

    List<GroupParentItem> groupParentItems = new ArrayList<>();
    GroupListAdapter listAdapter;

    int totalSelectedGroups, totalGroups;
    KeywordBundle inputKeywordBundle;
    Post currentPost;

    final PostToSingleGridVoMapper postToSingleGridVoMapper;

    GroupListMediatorComponent mediatorComponent;

    @Inject
    GroupListPresenter(GetPostById getPostByIdUseCase, GetKeywordBundleById getKeywordBundleByIdUseCase,
                       AddKeywordToBundle addKeywordToBundleUseCase, PostKeywordBundle postKeywordBundleUseCase,
                       VkontakteEndpoint vkontakteEndpoint, PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.listAdapter = createListAdapter(groupParentItems, createGroupClickCallback(), createAllGroupsSelectedCallback());
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.getKeywordBundleByIdUseCase = getKeywordBundleByIdUseCase;
        this.getKeywordBundleByIdUseCase.setPostExecuteCallback(createGetKeywordBundleByIdCallback());
        this.addKeywordToBundleUseCase = addKeywordToBundleUseCase;
        this.addKeywordToBundleUseCase.setPostExecuteCallback(createAddKeywordToBundleCallback());
        this.postKeywordBundleUseCase = postKeywordBundleUseCase;
        this.postKeywordBundleUseCase.setPostExecuteCallback(createPostKeywordBundleCallback());
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
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediatorComponent = DaggerGroupListMediatorComponent.builder()
                .groupListMediatorModule(new GroupListMediatorModule())
                .build();
        mediatorComponent.inject(this);
        mediatorComponent.mediator().attachSecond(this);
    }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediatorComponent.mediator().detachSecond();
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void addKeyword(Keyword keyword) {
        addKeywordToBundleUseCase.setParameters(new AddKeywordToBundle.Parameters(keyword));
        addKeywordToBundleUseCase.execute();
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

    /* Mediator */
    // ------------------------------------------
    @Override
    public void receiveAddKeywordRequest() {
        if (isViewAttached()) getView().openAddKeywordDialog();
    }

    @Override
    public void receivePostToGroupsRequest() {
        postToGroups();
    }

    @Override
    public void sendEmptyPost() {
        mediatorComponent.mediator().sendEmptyPost();
    }

    @Override
    public void sendPost(@Nullable PostSingleGridItemVO viewObject) {
        mediatorComponent.mediator().sendPost(viewObject);
    }

    @Override
    public void sendUpdatedSelectedGroupsCounter(int newCount, int total) {
        // TODO: update keywords bundle with counters in Repo
        inputKeywordBundle.setSelectedGroupsCount(newCount);
        inputKeywordBundle.setTotalGroupsCount(total);
        mediatorComponent.mediator().sendUpdatedSelectedGroupsCounter(newCount, total);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    protected void freshStart() {
        getPostByIdUseCase.execute();
        getKeywordBundleByIdUseCase.execute();
    }

    private void postToGroups() {
        Set<Long> selectedGroupIds = new TreeSet<>();
        for (GroupParentItem parentItem : listAdapter.getParentList()) {
            for (GroupChildItem childItem : parentItem.getChildList()) {
                if (childItem.isSelected()) selectedGroupIds.add(childItem.getId());
            }
        }
        vkontakteEndpoint.makeWallPosts(selectedGroupIds, currentPost,
                createMakeWallPostCallback(), createMakeWallPostsProgressCallback(),
                createPhotoUploadProgressCallback(), createPhotoPrepareProgressCallback());
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @Override
            public void onFinish(@Nullable Post post) {
                currentPost = post;
                if (post != null) {
                    sendPost(postToSingleGridVoMapper.map(post));
                } else {
                    sendEmptyPost();
                }
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
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
                if (isViewAttached()) getView().showError();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Boolean> createPostKeywordBundleCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @Override
            public void onFinish(@Nullable Boolean result) {
                // TODO: input keywords updated with group id in repository
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError();
            }
        };
    }

//    private UseCase.OnPostExecuteCallback<GroupBundle> createPutGroupBundleCallback() {
//        return new UseCase.OnPostExecuteCallback<GroupBundle>() {
//            @Override
//            public void onFinish(@Nullable GroupBundle bundle) {
//                // TODO: check for bad id ???
//                currentGroupBundle = bundle;
//                inputKeywordBundle.setGroupBundleId(bundle.id());
//                PostKeywordBundle.Parameters parameters = new PostKeywordBundle.Parameters(inputKeywordBundle);
//                postKeywordBundleUseCase.setParameters(parameters);
//                postKeywordBundleUseCase.execute();
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                if (isViewAttached()) getView().showError();
//            }
//        };
//    }

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
                sendUpdatedSelectedGroupsCounter(totalSelectedGroups, totalGroups);
                if (isViewAttached()) {
                    getView().showGroups(splitGroups.isEmpty());
                }

                // ------------------------------------------
//                long groupBundleId = inputKeywordBundle.getGroupBundleId();
//                Collection<Group> groups = ValueUtility.merge(splitGroups);
//                if (groupBundleId == Constant.BAD_ID) {
//                    Timber.v("add new groups bundle to repository");
//                    PutGroupBundle.Parameters parameters = new PutGroupBundle.Parameters.Builder()
//                            .setGroups(groups)
//                            .setKeywordBundleId(inputKeywordBundle.id())
//                            .setTitle("title default")  // TODO: set title for groups-bundle
//                            .build();
//                    putGroupBundleUseCase.setParameters(parameters);
//                    putGroupBundleUseCase.execute();
//                } else {
//                    Timber.v("update existing groups bundle in repository");
//                    GroupBundle groupsBundle = GroupBundle.builder()
//                            .setId(currentGroupBundle.id())
//                            .setGroups(groups)  // new collection of groups
//                            .setKeywordBundleId(currentGroupBundle.keywordBundleId())
//                            .setTimestamp(currentGroupBundle.timestamp())
//                            .setTitle(currentGroupBundle.title())
//                            .build();
//                    PostGroupBundle.Parameters parameters = new PostGroupBundle.Parameters(groupsBundle);
//                    postGroupBundleUseCase.setParameters(parameters);
//                    postGroupBundleUseCase.execute();
//                }
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
                if (isViewAttached()) getView().showError();
            }
        };
    }

    // ------------------------------------------
    private MultiUseCase.ProgressCallback createMakeWallPostsProgressCallback() {
        return (index, total) -> {
            Timber.v("Make wall posts progress: %s / %s", index, total);
            if (isViewAttached()) {
                if (index < total) {
                    getView().onPostingProgress(index, total);
                } else {
                    getView().onPostingComplete();
                }
            }
        };
    }

    private MultiUseCase.ProgressCallback createPhotoUploadProgressCallback() {
        return (index, total) -> {
            Timber.v("Photo uploading progress: %s / %s", index, total);
            if (isViewAttached()) {
                if (index < total) {
                    getView().onPhotoUploadProgress(index, total);
                } else {
                    getView().onPhotoUploadComplete();
                }
            }
        };
    }

    private MultiUseCase.ProgressCallback createPhotoPrepareProgressCallback() {
        return (index, total) -> {
            Timber.v("Photo preparing progress: %s / %s", index, total);
            if (isViewAttached()) getView().onPhotoUploadProgressInfinite();
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
            sendUpdatedSelectedGroupsCounter(totalSelectedGroups, totalGroups);
        };
    }

    private GroupListAdapter.OnCheckedChangeListener createExternalChildItemSwitcherCallback() {
        return (data, isChecked) -> {
            totalSelectedGroups += isChecked ? 1 : -1;
            sendUpdatedSelectedGroupsCounter(totalSelectedGroups, totalGroups);
        };
    }
}
