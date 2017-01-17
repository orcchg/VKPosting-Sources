package com.orcchg.vikstra.app.ui.group.list.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.orcchg.vikstra.data.source.memory.ContentUtility;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.group.GetGroupBundleById;
import com.orcchg.vikstra.domain.interactor.group.PostGroupBundle;
import com.orcchg.vikstra.domain.interactor.group.PutGroupBundle;
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
import com.orcchg.vikstra.domain.util.ValueUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {

    private final AddKeywordToBundle addKeywordToBundleUseCase;
    private final GetGroupBundleById getGroupBundleByIdUseCase;
    private final GetKeywordBundleById getKeywordBundleByIdUseCase;
    private final GetPostById getPostByIdUseCase;
    private final PostKeywordBundle postKeywordBundleUseCase;
    private final PostGroupBundle postGroupBundleUseCase;
    private final PutGroupBundle putGroupBundleUseCase;
    private final PutGroupReportBundle putGroupReportBundle;
    private final VkontakteEndpoint vkontakteEndpoint;

    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    private List<GroupParentItem> groupParentItems = new ArrayList<>();
    private GroupListAdapter listAdapter;

    int totalSelectedGroups, totalGroups;
    private boolean isKeywordBundleChanged, isGroupBundleChanged;
    private Keyword newlyAddedKeyword;
    private @NonNull GroupBundle inputGroupBundle;
    private @NonNull KeywordBundle inputKeywordBundle;
    private @Nullable Post currentPost;

    private GroupListMediatorComponent mediatorComponent;

    @Inject
    GroupListPresenter(AddKeywordToBundle addKeywordToBundleUseCase, GetGroupBundleById getGroupBundleByIdUseCase,
                       GetKeywordBundleById getKeywordBundleByIdUseCase, GetPostById getPostByIdUseCase,
                       PostKeywordBundle postKeywordBundleUseCase, PostGroupBundle postGroupBundleUseCase,
                       PutGroupBundle putGroupBundleUseCase, PutGroupReportBundle putGroupReportBundle,
                       VkontakteEndpoint vkontakteEndpoint, PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.listAdapter = createListAdapter(groupParentItems, createGroupClickCallback(), createAllGroupsSelectedCallback());
        this.addKeywordToBundleUseCase = addKeywordToBundleUseCase;
        this.addKeywordToBundleUseCase.setPostExecuteCallback(createAddKeywordToBundleCallback());
        this.getGroupBundleByIdUseCase = getGroupBundleByIdUseCase;
        this.getGroupBundleByIdUseCase.setPostExecuteCallback(createGetGroupBundleByIdCallback());
        this.getKeywordBundleByIdUseCase = getKeywordBundleByIdUseCase;
        this.getKeywordBundleByIdUseCase.setPostExecuteCallback(createGetKeywordBundleByIdCallback());
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.postKeywordBundleUseCase = postKeywordBundleUseCase;  // no callback - background task
        this.postGroupBundleUseCase = postGroupBundleUseCase;  // no callback - background task
        this.putGroupBundleUseCase = putGroupBundleUseCase;
        this.putGroupBundleUseCase.setPostExecuteCallback(createPutGroupBundleCallback());
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
    @DebugLog @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediatorComponent = DaggerGroupListMediatorComponent.builder()
                .groupListMediatorModule(new GroupListMediatorModule())
                .build();
        mediatorComponent.inject(this);
        mediatorComponent.mediator().attachSecond(this);
    }

    @Override
    public void onStart() {
        if (isViewAttached()) {  // TODO: try to use BaseListPresenter
            RecyclerView list = getView().getListView(GroupListFragment.RV_TAG);
            if (list.getAdapter() == null) {
                list.setAdapter(listAdapter);
            }
        } else {
            Timber.w("No View is attached");
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
        Timber.i("removeListItem: %s", position);
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

    @Override
    public void retry() {
        Timber.i("retry");
        groupParentItems.clear();
        inputGroupBundle = null;
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
    public void receivePostHasChangedRequest() {
        refreshPost();
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
    public void sendGroupsNotSelected() {
        mediatorComponent.mediator().sendGroupsNotSelected();
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
    @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading(GroupListFragment.RV_TAG);
        getKeywordBundleByIdUseCase.execute();
        getPostByIdUseCase.execute();
    }

    @DebugLog
    private void addGroupsToList(List<Group> groups, int index) {
        Timber.i("addGroupsToList");
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
        Timber.i("addKeyword: %s", keyword.toString());
        if (inputKeywordBundle.keywords().size() < Constant.KEYWORDS_LIMIT) {
            newlyAddedKeyword = keyword;
            addKeywordToBundleUseCase.setParameters(new AddKeywordToBundle.Parameters(keyword));
            addKeywordToBundleUseCase.execute();
        } else {
            sendKeywordsLimitReached(Constant.KEYWORDS_LIMIT);
        }
    }

    @DebugLog
    private void fillGroupsList(List<List<Group>> splitGroups) {
        Timber.i("fillGroupsList");
        // TODO: batch by 20 groups and load-more
        for (int i = 0; i < splitGroups.size(); ++i) {
            addGroupsToList(splitGroups.get(i), i);
        }

        listAdapter.notifyParentDataSetChanged(false);
        sendUpdatedSelectedGroupsCounter(totalSelectedGroups, totalGroups);
        if (isViewAttached()) getView().showGroups(splitGroups.isEmpty());
    }

    private void postToGroups() {
        Timber.i("postToGroups");
        if (currentPost != null) {
            Timber.d("Selected single Post, start wall posting...");
            // exclude ids duplication
            Set<Group> selectedGroups = new TreeSet<>((lhs, rhs) -> (int) (lhs.id() - rhs.id()));
            for (GroupParentItem parentItem : listAdapter.getParentList()) {
                for (GroupChildItem childItem : parentItem.getChildList()) {
                    if (childItem.isSelected()) selectedGroups.add(childItem.getGroup());
                }
            }
            if (selectedGroups.isEmpty()) {
                Timber.d("No Group-s selected, send warning");
                sendGroupsNotSelected();
                return;
            }
            sendPostingStartedMessage(true);
            vkontakteEndpoint.makeWallPostsWithDelegate(selectedGroups, currentPost,
                    createMakeWallPostCallback(), getView(), getView());
            if (isViewAttached()) {
                if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
                    Timber.d("Open ReportScreen in interactive mode showing posting progress");
                    ContentUtility.InMemoryStorage.setSelectedGroupsForPosting(new ArrayList<>(selectedGroups));  // preserve ordering
                    getView().openInteractiveReportScreen(currentPost.id());
                    // TODO: report screen could subscribe for posting-progress callback
                    // TODO:        too late, missing some early reports
                } else {
                    Timber.d("Show popup with wall posting progress");
                    getView().openStatusScreen();
                }
            } else {
                Timber.w("No View is attached");
            }
        } else {
            Timber.d("No Post was selected, send warning");
            sendPostNotSelected();
        }
    }

    private void postKeywordBundleUpdate() {
        Timber.i("postKeywordBundleUpdate");
        if (isKeywordBundleChanged) {
            Timber.d("Input KeywordBundle has been changed, it will be updated in repository");
            isKeywordBundleChanged = false;
            postKeywordBundleUseCase.setParameters(new PostKeywordBundle.Parameters(inputKeywordBundle));
            postKeywordBundleUseCase.execute();  // silent update without callback
            sendKeywordBundleChanged();
        } else {
            Timber.d("Input KeywordBundle wasn't changed");
        }
    }

    private void refreshPost() {
        Timber.i("refreshPost");
        getPostByIdUseCase.execute();
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Boolean> createAddKeywordToBundleCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @DebugLog @Override
            public void onFinish(@Nullable Boolean result) {
                Timber.i("Use-Case: succeeded to add Keyword to KeywordBundle");
                if (result != null && result) {
                    Timber.d("Adding Keyword and requesting more Group-s from network");
                    inputKeywordBundle.keywords().add(newlyAddedKeyword);
                    isKeywordBundleChanged = true;
                    GroupParentItem item = new GroupParentItem(newlyAddedKeyword);
                    groupParentItems.add(0, item);  // add new item on top of the list

                    List<Keyword> keywords = new ArrayList<>();
                    keywords.add(newlyAddedKeyword);
                    newlyAddedKeyword = null;  // drop temporary keyword
                    vkontakteEndpoint.getGroupsByKeywordsSplit(keywords, createGetGroupsByKeywordsListCallback());
                } else {
                    Timber.d("Failed to add Keyword, but just warn user via popup");
                    sendAddKeywordError();
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to add Keyword to KeywordBundle");
                sendAddKeywordError();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<GroupBundle> createGetGroupBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<GroupBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable GroupBundle bundle) {
                if (bundle == null) {
                    Timber.wtf("No GroupBundle found by id associated with input KeywordBundle, %s",
                            "such id has improper value due to wrong association between instances at creation");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get GroupBundle by id");
                inputGroupBundle = bundle;
                fillGroupsList(bundle.splitGroupsByKeywords());
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get GroupBundle by id");
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<KeywordBundle> createGetKeywordBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                if (bundle == null) {
                    Timber.wtf("KeywordBundle wasn't found by id: %s", getKeywordBundleByIdUseCase.getKeywordBundleId());
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get KeywordBundle by id");
                inputKeywordBundle = bundle;
                for (Keyword keyword : bundle) {
                    GroupParentItem item = new GroupParentItem(keyword);
                    groupParentItems.add(item);
                }
                long groupBundleId = inputKeywordBundle.getGroupBundleId();
                if (groupBundleId == Constant.BAD_ID) {
                    Timber.d("There is no GroupBundle associated with input KeywordBundle, perform network request");
                    vkontakteEndpoint.getGroupsByKeywordsSplit(bundle.keywords(), createGetGroupsByKeywordsListCallback());
                } else {
                    Timber.d("Loading GroupBundle associated with input KeywordBundle from repository");
                    getGroupBundleByIdUseCase.setGroupBundleId(groupBundleId);  // set proper id
                    getGroupBundleByIdUseCase.execute();
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get KeywordBundle by id");
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @DebugLog @Override
            public void onFinish(@Nullable Post post) {
                Timber.i("Use-Case: succeeded to get Post by id");
                currentPost = post;
                if (post != null) {
                    sendPost(postToSingleGridVoMapper.map(post));
                } else {
                    sendEmptyPost();
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get Post by id");
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<GroupBundle> createPutGroupBundleCallback() {
        return new UseCase.OnPostExecuteCallback<GroupBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable GroupBundle bundle) {
                if (bundle == null) {
                    Timber.wtf("Failed to put new GroupBundle to repository - item not created, as expected");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to put GroupBundle");
                Timber.d("Update input KeywordBundle and associate it with newly created GroupBundle (by setting id)");
                inputKeywordBundle.setGroupBundleId(bundle.id());
                getGroupBundleByIdUseCase.setGroupBundleId(bundle.id());  // set proper id
                postKeywordBundleUpdate();
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to put GroupBundle");
                // TODO: failed to create GroupBundle in repo
            }
        };
    }

    private UseCase.OnPostExecuteCallback<GroupReportBundle> createPutGroupReportBundleCallback() {
        return new UseCase.OnPostExecuteCallback<GroupReportBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable GroupReportBundle bundle) {
                if (bundle == null) {
                    Timber.wtf("Failed to put new GroupReportBundle to repository - item not created, as expected");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to put GroupReportBundle");
                sendPostingStartedMessage(false);
                if (isViewAttached()) {
                    getView().updateGroupReportBundleId(bundle.id());
                    getView().onReportReady(bundle.id(), getPostByIdUseCase.getPostId());
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to put GroupReportBundle");
                // TODO: failed to put reports - retry posting?
                sendPostingStartedMessage(false);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<List<List<Group>>> createGetGroupsByKeywordsListCallback() {
        return new UseCase.OnPostExecuteCallback<List<List<Group>>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<List<Group>> splitGroups) {
                if (splitGroups == null) {
                    Timber.wtf("Split list of Group-s must not be null, it could be empty at least");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get list of Group-s by list of Keyword-s");
                fillGroupsList(splitGroups);

                List<Group> groups = ValueUtility.merge(splitGroups);
                long groupBundleId = getGroupBundleByIdUseCase.getGroupBundleId();
                if (groupBundleId == Constant.BAD_ID) {
                    Timber.d("create new groups bundle and store it in repository, update id in associated keywords bundle");
                    PutGroupBundle.Parameters parameters = new PutGroupBundle.Parameters.Builder()
                            .setGroups(groups)
                            .setKeywordBundleId(inputKeywordBundle.id())
                            .setTitle("title")  // TODO: set group-bundle title from Toolbar
                            .build();
                    putGroupBundleUseCase.setParameters(parameters);
                    putGroupBundleUseCase.execute();
                } else {
                    Timber.d("refresh already existing groups bundle in repository");
                    GroupBundle groupBundle = GroupBundle.builder()
                            .setId(groupBundleId)
                            .setGroups(groups)
                            .setKeywordBundleId(inputKeywordBundle.id())
                            .setTimestamp(inputGroupBundle.timestamp())
                            .setTitle(inputGroupBundle.title())  // TODO: set group-bundle title from Toolbar
                            .build();
                    PostGroupBundle.Parameters parameters = new PostGroupBundle.Parameters(groupBundle);
                    postGroupBundleUseCase.setParameters(parameters);
                    postGroupBundleUseCase.execute();  // silent update without callback
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get list of Group-s by list of Keyword-s");
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<List<GroupReportEssence>> createMakeWallPostCallback() {
        return new UseCase.OnPostExecuteCallback<List<GroupReportEssence>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<GroupReportEssence> reports) {
                Timber.i("Use-Case: succeeded to make wall posting");
                PutGroupReportBundle.Parameters parameters = new PutGroupReportBundle.Parameters(reports);
                putGroupReportBundle.setParameters(parameters);
                putGroupReportBundle.execute();
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to make wall posting");
                sendPostingStartedMessage(false);
                if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
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
