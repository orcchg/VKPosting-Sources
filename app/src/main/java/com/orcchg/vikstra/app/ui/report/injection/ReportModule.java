package com.orcchg.vikstra.app.ui.report.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.report.GetGroupReportBundleById;
import com.orcchg.vikstra.domain.repository.IReportRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class ReportModule {

    protected final long groupReportBundleId;

    public ReportModule(long groupReportBundleId) {
        this.groupReportBundleId = groupReportBundleId;
    }

    @Provides @PerActivity
    protected GetGroupReportBundleById provideGetGroupReportBundleById(IReportRepository repository,
           ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        return new GetGroupReportBundleById(groupReportBundleId, repository, threadExecutor, postExecuteScheduler);
    }
}
