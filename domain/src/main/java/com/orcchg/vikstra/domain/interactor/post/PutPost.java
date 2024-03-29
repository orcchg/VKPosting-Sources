package com.orcchg.vikstra.domain.interactor.post;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.IPutUseCase;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.PostEssence;
import com.orcchg.vikstra.domain.repository.IPostRepository;

import javax.inject.Inject;

public class PutPost extends UseCase<Post> implements IPutUseCase {

    public static class Parameters implements IParameters {
        final PostEssence essence;

        public Parameters(PostEssence essence) {
            this.essence = essence;
        }
    }

    private final IPostRepository postRepository;
    private Parameters parameters;

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
    protected Post doAction() {
        if (parameters == null) throw new NoParametersException();
        return postRepository.addPost(parameters.essence);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }

    @Override
    public long getReservedId() {
        return postRepository.getLastId() + 1;
    }
}
