package com.orcchg.vikstra.app.ui.group.list.fragment;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.orcchg.vikstra.BuildConfig;
import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.group.list.OnAllGroupsSelectedListener;
import com.orcchg.vikstra.app.ui.group.list.OnGroupClickListener;
import com.orcchg.vikstra.app.ui.group.list.injection.DaggerGroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorComponent;
import com.orcchg.vikstra.app.ui.group.list.injection.GroupListMediatorModule;
import com.orcchg.vikstra.app.ui.group.list.listview.child.GroupChildItem;
import com.orcchg.vikstra.app.ui.group.list.listview.parent.GroupParentItem;
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
import com.orcchg.vikstra.domain.util.DebugSake;
import com.orcchg.vikstra.domain.util.ValueUtility;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
    private boolean fetchedInputGroupBundleFromRepo, isAddingNewKeyword, isRefreshing;  // state control flags
    private boolean isKeywordBundleChanged, isGroupBundleChanged;
    private Keyword newlyAddedKeyword;
    private GroupBundle inputGroupBundle;
    private KeywordBundle inputKeywordBundle;
    private Post currentPost;

    private GroupListMediatorComponent mediatorComponent;

    private static final class StateContainer {
        private static final int ERROR_LOAD = -1;
        private static final int START = 0;
        private static final int KEYWORDS_LOADED = 1;
        private static final int GROUPS_LOADED = 2;
        private static final int REFRESHING = 3;
        private static final int ADD_KEYWORD_START = 4;
        private static final int ADD_KEYWORD_FINISH = 5;

        @IntDef({
            ERROR_LOAD,
            START,
            KEYWORDS_LOADED,
            GROUPS_LOADED,
            REFRESHING,
            ADD_KEYWORD_START,
            ADD_KEYWORD_FINISH
        })
        @Retention(RetentionPolicy.SOURCE)
        private @interface State {}
    }

    private @StateContainer.State int state;

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

    /* State */
    // --------------------------------------------------------------------------------------------
    /**
     * State machine:
     *
     *                           START ----- < ------ ERROR_LOAD  { user retry }
     *                             |                 |
     *                             |                 |
     *                        KEYWORDS_LOADED  or    #
     *                             |                 |
     *                             |                 |
     *                             |                 |
     *                        GROUPS_LOADED    or    #
     *                          |       |
     *                          |       |
     *                          |       |
     * { user refresh }  REFRESHING ->--|----------- < ------- < ---------|
     *                                                                    |
     *                                                                    |
     * { user add keyword }  ADD_KEYWORD_START -->-- ADD_KEYWORD_FINISH --|
     *
     *
     * { user remove keyword }  background execution
     */

    @DebugLog
    private void setState(@StateContainer.State int newState) {
        @StateContainer.State int previousState = state;
        Timber.i("Previous state [%s], New state: %s", previousState, newState);

        // check consistency between state transitions
        if (previousState == StateContainer.ERROR_LOAD && newState != StateContainer.START ||
            // forbid transition from any kind of loading to refreshing
            previousState != StateContainer.GROUPS_LOADED && newState == StateContainer.REFRESHING) {
            Timber.e("Illegal state transition from [%s] to [%s]", previousState, newState);
            throw new IllegalStateException();
        }

        state = newState;
    }

    // ------------------------------------------
    /**
     * Go to ERROR_LOAD state, when some critical data was not loaded
     */
    private void stateErrorLoad() {
        setState(StateContainer.ERROR_LOAD);
        // enter ERROR_LOAD state logic

        if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
    }

    // ------------------------------------------
    /**
     * Go to START state, drop all previous values and prepare to fresh start
     */
    private void stateStart() {
        setState(StateContainer.START);
        // enter START state logic

        currentPost = null;
        inputGroupBundle = null;
        inputKeywordBundle = null;

        isGroupBundleChanged = false;
        isKeywordBundleChanged = false;

        totalSelectedGroups = 0;
        totalGroups = 0;

        groupParentItems.clear();
        listAdapter.clear();

        // fresh start - show loading, disable swipe-to-refresh
        if (isViewAttached()) {
            getView().enableSwipeToRefresh(false);
            getView().showLoading(GroupListFragment.RV_TAG);
        }

        sendShowPostingButtonRequest(false);  // hide posting button on start

        // fresh start - load input KeywordBundle and Post
        getKeywordBundleByIdUseCase.execute();
        getPostByIdUseCase.execute();
    }

    // ------------------------------------------
    /**
     * Go to KEYWORDS_LOADED state, assign input KeywordBundle and make Parent items in expandable list
     */
    private void stateKeywordsLoaded(@NonNull KeywordBundle bundle) {
        setState(StateContainer.KEYWORDS_LOADED);
        // enter KEYWORDS_LOADED state logic

        inputKeywordBundle = bundle;  // assign input KeywordBundle

        // fill Parent items in expandable list
        fillKeywordsList(bundle);

        // decide the way how to load GroupBundle and perform loading
        long groupBundleId = inputKeywordBundle.getGroupBundleId();
        // TODO: this drops all selection after refresh - fix it
        fetchedInputGroupBundleFromRepo = groupBundleId != Constant.BAD_ID;
        if (groupBundleId == Constant.BAD_ID) {
            Timber.d("There is no GroupBundle associated with input KeywordBundle, perform network request");
            vkontakteEndpoint.getGroupsByKeywordsSplit(bundle.keywords(), createGetGroupsByKeywordsListCallback());
        } else {
            Timber.d("Loading GroupBundle associated with input KeywordBundle from repository");
            getGroupBundleByIdUseCase.setGroupBundleId(groupBundleId);  // set proper id
            getGroupBundleByIdUseCase.execute();
        }
    }

    // ------------------------------------------
    /**
     * Go to GROUPS_LOADED state, assign input GroupBundle and fill expandable list with Child items
     */
    private void stateGroupsLoaded(@NonNull GroupBundle bundle, List<List<Group>> splitGroups) {
        setState(StateContainer.GROUPS_LOADED);
        // enter GROUPS_LOADED state logic

        inputGroupBundle = bundle;  // assign input GroupBundle

        // hide progress list item if it was previously visible after new Keyword's been added
        listAdapter.setAddingNewItem(false, null);

        // fill Child items in expandable list and show it
        fillGroupsList(splitGroups);

        isRefreshing = false;  // drop flag after filling, where it is used
        isAddingNewKeyword = false;  // don't re-use state control flag

        // enable swipe-to-refresh after all Group-s loaded, show Group-s in expandable list
        if (isViewAttached()) {
            getView().enableSwipeToRefresh(true);
            getView().showGroups(splitGroups.isEmpty());
        }

        sendShowPostingButtonRequest(true);  // show posting button when Group-s loaded
        sendUpdatedSelectedGroupsCounter(totalSelectedGroups, totalGroups);
    }

    // ------------------------------------------
    /**
     * Go to REFRESHING state, reloading GroupBundle by existing Keyword-s
     */
    private void stateRefreshing() {
        setState(StateContainer.REFRESHING);
        // enter REFRESHING state logic

        isRefreshing = true;

        // disable swipe-to-refresh while another refreshing is in progress
        if (isViewAttached()) {
            getView().showLoading(GroupListFragment.RV_TAG);
            getView().enableSwipeToRefresh(false);
        }

        sendShowPostingButtonRequest(false);  // hide posting button while refreshing

        totalSelectedGroups = 0;
        totalGroups = 0;

        // load actual Group-s from Vkontakte endpoint
        vkontakteEndpoint.getGroupsByKeywordsSplit(inputKeywordBundle.keywords(), createGetGroupsByKeywordsListCallback());
    }

    // ------------------------------------------
    private void stateAddKeywordStart(Keyword keyword) {
        setState(StateContainer.ADD_KEYWORD_START);
        // enter ADD_KEYWORD_START state logic

        // disable swipe-to-refresh while add keyword is in progress
        if (isViewAttached()) getView().enableSwipeToRefresh(false);

        newlyAddedKeyword = keyword;

        // show progress list item while adding new Keyword with Group-s
        listAdapter.setAddingNewItem(true, keyword);

        addKeywordToBundleUseCase.setParameters(new AddKeywordToBundle.Parameters(keyword));
        addKeywordToBundleUseCase.execute();
    }

    private void stateAddKeywordFinish(boolean result) {
        setState(StateContainer.ADD_KEYWORD_FINISH);
        // enter ADD_KEYWORD_FINISH state logic

        if (result) {
            Timber.d("Adding Keyword and requesting more Group-s from network");
            // add new Keyword to the head of input KeywordBundle to preserve ordering
            inputKeywordBundle.keywords().add(0, newlyAddedKeyword);
            isKeywordBundleChanged = true;

            GroupParentItem item = new GroupParentItem(newlyAddedKeyword);
            groupParentItems.add(0, item);  // add new item on top of the list

            // prepare parameters to make new request for Group-s by Keyword
            List<Keyword> keywords = new ArrayList<>();
            keywords.add(newlyAddedKeyword);
            newlyAddedKeyword = null;  // drop temporary keyword
            isAddingNewKeyword = true;  // to manipulate with newly fetched Group-s properly
            vkontakteEndpoint.getGroupsByKeywordsSplit(keywords, createGetGroupsByKeywordsListCallback());
        } else {
            Timber.d("Failed to add Keyword, but just warn user via popup");
            sendAddKeywordError();
        }
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
        postGroupBundleUpdate();    // TODO: not sync with onActivityResult()
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
    public void removeChildListItem(int position, int parentPosition) {
        Timber.i("removeChildListItem: %s, %s", position, parentPosition);
        removeGroupAtPosition(position, parentPosition);
    }

    @Override
    public void removeParentListItem(int position) {
        Timber.i("removeParentListItem: %s", position);
        removeKeywordAtPosition(position);
    }

    @Override
    public void refresh() {
        Timber.i("refresh");
        stateRefreshing();
    }

    @Override
    public void retry() {
        Timber.i("retry");
        stateStart();
    }

    /* Mediator */
    // --------------------------------------------------------------------------------------------
    @Override
    public void receiveAddKeywordRequest(Keyword keyword) {
        addKeyword(keyword);
    }

    @Override
    public long receiveAskForGroupBundleIdToDump() {
        return inputGroupBundle != null ? inputGroupBundle.id() : Constant.BAD_ID;
    }

    @Override
    public void receiveAskForRetry() {
        retry();
    }

    @Override
    public void receivePostHasChangedRequest() {
        refreshPost();
    }

    @Override
    public void receivePostToGroupsRequest() {
        postToGroups();
    }

    /* Debugging */
    // ------------------------------------------
    @DebugSake @Override
    public void receivePostingTimeout(int timeout) {
        vkontakteEndpoint.setPostingInterval(timeout);
    }

    // ------------------------------------------
    @Override
    public void sendAddKeywordError() {
        mediatorComponent.mediator().sendAddKeywordError();
    }

    @Override
    public void sendEmptyPost() {
        mediatorComponent.mediator().sendEmptyPost();
    }

    @Override
    public void sendErrorPost() {
        mediatorComponent.mediator().sendErrorPost();
    }

    @Override
    public void sendGroupBundleChanged() {
        mediatorComponent.mediator().sendGroupBundleChanged();
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
    public void sendShowPostingButtonRequest(boolean isVisible) {
        mediatorComponent.mediator().sendShowPostingButtonRequest(isVisible);
    }

    @Override
    public void sendUpdatedSelectedGroupsCounter(int newCount, int total) {
        inputKeywordBundle.setSelectedGroupsCount(newCount);
        inputKeywordBundle.setTotalGroupsCount(total);
        isKeywordBundleChanged = true;  // as counters have been changed
        mediatorComponent.mediator().sendUpdatedSelectedGroupsCounter(newCount, total);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        stateStart();
    }

    @DebugLog
    private void addGroupsToList(List<Group> groups, int keywordIndex) {
        Timber.i("addGroupsToList: total groups = %s, index = %s", groups.size(), keywordIndex);
        if (AppConfig.INSTANCE.isAllGroupsSortedByMembersCount()) Collections.sort(groups);

        /**
         * Select all groups only in the following cases:
         * - Group-s were fetched from Endpoint, not repository (no GroupBundle associated with input KeywordBundle)
         * - Added new Keyword to list, resulting new Group-s to be fetched from Endpoint
         *
         * and 'isAllGroupsSelected' configuration must be enabled in both cases.
         */
        boolean shouldSelectAllGroups = AppConfig.INSTANCE.isAllGroupsSelected() &&
                (!fetchedInputGroupBundleFromRepo || isAddingNewKeyword || isRefreshing);

        int xSelectedCount = 0, xTotalGroups = 0;
        List<GroupChildItem> childItems = new ArrayList<>(groups.size());
        for (Group group : groups) {
            ++xTotalGroups;
            GroupChildItem childItem = new GroupChildItem(group);
            childItems.add(childItem);
            if (shouldSelectAllGroups) {
                childItem.setSelected(true);
                ++totalSelectedGroups;
                ++xSelectedCount;
            } else if (group.isSelected()) {
                ++totalSelectedGroups;
                ++xSelectedCount;
            }
        }
        totalGroups += xTotalGroups;
        GroupParentItem parentItem = groupParentItems.get(keywordIndex);
        parentItem.setSelectedCount(xSelectedCount);
        parentItem.setChildList(childItems);
    }

    private void addKeyword(Keyword keyword) {
        Timber.i("addKeyword: %s", keyword.toString());
        if (inputKeywordBundle.keywords().size() < Constant.KEYWORDS_LIMIT) {
            stateAddKeywordStart(keyword);
        } else {
            sendKeywordsLimitReached(Constant.KEYWORDS_LIMIT);
        }
    }

    @DebugLog
    private void fillKeywordsList(@NonNull KeywordBundle bundle) {
        Timber.i("fillKeywordsList: %s", bundle.keywords().size());
        for (Keyword keyword : bundle) {
            Timber.v(keyword.toString());
            GroupParentItem item = new GroupParentItem(keyword);
            groupParentItems.add(item);
        }
        Timber.d("Total Parent list items: %s", groupParentItems.size());
    }

    @DebugLog
    private void fillGroupsList(List<List<Group>> splitGroups) {
        Timber.i("fillGroupsList: %s", splitGroups.size());
        // TODO: batch by 20 groups and load-more

        /**
         * @Updated
         * Restore correspondence between the order of Keyword-s in expandable list and the order
         * of lists of Group-s in 'splitGroups' parameter. This algorithm loops over all lists in
         * 'splitGroups' trying the first Group in each list and compares it's Keyword with those in
         * Parent item from expandable list. No matching means error in program and leads to exception.
         *
         * @Deprecated
         * Here we fill each Parent item with Group-s corresponding to the Keyword of this Parent item.
         * We rely on strong correspondence between order of Keyword-s in list and input KeywordBundle
         * and the order of sub-lists in the 'splitGroups' parameter: each sub-list at position 'k'
         * strongly corresponds to the Keyword (and Parent item) at the same position 'k'. This is
         * quite stable suggestion because we add new Parent list items (and Keyword-s) on top of the
         * existing expandable list (and input KeywordBundle) and store these item in ordered collection
         * {@link java.util.List} instead of unordered {@link java.util.Collection}, and on the other hand
         * the order of sub-lists in 'splitGroups' retrieved from the endpoint or repository strongly
         * coincides with the order of input Keyword-s as a parameter or request.
         */
        for (int i = 0; i < groupParentItems.size(); ++i) {
            Keyword keyword = groupParentItems.get(i).getKeyword();  // take each Keyword
            List<Group> groups = null;
            for (List<Group> item : splitGroups) {
                if (!item.isEmpty()) {
                    Group group = item.get(0);
                    if (keyword.equals(group.keyword())) {
                        groups = item;
                        break;
                    }
                }
            }
            /**
             * 'null' means no correspondence found between Keyword and sub-list of Group-s.
             * This is likely the case, when sub-list of Group-s is just empty or we have just
             * added one single Keyword and have found sub-list corresponding to it, so we don't need
             * to touch the other Keyword-s and modify their Parent list items.
             *
             * If there is a non-empty sub-list but no corresponding Keyword, it is an error in
             * implementation, but there is no diagnostic logs about it.
             */
            if (groups != null) addGroupsToList(groups, i);
        }

        listAdapter.notifyParentDataSetChanged(false);
    }

    // ------------------------------------------
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
            Timber.d("Total selected Group-s: %s", selectedGroups.size());
            if (BuildConfig.DEBUG) {
                for (Group group : selectedGroups) {
                    Timber.v("Selected Group: [%s] %s %s", group.id(), group.name(), group.membersCount());
                }
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

    // ------------------------------------------
    private void postGroupBundleUpdate() {
        Timber.i("postGroupBundleUpdate: %s", isGroupBundleChanged);
        if (isGroupBundleChanged) {
            Timber.d("Input GroupBundle has been changed, it will be updated in repository");
            isGroupBundleChanged = false;
            postGroupBundleUseCase.setParameters(new PostGroupBundle.Parameters(inputGroupBundle));
            postGroupBundleUseCase.execute();  // silent update without callback
            sendGroupBundleChanged();
        } else {
            Timber.d("Input GroupBundle wasn't changed");
        }
    }

    private void postKeywordBundleUpdate() {
        Timber.i("postKeywordBundleUpdate: %s", isKeywordBundleChanged);
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

    // ------------------------------------------
    private void removeGroupAtPosition(int position, int keywordPosition) {
        Timber.i("removeGroupAtPosition: %s, %s", position, keywordPosition);
        GroupParentItem item = groupParentItems.get(keywordPosition);
        --totalSelectedGroups;
        --totalGroups;
        sendUpdatedSelectedGroupsCounter(totalSelectedGroups, totalGroups);

        Collection<Group> groupsToRemove = new ArrayList<>();
        GroupChildItem childItem = item.getChildList().get(position);
        groupsToRemove.add(childItem.getGroup());
        inputGroupBundle.groups().removeAll(groupsToRemove);
        isGroupBundleChanged = true;
        postGroupBundleUpdate();  // refresh now, don't wait till screen closed

        item.getChildList().remove(position);
        item.incrementSelectedCount(childItem.isSelected() ? -1 : 0);
        listAdapter.notifyParentChanged(keywordPosition);
        listAdapter.notifyChildRemoved(position, keywordPosition);
    }

    private void removeKeywordAtPosition(int position) {
        Timber.i("removeKeywordAtPosition: %s", position);
        GroupParentItem item = groupParentItems.get(position);
        totalSelectedGroups -= item.getSelectedCount();
        totalGroups -= item.getChildCount();
        sendUpdatedSelectedGroupsCounter(totalSelectedGroups, totalGroups);

        /**
         * User can remove all Parent items from expandable list making input KeywordBundle empty.
         * Although user can continue and add new Keyword to list later on GroupListScreen, empty
         * KeywordBundle-s are not allowed and will be wiped out from repository when user navigates
         * back to MainScreen or KeywordListScreen.
         */
        Keyword keyword = groupParentItems.get(position).getKeyword();
        inputKeywordBundle.keywords().remove(keyword);
        postKeywordBundleUpdate();  // refresh now, don't wait till screen closed

        Collection<Group> groupsToRemove = new ArrayList<>();
        for (GroupChildItem childItem : item.getChildList()) {
            groupsToRemove.add(childItem.getGroup());
        }
        inputGroupBundle.groups().removeAll(groupsToRemove);
        isGroupBundleChanged = true;
        postGroupBundleUpdate();  // refresh now, don't wait till screen closed

        groupParentItems.remove(position);
        listAdapter.notifyParentRemoved(position);

        if (groupParentItems.isEmpty() && isViewAttached()) getView().showEmptyList(GroupListFragment.RV_TAG);
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Boolean> createAddKeywordToBundleCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @DebugLog @Override
            public void onFinish(@Nullable Boolean result) {
                Timber.i("Use-Case: succeeded to add Keyword to KeywordBundle");
                stateAddKeywordFinish(result != null && result);
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to add Keyword to KeywordBundle");
                stateAddKeywordFinish(false);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<GroupBundle> createGetGroupBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<GroupBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable GroupBundle bundle) {
                if (bundle == null) {
                    Timber.e("No GroupBundle found by id associated with input KeywordBundle, %s",
                            "such id has improper value due to wrong association between instances at creation");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get GroupBundle by id");
                stateGroupsLoaded(bundle, bundle.splitGroupsByKeywords());
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get GroupBundle by id");
                stateErrorLoad();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<KeywordBundle> createGetKeywordBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                if (bundle == null) {
                    Timber.e("KeywordBundle wasn't found by id: %s", getKeywordBundleByIdUseCase.getKeywordBundleId());
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get KeywordBundle by id");
                stateKeywordsLoaded(bundle);
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get KeywordBundle by id");
                stateErrorLoad();
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
                sendErrorPost();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<GroupBundle> createPutGroupBundleCallback() {
        return new UseCase.OnPostExecuteCallback<GroupBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable GroupBundle bundle) {
                if (bundle == null) {
                    Timber.e("Failed to put new GroupBundle to repository - item not created, as expected");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to put GroupBundle");
                stateGroupsLoaded(bundle, bundle.splitGroupsByKeywords());

                Timber.d("Update input KeywordBundle and associate it with newly created GroupBundle (by setting id)");
                isKeywordBundleChanged = true;
                inputKeywordBundle.setGroupBundleId(bundle.id());
                getGroupBundleByIdUseCase.setGroupBundleId(bundle.id());  // set proper id
                postKeywordBundleUpdate();
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to put GroupBundle");
                // TODO: failed to put GroupBundle to repo
            }
        };
    }

    private UseCase.OnPostExecuteCallback<GroupReportBundle> createPutGroupReportBundleCallback() {
        return new UseCase.OnPostExecuteCallback<GroupReportBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable GroupReportBundle bundle) {
                if (bundle == null) {
                    Timber.e("Failed to put new GroupReportBundle to repository - item not created, as expected");
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
                // TODO: failed to put GroupBundleReport - retry posting?
                sendPostingStartedMessage(false);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<List<List<Group>>> createGetGroupsByKeywordsListCallback() {
        return new UseCase.OnPostExecuteCallback<List<List<Group>>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<List<Group>> splitGroups) {
                if (splitGroups == null) {
                    Timber.e("Split list of Group-s must not be null, it could be empty at least");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get list of Group-s by list of Keyword-s");

                List<Group> groups = ValueUtility.merge(splitGroups);
                if (inputGroupBundle == null || inputGroupBundle.id() == Constant.BAD_ID) {
                    Timber.d("Create new GroupsBundle and put it to repository, update id in associated input KeywordBundle");
                    PutGroupBundle.Parameters parameters = new PutGroupBundle.Parameters.Builder()
                            .setGroups(groups)
                            .setKeywordBundleId(inputKeywordBundle.id())
                            .setTitle("title")  // TODO: set group-bundle title from Toolbar
                            .build();
                    putGroupBundleUseCase.setParameters(parameters);
                    putGroupBundleUseCase.execute();
                } else {
                    Timber.d("Refresh already existing GroupBundle in repository");
                    if (isAddingNewKeyword) {
                        /**
                         * If we are adding new Keyword to list, we should append newly fetched
                         * Group-s to existing ones, otherwise we will wrongly rewrite them and get
                         * inconsistency between number of Keyword-s and Parent items in expandable list
                         * and actual number of Keyword-s inside 'inputGroupBundle' model, leading to crash.
                         *
                         * On the other hand, 'else' branch is performed when expandable list is
                         * refreshing to fetch actual set of Group-s from the Endpoint
                         */
                        groups.addAll(inputGroupBundle.groups());
                    }
                    GroupBundle bundle = GroupBundle.builder()
                            .setId(inputGroupBundle.id())
                            .setGroups(groups)
                            .setKeywordBundleId(inputKeywordBundle.id())
                            .setTimestamp(inputGroupBundle.timestamp())
                            .setTitle(inputGroupBundle.title())  // TODO: set group-bundle title from Toolbar
                            .build();

                    /**
                     * Note the separation between list of Group-s within the GroupBundle parameter
                     * (as a result of {@link GroupBundle#splitGroupsByKeywords() method call} and
                     * autonomous list of Group-a as 'splitGroups' parameter. In case of adding new
                     * Keyword to the expandable list we should only fill the latter with those Group-s
                     * corresponding to the newly added Keyword, but the 'bundle' parameter contains
                     * the whole set of Group-s for all available Keyword-s in the input KeywordBundle.
                     */
                    stateGroupsLoaded(bundle, splitGroups);

                    isGroupBundleChanged = true;
                    postGroupBundleUpdate();
                }
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get list of Group-s by list of Keyword-s");
                stateErrorLoad();
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
                // TODO: error on wall posting properly
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
            isGroupBundleChanged = true;  // as Group-s 'isSelected' flag has been changed
        };
    }

    private GroupListAdapter.OnCheckedChangeListener createExternalChildItemSwitcherCallback() {
        return (data, isChecked) -> {
            totalSelectedGroups += isChecked ? 1 : -1;
            sendUpdatedSelectedGroupsCounter(totalSelectedGroups, totalGroups);
            isGroupBundleChanged = true;  // as Group-s 'isSelected' flag has been changed
        };
    }
}
