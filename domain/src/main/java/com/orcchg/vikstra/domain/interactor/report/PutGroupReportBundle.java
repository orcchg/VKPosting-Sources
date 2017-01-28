package com.orcchg.vikstra.domain.interactor.report;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.repository.IReportRepository;

import java.util.List;

import javax.inject.Inject;

public class PutGroupReportBundle extends UseCase<GroupReportBundle> {

    public static class Parameters implements IParameters {
        List<GroupReportEssence> essences;

        public Parameters(List<GroupReportEssence> essences) {
            this.essences = essences;
        }
    }

    private final IReportRepository reportRepository;
    private Parameters parameters;

    @Inject
    PutGroupReportBundle(IReportRepository reportRepository, ThreadExecutor threadExecutor,
                         PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.reportRepository = reportRepository;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected GroupReportBundle doAction() {
        if (parameters == null) throw new NoParametersException();
        return reportRepository.addGroupReports(parameters.essences);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
