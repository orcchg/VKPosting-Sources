package com.orcchg.vikstra.domain.interactor.post;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.essense.PostEssense;
import com.orcchg.vikstra.domain.repository.IPostRepository;

import javax.inject.Inject;

public class PutPost extends UseCase<Boolean> {

    public static class Parameters {
        PostEssense essense;

        public Parameters(PostEssense essense) {
            this.essense = essense;
        }
    }

    final IPostRepository postRepository;
    Parameters parameters;

    @Inject
    PutPost(IPostRepository postRepository, ThreadExecutor threadExecutor,
            PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.postRepository = postRepository;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected Boolean doAction() {
        if (parameters == null) throw new NoParametersException();
        return postRepository.addPost(parameters.essense);
    }
}
