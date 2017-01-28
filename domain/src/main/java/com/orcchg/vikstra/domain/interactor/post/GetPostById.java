package com.orcchg.vikstra.domain.interactor.post;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.common.IdParameters;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.repository.IPostRepository;
import com.orcchg.vikstra.domain.util.Constant;

import javax.inject.Inject;

public class GetPostById extends UseCase<Post> {

    private long id = Constant.BAD_ID;
    private final IPostRepository postRepository;

    @Inject
    public GetPostById(long id, IPostRepository postRepository,
                       ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.id = id;
        this.postRepository = postRepository;
    }

    public void setPostId(long id) {
        this.id = id;
    }

    public long getPostId() {
        return id;
    }

    @Nullable @Override
    protected Post doAction() {
        return postRepository.post(id);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return new IdParameters(id);
    }
}
