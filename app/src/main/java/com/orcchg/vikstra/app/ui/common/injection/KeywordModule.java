package com.orcchg.vikstra.app.ui.common.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.keyword.AddKeywordToBundle;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundleById;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class KeywordModule {

    protected final long keywordBundleId;

    protected KeywordModule(long keywordBundleId) {
        this.keywordBundleId = keywordBundleId;
    }

    @Provides @PerActivity
    protected AddKeywordToBundle provideAddKeywordToBundle(IKeywordRepository repository,
           ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        return new AddKeywordToBundle(keywordBundleId, repository, threadExecutor, postExecuteScheduler);
    }

    @Provides @PerActivity
    protected GetKeywordBundleById provideGetKeywordBundleById(IKeywordRepository repository,
           ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        return new GetKeywordBundleById(keywordBundleId, repository, threadExecutor, postExecuteScheduler);
    }
}
