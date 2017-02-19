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
import com.orcchg.vikstra.app.ui.viewobject.ReportListItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.GroupReportEssenceToVoMapper;
import com.orcchg.vikstra.app.ui.viewobject.mapper.GroupReportToVoMapper;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.data.source.direct.vkontakte.VkontakteEndpoint;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.exception.vkontakte.VkUseCaseExceptionFactory;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundleById;
import com.orcchg.vikstra.domain.interactor.post.GetPostById;
import com.orcchg.vikstra.domain.interactor.report.DumpGroupReports;
import com.orcchg.vikstra.domain.interactor.report.GetGroupReportBundleById;
import com.orcchg.vikstra.domain.interactor.report.PostGroupReportBundle;
import com.orcchg.vikstra.domain.interactor.report.PutGroupReportBundle;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.Heavy;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.model.essense.mapper.GroupReportEssenceMapper;
import com.orcchg.vikstra.domain.model.misc.EmailContent;
import com.orcchg.vikstra.domain.util.Constant;
import com.orcchg.vikstra.domain.util.endpoint.EndpointUtility;
import com.orcchg.vikstra.domain.util.file.FileUtility;
import com.vk.sdk.api.VKError;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class ReportPresenter extends BaseListPresenter<ReportContract.View> implements ReportContract.Presenter {
    private static final int PrID = Constant.PresenterId.REPORT_PRESENTER;

    private final GetGroupReportBundleById getGroupReportBundleByIdUseCase;
    private final GetKeywordBundleById getKeywordBundleByIdUseCase;
    private final GetPostById getPostByIdUseCase;
    private final DumpGroupReports dumpGroupReportsUseCase;
    private final PostGroupReportBundle postGroupReportBundleUseCase;
    private final PutGroupReportBundle putGroupReportBundleUseCase;
    private final VkontakteEndpoint vkontakteEndpoint;

    private final GroupReportToVoMapper groupReportToVoMapper;
    private final GroupReportEssenceMapper groupReportEssenceMapper;
    private final GroupReportEssenceToVoMapper groupReportEssenceToVoMapper;
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
            private static final int POSTING_PROGRESS = 1;
            private static final int POSTING_CANCEL = 2;
            private static final int POSTING_FINISH = 3;
            private static final int PAUSE = 4;
            private static final int RESUME = 5;
            private static final int INTERRUPT = 6;

            @IntDef({
                BEGIN,
                POSTING_PROGRESS,
                POSTING_CANCEL,
                POSTING_FINISH,
                PAUSE, RESUME, INTERRUPT
            })
            @Retention(RetentionPolicy.SOURCE)
            @interface State {}
        }
    }

    // --------------------------------------------------------------------------------------------
    private static final class MementoCommon {
        private @Nullable String email;
        private long keywordBundleId = Constant.BAD_ID;
//        int postedWithCancel = 0;
//        int postedWithFailure = 0;
//        int postedWithSuccess = 0;
//        int totalForPosting = 0;
    }

    // ------------------------------------------
    @InteractiveMode
    private static final class MementoInteractive {
        private @StateContainer.Interactive.State int state = StateContainer.Interactive.BEGIN;

        boolean isFinishedPosting;
        boolean isWallPostingPaused;
    }

    // ------------------------------------------
    @NormalMode
    private static final class MementoNormal {
        private @StateContainer.Normal.State int state = StateContainer.Normal.START;

        private Post currentPost;
    }

//    @Deprecated
//    private static final class Memento {
//        private static final String BUNDLE_KEY_EMAIL = "bundle_key_email_" + PrID;
//        private static final String BUNDLE_KEY_FLAG_IS_FINISHED_POSTING = "bundle_key_flag_is_finished_posting_" + PrID;
//        private static final String BUNDLE_KEY_FLAG_IS_WALL_POSTING_PAUSED = "bundle_key_flag_is_wall_posting_paused_" + PrID;
//        private static final String BUNDLE_KEY_FLAG_POSTED_WITH_CANCEL = "bundle_key_flag_posted_with_cancel_" + PrID;
//        private static final String BUNDLE_KEY_FLAG_POSTED_WITH_FAILURE = "bundle_key_flag_posted_with_failure_" + PrID;
//        private static final String BUNDLE_KEY_FLAG_POSTED_WITH_SUCCESS = "bundle_key_flag_posted_with_success_" + PrID;
//        private static final String BUNDLE_KEY_FLAG_TOTAL_FOR_POSTING = "bundle_key_flag_total_for_posting_" + PrID;
//        private static final String BUNDLE_KEY_STORED_REPORTS_ID = "bundle_key_stored_reports_id_" + PrID;
//        private static final String BUNDLE_KEY_KEYWORD_BUNDLE_ID = "bundle_key_keyword_bundle_id_" + PrID;
//        private static final String BUNDLE_KEY_CURRENT_POST = "bundle_key_current_post_" + PrID;
//        private static final String BUNDLE_KEY_SERVICE_GRB_ID = "bundle_key_service_grb_id_" + PrID;
//        private static final String BUNDLE_KEY_SERVICE_GRB_TS = "bundle_key_service_grb_ts_" + PrID;
//
//        @InteractiveMode boolean isFinishedPosting;
//        @InteractiveMode boolean isWallPostingPaused;
//        @InteractiveMode int postedWithCancel = 0;
//        @InteractiveMode int postedWithFailure = 0;
//        @InteractiveMode int postedWithSuccess = 0;
//        @InteractiveMode int totalForPosting = 0;
//        @InteractiveMode long storedReportsId = Constant.BAD_ID;
//
//        /**
//         * This field corresponds to id of {@link GroupReportBundle} actually created and stored to
//         * repository by {@link WallPostingService}, same for timestamp value.
//         */
//        @InteractiveMode long serviceGroupReportBundleId = Constant.BAD_ID;
//        @InteractiveMode long serviceGroupReportBundleTimestamp = 0;
//
//        private @Nullable String email;
//        private long keywordBundleId = Constant.BAD_ID;
//        private Post currentPost;
//
//        @DebugLog
//        private void toBundle(Bundle outState) {
//            outState.putBoolean(BUNDLE_KEY_FLAG_IS_FINISHED_POSTING, isFinishedPosting);
//            outState.putBoolean(BUNDLE_KEY_FLAG_IS_WALL_POSTING_PAUSED, isWallPostingPaused);
//            outState.putInt(BUNDLE_KEY_FLAG_POSTED_WITH_CANCEL, postedWithCancel);
//            outState.putInt(BUNDLE_KEY_FLAG_POSTED_WITH_FAILURE, postedWithFailure);
//            outState.putInt(BUNDLE_KEY_FLAG_POSTED_WITH_SUCCESS, postedWithSuccess);
//            outState.putInt(BUNDLE_KEY_FLAG_TOTAL_FOR_POSTING, totalForPosting);
//            outState.putLong(BUNDLE_KEY_STORED_REPORTS_ID, storedReportsId);
//            outState.putString(BUNDLE_KEY_EMAIL, email);
//            outState.putLong(BUNDLE_KEY_KEYWORD_BUNDLE_ID, keywordBundleId);
//            outState.putParcelable(BUNDLE_KEY_CURRENT_POST, currentPost);
//            outState.putLong(BUNDLE_KEY_SERVICE_GRB_ID, serviceGroupReportBundleId);
//            outState.putLong(BUNDLE_KEY_SERVICE_GRB_TS, serviceGroupReportBundleTimestamp);
//        }
//
//        @DebugLog
//        private static Memento fromBundle(Bundle savedInstanceState) {
//            Memento memento = new Memento();
//            memento.isFinishedPosting = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_IS_FINISHED_POSTING, false);
//            memento.isWallPostingPaused = savedInstanceState.getBoolean(BUNDLE_KEY_FLAG_IS_WALL_POSTING_PAUSED, false);
//            memento.postedWithCancel = savedInstanceState.getInt(BUNDLE_KEY_FLAG_POSTED_WITH_CANCEL, 0);
//            memento.postedWithFailure = savedInstanceState.getInt(BUNDLE_KEY_FLAG_POSTED_WITH_FAILURE, 0);
//            memento.postedWithSuccess = savedInstanceState.getInt(BUNDLE_KEY_FLAG_POSTED_WITH_SUCCESS, 0);
//            memento.totalForPosting = savedInstanceState.getInt(BUNDLE_KEY_FLAG_TOTAL_FOR_POSTING, 0);
//            memento.storedReportsId = savedInstanceState.getLong(BUNDLE_KEY_STORED_REPORTS_ID, Constant.BAD_ID);
//            memento.email = savedInstanceState.getString(BUNDLE_KEY_EMAIL);
//            memento.keywordBundleId = savedInstanceState.getLong(BUNDLE_KEY_KEYWORD_BUNDLE_ID, Constant.BAD_ID);
//            memento.currentPost = savedInstanceState.getParcelable(BUNDLE_KEY_CURRENT_POST);
//            memento.serviceGroupReportBundleId = savedInstanceState.getLong(BUNDLE_KEY_SERVICE_GRB_ID, Constant.BAD_ID);
//            memento.serviceGroupReportBundleTimestamp = savedInstanceState.getLong(BUNDLE_KEY_SERVICE_GRB_TS, 0);
//            return memento;
//        }
//    }

    // --------------------------------------------------------------------------------------------
    @Inject
    ReportPresenter(GetGroupReportBundleById getGroupReportBundleByIdUseCase,
                    GetKeywordBundleById getKeywordBundleByIdUseCase, GetPostById getPostByIdUseCase,
                    DumpGroupReports dumpGroupReportsUseCase, PostGroupReportBundle postGroupReportBundleUseCase,
                    PutGroupReportBundle putGroupReportBundleUseCase, VkontakteEndpoint vkontakteEndpoint,
                    GroupReportToVoMapper groupReportToVoMapper, GroupReportEssenceMapper groupReportEssenceMapper,
                    GroupReportEssenceToVoMapper groupReportEssenceToVoMapper,
                    PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.listAdapter = createListAdapter();
        this.getGroupReportBundleByIdUseCase = getGroupReportBundleByIdUseCase;
        this.getGroupReportBundleByIdUseCase.setPostExecuteCallback(createGetGroupReportBundleByIdCallback());
        this.getKeywordBundleByIdUseCase = getKeywordBundleByIdUseCase;  // this use-case never executes
        this.getPostByIdUseCase = getPostByIdUseCase;
        this.getPostByIdUseCase.setPostExecuteCallback(createGetPostByIdCallback());
        this.dumpGroupReportsUseCase = dumpGroupReportsUseCase;
        this.dumpGroupReportsUseCase.setPostExecuteCallback(createDumpGroupReportsCallback());
        this.postGroupReportBundleUseCase = postGroupReportBundleUseCase;  // no callback - background task
        this.putGroupReportBundleUseCase = putGroupReportBundleUseCase;  // no callback - background task
        this.vkontakteEndpoint = vkontakteEndpoint;
        this.groupReportToVoMapper = groupReportToVoMapper;
        this.groupReportEssenceMapper = groupReportEssenceMapper;
        this.groupReportEssenceToVoMapper = groupReportEssenceToVoMapper;
        this.postToSingleGridVoMapper = postToSingleGridVoMapper;

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
     *                            START ----- < ------ < ----- < ----- < ---- ERROR_LOAD  { user retry }
     *                              |                                              |
     *                              |                                              |
     *                       REPORTS_LOADED  or  ---- > ---- > ----- > ---- > ---- #
     *                          |       |
     *                          |       ^
     *                          |       |
     * { user refresh }  REFRESHING ->--|------------------- < ------------ < ---- #
     *                                                                             |
     *                                                                             |
     * { user delete posts }  DELETE_REPORTS_START -->-- DELETE_REPORTS_FINISH ----|-- > -- DELETE_REPORTS_ERROR
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
     * Go to START state, drop all previous values and prepare to fresh start
     */
    @NormalMode
    private void stateStart() {
        Timber.i("stateStart");
        setNormalState(StateContainer.Normal.START);
        // enter START state logic

        inputGroupReportBundle = null;
        mementoNormal.currentPost = null;
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
                        .setPostId(mementoNormal.currentPost.id())
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

    // ------------------------------------------
    /**
     * Go to REFRESHING state, reloading GroupReportBundle from repository
     */
    @NormalMode
    private void stateRefreshing() {
        Timber.i("stateRefreshing");
        setNormalState(StateContainer.Normal.REFRESHING);
        // enter REFRESHING state logic

        // disable swipe-to-refresh while another refreshing is in progress
        if (isViewAttached()) {
            getView().enableSwipeToRefresh(false);
            getView().showLoading(getListTag());
        }
    }

    // --------------------------------------------------------------------------------------------
    /**
     * State machine (Interactive mode):
     *
     *                          BEGIN
     *                            |
     *                            |
     *    # --- > --- > -- POSTING_PROGRESS ---- > ---- > ---- > ---- #
     *    |                       |                                   |
     *    |                       |                                   |
     *    |                POSTING_FINISH                      POSTING_CANCEL  { user interrupt }
     *    |                       |                                   |
     *    |                       |                                   |
     *    |                       |                                   |
     *    # ---- < ---- #         # ----- > ----- > ------ > -------- # ------ > ---- REPORTS_LOADED
     *                  |                                                           { to normal mode }
     *                  |                                                                    |
     *   PAUSE -->-- RESUME  { user pause / resume }                                         ^
     *                                                                                       |
     *                                                                                       |
     *   { user interrupt }  INTERRUPT  --------- > ------ > ----------------- > ----------- #
     */

    @DebugLog @InteractiveMode
    private void setInteractiveState(@StateContainer.Interactive.State int newState) {
        @StateContainer.Interactive.State int previousState = mementoInteractive.state;
        Timber.i("Previous state [%s], New state: %s", previousState, newState);

        // check consistency between state transitions
        if ((previousState != StateContainer.Interactive.BEGIN && previousState != StateContainer.Interactive.RESUME
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
     * Go to BEGIN state, prepare to receive incoming items
     */
    @InteractiveMode
    private void stateBegin() {
        Timber.i("stateBegin");
        setInteractiveState(StateContainer.Interactive.BEGIN);
        // enter BEGIN state logic

        mementoInteractive.isFinishedPosting = false;
        mementoInteractive.isWallPostingPaused = false;
    }

    // ------------------------------------------
    /**
     * Go to POSTING_PROGRESS state, parse essence and make GroupReport item from it, add this item
     * to list in reversed order and update counters.
     */
    @InteractiveMode @SuppressWarnings("unchecked")
    private void statePostingProgress(int success, int failure, int cancel, int total, GroupReportEssence model) {
        Timber.i("statePostingProgress");
        setInteractiveState(StateContainer.Interactive.POSTING_PROGRESS);
        // enter POSTING_PROGRESS state logic

        mementoCommon.totalForPosting = total;  // save counter to use further

        Timber.v("Posting stat: success [%s], failure [%s], cancel [%s], total [%s]", success, failure, cancel, total);
        // TODO: not properly counted if there are retry-failed use-cases
        if (!mementoInteractive.isFinishedPosting) {
            mementoInteractive.isFinishedPosting = cancel + failure + success == total;
        }

        ReportListItemVO viewObject = groupReportEssenceToVoMapper.map(model);
        listAdapter.addInverse(viewObject);  // add items on top of the list
        if (isViewAttached()) {
            getView().showGroupReports(false);  // idempotent call (no-op if list items are already visible)
            getView().updatePostedCounters(success, total);
            getView().getListView(getListTag()).smoothScrollToPosition(0);  // scroll on top of the list as items are incoming
            // TODO: estimate time to complete posting, use DomainConfig.INSTANCE.multiUseCaseSleepInterval
        }

        long timestamp = System.currentTimeMillis();
        groupReportEssenceMapper.setGroupReportId(Constant.INIT_ID);  // fictive id
        groupReportEssenceMapper.setTimestamp(timestamp);
        storedReports.add(groupReportEssenceMapper.map(model));
    }

    // ------------------------------------------
    /**
     * Go to POSTING_FINISH state, notify posting finished and show popup with counters
     */
    @InteractiveMode
    private void statePostingFinish() {
        Timber.i("statePostingFinish");
        setInteractiveState(StateContainer.Interactive.POSTING_FINISH);
        // enter POSTING_FINISH state logic

        mementoInteractive.isFinishedPosting = true;

        if (isViewAttached()) {
            getView().onPostingFinished(mementoCommon.postedWithSuccess, mementoCommon.totalForPosting);
        }

        Timber.i("Posting has been finished");
    }

    // ------------------------------------------
    /**
     * Go to POSTING_CANCEL state, notify posting cancelled and check access token
     */
    @InteractiveMode
    private void statePostingCancel(Throwable reason) {
        Timber.i("statePostingCancel");
        setInteractiveState(StateContainer.Interactive.POSTING_CANCEL);
        // enter POSTING_CANCEL state logic

        mementoInteractive.isFinishedPosting = true;

        if (EndpointUtility.hasAccessTokenExhausted(reason)) {
            Timber.w("Access Token has exhausted !");
            if (isViewAttached()) getView().onAccessTokenExhausted();
        } else {
            if (isViewAttached()) getView().onPostingCancel();
        }

        Timber.i("Posting has been cancelled");
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

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isInteractiveMode()) {
            // get id reserved for the item to store in repository next
            memento.storedReportsId = putGroupReportBundleUseCase.getReservedId();
            // put everything available in 'storedReports' to repository
            List<GroupReportEssence> essences = groupReportEssenceMapper.mapBack(storedReports);  // 'id' and 'timestamp' are ignored
            PutGroupReportBundle.Parameters parameters = new PutGroupReportBundle.Parameters(
                    essences, memento.keywordBundleId, memento.currentPost.id());
            putGroupReportBundleUseCase.setParameters(parameters);
            putGroupReportBundleUseCase.execute();  // silent create without callback
        }
        memento.toBundle(outState);
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

    @Override
    public void onPostingCancel() {
        statePostingCancel(null);  // TODO: reason properly
    }

    @Override
    public void onPostingFinish() {
        statePostingFinish();
    }

    @InteractiveMode @Override
    public void onPostingResult(int success, int failure, int cancel, int total, GroupReportEssence model) {
        statePostingProgress(success, failure, cancel, total, model);
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

    @InteractiveMode @Override
    public void interruptPostingAndClose(boolean shouldClose) {
        Timber.i("interruptPostingAndClose: %s", shouldClose);
        stateInterrupt(shouldClose);
    }

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
//        /**
//         * Currently there are two different, but similar {@link GroupReportBundle} items - one from
//         * {@link WallPostingService} - created and then put to repository - and potential item,
//         * which will be composed from {@link ReportPresenter.Memento#storedReports} and then put to
//         * repository with {@link ReportPresenter.Memento#storedReportsId} when ReportScreen will be
//         * in the middle of destruction-restoration process.
//         *
//         * Any reverting performed here will affect the latter {@link GroupReportBundle} and won't
//         * have any effect on the former. But user could still open notification and see the former
//         * item loaded to ReportScreen, missing all revert-statuses. To avoid that, we dismiss notification.
//         */
//        if (isViewAttached()) getView().cancelPreviousNotifications();
//
//        List<GroupReport> successReports = new ArrayList<>();
//        for (GroupReport report : storedReports) {  // only successful posts can be reverted
//            if (report.status() == GroupReport.STATUS_SUCCESS) successReports.add(report);
//        }
//        if (successReports.isEmpty()) {
//            // TODO: exclude success reports from 'storeReports' after revert
//            if (isViewAttached()) getView().onPostRevertingEmpty();
//        } else {
//            if (isViewAttached()) getView().onPostRevertingStarted();
//            vkontakteEndpoint.deleteWallPosts(successReports, createDeleteWallPostsCallback());
//        }
    }

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
//        if (isViewAttached()) getView().showLoading(getListTag());
//        /**
//         * Load GroupReportBundle from repository either if we aren't in interactive mode or we are
//         * in such mode, but have also restored everything from repository that we had previously
//         * managed to store. So, now it is allowed to swipe-to-refresh (by default) and this will lead
//         * {@link ReportPresenter#retry()} method to be invoked, and then {@link ReportPresenter#freshStart()},
//         * and we will get here, but still in interactive mode - so, we just retry and reload items
//         * from repository.
//         */
//        if (!isInteractiveMode() || isStateRestored()) {
//            getGroupReportBundleByIdUseCase.execute();
//        } else if (isViewAttached()) {
//            // disable swipe-to-refresh when GroupReport-s are coming interactively
//            getView().enableSwipeToRefresh(false);
//        }
//        getPostByIdUseCase.execute();
    }

    @Override
    protected void onRestoreState() {
        memento = Memento.fromBundle(savedInstanceState);
        if (isInteractiveMode()) {
            /**
             * Restore Post in interactive mode. In standard mode {@link ReportPresenter#freshStart()}
             * will be invoked and Post will be re-loaded from repository. So, this line is placed here
             * inside if-statement in order not to re-load Post twice.
             */
            applyPost(memento.currentPost);  // restore Post

            // restore all those GroupReport-s from repository that we had managed to store.
            memento.isFinishedPosting = true;  // assume posting has finished on state restore
            if (isViewAttached()) getView().enableButtonsOnPostingFinished();

            getGroupReportBundleByIdUseCase.setGroupReportId(memento.storedReportsId);
            getGroupReportBundleByIdUseCase.execute();
        } else {
            freshStart();  // nothing to be restored
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
    @SuppressWarnings("unchecked")
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
//                    if (isInteractiveMode()) {
//                        /**
//                         * In interactive mode items are incoming from the oldest to the recents,
//                         * this order is reverse to the order these items are stored in GroupReportBundle.
//                         *
//                         * We can reach this place in interactive mode only when ReportScreen is restored
//                         * and hence fetched all it's data from repository, where order is direct.
//                         * So, in order to keep user experience, we must populate list with restored
//                         * items in reversed order.
//                         */
//                        listAdapter.populateInverse(vos, false);  // items was restored from repository in interactive mode
//                    } else {
//                        listAdapter.populate(vos, false);
//                    }
//
//                    /**
//                     * Populate {@link ReportPresenter#storedReports} when get GroupReportBundle
//                     * use-case has finished with data inside. In interactive mode, this is only
//                     * possible when the entire ReportScreen is restored after destruction, i.e.
//                     * {@link ReportPresenter#onRestoreState()} is the only place where
//                     * {@link GetGroupReportBundleById} use-case is executed.
//                     */
//                    storedReports.clear();
//                    storedReports.addAll(bundle.groupReports());
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
                mementoNormal.currentPost = post;
                applyPost(post);
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get Post by id");
                mementoNormal.currentPost = null;  // post has been dropped
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
//                int totalReverted = 0;
//                int position = 0;
//                for (GroupReport report : storedReports) {  // only successful posts can be reverted
//                    if (report.status() == GroupReport.STATUS_SUCCESS) {
//                        ++totalReverted;
//                        report.setReverted(true);
//                        int xposition = position;
//                        if (isInteractiveMode()) {
//                            // in interactive mode items in list adapter have reversed order
//                            xposition = storedReports.size() - 1 - position;
//                        }
//                        ((ReportAdapter) listAdapter).setItemRevertedSilent(xposition, true);
//                    }
//                    ++position;
//                }
//                if (totalReverted > 0) {
//                    Timber.d("Total reverted successful posts: %s", totalReverted);
//                    listAdapter.notifyDataSetChanged();  // visual changes
//                    if (!isInteractiveMode() || isStateRestored()) {
//                        Timber.d("Update GroupReport-s in repository in non-interactive mode or after state restored");
//                        GroupReportBundle bundle = GroupReportBundle.builder()
//                                .setId(inputGroupReportBundle.id())
//                                .setGroupReports(storedReports)
//                                .setKeywordBundleId(memento.keywordBundleId)
//                                .setPostId(memento.currentPost.id())
//                                .setTimestamp(inputGroupReportBundle.timestamp())
//                                .build();
//                        postGroupReportBundleUseCase.setParameters(new PostGroupReportBundle.Parameters(bundle));
//                        postGroupReportBundleUseCase.execute();  // silent update without callback
//                    }
//                }
//                if (isViewAttached()) {
//                    getView().onPostRevertingFinished();
//                    getView().updatePostedCounters(0, memento.totalForPosting);
//                }
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
        boolean flag = isViewAttached() && getView().isForceDisableInteractiveMode();
        return !flag;
    }

    @SuppressWarnings("unchecked")
    private void fillReportsList(@NonNull GroupReportBundle bundle) {
        List<ReportListItemVO> vos = groupReportToVoMapper.map(bundle.groupReports());
        listAdapter.populate(vos, false);
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
