package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.exception.vkontakte.VkUseCaseRetryException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.vk.sdk.api.model.VKAttachments;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

public class MakeWallPostToGroups extends MultiUseCase<GroupReportEssence, List<Ordered<GroupReportEssence>>> {

    public static class Parameters {
        List<Group> groups;
        VKAttachments attachments;
        String message;

        Parameters(Builder builder) {
            this.groups = builder.groups;
            this.attachments = builder.attachments;
            this.message = builder.message;
        }

        public void setAttachments(VKAttachments attachments) {
            this.attachments = attachments;
        }

        public List<Group> getGroups() {
            return groups;
        }
        public VKAttachments getAttachments() {
            return attachments;
        }
        public String getMessage() {
            return message;
        }

        public static class Builder {
            List<Group> groups;
            VKAttachments attachments;
            String message;

            public Builder setGroups(List<Group> groups) {
                this.groups = groups;
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
        setAllowedError(VkUseCaseRetryException.class);
        sleepInterval = 200;  // to avoid Captcha error
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    protected List<? extends UseCase<GroupReportEssence>> createUseCases() {
        if (parameters == null) throw new NoParametersException();

        total = parameters.groups.size();  // update total count
        Timber.d("Wall posting, total count: %s", total);
        List<MakeWallPost> useCases = new ArrayList<>();
        for (Group group : parameters.groups) {
            MakeWallPost.Parameters xparameters = new MakeWallPost.Parameters.Builder()
                    .setAttachments(parameters.attachments)
                    .setDestinationGroup(group)
                    .setMessage(parameters.message)
                    .build();
            MakeWallPost useCase = new MakeWallPost();
            useCase.setParameters(xparameters);
            useCases.add(useCase);
        }
        return useCases;
    }
}
