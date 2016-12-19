package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Post;
import com.vk.sdk.api.model.VKWallPostResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

public class MakeWallPostToGroups extends MultiUseCase<VKWallPostResult, List<VKWallPostResult>> {

    public static class Parameters {
        Collection<Long> groupIds;
        Post post;

        Parameters(Builder builder) {
            this.groupIds = builder.groupIds;
            this.post = builder.post;
        }

        public static class Builder {
            Collection<Long> groupIds;
            Post post;

            public Builder setGroupIds(Collection<Long> groupIds) {
                this.groupIds = groupIds;
                return this;
            }

            public Builder setPost(Post post) {
                this.post = post;
                return this;
            }

            public Parameters build() {
                return new Parameters(this);
            }
        }
    }

    Parameters parameters;

    @Inject
    public MakeWallPostToGroups(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(0, threadExecutor, postExecuteScheduler);  // total count will be set later
//        setAllowedError();  // TODO: allow VkError with code = 6
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    protected List<? extends UseCase<VKWallPostResult>> createUseCases() {
        if (parameters == null) throw new NoParametersException();

        total = parameters.groupIds.size();  // update total count
        List<MakeWallPost> useCases = new ArrayList<>();
        for (long groupId : parameters.groupIds) {
            MakeWallPost.Parameters xparameters = new MakeWallPost.Parameters.Builder()
                    .setOwnerId(Long.toString(groupId))
                    .setMessage(parameters.post.description())
                    .setAttachments(parameters.post)
                    .build();
            MakeWallPost useCase = new MakeWallPost();
            useCase.setParameters(xparameters);
            useCases.add(useCase);
        }
        return useCases;
    }
}
