package com.orcchg.vikstra.app.ui.report.main.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.report.main.Holder;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundleById;
import com.orcchg.vikstra.domain.interactor.report.DumpGroupReports;
import com.orcchg.vikstra.domain.interactor.report.GetGroupReportBundleById;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;
import com.orcchg.vikstra.domain.repository.IReportRepository;
import com.orcchg.vikstra.domain.util.file.ReportComposer;

import dagger.Module;
import dagger.Provides;

@Module
public class ReportModule {

    protected final long groupReportBundleId;
    protected final long keywordBundleId;
    private final String pathToDumpFile;
    private final boolean isInteractiveMode;

    public ReportModule(long groupReportBundleId, long keywordBundleId, String pathToDumpFile, boolean isInteractiveMode) {
        this.groupReportBundleId = groupReportBundleId;
        this.keywordBundleId = keywordBundleId;
        this.pathToDumpFile = pathToDumpFile;
        this.isInteractiveMode = isInteractiveMode;
    }

    @Provides @PerActivity
    protected GetGroupReportBundleById provideGetGroupReportBundleById(IReportRepository repository,
           ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        return new GetGroupReportBundleById(groupReportBundleId, repository, threadExecutor, postExecuteScheduler);
    }

    @Provides @PerActivity
    protected GetKeywordBundleById provideGetKeywordBundleById(IKeywordRepository repository,
            ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        return new GetKeywordBundleById(keywordBundleId, repository, threadExecutor, postExecuteScheduler);
    }

    @Provides @PerActivity
    protected DumpGroupReports provideDumpGroupReports(ReportComposer reportComposer, IReportRepository reportRepository,
           ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        return new DumpGroupReports(pathToDumpFile, reportComposer, reportRepository, threadExecutor, postExecuteScheduler);
    }

    @Provides @PerActivity
    protected Holder provideHolder() {
        return new Holder(isInteractiveMode);
    }
}
