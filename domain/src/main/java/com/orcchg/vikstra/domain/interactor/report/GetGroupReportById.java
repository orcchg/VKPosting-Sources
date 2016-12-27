package com.orcchg.vikstra.domain.interactor.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.repository.IReportRepository;

import javax.inject.Inject;

public class GetGroupReportById extends UseCase<GroupReport> {

    final long id;
    final IReportRepository reportRepository;

    boolean shouldPollModelFromRepo;

    @Inject
    public GetGroupReportById(long id, IReportRepository reportRepository,
                              ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.id = id;
        this.reportRepository = reportRepository;
    }

    public void setShouldPollModelFromRepo(boolean shouldPollModelFromRepo) {
        this.shouldPollModelFromRepo = shouldPollModelFromRepo;
    }

    public long getGroupReportId() {
        return id;
    }

    @Nullable @Override
    protected GroupReport doAction() {
        if (shouldPollModelFromRepo) {
            return reportRepository.pollGroupReport(id);
        }
        return reportRepository.groupReport(id);
    }
}
