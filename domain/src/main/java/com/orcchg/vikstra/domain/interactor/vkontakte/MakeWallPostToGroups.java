package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKWallPostResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

public class MakeWallPostToGroups extends MultiUseCase<VKWallPostResult, List<VKWallPostResult>> {

    public static class Parameters {
        Collection<Long> groupIds;
        VKAttachments attachments;
        String message;

        Parameters(Builder builder) {
            this.groupIds = builder.groupIds;
            this.attachments = builder.attachments;
            this.message = builder.message;
        }

        public static class Builder {
            Collection<Long> groupIds;
            VKAttachments attachments;
            String message;

            public Builder setGroupIds(Collection<Long> groupIds) {
                this.groupIds = groupIds;
                return this;
            }

            public Builder setAttachments(VKAttachments attachments) {
                this.attachments = attachments;
                return this;
            }

            public Builder setMessage(String message) {
                this.message = message;
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
                    .setAttachments(parameters.attachments)
                    .setMessage(parameters.message)
                    .build();
            MakeWallPost useCase = new MakeWallPost();
            useCase.setParameters(xparameters);
            useCases.add(useCase);
        }
        return useCases;
    }
}
