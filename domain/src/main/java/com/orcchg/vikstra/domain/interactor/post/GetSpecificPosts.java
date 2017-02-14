package com.orcchg.vikstra.domain.interactor.post;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.common.IdsParameters;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.repository.IPostRepository;

import java.util.List;

import javax.inject.Inject;

public class GetSpecificPosts extends UseCase<List<Post>> {

    public static class Parameters extends IdsParameters {
        public Parameters(long... ids) {
            super(ids);
        }
    }

    private final IPostRepository postRepository;
    private Parameters parameters;

    @Inject
    GetSpecificPosts(IPostRepository postRepository, ThreadExecutor threadExecutor,
                     PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.postRepository = postRepository;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected List<Post> doAction() {
        if (parameters == null) throw new NoParametersException();
        return postRepository.posts(parameters.ids);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
