package com.orcchg.vikstra.domain.interactor.post;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.common.IdParameters;
import com.orcchg.vikstra.domain.repository.IPostRepository;
import com.orcchg.vikstra.domain.util.Constant;

import javax.inject.Inject;

public class DeletePost extends UseCase<Boolean> {

    private long id = Constant.BAD_ID;
    private final IPostRepository postRepository;

    @Inject
    public DeletePost(IPostRepository postRepository,
                      ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.postRepository = postRepository;
    }

    public void setPostId(long id) {
        this.id = id;
    }

    public long getPostId() {
        return id;
    }

    @Nullable @Override
    protected Boolean doAction() {
        return postRepository.deletePost(id);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return new IdParameters(id);
    }
}
