package com.orcchg.vikstra.app.ui.group.list.fragment.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.group.GetGroupBundleById;
import com.orcchg.vikstra.domain.repository.IGroupRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class GroupListModule {

    protected final long groupBundleId;

    public GroupListModule(long groupBundleId) {
        this.groupBundleId = groupBundleId;
    }

    @Provides @PerActivity
    protected GetGroupBundleById provideGetGroupBundleById(IGroupRepository repository,
           ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        return new GetGroupBundleById(groupBundleId, repository, threadExecutor, postExecuteScheduler);
    }
}
