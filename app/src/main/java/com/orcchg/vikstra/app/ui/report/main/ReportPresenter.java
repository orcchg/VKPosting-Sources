package com.orcchg.vikstra.app.ui.report.main;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orcchg.vikstra.app.AppConfig;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.report.service.WallPostingService;
import com.orcchg.vikstra.app.ui.viewobject.ReportListItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.GroupReportToVoMapper;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundleById;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.report.DumpGroupReports;
import com.orcchg.vikstra.domain.interactor.report.GetGroupReportBundleById;
import com.orcchg.vikstra.domain.interactor.report.PostGroupReportBundle;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.Heavy;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.misc.EmailContent;
import com.orcchg.vikstra.domain.model.misc.PostingUnit;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.endpoint.EndpointUtility;
import com.orcchg.vikstra.domain.util.file.FileUtility;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class ReportPresenter extends BaseListPresenter<ReportContract.View> implements ReportContract.Presenter {
    private static final int PrID = Constant.PresenterId.REPORT_PRESENTER;

    private final GetGroupReportBundleById getGroupReportBundleByIdUseCase;
    private final GetPostById getPostByIdUseCase;
    private final DumpGroupReports dumpGroupReportsUseCase;
    private final PostGroupReportBundle postGroupReportBundleUseCase;
    private final VkontakteEndpoint vkontakteEndpoint;

    private final GroupReportToVoMapper groupReportToVoMapper;
    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    private @InteractiveMode @Heavy List<GroupReport> storedReports = new ArrayList<>();
    private @Heavy GroupReportBundle inputGroupReportBundle;  // used only in non-interactive mode

    private MementoCommon mementoCommon = new MementoCommon();
    private MementoInteractive mementoInteractive = new MementoInteractive();
    private MementoNormal mementoNormal = new MementoNormal();

    // --------------------------------------------------------------------------------------------
    private static final class StateContainer {
        @NormalMode
        private static final class Normal {
            private static final int ERROR_LOAD = -1;
            private static final int START = 0;
            private static final int REPORTS_LOADED = 1;
            private static final int DELETE_REPORTS_START = 2;
            private static final int DELETE_REPORTS_FINISH = 3;
            private static final int DELETE_REPORTS_ERROR = 4;
            private static final int REFRESHING = 5;

            @IntDef({
                ERROR_LOAD,
                START,
                REPORTS_LOADED,
                DELETE_REPORTS_START,
                DELETE_REPORTS_FINISH,
                DELETE_REPORTS_ERROR,
                REFRESHING
            })
            @Retention(RetentionPolicy.SOURCE)
            @interface State {}
        }

        @InteractiveMode
        private static final class Interactive {
            private static final int BEGIN = 0;
            private static final int READY = 1;
            private static final int POSTING_PROGRESS = 2;
            private static final int POSTING_CANCEL = 3;
            private static final int POSTING_FINISH = 4;
            private static final int PAUSE = 5;
            private static final int RESUME = 6;
            private static final int INTERRUPT = 7;
            private static final int FETCH_REPORTS = 8;

            @IntDef({
                BEGIN, READY,
                POSTING_PROGRESS,
                POSTING_CANCEL,
                POSTING_FINISH,
                PAUSE, RESUME, INTERRUPT,
                FETCH_REPORTS
            })
            @Retention(RetentionPolicy.SOURCE)
            @interface State {}
        }
    }

    // --------------------------------------------------------------------------------------------
    private static final class MementoCommon {
        private static final String BUNDLE_KEY_FLAG_IS_INTERACTIVE_MODE = "bundle_key_flag_is_interactive_mode_" + PrID;
        private static final String BUNDLE_KEY_EMAIL = "bundle_key_email_" + PrID;
        private static final String BUNDLE_KEY_KEYWORD_BUNDLE_ID = "bundle_key_keyword_bundle_id_" + PrID;
        private static final String BUNDLE_KEY_CURRENT_POST = "bundle_key_current_post_" + PrID;
        private static final String BUNDLE_KEY_FLAG_POSTED_WITH_CANCEL = "bundle_key_flag_posted_with_cancel_" + PrID;
        private static final String BUNDLE_KEY_FLAG_POSTED_WITH_FAILURE = "bundle_key_flag_posted_with_failure_" + PrID;
        private static final String BUNDLE_KEY_FLAG_POSTED_WITH_SUCCESS = "bundle_key_flag_posted_with_success_" + PrID;
        private static final String BUNDLE_KEY_FLAG_TOTAL_FOR_POSTING = "bundle_key_flag_total_for_posting_" + PrID;

        private boolean isInteractiveMode = true;  // this flag may change over time

        private @Nullable String email;
        private long keywordBundleId = Constant.BAD_ID;
        private Post currentPost;
        int postedWithCancel = 0;
        int postedWithFailure = 0;
        int postedWithSuccess = 0;
        int totalForPosting = 0;

        @DebugLog
        private void toBundle(Bundle outState) {
            outState.putBoolean(BUNDLE_KEY_FLAG_IS_INTERACTIVE_MODE, isInteractiveMode);
            outState.putString(BUNDLE_KEY_EMAIL, email);
            outState.putLong(BUNDLE_KEY_KEYWORD_BUNDLE_ID, keywordBundleId);
            outState.putParcelable(BUNDLE_KEY_CURRENT_POST, currentPost);
            outState.putInt(BUNDLE_KEY_FLAG_POSTED_WITH_CANCEL, postedWithCancel);
            outState.putInt(BUNDLE_KEY_FLAG_POSTED_WITH_FAILURE, postedWithFailure);
            outState.putInt(BUNDLE_KEY_FLAG_POSTED_WITH_SUCCESS, postedWithSuccess);
            outState.putInt(BUNDLE_KEY_FLAG_TOTAL_FOR_POSTING, totalForPosting);
        }

        @DebugLog
        private static MementoCommon fromBundle(Bundle savedInstanceState) {
            MementoCommon memento = new MementoCommon();
            memento.isInteractiveMode = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_IS_INTERACTIVE_MODE, false);
            memento.email = savedInstanceState.getString(BUNDLE_KEY_EMAIL);
            memento.keywordBundleId = savedInstanceState.getLong(BUNDLE_KEY_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
            memento.currentPost = savedInstanceState.getParcelable(BUNDLE_KEY_CURRENT_POST);
            memento.postedWithCancel = savedInstanceState.getInt(BUNDLE_KEY_FLAG_POSTED_WITH_CANCEL, 0);
            memento.postedWithFailure = savedInstanceState.getInt(BUNDLE_KEY_FLAG_POSTED_WITH_FAILURE, 0);
            memento.postedWithSuccess = savedInstanceState.getInt(BUNDLE_KEY_FLAG_POSTED_WITH_SUCCESS, 0);
            memento.totalForPosting = savedInstanceState.getInt(BUNDLE_KEY_FLAG_TOTAL_FOR_POSTING, 0);
            return memento;
        }
    }

    // ------------------------------------------
    @InteractiveMode
    private static final class MementoInteractive {
        private static final String BUNDLE_KEY_STATE_INTERACTIVE = "bundle_key_state_interactive_" + PrID;
        private static final String BUNDLE_KEY_FLAG_IS_FINISHED_POSTING = "bundle_key_flag_is_finished_posting_" + PrID;
        private static final String BUNDLE_KEY_FLAG_IS_WALL_POSTING_PAUSED = "bundle_key_flag_is_wall_posting_paused_" + PrID;

        private @StateContainer.Interactive.State int state = StateContainer.Interactive.BEGIN;
        boolean isFinishedPosting;
        boolean isWallPostingPaused;

        @DebugLog
        private void toBundle(Bundle outState) {
            outState.putInt(BUNDLE_KEY_STATE_INTERACTIVE, state);
            outState.putBoolean(BUNDLE_KEY_FLAG_IS_FINISHED_POSTING, isFinishedPosting);
            outState.putBoolean(BUNDLE_KEY_FLAG_IS_WALL_POSTING_PAUSED, isWallPostingPaused);
        }

        @DebugLog @SuppressWarnings("ResourceType")
        private static MementoInteractive fromBundle(Bundle savedInstanceState) {
            MementoInteractive memento = new MementoInteractive();
            memento.state = savedInstanceState.getInt(BUNDLE_KEY_STATE_INTERACTIVE, StateContainer.Interactive.BEGIN);
            memento.isFinishedPosting = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_IS_FINISHED_POSTING, false);
            memento.isWallPostingPaused = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_IS_WALL_POSTING_PAUSED, false);
            return memento;
        }
    }

    // ------------------------------------------
    @NormalMode
    private static final class MementoNormal {
        private static final String BUNDLE_KEY_STATE_NORMAL = "bundle_key_state_normal_" + PrID;

        private @StateContainer.Normal.State int state = StateContainer.Normal.START;

        @DebugLog
        private void toBundle(Bundle outState) {
            outState.putInt(BUNDLE_KEY_STATE_NORMAL, state);
        }

        @DebugLog @SuppressWarnings("ResourceType")
        private static MementoNormal fromBundle(Bundle savedInstanceState) {
            MementoNormal memento = new MementoNormal();
            memento.state = savedInstanceState.getInt(BUNDLE_KEY_STATE_NORMAL, StateContainer.Normal.START);
            return memento;
        }
    }

    // --------------------------------------------------------------------------------------------
    @Inject
    ReportPresenter(Holder holder, GetGroupReportBundleById getGroupReportBundleByIdUseCase,
                    GetKeywordBundleById getKeywordBundleByIdUseCase, GetPostById getPostByIdUseCase,
                    DumpGroupReports dumpGroupReportsUseCase, PostGroupReportBundle postGroupReportBundleUseCase,
                    VkontakteEndpoint vkontakteEndpoint,
                    GroupReportToVoMapper groupReportToVoMapper, PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.listAdapter = createListAdapter();
        this.getGroupReportBundleByIdUseCase = getGroupReportBundleByIdUseCase;
        this.getGroupReportBundleByIdUseCase.setPostExecuteCallback(createGetGroupReportBundleByIdCallback());
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.dumpGroupReportsUseCase = dumpGroupReportsUseCase;
        this.dumpGroupReportsUseCase.setPostExecuteCallback(createDumpGroupReportsCallback());
        this.postGroupReportBundleUseCase = postGroupReportBundleUseCase;  // no callback - background task
        this.vkontakteEndpoint = vkontakteEndpoint;
        this.groupReportToVoMapper = groupReportToVoMapper;
        this.postToSingleGridVoMapper = postToSingleGridVoMapper;

        mementoCommon.isInteractiveMode = holder.isInteractiveMode();
        mementoCommon.keywordBundleId = getKeywordBundleByIdUseCase.getKeywordBundleId();  // use-case only to provide value
    }

    @Override
    protected BaseAdapter createListAdapter() {
        ReportAdapter adapter = new ReportAdapter();
        adapter.setOnItemClickListener((view, viewObject, position) -> {
            if (isViewAttached()) getView().openGroupDetailScreen(viewObject.groupId());
        });
        adapter.setOnErrorClickListener((view) -> retryLoadMore());
        return adapter;
    }

    @Override
    protected int getListTag() {
        return ReportFragment.RV_TAG;
    }

    /* State */
    // --------------------------------------------------------------------------------------------
    /**
     * State machine (Normal mode):
     *
     *          { user refresh }  START ----- < ------ < ----- < ----- < ---- ERROR_LOAD  { user retry }
     *                              |                                              |
     *                              |                                              |
     *                       REPORTS_LOADED  or  ---- > ---- > ----- > ---- > ---- #
     *                              |
     *                              ^
     *                              |
     *                              # ------- < ---- < ----- < ------------ < ---- #
     *                                                                             |
     *                                                                             |
     * { user delete posts }  DELETE_REPORTS_START -->-- DELETE_REPORTS_FINISH --- # -- > -- DELETE_REPORTS_ERROR
     */

    @DebugLog @NormalMode
    private void setNormalState(@StateContainer.Normal.State int newState) {
        @StateContainer.Normal.State int previousState = mementoNormal.state;
        Timber.i("Previous state [%s], New state: %s", previousState, newState);

        // check consistency between state transitions
        if (previousState == StateContainer.Normal.ERROR_LOAD && newState != StateContainer.Normal.START ||
            // forbid transition from any kind of loading to refreshing
            (previousState != StateContainer.Normal.REPORTS_LOADED && previousState != StateContainer.Normal.REFRESHING)
                    && newState == StateContainer.Normal.REFRESHING) {
            Timber.e("Illegal state transition from [%s] to [%s]", previousState, newState);
            throw new IllegalStateException(String.format(Locale.ENGLISH, "Transition from %s to %s", previousState, newState));
        }

        mementoNormal.state = newState;
    }

    @DebugLog @NormalMode
    private void assignNormalState(@StateContainer.Normal.State int newState) {
        Timber.d("assignNormalState: %s", newState);
        setNormalState(newState);  // verbose call
    }

    // ------------------------------------------
    /**
     * Go to ERROR_LOAD state, when some critical data was not loaded
     */
    @NormalMode
    private void stateErrorLoad() {
        Timber.i("stateErrorLoad");
        setNormalState(StateContainer.Normal.ERROR_LOAD);
        // enter ERROR_LOAD state logic

        if (isViewAttached()) getView().showError(getListTag());
    }

    // ------------------------------------------
    /**
     * Go to START state, drop all previous values, load input GroupReportBundle and Post
     */
    @NormalMode
    private void stateStart() {
        Timber.i("stateStart");
        setNormalState(StateContainer.Normal.START);
        // enter START state logic

        inputGroupReportBundle = null;
        freshClearAndPrepare();

        // fresh start - load input GroupReportBundle and Post
        getGroupReportBundleByIdUseCase.execute();
        getPostByIdUseCase.execute();
    }

    // ------------------------------------------
    /**
     * Go to REPORTS_LOADED state, assign input GroupReportBundle and fill list with items
     */
    @NormalMode
    private void stateReportsLoaded(@NonNull GroupReportBundle bundle) {
        Timber.i("stateReportsLoaded");
        setNormalState(StateContainer.Normal.REPORTS_LOADED);
        // enter REPORTS_LOADED state logic

        inputGroupReportBundle = bundle;  // assign input GroupReportBundle

        // fill items in list
        List<GroupReport> reports = bundle.groupReports();
        listAdapter.clear();  // idempotent operation
        fillReportsList(bundle);

        int[] counters = bundle.statusCount();

        // enable swipe-to-refresh after GroupReportBundle loaded, show GroupReport-s in list
        if (isViewAttached()) {
            getView().enableSwipeToRefresh(true);
            getView().showGroupReports(reports.isEmpty());
            getView().updatePostedCounters(counters[GroupReport.STATUS_SUCCESS], bundle.groupReports().size());
        }
    }

    // ------------------------------------------
    /**
     * Go to DELETE_REPORTS_START state, start reverting all reports
     */
    @NormalMode
    private void stateDeleteReportsStart() {
        Timber.i("stateDeleteReportsStart");
        setNormalState(StateContainer.Normal.DELETE_REPORTS_START);
        // enter DELETE_REPORTS_START state logic

        runDeletionOverAllSuccessReports(inputGroupReportBundle.groupReports());
    }

    // ------------------------------------------
    /**
     * Go to DELETE_REPORTS_FINISH state, refresh list items with reverted status and go to idle
     */
    @NormalMode
    private void stateDeleteReportsFinished(boolean result) {
        Timber.i("stateDeleteReportsFinished");
        setNormalState(StateContainer.Normal.DELETE_REPORTS_FINISH);
        // enter DELETE_REPORTS_FINISH state logic

        if (result) {
            List<GroupReport> storedReports = inputGroupReportBundle.groupReports();
            int totalReverted = setListItemsReverted(storedReports);
            if (totalReverted > 0) {
                Timber.d("Update GroupReport-s in repository in non-interactive mode or after state restored");
                GroupReportBundle bundle = GroupReportBundle.builder()
                        .setId(inputGroupReportBundle.id())
                        .setGroupReports(storedReports)
                        .setKeywordBundleId(mementoCommon.keywordBundleId)
                        .setPostId(mementoCommon.currentPost.id())
                        .setTimestamp(inputGroupReportBundle.timestamp())
                        .build();
                postGroupReportBundleUseCase.setParameters(new PostGroupReportBundle.Parameters(bundle));
                postGroupReportBundleUseCase.execute();  // silent update without callback
            }
            if (isViewAttached()) {
                getView().onPostRevertingFinished();
                getView().updatePostedCounters(0, mementoCommon.totalForPosting);
            }
        }

        assignNormalState(StateContainer.Normal.REPORTS_LOADED);  // get to the final state
    }

    // ------------------------------------------
    /**
     * Go to DELETE_REPORTS_ERROR state, show notification and go to idle
     */
    @NormalMode
    private void stateDeleteReportsError() {
        Timber.i("stateDeleteReportsError");
        setNormalState(StateContainer.Normal.DELETE_REPORTS_ERROR);
        // enter DELETE_REPORTS_ERROR sate logic

        if (isViewAttached()) getView().onPostRevertingError();

        assignNormalState(StateContainer.Normal.REPORTS_LOADED);  // get to the final state
    }

    // --------------------------------------------------------------------------------------------
    /**
     * State machine (Interactive mode):
     *
     *                          BEGIN
     *                            |
     *                            |
     *                          READY  { ready to receive incoming data }
     *                            |
     *                            |
     *                            |
     *    # --- > --- > -- POSTING_PROGRESS ---- > ---- > ---- > ---- #
     *    |                       |                                   |
     *    |                       |                                   |
     *    |                POSTING_FINISH                      POSTING_CANCEL  { user interrupt }
     *    |                       |                                   |
     *    |                       |                                   |
     *    |                       |                                   |
     *    # ---- < ---- #         # ----- > ----- > ------ #          |                      REPORTS_LOADED
     *                  |                                  |          |                    { to normal mode }
     *                  |                                  |          |                             |
     *   PAUSE -->-- RESUME  { user pause / resume }       # --- > -- # --- FETCH_REPORTS --- > --- #
     *                                                                |  { to normal mode }
     *                                                                |
     *   { user interrupt }  INTERRUPT  --------- > ------ > -------- #
     */

    @DebugLog @InteractiveMode
    private void setInteractiveState(@StateContainer.Interactive.State int newState) {
        @StateContainer.Interactive.State int previousState = mementoInteractive.state;
        Timber.i("Previous state [%s], New state: %s", previousState, newState);

        // check consistency between state transitions
        if ((previousState != StateContainer.Interactive.BEGIN && newState == StateContainer.Interactive.READY) ||
            (previousState != StateContainer.Interactive.READY && previousState != StateContainer.Interactive.RESUME
                    && previousState != StateContainer.Interactive.POSTING_PROGRESS
                    && newState == StateContainer.Interactive.POSTING_PROGRESS)/* ||
            (previousState != StateContainer.Interactive.POSTING_CANCEL && previousState != StateContainer.Interactive.POSTING_FINISH
                    && newState == StateContainer.Normal.REPORTS_LOADED)*/) {
            Timber.e("Illegal state transition from [%s] to [%s]", previousState, newState);
            throw new IllegalStateException(String.format(Locale.ENGLISH, "Transition from %s to %s", previousState, newState));
        }

        mementoInteractive.state = newState;
    }

    // ------------------------------------------
    /**
     * Go to BEGIN state, drop all previous values and load Post
     */
    @InteractiveMode
    private void stateBegin() {
        Timber.i("stateBegin");
        setInteractiveState(StateContainer.Interactive.BEGIN);
        // enter BEGIN state logic

        mementoInteractive.isFinishedPosting = false;
        mementoInteractive.isWallPostingPaused = false;
        storedReports.clear();
        freshClearAndPrepare();

        getPostByIdUseCase.execute();  // fresh start - load Post

        stateReady();  // proceed to the next state - READY
    }

    // ------------------------------------------
    /**
     * Go to READY state, prepare to receive incoming items
     */
    @InteractiveMode
    private void stateReady() {
        Timber.i("stateReady");
        setInteractiveState(StateContainer.Interactive.READY);
        // enter READY state logic
    }

    // ------------------------------------------
    /**
     * Go to POSTING_PROGRESS state, parse essence and make GroupReport item from it, add this item
     * to list in reversed order and update counters.
     */
    @InteractiveMode
    private void statePostingProgress(PostingUnit postingUnit) {
        Timber.i("statePostingProgress");
        setInteractiveState(StateContainer.Interactive.POSTING_PROGRESS);
        // enter POSTING_PROGRESS state logic

        deployPostingUnit(postingUnit);  // deploy next incoming item
    }

    // ------------------------------------------
    /**
     * Go to POSTING_FINISH state, notify posting finished and show popup with counters
     */
    @InteractiveMode
    private void statePostingFinish(long groupReportBundleId) {
        Timber.i("statePostingFinish");
        setInteractiveState(StateContainer.Interactive.POSTING_FINISH);
        // enter POST_FINISH state logic

        mementoInteractive.isFinishedPosting = true;

        if (isViewAttached()) {
            getView().onPostingFinished(mementoCommon.postedWithSuccess, mementoCommon.totalForPosting);
        }

        Timber.i("Posting has been finished");
        stateFetchReports(groupReportBundleId);  // proceed to the next state - FETCH_REPORTS
    }

    // ------------------------------------------
    /**
     * Go to POSTING_CANCEL state, notify posting cancelled and check access token
     */
    @InteractiveMode
    private void statePostingCancel(int apiErrorCode, long groupReportBundleId) {
        Timber.i("statePostingCancel");
        setInteractiveState(StateContainer.Interactive.POSTING_CANCEL);
        // enter POSTING_CANCEL state logic

        mementoInteractive.isFinishedPosting = true;

        if (EndpointUtility.hasAccessTokenExhausted(apiErrorCode)) {
            Timber.w("Access Token has exhausted !");
            if (isViewAttached()) getView().onAccessTokenExhausted();
        } else {
            if (isViewAttached()) getView().onPostingCancel();
        }

        Timber.i("Posting has been cancelled");
        stateFetchReports(groupReportBundleId);  // proceed to the next state - FETCH_REPORTS
    }

    // ------------------------------------------
    /**
     * Go to PAUSE state, notify posting paused
     */
    @InteractiveMode
    private void statePause() {
        Timber.i("statePause");
        setInteractiveState(StateContainer.Interactive.PAUSE);
        // enter PAUSE state logic

        mementoInteractive.isWallPostingPaused = true;

        if (isViewAttached()) getView().onWallPostingSuspend(true);
    }

    // ------------------------------------------
    /**
     * Go to RESUME state, notify posting resumed
     */
    @InteractiveMode
    private void stateResume() {
        Timber.i("stateResume");
        setInteractiveState(StateContainer.Interactive.RESUME);
        // enter RESUME state logic

        mementoInteractive.isWallPostingPaused = false;

        if (isViewAttached()) getView().onWallPostingSuspend(false);
    }

    // ------------------------------------------
    /**
     * Go to INTERRUPT state, notify posting terminated and shutdown thread pool executor
     */
    @InteractiveMode
    private void stateInterrupt(boolean shouldClose) {
        Timber.i("stateInterrupt: %s", shouldClose);
        setInteractiveState(StateContainer.Interactive.INTERRUPT);
        // enter INTERRUPT state logic

        if (mementoInteractive.isFinishedPosting) return;  // no-op if no posting is in progress

        if (isInteractiveMode()) {
            /**
             * Setting this flag we disable warning popup on back pressed. But this will be set in
             * anyway in {@link ReportPresenter#createPostingFinishedCallback()} callback.
             */
            mementoInteractive.isFinishedPosting = true;

            ApplicationComponent component = getApplicationComponent();
            if (component != null) component.threadExecutor().shutdownNow();
        }

        if (isViewAttached()) {
            getView().onWallPostingInterrupt();
            if (shouldClose) getView().closeView();
        }
    }

    // ------------------------------------------
    /**
     * Go to FETCH_REPORTS state, load GroupReportBundle stored to repository by Service
     */
    @InteractiveMode
    private void stateFetchReports(long groupReportBundleId) {
        Timber.i("stateFetchReports");
        setInteractiveState(StateContainer.Interactive.FETCH_REPORTS);
        // enter FETCH_REPORTS state logic

        // fetch GroupReportBundle stored to repository by Service when it has finished
        getGroupReportBundleByIdUseCase.setGroupReportId(groupReportBundleId);
        getGroupReportBundleByIdUseCase.execute();
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isInteractiveMode()) {
            mementoInteractive.toBundle(outState);
        } else {
            mementoNormal.toBundle(outState);
        }
        mementoCommon.toBundle(outState);
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onCloseView() {
        Timber.i("onCloseView");
        if (isViewAttached()) {
            if (isInteractiveMode() && !mementoInteractive.isFinishedPosting) {
                getView().openCloseWhilePostingDialog();
            } else {
                getView().closeView();
            }
        }
    }

    /**
     * @Interactive_mode
     * {@link GroupReport} instances are incoming over time and stored in {@link ReportPresenter#storedReports}
     * collection. This collection will be dumped into file when wall posting process finishes.
     *
     * @Standard_mode
     * {@link GroupReportBundle} has already been recorded to repository and fetched as ReportScreen
     * is shown. It will be dumped directly from repository to file at any moment when the user
     * clicks the button.
     */
    @Override
    public void onDumpPressed() {
        Timber.i("onDumpPressed");
        boolean notReady = true;
        if (isInteractiveMode()) {
            if (mementoInteractive.isFinishedPosting && !storedReports.isEmpty()) {
                dumpGroupReportsUseCase.setParameters(new DumpGroupReports.Parameters(storedReports));
                notReady = false;
            }
        } else {
            long groupReportBundleId = getGroupReportBundleByIdUseCase.getGroupReportId();
            if (groupReportBundleId != Constant.BAD_ID) {
                Timber.d("GroupReportBundle id [%s] is valid, ready to dump", groupReportBundleId);
                dumpGroupReportsUseCase.setParameters(new DumpGroupReports.Parameters(groupReportBundleId));
                notReady = false;
            }
        }

        if (notReady) {
            Timber.d("GroupReportBundle is not available to dump");
            if (isViewAttached()) getView().openDumpNotReadyDialog();
        } else if (isViewAttached()) {
            switch (AppConfig.INSTANCE.sendDumpFilesVia()) {
                case AppConfig.SEND_DUMP_FILE:
                    Timber.d("Dumping GroupReportBundle to file...");
                    getView().openEditDumpFileNameDialog();
                    break;
                case AppConfig.SEND_DUMP_EMAIL:
                    Timber.d("Sending GroupReportBundle to email...");
                    getView().openEditDumpEmailDialog();
                    break;
                case AppConfig.SEND_DUMP_SHARE:
                    Timber.d("Sharing GroupReportBundle...");
                    performDumping(getView().getDumpFilename());
                    break;
            }
        }
    }

    @InteractiveMode @Override
    public void onSuspendClick() {
        Timber.i("onSuspendClick");
        if (mementoInteractive.isWallPostingPaused) {
            stateResume();
        } else {
            statePause();
        }
    }

    // ------------------------------------------
    @Override
    public void onPostingCancel(int apiErrorCode, long groupReportBundleId) {
        statePostingCancel(apiErrorCode, groupReportBundleId);
    }

    @Override
    public void onPostingFinish(long groupReportBundleId) {
        statePostingFinish(groupReportBundleId);
    }

    @DebugLog @InteractiveMode @Override
    public void onPostingProgress(PostingUnit postingUnit) {
        /**
         * Sometimes user can click suspend button in the middle of some single wall posting executes,
         * and this execution will then finish (all new ones just get suspended) finish as usual,
         * delivering it's result via callback to {@link WallPostingService} and the latter, in turn,
         * will broadcast an intent with extras here to ReportScreen. This result is 'tardy'.
         *
         * But the state of ReportScreen has previously changed to {@link StateContainer.Interactive.PAUSE},
         * as user had clicked suspend button before. Thus, any incoming intent with progress unit
         * will attempt to change state again to {@link StateContainer.Interactive.POSTING_PROGRESS},
         * which is illegal transition.
         *
         * So, we must deploy all incoming data items, if any, while ReportScreen is paused.
         * Same is true for interruption - after change to state {@link StateContainer.Interactive.INTERRUPT}.
         */
        switch (mementoInteractive.state) {
            case StateContainer.Interactive.BEGIN:
            case StateContainer.Interactive.PAUSE:
            case StateContainer.Interactive.INTERRUPT:
                Timber.w("Received tardy posting unit after suspension - deploy w/o state transition");
                deployPostingUnit(postingUnit);  // visualize item w/o state transition
                return;
        }
        statePostingProgress(postingUnit);
    }

    // ------------------------------------------
    @InteractiveMode @Override
    public void interruptPostingAndClose(boolean shouldClose) {
        Timber.i("interruptPostingAndClose: %s", shouldClose);
        stateInterrupt(shouldClose);
    }

    // ------------------------------------------
    @DebugLog @Override
    public void performDumping(String path) {
        performDumping(path, null);
    }

    @DebugLog @Override
    public void performDumping(String path, @Nullable String email) {
        Timber.i("performDumping: path=%s, email=%s", path, email);
        mementoCommon.email = email;
        dumpGroupReportsUseCase.setPath(path);
        dumpGroupReportsUseCase.execute();
    }

    @Override
    public void performReverting() {
        Timber.i("performReverting");
        stateDeleteReportsStart();
    }

    // ------------------------------------------
    @Override
    public void retry() {
        Timber.i("retry");
        stateStart();
    }

    @Override
    public void retryPost() {
        Timber.i("retryPost");
        getPostByIdUseCase.execute();
    }

    /* List */
    // ------------------------------------------
    @Override
    protected void onLoadMore() {
        // TODO: on load more
    }

    private void retryLoadMore() {
        listAdapter.onError(false); // show loading more
        // TODO: load more limit-offset
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        if (isInteractiveMode()) {
            /**
             * TODO: fresh start occurs in onStart(), but we could have already received some items
             * TODO: in onCreate() since we subscribe there. So that, there could be state-inconsistency/
             */
            stateBegin();
        } else {
            stateStart();
        }
    }

    @Override
    protected void onRestoreState() {
        mementoCommon = MementoCommon.fromBundle(savedInstanceState);
        if (isInteractiveMode()) {
            mementoInteractive = MementoInteractive.fromBundle(savedInstanceState);
            /**
             * Restore Post in interactive mode. In standard mode {@link ReportPresenter#freshStart()}
             * will be invoked and Post will be re-loaded from repository. So, this line is placed here
             * inside if-statement in order not to re-load Post twice.
             */
            applyPost(mementoCommon.currentPost);  // restore Post

            // TODO: re-attach to running service
        } else {
            mementoNormal = MementoNormal.fromBundle(savedInstanceState);
            freshStart();  // nothing to be restored
        }
    }

    // ------------------------------------------
    /**
     * Clears all content before fresh start. Used both in {@link NormalMode} and {@link InteractiveMode}.
     */
    private void freshClearAndPrepare() {
        mementoCommon.currentPost = null;
        mementoCommon.postedWithCancel  = 0;  // drop counter
        mementoCommon.postedWithFailure = 0;  // drop counter
        mementoCommon.postedWithSuccess = 0;  // drop counter
        mementoCommon.totalForPosting = 0;

        listAdapter.clear();  // idempotent operation
        dropListStat();

        // fresh start - show loading, disable swipe-to-refresh
        if (isViewAttached()) {
            getView().enableSwipeToRefresh(false);
            getView().showLoading(getListTag());
        }
    }

    /**
     * Extract data from {@link PostingUnit}, assign counters and add list item.
     */
    @SuppressWarnings("unchecked")
    private void deployPostingUnit(PostingUnit postingUnit) {
        // save counters to use further
        mementoCommon.postedWithCancel  = postingUnit.cancelCount();
        mementoCommon.postedWithFailure = postingUnit.failureCount();
        mementoCommon.postedWithSuccess = postingUnit.successCount();
        mementoCommon.totalForPosting   = postingUnit.totalCount();

        storedReports.add(postingUnit.groupReport());  // save incoming model to internal list

        ReportListItemVO viewObject = groupReportToVoMapper.map(postingUnit.groupReport());
        listAdapter.addInverse(viewObject);  // add items on top of the list

        if (isViewAttached()) {
            getView().showGroupReports(false);  // idempotent call (no-op if list items are already visible)
            getView().updatePostedCounters(postingUnit.successCount(), postingUnit.totalCount());
            getView().getListView(getListTag()).smoothScrollToPosition(0);  // scroll on top of the list as items are incoming
            // TODO: estimate time to complete posting, use DomainConfig.INSTANCE.multiUseCaseSleepInterval
        }
    }

    private void runDeletionOverAllSuccessReports(List<GroupReport> storedReports) {
        List<GroupReport> successReports = new ArrayList<>();
        for (GroupReport report : storedReports) {  // only successful posts can be reverted
            if (report.status() == GroupReport.STATUS_SUCCESS) successReports.add(report);
        }
        // TODO: exclude success reports from 'storeReports' after revert

        if (successReports.isEmpty()) {
            if (isViewAttached()) getView().onPostRevertingEmpty();
        } else {
            if (isViewAttached()) getView().onPostRevertingStarted();
            vkontakteEndpoint.deleteWallPosts(successReports, createDeleteWallPostsCallback());
        }
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<GroupReportBundle> createGetGroupReportBundleByIdCallback() {
        return new UseCase.OnPostExecuteCallback<GroupReportBundle>() {
            @DebugLog @Override
            public void onFinish(@Nullable GroupReportBundle bundle) {
                long groupReportBundleId = getGroupReportBundleByIdUseCase.getGroupReportId();
                if (bundle == null || bundle.groupReports() == null) {
                    Timber.e("GroupReportBundle wasn't found by id [%s], or groupReports property is null", groupReportBundleId);
                    throw new ProgramException();
                }
                Timber.i("Use-Case: succeeded to get GroupReportBundle by id");
                stateReportsLoaded(bundle);
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get GroupReportBundle by id");
                stateErrorLoad();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Post> createGetPostByIdCallback() {
        return new UseCase.OnPostExecuteCallback<Post>() {
            @DebugLog @Override
            public void onFinish(@Nullable Post post) {
                Timber.i("Use-Case: succeeded to get Post by id");
                mementoCommon.currentPost = post;
                applyPost(post);
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get Post by id");
                mementoCommon.currentPost = null;  // post has been dropped
                if (isViewAttached()) getView().showErrorPost();
            }
        };
    }

    private UseCase.OnPostExecuteCallback<String> createDumpGroupReportsCallback() {
        return new UseCase.OnPostExecuteCallback<String>() {
            @Override
            public void onFinish(@Nullable String path) {
                if (!TextUtils.isEmpty(path)) {
                    Timber.i("Use-Case: succeeded to dump GroupReport-s");
                    EmailContent.Builder builder = EmailContent.builder()
                            .setAttachment(FileUtility.uriFromFile(path));

                    switch (AppConfig.INSTANCE.sendDumpFilesVia()) {
                        case AppConfig.SEND_DUMP_FILE:
                            if (isViewAttached()) getView().showDumpSuccess(path);
                            break;
                        case AppConfig.SEND_DUMP_EMAIL:
                            if (!TextUtils.isEmpty(mementoCommon.email)) {
                                Timber.d("Report-s have been dumped to file [%s]. Now send it via email", path);
                                String[] recipients = mementoCommon.email.split(",");
                                builder.setRecipients(Arrays.asList(recipients));
                            }
                            // proceed opening email screen without break
                        case AppConfig.SEND_DUMP_SHARE:
                            if (isViewAttached()) getView().openEmailScreen(builder);
                            break;
                    }
                } else {
                    Timber.e("Use-Case: failed to dump GroupReport-s");
                    if (isViewAttached()) getView().showDumpError();
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to dump GroupReport-s");
                if (isViewAttached()) getView().showDumpError();
            }
        };
    }

    // ------------------------------------------
    private UseCase.OnPostExecuteCallback<Boolean> createDeleteWallPostsCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @Override
            public void onFinish(@Nullable Boolean result) {
                Timber.i("Use-Case: succeeded to delete wall Post-s");
                stateDeleteReportsFinished(result != null ? result : false);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to delete wall Post-s");
                stateDeleteReportsError();
            }
        };
    }

    /* Utility */
    // --------------------------------------------------------------------------------------------
    private void applyPost(Post post) {
        if (isViewAttached()) {
            if (post != null) {
                getView().showPost(postToSingleGridVoMapper.map(post));
            } else {
                getView().showEmptyPost();
            }
        }
    }

    private boolean isInteractiveMode() {
        return mementoCommon.isInteractiveMode;
    }

    @SuppressWarnings("unchecked")
    private void fillReportsList(@NonNull GroupReportBundle bundle) {
        List<ReportListItemVO> vos = groupReportToVoMapper.map(bundle.groupReports());
        if (isInteractiveMode()) {
            /**
             * In interactive mode items are incoming from the oldest to the recents,
             * this order is reverse to the order these items are stored in GroupReportBundle.
             *
             * So, in order to keep user experience, we must populate list with restored
             * items in reversed order.
             */
            listAdapter.populateInverse(vos, false);  // items was restored from repository in interactive mode
        } else {
            listAdapter.populate(vos, false);
        }
    }

    private int setListItemsReverted(List<GroupReport> storedReports) {
        int totalReverted = 0;
        int position = 0;
        for (GroupReport report : storedReports) {  // only successful posts can be reverted
            if (report.status() == GroupReport.STATUS_SUCCESS) {
                ++totalReverted;
                report.setReverted(true);
                int xposition = position;
                if (isInteractiveMode()) {
                    // in interactive mode items in list adapter have reversed order
                    xposition = storedReports.size() - 1 - position;
                }
                ((ReportAdapter) listAdapter).setItemRevertedSilent(xposition, true);
            }
            ++position;
        }
        if (totalReverted > 0) {
            Timber.d("Total reverted successful posts: %s", totalReverted);
            listAdapter.notifyDataSetChanged();  // visual changes
        }
        return totalReverted;
    }
}
