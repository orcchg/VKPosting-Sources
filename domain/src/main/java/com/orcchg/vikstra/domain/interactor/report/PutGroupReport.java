package com.orcchg.vikstra.domain.interactor.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.repository.IReportRepository;

import javax.inject.Inject;

public class PutGroupReport extends UseCase<GroupReport> {

    public static class Parameters {
        GroupReportEssence essence;

        public Parameters(GroupReportEssence essence) {
            this.essence = essence;
        }
    }

    final IReportRepository reportRepository;
    Parameters parameters;

    @Inject
    PutGroupReport(IReportRepository reportRepository, ThreadExecutor threadExecutor,
                   PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.reportRepository = reportRepository;
    }

    @Nullable @Override
    protected GroupReport doAction() {
        if (parameters == null) throw new NoParametersException();
        return reportRepository.addGroupReport(parameters.essence);
    }
}
