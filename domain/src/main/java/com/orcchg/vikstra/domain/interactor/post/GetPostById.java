package com.orcchg.vikstra.domain.interactor.post;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.repository.IPostRepository;

import javax.inject.Inject;

public class GetPostById extends UseCase<Post> {

    final long id;
    final IPostRepository postRepository;

    @Inject
    public GetPostById(long id, IPostRepository postRepository,
                       ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.id = id;
        this.postRepository = postRepository;
    }

    public long getPostId() {
        return id;
    }

    @Nullable @Override
    protected Post doAction() {
        return postRepository.post(id);
    }
}
