package com.orcchg.vikstra.domain.interactor.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.repository.IReportRepository;
import com.orcchg.vikstra.domain.util.endpoint.EndpointUtility;

import java.util.List;

import javax.inject.Inject;

public class GetGroupReportBundlesForUser extends UseCase<List<GroupReportBundle>> {

    private final IReportRepository reportRepository;

    @Inject
    GetGroupReportBundlesForUser(IReportRepository reportRepository, ThreadExecutor threadExecutor,
                                 PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.reportRepository = reportRepository;
    }

    @Nullable @Override
    protected List<GroupReportBundle> doAction() {
        long userId = EndpointUtility.getCurrentUserId();
        return reportRepository.groupReportsForUser(userId);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return null;
    }
}
