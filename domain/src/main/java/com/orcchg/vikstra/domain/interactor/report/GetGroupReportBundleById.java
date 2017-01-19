package com.orcchg.vikstra.domain.interactor.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.repository.IReportRepository;

import javax.inject.Inject;

public class GetGroupReportBundleById extends UseCase<GroupReportBundle> {

    private final long id;
    private final IReportRepository reportRepository;

    @Inject
    public GetGroupReportBundleById(long id, IReportRepository reportRepository,
                                    ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.id = id;
        this.reportRepository = reportRepository;
    }

    /**
     * For internal use within another {@link UseCase} and synchronous calls only
     */
    GetGroupReportBundleById(long id, IReportRepository reportRepository) {
        this.id = id;
        this.reportRepository = reportRepository;
    }

    public long getGroupReportId() {
        return id;
    }

    @Nullable @Override
    protected GroupReportBundle doAction() {
        return reportRepository.groupReports(id);
    }
}
