package com.orcchg.vikstra.domain.interactor.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.common.ListParameters;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.repository.IReportRepository;

import java.util.List;

import javax.inject.Inject;

public class GetGroupReportBundles extends UseCase<List<GroupReportBundle>> {

    public static class Parameters extends ListParameters {
        protected Parameters(Parameters.Builder builder) {
            super(builder);
        }

        public static class Builder extends ListParameters.Builder<Builder> {
            public Parameters build() {
                return new Parameters(this);
            }
        }
    }

    private final IReportRepository reportRepository;
    private Parameters parameters;

    @Inject
    GetGroupReportBundles(IReportRepository reportRepository, ThreadExecutor threadExecutor,
                          PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.reportRepository = reportRepository;
        this.parameters = new Parameters.Builder().build();
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected List<GroupReportBundle> doAction() {
        if (parameters == null) throw new NoParametersException();
        int limit = parameters.limit();
        int offset = parameters.offset();
        return reportRepository.groupReports(limit, offset);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
