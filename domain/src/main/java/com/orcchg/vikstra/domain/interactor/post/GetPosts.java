package com.orcchg.vikstra.domain.interactor.post;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.common.ListParameters;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.repository.IPostRepository;

import java.util.List;

import javax.inject.Inject;

public class GetPosts extends UseCase<List<Post>> {

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

    final IPostRepository postRepository;
    Parameters parameters;

    @Inject
    GetPosts(IPostRepository postRepository, ThreadExecutor threadExecutor,
             PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.postRepository = postRepository;
        this.parameters = new Parameters.Builder().build();
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Nullable @Override
    protected List<Post> doAction() {
        int limit = parameters.limit();
        int offset = parameters.offset();
        return postRepository.posts(limit, offset);
    }
}
