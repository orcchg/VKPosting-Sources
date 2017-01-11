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
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.group.GetGroupBundleById;
import com.orcchg.vikstra.domain.interactor.keyword.AddKeywordToBundle;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundleById;
import com.orcchg.vikstra.domain.interactor.keyword.PostKeywordBundle;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.report.PutGroupReportBundle;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {

    private final GetGroupBundleById getGroupBundleByIdUseCase;
    private final GetPostById getPostByIdUseCase;
    private final GetKeywordBundleById getKeywordBundleByIdUseCase;
    private final AddKeywordToBundle addKeywordToBundleUseCase;
    private final PostKeywordBundle postKeywordBundleUseCase;
    private final PutGroupReportBundle putGroupReportBundle;
    private final VkontakteEndpoint vkontakteEndpoint;

    private List<GroupParentItem> groupParentItems = new ArrayList<>();
    private GroupListAdapter listAdapter;

    int totalSelectedGroups, totalGroups;
    private boolean isKeywordBundleChanged;
    private Keyword newlyAddedKeyword;
    private KeywordBundle inputKeywordBundle;
    private Post currentPost;

    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    private GroupListMediatorComponent mediatorComponent;

    @Inject
    GroupListPresenter(GetGroupBundleById getGroupBundleByIdUseCase,
                       GetPostById getPostByIdUseCase, GetKeywordBundleById getKeywordBundleByIdUseCase,
                       AddKeywordToBundle addKeywordToBundleUseCase, PostKeywordBundle postKeywordBundleUseCase,
                       PutGroupReportBundle putGroupReportBundle, VkontakteEndpoint vkontakteEndpoint,
                       PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.listAdapter = createListAdapter(groupParentItems, createGroupClickCallback(), createAllGroupsSelectedCallback());
        this.getGroupBundleByIdUseCase = getGroupBundleByIdUseCase;
        this.getGroupBundleByIdUseCase.setPostExecuteCallback(createGetGroupBundleByIdCallback());
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.getKeywordBundleByIdUseCase = getKeywordBundleByIdUseCase;
        this.getKeywordBundleByIdUseCase.setPostExecuteCallback(createGetKeywordBundleByIdCallback());
        this.addKeywordToBundleUseCase = addKeywordToBundleUseCase;
        this.addKeywordToBundleUseCase.setPostExecuteCallback(createAddKeywordToBundleCallback());
        this.postKeywordBundleUseCase = postKeywordBundleUseCase;  // no callback - background task
        this.putGroupReportBundle = putGroupReportBundle;
        this.putGroupReportBundle.setPostExecuteCallback(createPutGroupReportBundleCallback());
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
    public void onStop() {
        super.onStop();
        postKeywordBundleUpdate();  // TODO: not sync with onActivityResult()
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediatorComponent.mediator().detachSecond();
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void removeListItem(int position) {
        GroupParentItem item = groupParentItems.get(position);
        totalSelectedGroups -= item.getSelectedCount();
        totalGroups -= item.getChildCount();
        sendUpdatedSelectedGroupsCounter(totalSelectedGroups, totalGroups);
        postKeywordBundleUpdate();  // refresh now, don't wait till onDestroy()

        Keyword keyword = groupParentItems.get(position).getKeyword();
        inputKeywordBundle.keywords().remove(keyword);
        groupParentItems.remove(position);
        listAdapter.notifyParentRemoved(position);
    }

    @DebugLog @Override
    public void retry() {
        groupParentItems.clear();
        inputKeywordBundle = null;
        isKeywordBundleChanged = false;
        currentPost = null;
        totalSelectedGroups = 0;
        totalGroups = 0;
        listAdapter.clear();
        freshStart();
    }

    /* Mediator */
    // ------------------------------------------
    @Override
    public void receiveAddKeywordRequest(Keyword keyword) {
        addKeyword(keyword);
    }

    @Override
    public void receivePostToGroupsRequest() {
        postToGroups();
    }

    @Override
    public void sendAddKeywordError() {
        mediatorComponent.mediator().sendAddKeywordError();
    }

    @Override
    public void sendEmptyPost() {
        mediatorComponent.mediator().sendEmptyPost();
    }

    @Override
    public void sendKeywordBundleChanged() {
        mediatorComponent.mediator().sendKeywordBundleChanged();
    }

    @Override
    public void sendKeywordsLimitReached(int limit) {
        mediatorComponent.mediator().sendKeywordsLimitReached(limit);
    }

    @Override
    public void sendPost(@Nullable PostSingleGridItemVO viewObject) {
        mediatorComponent.mediator().sendPost(viewObject);
    }

    @Override
    public void sendPostNotSelected() {
        mediatorComponent.mediator().sendPostNotSelected();
    }

    @Override
    public void sendPostingStartedMessage(boolean isStarted) {
        mediatorComponent.mediator().sendPostingStartedMessage(isStarted);
    }

    @Override
    public void sendUpdatedSelectedGroupsCounter(int newCount, int total) {
        inputKeywordBundle.setSelectedGroupsCount(newCount);
        inputKeywordBundle.setTotalGroupsCount(total);
        isKeywordBundleChanged = true;
        mediatorComponent.mediator().sendUpdatedSelectedGroupsCounter(newCount, total);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @DebugLog @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading(GroupListFragment.RV_TAG);
        getKeywordBundleByIdUseCase.execute();
        getPostByIdUseCase.execute();
    }

    void addGroupsToList(List<Group> groups, int index) {
        if (AppConfig.INSTANCE.isAllGroupsSortedByMembersCount()) Collections.sort(groups);
        int xTotalGroups = 0;
        List<GroupChildItem> childItems = new ArrayList<>(groups.size());
        for (Group group : groups) {
            if (AppConfig.INSTANCE.useOnlyGroupsWhereCanPostFreely() && !group.canPost()) {
                continue;  // skip groups where is no access for current user to make wall post
            }
            ++xTotalGroups;
            GroupChildItem childItem = new GroupChildItem(group);
            childItems.add(childItem);
            if (AppConfig.INSTANCE.isAllGroupsSelected()) {
                childItem.setSelected(true);
                ++totalSelectedGroups;
            }
        }
        totalGroups += xTotalGroups;
        GroupParentItem parentItem = groupParentItems.get(index);
        if (AppConfig.INSTANCE.isAllGroupsSelected()) parentItem.setSelectedCount(xTotalGroups);
        parentItem.setChildList(childItems);
    }

    private void addKeyword(Keyword keyword) {
        if (inputKeywordBundle.keywords().size() < Constant.KEYWORDS_LIMIT) {
            newlyAddedKeyword = keyword;
            addKeywordToBundleUseCase.setParameters(new AddKeywordToBundle.Parameters(keyword));
            addKeywordToBundleUseCase.execute();
        } else {
            sendKeywordsLimitReached(Constant.KEYWORDS_LIMIT);
        }
    }

    private void postToGroups() {
        if (currentPost != null) {
            // exclude ids duplication
            Set<Group> selectedGroups = new TreeSet<>((lhs, rhs) -> (int) (lhs.id() - rhs.id()));
            for (GroupParentItem parentItem : listAdapter.getParentList()) {
                for (GroupChildItem childItem : parentItem.getChildList()) {
                    if (childItem.isSelected()) selectedGroups.add(childItem.getGroup());
                }
            }
            sendPostingStartedMessage(true);
            vkontakteEndpoint.makeWallPostsWithDelegate(selectedGroups, currentPost,
                    createMakeWallPostCallback(), getView(), getView());
            if (isViewAttached()) getView().openStatusScreen();
        } else {
            Timber.d("No post selected, nothing to be done");
            sendPostNotSelected();
        }
    }

    private void postKeywordBundleUpdate() {
        if (isKeywordBundleChanged) {
            isKeywordBundleChanged = false;
            postKeywordBundleUseCase.setParameters(new PostKeywordBundle.Parameters(inputKeywordBundle));
            postKeywordBundleUseCase.execute();  // silent update without callback
            sendKeywordBundleChanged();
        }
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<GroupBundle> createGetGroupBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<GroupBundle>() {
            @Override
            public void onFinish(@Nullable GroupBundle bundle) {
                // TODO: impl
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
            }
        };
    }

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
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<KeywordBundle> createGetKeywordBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                if (bundle == null) {
                    Timber.e("KeywordBundle wasn't found by id: %s", getKeywordBundleByIdUseCase.getKeywordBundleId());
                    throw new ProgramException();
                }
                inputKeywordBundle = bundle;
                for (Keyword keyword : bundle) {
                    GroupParentItem item = new GroupParentItem(keyword);
                    groupParentItems.add(item);
                }
                long groupBundleId = inputKeywordBundle.getGroupBundleId();
                if (groupBundleId == Constant.BAD_ID) {
                    // there is not GroupBundle associated with input KeywordBundle, perform network request
                    vkontakteEndpoint.getGroupsByKeywordsSplit(bundle.keywords(), createGetGroupsByKeywordsListCallback());
                } else {
                    // loading GroupBundle associated with input KeywordBundle from repository
                    getGroupBundleByIdUseCase.setGroupBundleId(groupBundleId);  // set proper id
                    getGroupBundleByIdUseCase.execute();
                }
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Boolean> createAddKeywordToBundleCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @Override
            public void onFinish(@Nullable Boolean result) {
                if (result != null && result) {
                    inputKeywordBundle.keywords().add(newlyAddedKeyword);
                    isKeywordBundleChanged = true;
                    GroupParentItem item = new GroupParentItem(newlyAddedKeyword);
                    groupParentItems.add(0, item);  // add new item on top of the list

                    Collection<Keyword> keywords = new ArrayList<>();
                    keywords.add(newlyAddedKeyword);
                    newlyAddedKeyword = null;  // drop temporary keyword
                    vkontakteEndpoint.getGroupsByKeywordsSplit(keywords, createGetGroupsByKeywordsListCallback());
                } else {
                    sendAddKeywordError();
                }
            }

            @Override
            public void onError(Throwable e) {
                sendAddKeywordError();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<GroupReportBundle> createPutGroupReportBundleCallback() {
        return new UseCase.OnPostExecuteCallback<GroupReportBundle>() {
            @Override
            public void onFinish(@Nullable GroupReportBundle bundle) {
                if (bundle == null) {
                    Timber.e("Failed to create new GroupReportBundle and put it to Repository");
                    throw new ProgramException();
                }
                sendPostingStartedMessage(false);
                if (isViewAttached()) {
                    getView().updateGroupReportBundleId(bundle.id());
                    getView().onReportReady(bundle.id(), getPostByIdUseCase.getPostId());
                }
            }

            @Override
            public void onError(Throwable e) {
                // TODO: failed to put reports - retry posting?
                sendPostingStartedMessage(false);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<List<List<Group>>> createGetGroupsByKeywordsListCallback() {
        return new UseCase.OnPostExecuteCallback<List<List<Group>>>() {
            @Override
            public void onFinish(@Nullable List<List<Group>> splitGroups) {
                if (splitGroups == null) {
                    Timber.e("Split groups list cannot be null, but could be empty instead");
                    throw new ProgramException();
                }
                // TODO: batch by 20 groups and load-more
                for (int i = 0; i < splitGroups.size(); ++i) {
                    addGroupsToList(splitGroups.get(i), i);
                }

                listAdapter.notifyParentDataSetChanged(false);
                sendUpdatedSelectedGroupsCounter(totalSelectedGroups, totalGroups);
                if (isViewAttached()) {
                    getView().showGroups(splitGroups.isEmpty());
                }

                // TODO: put new 'groups-bundle' if no one associated with input 'keywords-bundle'
                // TODO:                         or update existing one
                // TODO: update input 'keywords-bundle' with newly create 'groups-bundle' id
            }

            @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<List<GroupReportEssence>> createMakeWallPostCallback() {
        return new UseCase.OnPostExecuteCallback<List<GroupReportEssence>>() {
            @Override
            public void onFinish(@Nullable List<GroupReportEssence> reports) {
                PutGroupReportBundle.Parameters parameters = new PutGroupReportBundle.Parameters(reports);
                putGroupReportBundle.setParameters(parameters);
                putGroupReportBundle.execute();
            }

            @Override
            public void onError(Throwable e) {
                sendPostingStartedMessage(false);
                if (isViewAttached()) {
                    getView().showError(GroupListFragment.RV_TAG);
                }
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
