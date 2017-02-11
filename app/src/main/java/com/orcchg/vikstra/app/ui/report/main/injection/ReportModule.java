package com.orcchg.vikstra.app.ui.report.main.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.report.DumpGroupReports;
import com.orcchg.vikstra.domain.interactor.report.GetGroupReportBundleById;
import com.orcchg.vikstra.domain.repository.IReportRepository;
import com.orcchg.vikstra.domain.util.file.ReportComposer;

import dagger.Module;
import dagger.Provides;

@Module
public class ReportModule {

    protected final long groupReportBundleId;
    private final String pathToDumpFile;

    public ReportModule(long groupReportBundleId, String pathToDumpFile) {
        this.groupReportBundleId = groupReportBundleId;
        this.pathToDumpFile = pathToDumpFile;
    }

    @Provides @PerActivity
    protected GetGroupReportBundleById provideGetGroupReportBundleById(IReportRepository repository,
           ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        return new GetGroupReportBundleById(groupReportBundleId, repository, threadExecutor, postExecuteScheduler);
    }

    @Provides @PerActivity
    protected DumpGroupReports provideDumpGroupReports(ReportComposer reportComposer, IReportRepository reportRepository,
           ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        return new DumpGroupReports(pathToDumpFile, reportComposer, reportRepository, threadExecutor, postExecuteScheduler);
    }
}
