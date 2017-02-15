package com.orcchg.vikstra.app.ui.group.list.fragment;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

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
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.group.GetGroupBundleById;
import com.orcchg.vikstra.domain.interactor.group.PostGroupBundle;
import com.orcchg.vikstra.domain.interactor.group.PutGroupBundle;
import com.orcchg.vikstra.domain.interactor.keyword.AddKeywordToBundle;
import com.orcchg.vikstra.domain.interactor.keyword.DeleteKeywordBundle;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundleById;
import com.orcchg.vikstra.domain.interactor.keyword.PostKeywordBundle;
import com.orcchg.vikstra.domain.interactor.keyword.PutKeywordBundle;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.report.PutGroupReportBundle;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.Heavy;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.model.parcelable.ParcelableKeywordBundle;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.DebugSake;
import com.orcchg.vikstra.domain.util.ValueUtility;
import com.orcchg.vikstra.domain.util.endpoint.EndpointUtility;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GroupListPresenter extends BasePresenter<GroupListContract.View> implements GroupListContract.Presenter {
    private static final int PrID = Constant.PresenterId.GROUP_LIST_FRAGMENT_PRESENTER;

    private final AddKeywordToBundle addKeywordToBundleUseCase;
    private final DeleteKeywordBundle deleteKeywordBundleUseCase;
    private final GetGroupBundleById getGroupBundleByIdUseCase;
    private final GetKeywordBundleById getKeywordBundleByIdUseCase;
    private final GetPostById getPostByIdUseCase;
    private final PostKeywordBundle postKeywordBundleUseCase;
    private final PutKeywordBundle putKeywordBundleUseCase;
    private final PostGroupBundle postGroupBundleUseCase;
    private final PutGroupBundle putGroupBundleUseCase;
    private final PutGroupReportBundle putGroupReportBundleUseCase;
    private final VkontakteEndpoint vkontakteEndpoint;
    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    private GroupListMediatorComponent mediatorComponent;

    // --------------------------------------------------------------------------------------------
    private List<GroupParentItem> groupParentItems = new ArrayList<>();
    private GroupListAdapter listAdapter;

    /**
     * These fields aren't included in {@link Memento} because this will be refreshed or applied
     * automatically sometime between {@link GroupListPresenter#onStop()} and {@link GroupListPresenter#onRestoreState()}.
     */
    int totalSelectedGroups, totalGroups;
    private boolean isKeywordBundleChanged, isGroupBundleChanged;
    private @Heavy GroupBundle inputGroupBundle;  // too heave object to store in Memento - only it's id will be included

    private Memento memento = new Memento();

    private @StateContainer.State int chainedStateRestore = StateContainer.NONE;

    // --------------------------------------------------------------------------------------------
    private static final class StateContainer {
        private static final int NONE = -2;  // for internal mechanisms only, no explicit transition
        private static final int ERROR_LOAD = -1;
        private static final int START = 0;
        private static final int KEYWORDS_CREATE_START = 1, KEYWORDS_CREATE_FINISH = 2;
        private static final int KEYWORDS_LOADED = 3;
        private static final int GROUPS_LOADED = 4;
        private static final int REFRESHING = 5;
        private static final int ADD_KEYWORD_START = 6, ADD_KEYWORD_FINISH = 7;

        @IntDef({
            NONE,
            ERROR_LOAD,
            START,
            KEYWORDS_CREATE_START, KEYWORDS_CREATE_FINISH,
            KEYWORDS_LOADED,
            GROUPS_LOADED,
            REFRESHING,
            ADD_KEYWORD_START, ADD_KEYWORD_FINISH
        })
        @Retention(RetentionPolicy.SOURCE)
        private @interface State {}
    }

    // --------------------------------------------------------------------------------------------
    private static final class Memento {
        private static final String BUNDLE_KEY_INPUT_GROUP_BUNDLE_ID = "bundle_key_input_group_bundle_id_" + PrID;
        private static final String BUNDLE_KEY_INPUT_KEYWORD_BUNDLE = "bundle_key_input_keyword_bundle_" + PrID;
        private static final String BUNDLE_KEY_CURRENT_POST = "bundle_key_current_post_" + PrID;
        private static final String BUNDLE_KEY_NEWLY_ADDED_KEYWORD = "bundle_key_newly_added_keyword_" + PrID;
        private static final String BUNDLE_KEY_STATE = "bundle_key_state_" + PrID;
        private static final String BUNDLE_KEY_FLAG_ADD_KEYWORD_FINISHED_RESULT = "bundle_key_add_keyword_finished_result_" + PrID;
        private static final String BUNDLE_KEY_FLAG_IS_ADDING_NEW_KEYWORD = "bundle_key_flag_is_adding_new_keyword_" + PrID;
        private static final String BUNDLE_KEY_FLAG_IS_REFRESHING = "bundle_key_flag_is_refreshing_" + PrID;
        private static final String BUNDLE_KEY_FLAG_FETCHED_INPUT_GROUP_BUNDLE_FROM_REPO = "bundle_key_flag_fetched_input_group_bundle_from_repo_" + PrID;
        private static final String BUNDLE_KEY_FLAG_WAS_INPUT_KEYWORD_BUNDLE_CREATED = "bundle_key_flag_was_input_keyword_bundle_created_" + PrID;
        private static final String BUNDLE_KEY_USE_CASE_PARAMETER_ADD_KEYWORD_TO_BUNDLE_ID = "bundle_key_use_case_parameter_add_keyword_to_bundle_id_" + PrID;
        private static final String BUNDLE_KEY_USE_CASE_PARAMETER_GET_KEYWORD_BUNDLE_BY_ID_ID = "bundle_key_use_case_parameter_get_keyword_bundle_by_id_id_" + PrID;

        private long inputGroupBundleId = Constant.BAD_ID;
        private KeywordBundle inputKeywordBundle;
        private Post currentPost;
        private Keyword newlyAddedKeyword;

        private long useCaseParameter_addKeywordToBundle_id = Constant.BAD_ID;
        private long useCaseParameter_getKeywordBundleById_id = Constant.BAD_ID;

        private @StateContainer.State int state = StateContainer.START;
        // state control flags
        private boolean addKeywordFinishedResult;
        private boolean isAddingNewKeyword;
        private boolean isRefreshing;
        private boolean fetchedInputGroupBundleFromRepo;
        private boolean wasInputKeywordBundleCreated;

        @DebugLog
        private void toBundle(Bundle outState) {
            outState.putLong(BUNDLE_KEY_INPUT_GROUP_BUNDLE_ID, inputGroupBundleId);
            outState.putParcelable(BUNDLE_KEY_INPUT_KEYWORD_BUNDLE, new ParcelableKeywordBundle(inputKeywordBundle));
            outState.putParcelable(BUNDLE_KEY_CURRENT_POST, currentPost);
            outState.putParcelable(BUNDLE_KEY_NEWLY_ADDED_KEYWORD, newlyAddedKeyword);
            outState.putInt(BUNDLE_KEY_STATE, state);
            outState.putBoolean(BUNDLE_KEY_FLAG_ADD_KEYWORD_FINISHED_RESULT, addKeywordFinishedResult);
            outState.putBoolean(BUNDLE_KEY_FLAG_IS_ADDING_NEW_KEYWORD, isAddingNewKeyword);
            outState.putBoolean(BUNDLE_KEY_FLAG_IS_REFRESHING, isRefreshing);
            outState.putBoolean(BUNDLE_KEY_FLAG_FETCHED_INPUT_GROUP_BUNDLE_FROM_REPO, fetchedInputGroupBundleFromRepo);
            outState.putBoolean(BUNDLE_KEY_FLAG_WAS_INPUT_KEYWORD_BUNDLE_CREATED, wasInputKeywordBundleCreated);
            outState.putLong(BUNDLE_KEY_USE_CASE_PARAMETER_ADD_KEYWORD_TO_BUNDLE_ID, useCaseParameter_addKeywordToBundle_id);
            outState.putLong(BUNDLE_KEY_USE_CASE_PARAMETER_GET_KEYWORD_BUNDLE_BY_ID_ID, useCaseParameter_getKeywordBundleById_id);
        }

        @DebugLog @SuppressWarnings("ResourceType")
        private static Memento fromBundle(Bundle savedInstanceState) {
            Memento memento = new Memento();
            memento.inputGroupBundleId = savedInstanceState.getLong(BUNDLE_KEY_INPUT_GROUP_BUNDLE_ID, Constant.BAD_ID);
            memento.inputKeywordBundle = ((ParcelableKeywordBundle) savedInstanceState.getParcelable(BUNDLE_KEY_INPUT_KEYWORD_BUNDLE)).get();
            memento.currentPost = savedInstanceState.getParcelable(BUNDLE_KEY_CURRENT_POST);
            memento.newlyAddedKeyword = savedInstanceState.getParcelable(BUNDLE_KEY_NEWLY_ADDED_KEYWORD);
            memento.state = savedInstanceState.getInt(BUNDLE_KEY_STATE, StateContainer.START);
            memento.addKeywordFinishedResult = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_ADD_KEYWORD_FINISHED_RESULT, false);
            memento.isAddingNewKeyword = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_IS_ADDING_NEW_KEYWORD, false);
            memento.isRefreshing = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_IS_REFRESHING, false);
            memento.fetchedInputGroupBundleFromRepo = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_FETCHED_INPUT_GROUP_BUNDLE_FROM_REPO, false);
            memento.wasInputKeywordBundleCreated = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_WAS_INPUT_KEYWORD_BUNDLE_CREATED, false);
            memento.useCaseParameter_addKeywordToBundle_id = savedInstanceState.getLong(BUNDLE_KEY_USE_CASE_PARAMETER_ADD_KEYWORD_TO_BUNDLE_ID, Constant.BAD_ID);
            memento.useCaseParameter_getKeywordBundleById_id = savedInstanceState.getLong(BUNDLE_KEY_USE_CASE_PARAMETER_GET_KEYWORD_BUNDLE_BY_ID_ID, Constant.BAD_ID);
            return memento;
        }
    }

    // --------------------------------------------------------------------------------------------
    @Inject
    GroupListPresenter(AddKeywordToBundle addKeywordToBundleUseCase, DeleteKeywordBundle deleteKeywordBundleUseCase,
                       GetGroupBundleById getGroupBundleByIdUseCase, GetKeywordBundleById getKeywordBundleByIdUseCase,
                       GetPostById getPostByIdUseCase, PostKeywordBundle postKeywordBundleUseCase,
                       PutKeywordBundle putKeywordBundleUseCase, PostGroupBundle postGroupBundleUseCase,
                       PutGroupBundle putGroupBundleUseCase, PutGroupReportBundle putGroupReportBundleUseCase,
                       VkontakteEndpoint vkontakteEndpoint, PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.listAdapter = createListAdapter(groupParentItems, createGroupClickCallback(), createAllGroupsSelectedCallback());
        this.addKeywordToBundleUseCase = addKeywordToBundleUseCase;
        this.addKeywordToBundleUseCase.setPostExecuteCallback(createAddKeywordToBundleCallback());
        this.deleteKeywordBundleUseCase = deleteKeywordBundleUseCase;  // no callback - background task
        this.getGroupBundleByIdUseCase = getGroupBundleByIdUseCase;
        this.getGroupBundleByIdUseCase.setPostExecuteCallback(createGetGroupBundleByIdCallback());
        this.getKeywordBundleByIdUseCase = getKeywordBundleByIdUseCase;
        this.getKeywordBundleByIdUseCase.setPostExecuteCallback(createGetKeywordBundleByIdCallback());
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.postKeywordBundleUseCase = postKeywordBundleUseCase;  // no callback - background task
        this.putKeywordBundleUseCase = putKeywordBundleUseCase;
        this.putKeywordBundleUseCase.setPostExecuteCallback(createPutKeywordBundleCallback());
        this.postGroupBundleUseCase = postGroupBundleUseCase;  // no callback - background task
        this.putGroupBundleUseCase = putGroupBundleUseCase;
        this.putGroupBundleUseCase.setPostExecuteCallback(createPutGroupBundleCallback());
        this.putGroupReportBundleUseCase = putGroupReportBundleUseCase;
        this.putGroupReportBundleUseCase.setPostExecuteCallback(createPutGroupReportBundleCallback());
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
     *                           START ----- < ------ < ----- < ----- < ---- ERROR_LOAD  { user retry }
     *                             |                                              |
     *                             |                                              |
     *                        KEYWORDS_LOADED  or  KEYWORDS_CREATE_START  or ---- #
     *                             |                      |                       |
     *                             |                      |                       |
     *                             |----- < ---- < -- KEYWORDS_CREATE_FINISH      |
     *                             |                                              |
     *                             |                                              |
     *                        GROUPS_LOADED    or  ----- > ----- > ----- > ------ #
     *                          |       |
     *                          |       ^
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
        @StateContainer.State int previousState = memento.state;
        Timber.i("Previous state [%s], New state: %s", previousState, newState);

        // check consistency between state transitions
        if (previousState == StateContainer.ERROR_LOAD && newState != StateContainer.START ||
            // forbid transition from any kind of loading to refreshing
            (previousState != StateContainer.GROUPS_LOADED && previousState != StateContainer.REFRESHING) && newState == StateContainer.REFRESHING) {
            Timber.e("Illegal state transition from [%s] to [%s]", previousState, newState);
            throw new IllegalStateException(String.format(Locale.ENGLISH, "Transition from %s to %s", previousState, newState));
        }

        memento.state = newState;
    }

    @DebugLog
    private void assignState(@StateContainer.State int newState) {
        Timber.d("assignState: %s", newState);
        setState(newState);  // verbose call
    }

    // ------------------------------------------
    /**
     * Go to ERROR_LOAD state, when some critical data was not loaded
     */
    private void stateErrorLoad() {
        Timber.i("stateErrorLoad");
        setState(StateContainer.ERROR_LOAD);
        // enter ERROR_LOAD state logic

        if (isViewAttached()) getView().showError(GroupListFragment.RV_TAG);
    }

    // ------------------------------------------
    /**
     * Go to START state, drop all previous values and prepare to fresh start
     */
    private void stateStart() {
        Timber.i("stateStart");
        setState(StateContainer.START);
        // enter START state logic

        inputGroupBundle = null;
        memento.inputGroupBundleId = Constant.BAD_ID;
        memento.inputKeywordBundle = null;
        memento.currentPost = null;
        memento.newlyAddedKeyword = null;

        isGroupBundleChanged = false;
        isKeywordBundleChanged = false;

        memento.addKeywordFinishedResult = false;
        memento.isAddingNewKeyword = false;
        memento.isRefreshing = false;
        memento.fetchedInputGroupBundleFromRepo = false;
        memento.wasInputKeywordBundleCreated = false;

        totalSelectedGroups = 0;
        totalGroups = 0;

        groupParentItems.clear();  // idempotent operation
        listAdapter.clear();  // idempotent operation

        // fresh start - show loading, disable swipe-to-refresh
        if (isViewAttached()) {
            getView().enableSwipeToRefresh(false);
            getView().showLoading(GroupListFragment.RV_TAG);
        }

        sendEnableAddKeywordButtonRequest(false);  // disable add keyword button on start
        sendShowPostingButtonRequest(false);  // hide posting button on start

        // fresh start - load input KeywordBundle and Post
        getKeywordBundleByIdUseCase.execute();
        getPostByIdUseCase.execute();
    }

    // ------------------------------------------
    /**
     * Go to KEYWORDS_CREATE_START state, initialize input KeywordBundle
     */
    private void stateKeywordBundleCreateStart() {
        Timber.i("stateKeywordBundleCreateStart");
        setState(StateContainer.KEYWORDS_CREATE_START);
        // enter KEYWORDS_CREATE_START state logic

        memento.wasInputKeywordBundleCreated = true;

        // create new empty KeywordBundle in repository
        PutKeywordBundle.Parameters parameters = new PutKeywordBundle.Parameters.Builder()
                .setTitle(sendAskForTitle())
                .setKeywords(new ArrayList<>())  // empty Keyword-s
                .build();
        putKeywordBundleUseCase.setParameters(parameters);
        putKeywordBundleUseCase.execute();
    }

    // ------------------------------------------
    /**
     * Go to KEYWORDS_CREATE_FINISH state, show stub with 'add new Keyword' button
     */
    private void stateKeywordBundleCreateFinish(@NonNull KeywordBundle bundle) {
        Timber.i("stateKeywordBundleCreateFinish");
        setState(StateContainer.KEYWORDS_CREATE_FINISH);
        // enter KEYWORDS_CREATE_FINISH state logic

        memento.inputKeywordBundle = bundle;  // initialize input KeywordBundle (empty)

        /**
         * We are working with newly created KeywordBundle on GroupListScreen instead of a fetched one
         * from repository. So, we should assign proper id to use-cases, where it's necessary:
         *
         * - addition of new Keyword is now goes to new KeywordBundle
         * - retrying is now tries to re-fetch this new KeywordBundle
         */
        addKeywordToBundleUseCase.setKeywordBundleId(bundle.id());
        getKeywordBundleByIdUseCase.setKeywordBundleId(bundle.id());

        // show empty stub with suggestion to add new Keyword to the newly created KeywordBundle
        if (isViewAttached()) getView().showEmptyList(GroupListFragment.RV_TAG);

        sendEnableAddKeywordButtonRequest(true);  // enable add keyword button when KeywordBundle is fresh
    }

    // ------------------------------------------
    /**
     * Go to KEYWORDS_LOADED state, assign input KeywordBundle and make Parent items in expandable list
     */
    private void stateKeywordsLoaded(@NonNull KeywordBundle bundle) {
        Timber.i("stateKeywordsLoaded");
        setState(StateContainer.KEYWORDS_LOADED);
        // enter KEYWORDS_LOADED state logic

        memento.inputKeywordBundle = bundle;  // assign input KeywordBundle
        sendUpdateTitleRequest(bundle.title());  // assign title

        // fill Parent items in expandable list
        groupParentItems.clear();
        listAdapter.clear();
        fillKeywordsList(bundle);

        // decide the way how to load GroupBundle and perform loading
        long groupBundleId = memento.inputKeywordBundle.getGroupBundleId();
        // TODO: this drops all selection after refresh - fix it
        memento.fetchedInputGroupBundleFromRepo = groupBundleId != Constant.BAD_ID;
        if (groupBundleId == Constant.BAD_ID) {
            Timber.d("There is no GroupBundle associated with input KeywordBundle, perform endpoint request");
            vkontakteEndpoint.getGroupsByKeywordsSplit(bundle.keywords(), createGetGroupsByKeywordsListCallback(), createCancelCallback());
        } else {
            Timber.d("Loading GroupBundle associated with input KeywordBundle from repository");
            restoreLoadedGroups(groupBundleId);  // Group-s are in repository already
        }
    }

    // ------------------------------------------
    /**
     * Go to GROUPS_LOADED state, assign input GroupBundle and fill expandable list with Child items
     */
    private void stateGroupsLoaded(@NonNull GroupBundle bundle, List<List<Group>> splitGroups) {
        Timber.i("stateGroupsLoaded, splitGroups size: %s", splitGroups.size());
        setState(StateContainer.GROUPS_LOADED);
        // enter GROUPS_LOADED state logic

        inputGroupBundle = bundle;  // assign input GroupBundle
        memento.inputGroupBundleId = bundle.id();  // keep id to restore the whole heavy bundle if need

        // hide progress list item if it was previously visible after new Keyword's been added
        listAdapter.setAddingNewItem(false, null);

        // fill Child items in expandable list and show it
        fillGroupsList(splitGroups);

        memento.isRefreshing = false;  // drop flag after filling, where it is used
        memento.isAddingNewKeyword = false;  // don't re-use state control flag

        // enable swipe-to-refresh after all Group-s loaded, show Group-s in expandable list
        if (isViewAttached()) {
            getView().enableSwipeToRefresh(true);
            /**
             * Decide, when to show empty stub instead of list items. The only case when empty stub
             * is shown is when there is no Parent items in the list (i.e. no Keyword-s).
             *
             * 'splitGroups' list could be empty when GroupBundle is fetched from repository and
             * doesn't contain any Group-s, so {@link GroupBundle#splitGroupsByKeywords()} returns
             * zero sub-lists. But there could be Parent list items from the KeywordBundle which
             * corresponds to this GroupBundle, and KeywordBundle may contain at least one Keyword.
             * But no Group-s were actually found for such Keyword-s. Anyway, we must show Parent list
             * items, no matter whether they are empty (no Group-s inside them) or not.
             *
             * Seems, that checking 'groupParentItems.isEmpty()' is quite enough.
             */
            getView().showGroups(splitGroups.isEmpty() && groupParentItems.isEmpty());
        }

        sendEnableAddKeywordButtonRequest(true);  // enable add keyword button when Group-s loaded
        sendShowPostingButtonRequest(true);  // show posting button when Group-s loaded
        sendUpdatedSelectedGroupsCounter(totalSelectedGroups, totalGroups);

        Timber.d("Chained state: %s", chainedStateRestore);
        @StateContainer.State int localChainedStateRestore = chainedStateRestore;
        switch (localChainedStateRestore) {
            case StateContainer.ADD_KEYWORD_START:
                chainedStateRestore = StateContainer.NONE;
                stateAddKeywordStart(memento.newlyAddedKeyword);
                break;
            case StateContainer.ADD_KEYWORD_FINISH:
                chainedStateRestore = StateContainer.NONE;
                stateAddKeywordFinish(memento.addKeywordFinishedResult);
                break;
            default:
                Timber.d("Not need to perform chained transition");
                /**
                 * Drop newly added Keyword, but after we have performed all operations dependent on it
                 */
                memento.newlyAddedKeyword = null;  // drop temporary Keyword
                break;
        }
    }

    // ------------------------------------------
    /**
     * Go to REFRESHING state, reloading GroupBundle by existing Keyword-s
     */
    private void stateRefreshing() {
        Timber.i("stateRefreshing");
        setState(StateContainer.REFRESHING);
        // enter REFRESHING state logic

        memento.isRefreshing = true;

        // disable swipe-to-refresh while another refreshing is in progress
        if (isViewAttached()) {
            getView().showLoading(GroupListFragment.RV_TAG);
            getView().enableSwipeToRefresh(false);
        }

        sendEnableAddKeywordButtonRequest(false);  // disable add keyword button while refreshing
        sendShowPostingButtonRequest(false);  // hide posting button while refreshing

        /**
         * We don't set input GroupBundle-s field {@link GroupListPresenter#inputGroupBundle} to null
         * because is will be refreshed when Group-s will be received in {@link GroupListPresenter#createGetGroupsByKeywordsListCallback()}
         * callback. Null value leads PUT-request to be executed creating a completely new GroupBundle
         * in repository any time user triggers refreshing, which is not desired behavior.
         */

        totalSelectedGroups = 0;
        totalGroups = 0;

        // load actual Group-s from Vkontakte endpoint
        vkontakteEndpoint.getGroupsByKeywordsSplit(memento.inputKeywordBundle.keywords(), createGetGroupsByKeywordsListCallback(), createCancelCallback());
    }

    // ------------------------------------------
    /**
     * Go to ADD_KEYWORD_START state, check possibility to add new Keyword and execute addition
     */
    private void stateAddKeywordStart(Keyword keyword) {
        Timber.i("stateAddKeywordStart");
        setState(StateContainer.ADD_KEYWORD_START);
        // enter ADD_KEYWORD_START state logic

        memento.addKeywordFinishedResult = false;  // drop flag, will be set at finish

        /**
         * Previous state could be either {@link StateContainer.KEYWORDS_CREATE_FINISH} or
         * {@link StateContainer.GROUPS_LOADED}. The former case means that input KeywordBundle
         * (i.e. {@link GroupListPresenter.Memento#inputKeywordBundle} field) contains no Keyword-s,
         * thus the following if-statement won't execute. Otherwise, we should assign the next state
         * manually - it will be {@link StateContainer.GROUPS_LOADED} - in order to bet into final state.
         */
        if (memento.inputKeywordBundle.keywords().contains(keyword)) {
            Timber.d("Keyword [%s] has already been added", keyword.keyword());
            sendAlreadyAddedKeyword(keyword.keyword());
            assignState(StateContainer.GROUPS_LOADED);  // get to the final state
            return;
        }

        memento.newlyAddedKeyword = keyword;

        sendEnableAddKeywordButtonRequest(false);  // disable add keyword button while adding new Keyword

        addKeywordToBundleUseCase.setParameters(new AddKeywordToBundle.Parameters(keyword));
        addKeywordToBundleUseCase.execute();
    }

    // ------------------------------------------
    /**
     * Go to ADD_KEYWORD_FINISH state, check result and load Group-s corresponding to the newly added Keyword
     */
    private void stateAddKeywordFinish(boolean result) {
        Timber.i("stateAddKeywordFinish: %s", result);
        setState(StateContainer.ADD_KEYWORD_FINISH);
        // enter ADD_KEYWORD_FINISH state logic

        /**
         *
         * @Screen_restored
         * Previous state could be only {@link StateContainer.ADD_KEYWORD_START}, there is already a
         * filter preventing from adding already existing Keyword repeatedly. Here we avoid from doing
         * the same in case the Screen has restored in this {@link StateContainer.ADD_KEYWORD_FINISH}
         * state, because our previous filter had no effect.
         *
         * Additional check {@link GroupListPresenter#inputGroupBundle} != null is required to distinguish
         * between restoring to this state in the following two cases:
         *
         * - there is input GroupBundle associated with input KeywordBundle
         * - there is no such association
         *
         * The former case is filtered here, preventing the same Keyword to be added twice both into
         * expandable list and repository.
         *
         * The latter case isn't filtered here, because we need to propagate the whole pipeline adding
         * new Keyword and creating new GroupBundle (in repository), associated with input KeywordBundle.
         * But we are in the process of restoring state, so we had previously added such Keyword
         * to the input KeywordBundle, and we must not do it again. This is why 'alreadyContainsKeyword'
         * flag is involved here.
         */
        Keyword keyword = memento.newlyAddedKeyword;
        boolean alreadyContainsKeyword = memento.inputKeywordBundle.keywords().contains(keyword);
        if (alreadyContainsKeyword && inputGroupBundle != null) {
            Timber.d("Keyword [%s] has already been added", keyword.keyword());
            assignState(StateContainer.GROUPS_LOADED);  // get to the final state
            return;
        }

        // disable swipe-to-refresh while add keyword is in progress
        if (isViewAttached()) {
            getView().enableSwipeToRefresh(false);
            // make expandable list view visible while adding new Parent list item, if it has been previously hidden
            if (!getView().isContentViewVisible(GroupListFragment.RV_TAG)) {
                getView().showContent(GroupListFragment.RV_TAG, false);
            }
        }

        sendEnableAddKeywordButtonRequest(false);  // disable add keyword button while adding new Keyword

        // show progress list item while adding new Keyword with Group-s
        listAdapter.setAddingNewItem(true, keyword);

        memento.addKeywordFinishedResult = result;  // set result flag to use in 'memento' restoration

        if (result) {
            Timber.d("Adding Keyword and requesting more Group-s from network");
            if (!alreadyContainsKeyword) {
                // add new Keyword to the head of input KeywordBundle to preserve ordering
                memento.inputKeywordBundle.keywords().add(0, keyword);
                isKeywordBundleChanged = true;
            } else {
                Timber.d("Restoring to state [%s], but already added Keyword to input KeywordBundle (before saving state)", memento.state);
            }

            GroupParentItem item = new GroupParentItem(keyword);
            groupParentItems.add(0, item);  // add new item on top of the list

            // prepare parameters to make new request for Group-s by Keyword
            List<Keyword> keywords = new ArrayList<>();
            keywords.add(keyword);
            memento.isAddingNewKeyword = true;  // to manipulate with newly fetched Group-s properly

            // fetch Group-s by newly added Keyword from the endpoint
            vkontakteEndpoint.getGroupsByKeywordsSplit(keywords, createGetGroupsByKeywordsListCallback(), createCancelCallback());
        } else {
            Timber.d("Failed to add Keyword, but just warn user via popup");
            memento.newlyAddedKeyword = null;  // drop temporary Keyword
            memento.isAddingNewKeyword = false;  // don't re-use state control flag (it is false already)

            // go to standard pipeline, but just assume that no Group-s were loaded
            stateGroupsLoaded(inputGroupBundle, new ArrayList<>());

            sendAddKeywordError();  // notify about failure
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
        postGroupBundleUpdate();  // TODO: not sync with onActivityResult()
        if (!shouldDeleteEmptyCreatedKeywordBundle()) {
            postKeywordBundleUpdate();  // TODO: not sync with onActivityResult()
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /**
         * Persist some important parameters for use-cases. These ids are assigned to {@link UseCase}
         * instances in some states (referenced as {@link StateContainer.State} values), but then used
         * in some other states, so they could be lost during save-restore state.
         *
         * The whole parameters of {@link UseCase} aren't stored, because such parameters will be
         * built again right before execution of the use-case (by implementation of this Screen).
         */
        memento.useCaseParameter_addKeywordToBundle_id = addKeywordToBundleUseCase.getKeywordBundleId();
        memento.useCaseParameter_getKeywordBundleById_id = getKeywordBundleByIdUseCase.getKeywordBundleId();
        memento.toBundle(outState);
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
    public void receiveAskForRetryPost() {
        refreshPost();
    }

    @Override
    public void receiveNewTitle(String newTitle) {
        if (memento.inputKeywordBundle != null) {
            isKeywordBundleChanged = true;
            postKeywordBundleTitleUpdate(newTitle);
        }

        if (inputGroupBundle != null) {
            isGroupBundleChanged = true;
            postGroupBundleTitleUpdate(newTitle);
        }
    }

    @Override
    public void receiveOnBackPressedNotification() {
        if (shouldDeleteEmptyCreatedKeywordBundle()) {
            /**
             * User is leaving or pausing GroupListScreen w/o any changes performed on newly created
             * empty KeywordBundle, so it must be deleted from repository.
             */
            Timber.d("Deleting empty KeywordBundle from repository, because it wasn't changed and hence - not needed at all");
            deleteKeywordBundleUseCase.setKeywordBundleId(memento.inputKeywordBundle.id());
            deleteKeywordBundleUseCase.execute();  // silent delete without callback
        }
    }

    @Override
    public void receivePostHasChangedRequest(long postId) {
        getPostByIdUseCase.setPostId(postId);
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
    public void sendAlreadyAddedKeyword(String keyword) {
        mediatorComponent.mediator().sendAlreadyAddedKeyword(keyword);
    }

    @Override
    public String sendAskForTitle() {
        return mediatorComponent.mediator().sendAskForTitle();
    }

    @Override
    public boolean sendAskForTitleChanged() {
        return mediatorComponent.mediator().sendAskForTitleChanged();
    }

    @Override
    public void sendEnableAddKeywordButtonRequest(boolean isEnabled) {
        mediatorComponent.mediator().sendEnableAddKeywordButtonRequest(isEnabled);
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
    public void sendPostingFailed() {
        mediatorComponent.mediator().sendPostingFailed();
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
        memento.inputKeywordBundle.setSelectedGroupsCount(newCount);
        memento.inputKeywordBundle.setTotalGroupsCount(total);
        isKeywordBundleChanged = true;  // as counters have been changed
        mediatorComponent.mediator().sendUpdatedSelectedGroupsCounter(newCount, total);
    }

    @Override
    public void sendUpdateTitleRequest(String newTitle) {
        mediatorComponent.mediator().sendUpdateTitleRequest(newTitle);
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        stateStart();
    }

    @Override
    protected void onRestoreState() {
        memento = Memento.fromBundle(savedInstanceState);
        applyPost(memento.currentPost);  // restore Post

        // restore important parameters for use-cases
        addKeywordToBundleUseCase.setKeywordBundleId(memento.useCaseParameter_addKeywordToBundle_id);
        getKeywordBundleByIdUseCase.setKeywordBundleId(memento.useCaseParameter_getKeywordBundleById_id);

        chainedStateRestore = StateContainer.NONE;  // this flag is used to perform chained state transitions

        Timber.d("Restore to state: %s", memento.state);
        switch (memento.state) {
            case StateContainer.ERROR_LOAD:              stateErrorLoad(); break;
            case StateContainer.START:                   stateStart(); break;
            case StateContainer.KEYWORDS_CREATE_START:   stateKeywordBundleCreateStart(); break;
            case StateContainer.KEYWORDS_CREATE_FINISH:  stateKeywordBundleCreateFinish(memento.inputKeywordBundle); break;
            case StateContainer.KEYWORDS_LOADED:         stateKeywordsLoaded(memento.inputKeywordBundle); break;

            /**
             * When we restore to any of {@link StateContainer#ADD_KEYWORD_START} or
             * {@link StateContainer#ADD_KEYWORD_FINISH} states, we should preliminary restore
             * expandable list items and then restore GroupBundle from repository via
             * {@link GroupListPresenter#restoreLoadedGroups(long)} method call in order to receive
             * correct input GroupBundle before add new Keyword to it, as ADD_KEYWORD_* states indicate.
             *
             * If we don't restore list and input GroupBundle in such manner, we will get an empty list
             * as visual bug and then we will get {@link IllegalStateException} thrown from
             * {@link KeywordBundle#setGroupBundleId(long)} method, because input GroupBundle is null
             * (i.e. {@link GroupListPresenter#inputGroupBundle} field is null) and fetching new Group-s
             * from endpoint during adding new Keyword will try to create (PUT) new GroupBundle in repository
             * and then assign it's id to an existing input KeywordBundle, which already has proper id set.
             *
             * ADD_KEYWORD_* states assume that input GroupBundle either already exists or it doesn't.
             * In the latter case, it will be created (PUT) in repository with Group-s, corresponding
             * to the Keyword being added.
             */
            case StateContainer.ADD_KEYWORD_START:
                if (memento.inputGroupBundleId == Constant.BAD_ID) {  // input GroupBundle wasn't exist before
                    Timber.d("input GroupBundle wasn't exist before, add new Keyword and then GroupBundle will be created in repository");
                    stateAddKeywordStart(memento.newlyAddedKeyword);
                    break;
                } else {
                    Timber.d("Fallback to restore existing GroupBundle before adding new Keyword to it");
                    chainedStateRestore = memento.state;
                }
                // proceed further, without break
            case StateContainer.ADD_KEYWORD_FINISH:
                if (memento.inputGroupBundleId == Constant.BAD_ID) {  // input GroupBundle wasn't exist before
                    Timber.d("input GroupBundle wasn't exist before, add new Keyword and then GroupBundle will be created in repository");
                    stateAddKeywordFinish(memento.addKeywordFinishedResult);
                    break;
                } else {
                    Timber.d("Fallback to restore existing GroupBundle before adding new Keyword to it");
                    chainedStateRestore = memento.state;
                }
                // proceed further, without break
            case StateContainer.REFRESHING:  // refreshing is just discarded, restore GroupBundle and then allow user to refresh manually
            case StateContainer.GROUPS_LOADED:
                /**
                 * First - clear and fill Parent items in expandable list, because it must be ready
                 * to receive Child items - {@link Group} instances - restored on the next step.
                 */
                restoreVisual();  // restore expandable list filling and screen title
                /**
                 * {@link GroupListPresenter#inputGroupBundle} is too heavy to be stored in {@link Bundle},
                 * but it's id in repository could be used to restore the whole model in memory and then
                 * fill expandable list with corresponding Child items.
                 */
                restoreLoadedGroups(memento.inputGroupBundleId);  // id must be valid, not BAD_ID
                break;
            default:
                Timber.e("Unreachable state: %s", memento.state);
                throw new ProgramException();
        }
    }

    @DebugLog
    private void restoreVisual() {
        groupParentItems.clear();  // idempotent operation
        listAdapter.clear();  // idempotent operation
        sendUpdateTitleRequest(memento.inputKeywordBundle.title());
        fillKeywordsList(memento.inputKeywordBundle);
    }

    @DebugLog
    private void restoreLoadedGroups(long groupBundleId) {
        Timber.i("restoreLoadedGroups: %s", groupBundleId);
        getGroupBundleByIdUseCase.setGroupBundleId(groupBundleId);  // set proper id
        getGroupBundleByIdUseCase.execute();
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
                (!memento.fetchedInputGroupBundleFromRepo || memento.isAddingNewKeyword || memento.isRefreshing);

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

    @DebugLog
    private void addKeyword(Keyword keyword) {
        Timber.i("addKeyword: %s", keyword.keyword());
        if (memento.inputKeywordBundle.keywords().size() < Constant.KEYWORDS_LIMIT) {
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

        listAdapter.notifyParentDataSetChanged(true);
    }

    // ------------------------------------------
    @DebugLog
    private void postToGroups() {
        Timber.i("postToGroups");
        if (memento.currentPost != null) {
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

            if (isViewAttached()) {
                getView().startWallPostingService(memento.inputKeywordBundle.id(), selectedGroups, memento.currentPost);
                if (AppConfig.INSTANCE.useInteractiveReportScreen()) {
                    Timber.d("Open ReportScreen in interactive mode showing posting progress");
                    getView().openInteractiveReportScreen(memento.inputKeywordBundle.id(), memento.currentPost.id());
                    // TODO: report screen could subscribe for posting-progress callback
                    // TODO:        too late, missing some early reports
                } else {
                    Timber.d("Show popup with wall posting progress");
                    getView().openStatusScreen();
                }
            } else {
                Timber.w("Unable to start Wall Posting Service: view isn't attached!");
            }
        } else {
            Timber.d("No Post was selected, send warning");
            sendPostNotSelected();
        }
    }

    // ------------------------------------------
    private void postGroupBundleUpdate() {
        Timber.i("postGroupBundleUpdate: %s", isGroupBundleChanged);
        postGroupBundleUpdate(null);
    }

    private void postGroupBundleTitleUpdate(String title) {
        Timber.i("postGroupBundleTitleUpdate: %s, changed = %s", title, isGroupBundleChanged);
        postGroupBundleUpdate(title);
    }

    private void postGroupBundleUpdate(String title) {
        if (isGroupBundleChanged) {
            Timber.d("Input GroupBundle has been changed, it will be updated in repository");
            isGroupBundleChanged = false;
            PostGroupBundle.Parameters parameters;
            if (TextUtils.isEmpty(title)) {
                parameters = new PostGroupBundle.Parameters(inputGroupBundle);
            } else {
                parameters = new PostGroupBundle.Parameters(inputGroupBundle.id(), title);
            }
            postGroupBundleUseCase.setParameters(parameters);
            postGroupBundleUseCase.execute();  // silent update without callback
            sendGroupBundleChanged();
        } else {
            Timber.d("Input GroupBundle wasn't changed");
        }
    }

    private void postKeywordBundleUpdate() {
        Timber.i("postKeywordBundleUpdate: %s", isKeywordBundleChanged);
        postKeywordBundleUpdate(null);
    }

    private void postKeywordBundleTitleUpdate(String title) {
        Timber.i("postKeywordBundleTitleUpdate: %s, changed = %s", title, isKeywordBundleChanged);
        postKeywordBundleUpdate(title);
    }

    private void postKeywordBundleUpdate(String title) {
        if (isKeywordBundleChanged) {
            Timber.d("Input KeywordBundle has been changed, it will be updated in repository");
            isKeywordBundleChanged = false;
            PostKeywordBundle.Parameters parameters;
            if (TextUtils.isEmpty(title)) {
                parameters = new PostKeywordBundle.Parameters(memento.inputKeywordBundle);
            } else {
                parameters = new PostKeywordBundle.Parameters(memento.inputKeywordBundle.id(), title);
            }
            postKeywordBundleUseCase.setParameters(parameters);
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
        GroupChildItem childItem = item.getChildList().get(position);
        if (childItem.isSelected()) --totalSelectedGroups;
        --totalGroups;
        sendUpdatedSelectedGroupsCounter(totalSelectedGroups, totalGroups);

        Collection<Group> groupsToRemove = new ArrayList<>();
        groupsToRemove.add(childItem.getGroup());
        inputGroupBundle.groups().removeAll(groupsToRemove);
        isGroupBundleChanged = true;
        postGroupBundleUpdate();  // refresh now, don't wait till screen closed

        item.getChildList().remove(position);
        item.incrementSelectedCount(childItem.isSelected() ? -1 : 0);
        listAdapter.notifyChildRemoved(keywordPosition, position);
        listAdapter.notifyParentChanged(keywordPosition);  // must follow 'notifyChildRemoved'
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
        memento.inputKeywordBundle.keywords().remove(keyword);
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

    // ------------------------------------------
    private boolean shouldDeleteEmptyCreatedKeywordBundle() {
        return memento.wasInputKeywordBundleCreated && !sendAskForTitleChanged() && memento.inputKeywordBundle.keywords().isEmpty();
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
                long keywordBundleId = getKeywordBundleByIdUseCase.getKeywordBundleId();
                if (keywordBundleId != Constant.BAD_ID && bundle == null) {
                    Timber.e("KeywordBundle wasn't found by id: %s", keywordBundleId);
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get KeywordBundle by id");
                if (bundle != null) {
                    Timber.d("Found existing KeywordBundle with id [%s]", keywordBundleId);
                    stateKeywordsLoaded(bundle);
                } else {  // bundle is null and id is BAD
                    Timber.d("New KeywordBundle instance will be created on GroupListScreen");
                    stateKeywordBundleCreateStart();
                }
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
                memento.currentPost = post;
                applyPost(post);
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get Post by id");
                memento.currentPost = null;  // post has been dropped
                sendErrorPost();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<KeywordBundle> createPutKeywordBundleCallback() {
        return new UseCase.OnPostExecuteCallback<KeywordBundle>() {
            @Override
            public void onFinish(@Nullable KeywordBundle bundle) {
                if (bundle == null) {
                    Timber.e("Failed to put new KeywordBundle to repository - item not created, as expected");
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to put KeywordBundle");
                stateKeywordBundleCreateFinish(bundle);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to put KeywordBundle");
                stateErrorLoad();
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
                memento.inputKeywordBundle.setGroupBundleId(bundle.id());
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
                    getView().onReportReady(bundle.id(), memento.inputKeywordBundle.id(), memento.currentPost.id());
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
                    if (AppConfig.INSTANCE.isAllGroupsSelected()) {
                        for (Group group : groups) group.setSelected(true);
                    }
                    PutGroupBundle.Parameters parameters = new PutGroupBundle.Parameters.Builder()
                            .setGroups(groups)
                            .setKeywordBundleId(memento.inputKeywordBundle.id())
                            .setTitle("title")  // TODO: set group-bundle title from Toolbar
                            .build();
                    putGroupBundleUseCase.setParameters(parameters);
                    putGroupBundleUseCase.execute();
                } else {
                    Timber.d("Refresh already existing GroupBundle in repository");
                    if (memento.isAddingNewKeyword) {
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
                            .setKeywordBundleId(memento.inputKeywordBundle.id())
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
                    stateGroupsLoaded(bundle, splitGroups);  // fetched Group-s from endpoint

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

    private MultiUseCase.CancelCallback createCancelCallback() {
        return (reason) -> {
            Timber.i("Searching for Group-s has been cancelled");
            if (isViewAttached()) {
                if (EndpointUtility.hasAccessTokenExhausted(reason)) {
                    Timber.w("Access Token has exhausted !");
                    getView().onAccessTokenExhausted();
                } else {
                    getView().onSearchingGroupsCancel();
                }
            }
        };
    }

    private UseCase.OnPostExecuteCallback<List<GroupReportEssence>> createMakeWallPostCallback() {
        return new UseCase.OnPostExecuteCallback<List<GroupReportEssence>>() {
            @DebugLog @Override
            public void onFinish(@Nullable List<GroupReportEssence> reports) {
                Timber.i("Use-Case: succeeded to make wall posting");
                PutGroupReportBundle.Parameters parameters = new PutGroupReportBundle.Parameters(
                        reports, memento.inputKeywordBundle.id(), memento.currentPost.id());
                putGroupReportBundleUseCase.setParameters(parameters);
                putGroupReportBundleUseCase.execute();
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

    /* Utility */
    // --------------------------------------------------------------------------------------------
    private void applyPost(Post post) {
        if (post != null) {
            sendPost(postToSingleGridVoMapper.map(post));
        } else {
            sendEmptyPost();
        }
    }
}
