package com.orcchg.vikstra.domain.interactor.vkontakte;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.vkontakte.model.VkSimpleResponseModel;
import com.orcchg.vikstra.domain.model.GroupReport;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public abstract class ProcessWallPosts extends MultiUseCase<VkSimpleResponseModel, List<Ordered<VkSimpleResponseModel>>> {

    public static class Parameters implements IParameters {
        List<GroupReport> groupReports;

        public Parameters(List<GroupReport> groupReports) {
            this.groupReports = groupReports;
        }
    }

    private Parameters parameters;

    @Inject @SuppressWarnings("unchecked")
    public ProcessWallPosts(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(0, threadExecutor, postExecuteScheduler);  // total count will be set later
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    protected abstract ProcessWallPost createProcessWallPostUseCase();

    @Override
    protected List<? extends UseCase<VkSimpleResponseModel>> createUseCases() {
        if (parameters == null) throw new NoParametersException();

        total = parameters.groupReports.size();  // update total count
        Timber.d("Process wall posts, total count: %s", total);
        List<ProcessWallPost> useCases = new ArrayList<>();
        for (GroupReport report : parameters.groupReports) {
            ProcessWallPost.Parameters xparameters = new ProcessWallPost.Parameters(report);
            ProcessWallPost useCase = createProcessWallPostUseCase();
            useCase.setParameters(xparameters);
            useCases.add(useCase);
        }
        return useCases;
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
