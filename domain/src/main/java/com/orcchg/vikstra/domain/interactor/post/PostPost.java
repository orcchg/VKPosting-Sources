package com.orcchg.vikstra.domain.interactor.post;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.repository.IPostRepository;

import javax.inject.Inject;

public class PostPost extends UseCase<Boolean> {

    public static class Parameters implements IParameters {
        Post post;

        public Parameters(Post post) {
            this.post = post;
        }
    }

    private final IPostRepository postRepository;
    private Parameters parameters;

    @Inject
    PostPost(IPostRepository postRepository, ThreadExecutor threadExecutor,
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
        return postRepository.updatePost(parameters.post);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
