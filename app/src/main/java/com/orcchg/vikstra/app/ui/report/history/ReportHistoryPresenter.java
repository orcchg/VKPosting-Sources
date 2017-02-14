package com.orcchg.vikstra.app.ui.report.history;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BaseListPresenter;
import com.orcchg.vikstra.app.ui.base.adapter.BaseAdapter;
import com.orcchg.vikstra.app.ui.viewobject.ReportHistoryListItemVO;
import com.orcchg.vikstra.app.ui.viewobject.mapper.PostToSingleGridVoMapper;
import com.orcchg.vikstra.domain.exception.ProgramException;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.keyword.GetSpecificKeywordBundles;
import com.orcchg.vikstra.domain.interactor.post.GetSpecificPosts;
import com.orcchg.vikstra.domain.interactor.report.GetGroupReportBundles;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.Heavy;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.util.Constant;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class ReportHistoryPresenter extends BaseListPresenter<ReportHistoryContract.View>
        implements ReportHistoryContract.Presenter {
    private static final int PrID = Constant.PresenterId.REPORT_HISTORY_PRESENTER;

    private final GetGroupReportBundles getGroupReportBundlesUseCase;
    private final GetSpecificKeywordBundles getSpecificKeywordBundlesUseCase;
    private final GetSpecificPosts getSpecificPostsUseCase;

    private final PostToSingleGridVoMapper postToSingleGridVoMapper;

    private @Heavy List<GroupReportBundle> groupReportBundles = new ArrayList<>();
    private @Heavy List<KeywordBundle> keywordBundles = new ArrayList<>();
    private @Heavy List<Post> posts = new ArrayList<>();

    private Memento memento = new Memento();

    // --------------------------------------------------------------------------------------------
    private static final class StateContainer {
        private static final int ERROR_LOAD = -1;
        private static final int START = 0;
        private static final int GROUP_REPORTS_LOADED = 1;
        private static final int KEYWORDS_LOADED = 2;
        private static final int POSTS_LOADED = 3;
        private static final int IDLE = 4;
        private static final int REFRESHING = 5;

        @IntDef({
            ERROR_LOAD,
            START,
            GROUP_REPORTS_LOADED,
            KEYWORDS_LOADED,
            POSTS_LOADED,
            IDLE,
            REFRESHING
        })
        @Retention(RetentionPolicy.SOURCE)
        private @interface State {}
    }

    private static final int ERROR_GROUP_REPORTS = 0;
    private static final int ERROR_KEYWORDS = 1;
    private static final int ERROR_POSTS = 2;
    @IntDef({ERROR_GROUP_REPORTS, ERROR_KEYWORDS, ERROR_POSTS})
    @Retention(RetentionPolicy.SOURCE)
    private @interface ErrorType {}

    // --------------------------------------------------------------------------------------------
    private static final class Memento {
        private static final String BUNDLE_KEY_STATE = "bundle_key_state_" + PrID;

        private @StateContainer.State int state = StateContainer.START;

        @DebugLog
        private void toBundle(Bundle outState) {
            outState.putInt(BUNDLE_KEY_STATE, state);
        }

        @DebugLog @SuppressWarnings("ResourceType")
        private static Memento fromBundle(Bundle savedInstanceState) {
            Memento memento = new Memento();
            memento.state = savedInstanceState.getInt(BUNDLE_KEY_STATE, StateContainer.START);
            return memento;
        }
    }

    // --------------------------------------------------------------------------------------------
    @Inject
    ReportHistoryPresenter(GetGroupReportBundles getGroupReportBundlesUseCase,
                           GetSpecificKeywordBundles getSpecificKeywordBundlesUseCase,
                           GetSpecificPosts getSpecificPostsUseCase,
                           PostToSingleGridVoMapper postToSingleGridVoMapper) {
        this.listAdapter = createListAdapter();
        this.getGroupReportBundlesUseCase = getGroupReportBundlesUseCase;
        this.getGroupReportBundlesUseCase.setPostExecuteCallback(createGetGroupReportBundlesCallback());
        this.getSpecificKeywordBundlesUseCase = getSpecificKeywordBundlesUseCase;
        this.getSpecificKeywordBundlesUseCase.setPostExecuteCallback(createGetSpecificKeywordBundlesCallback());
        this.getSpecificPostsUseCase = getSpecificPostsUseCase;
        this.getSpecificPostsUseCase.setPostExecuteCallback(createGetSpecificPostsCallback());
        this.postToSingleGridVoMapper = postToSingleGridVoMapper;
    }

    @Override
    protected BaseAdapter createListAdapter() {
        ReportHistoryAdapter adapter = new ReportHistoryAdapter();
        adapter.setOnItemClickListener((view, viewObject, position) -> {
            long groupReportBundleId = groupReportBundles.get(position).id();
            long keywordBundleId = groupReportBundles.get(position).keywordBundleId();
            long postId = groupReportBundles.get(position).postId();
            if (isViewAttached()) getView().openReportScreen(groupReportBundleId, keywordBundleId, postId);
        });
        adapter.setOnPostClickListener((view, viewObject, position) -> {
            long postId = groupReportBundles.get(position).postId();
            if (isViewAttached()) getView().openPostViewScreen(postId);
        });
        adapter.setOnErrorClickListener((view) -> retryLoadMore());
        return adapter;
    }

    @Override
    protected int getListTag() {
        return ReportHistoryFragment.RV_TAG;
    }

    /* State */
    // --------------------------------------------------------------------------------------------
    /**
     * State machine:
     *
     *  # ----- > ----- > ------ START ----- < ------ < ----- < ----- < ---- ERROR_LOAD  { user retry }
     *  |                          |                                              |
     *  |                          |                                              |
     *  |                GROUP_REPORTS_LOADED  ----- > ----- >   or   ----- > --- #
     *  |                          |                                              |
     *  |                          |                                              |
     *  |                  KEYWORDS_LOADED     ----- > ----- >   or   ----- > --- #
     *  |                          |                                              |
     *  |                          |                                              |
     *  |                     POSTS_LOADED     ----- > ----- >   or   ----- > --- #
     *  |                          |
     *  |                          |
     *  |                        IDLE
     *  |                          |
     *  |                          |
     *  # ----- < ----- < ---- REFRESHING  { user refresh }
     */

    @DebugLog
    private void setState(@StateContainer.State int newState) {
        @StateContainer.State int previousState = memento.state;
        Timber.i("Previous state [%s], New state: %s", previousState, newState);

        // check consistency between state transitions
        if (previousState == StateContainer.ERROR_LOAD && newState != StateContainer.START ||
            // forbid transition from any kind of loading to refreshing
            (previousState != StateContainer.IDLE && previousState != StateContainer.REFRESHING) && newState == StateContainer.REFRESHING) {
            Timber.e("Illegal state transition from [%s] to [%s]", previousState, newState);
            throw new IllegalStateException(String.format(Locale.ENGLISH, "Transition from %s to %s", previousState, newState));
        }

        memento.state = newState;
    }

    // ------------------------------------------
    /**
     * Go to ERROR_LOAD state, when some critical data was not loaded
     */
    private void stateErrorLoad(@ErrorType int errorType) {
        Timber.i("stateErrorLoad");
        setState(StateContainer.ERROR_LOAD);
        // enter ERROR_LOAD state logic

        switch (errorType) {
            case ERROR_GROUP_REPORTS:
                if (listMemento.currentSize <= 0) {
                    if (isViewAttached()) getView().showError(getListTag());
                } else {
                    listAdapter.onError(true);
                }
                break;
            case ERROR_KEYWORDS:
            case ERROR_POSTS:
                if (isViewAttached()) getView().showError(getListTag());
                break;
        }
    }

    // ------------------------------------------
    /**
     * Go to START state, drop all previous values and prepare to fresh start
     */
    private void stateStart() {
        Timber.i("stateStart");
        setState(StateContainer.START);
        // enter START state logic

        groupReportBundles.clear();
        keywordBundles.clear();
        posts.clear();
        listAdapter.clear();
        dropListStat();

        // fresh start - load input GroupReportBundle-s
        if (isViewAttached()) getView().showLoading(getListTag());
        getGroupReportBundlesUseCase.execute();
    }

    // ------------------------------------------
    /**
     * Go to GROUP_REPORTS_LOADED state, for each GroupReportBundle load corresponding KeywordBundle and Post
     */
    private void stateGroupReportsLoaded(@NonNull List<GroupReportBundle> bundles) {
        Timber.i("stateGroupReportsLoaded");
        setState(StateContainer.GROUP_REPORTS_LOADED);
        // enter GROUP_REPORTS_LOADED state logic

        Collections.sort(bundles);
        groupReportBundles = bundles;
        listMemento.currentSize += bundles.size();

        if (bundles.isEmpty()) {
            if (isViewAttached()) getView().showEmptyList(getListTag());
        } else {
            int size = bundles.size();
            long[] keywordBundleIds = new long[size];
            long[] postIds = new long[size];
            for (int i = 0; i < size; ++i) {
                keywordBundleIds[i] = bundles.get(i).keywordBundleId();
                postIds[i] = bundles.get(i).postId();
            }

            GetSpecificKeywordBundles.Parameters parameters1 = new GetSpecificKeywordBundles.Parameters(keywordBundleIds);
            GetSpecificPosts.Parameters parameters2 = new GetSpecificPosts.Parameters(postIds);

            getSpecificKeywordBundlesUseCase.setParameters(parameters1);
            getSpecificPostsUseCase.setParameters(parameters2);

            getSpecificKeywordBundlesUseCase.execute();
            getSpecificPostsUseCase.execute();
        }
    }

    // ------------------------------------------
    /**
     * Go to KEYWORDS_LOADED state, set finish status and check for another loading finished
     */
    private void stateKeywordsLoaded(@NonNull List<KeywordBundle> bundles) {
        Timber.i("stateKeywordsLoaded");
        setState(StateContainer.KEYWORDS_LOADED);
        // enter KEYWORDS_LOADED state logic

        keywordBundles = bundles;

        if (posts != null) stateIdle();
    }

    // ------------------------------------------
    /**
     * Go to POSTS_LOADED state, set finish status and check for another loading finished
     */
    private void statePostsLoaded(@NonNull List<Post> posts) {
        Timber.i("statePostsLoaded");
        setState(StateContainer.POSTS_LOADED);
        // enter POSTS_LOADED state logic

        this.posts = posts;

        if (keywordBundles != null) stateIdle();
    }

    // ------------------------------------------
    /**
     * Go to IDLE state, populate list of items with proper data
     */
    private void stateIdle() {
        Timber.i("stateIdle");
        setState(StateContainer.IDLE);
        // enter IDLE state logic

        populateList();
    }

    // ------------------------------------------
    /**
     * Go to REFRESHING state, fallback to START state
     */
    private void stateRefreshing() {
        Timber.i("stateRefreshing");
        setState(StateContainer.REFRESHING);
        // enter REFRESHING state logic

        stateStart();  // fallback to state START
    }

    /* Lifecycle */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        memento.toBundle(outState);
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
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
        stateStart();
    }

    @Override
    protected void onRestoreState() {
        memento = Memento.fromBundle(savedInstanceState);
        freshStart();  // nothing to be restored
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<List<GroupReportBundle>> createGetGroupReportBundlesCallback() {
        return new UseCase.OnPostExecuteCallback<List<GroupReportBundle>>() {
            @Override
            public void onFinish(@Nullable List<GroupReportBundle> bundles) {
                if (bundles == null) {
                    Timber.e("List of GroupReportBundle-s must not be null, it could be empty at least");
                    throw new ProgramException();
                } else {
                    Timber.i("Use-Case: succeeded to get list of GroupReportBundle-s");
                    stateGroupReportsLoaded(bundles);  // allow empty bundles
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get list of GroupReportBundle-s");
                stateErrorLoad(ERROR_GROUP_REPORTS);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<List<KeywordBundle>> createGetSpecificKeywordBundlesCallback() {
        return new UseCase.OnPostExecuteCallback<List<KeywordBundle>>() {
            @Override
            public void onFinish(@Nullable List<KeywordBundle> bundles) {
                if (bundles == null) {
                    Timber.e("List of KeywordBundle-s must not be null, it could be empty at least");
                    throw new ProgramException();
                } else {
                    Timber.i("Use-Case: succeeded to get list of KeywordBundle-s");
                    stateKeywordsLoaded(bundles);  // allow empty bundles
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get list of KeywordBundle-s");
                stateErrorLoad(ERROR_KEYWORDS);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<List<Post>> createGetSpecificPostsCallback() {
        return new UseCase.OnPostExecuteCallback<List<Post>>() {
            @Override
            public void onFinish(@Nullable List<Post> posts) {
                if (posts == null) {
                    Timber.e("List of Post-s must not be null, it could be empty at least");
                    throw new ProgramException();
                } else {
                    Timber.i("Use-Case: succeeded to get list of Post-s");
                    statePostsLoaded(posts);  // allow empty bundles
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("Use-Case: failed to get list of Posts-s");
                stateErrorLoad(ERROR_POSTS);
            }
        };
    }

    /* Utility */
    // --------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private boolean populateList() {
        List<ReportHistoryListItemVO> vos = new ArrayList<>();
        int size = groupReportBundles.size();
        for (int i = 0; i < size; ++i) {
            GroupReportBundle report = groupReportBundles.get(i);
            int[] counters = report.statusCount();
            int posted = counters[GroupReport.STATUS_SUCCESS];
            int total = report.groupReports().size();

            ReportHistoryListItemVO viewObject = ReportHistoryListItemVO.builder()
                    .setKeywords(keywordBundles.get(i).keywords())
                    .setPost(postToSingleGridVoMapper.map(posts.get(i)))
                    .setTimestamp(report.timestamp())
                    .build();
            viewObject.setPosted(posted);
            viewObject.setTotal(total);
            vos.add(viewObject);
        }

        listAdapter.clearSilent();  // TODO: take load-more into account later
        listAdapter.populate(vos, isThereMore());
        boolean isEmpty = vos.isEmpty();
        if (isViewAttached()) getView().showReports(isEmpty);
        return isEmpty;
    }
}
