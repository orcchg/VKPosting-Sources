package com.orcchg.vikstra.app.ui.group.list.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.GetKeywordBundleById;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class GroupListModule {

    private final long keywordBundleId;

    public GroupListModule(long keywordBundleId) {
        this.keywordBundleId = keywordBundleId;
    }

    @Provides @PerActivity
    GetKeywordBundleById provideGetKeywordBundleById(IKeywordRepository repository,
            ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        return new GetKeywordBundleById(keywordBundleId, repository, threadExecutor, postExecuteScheduler);
    }
}
