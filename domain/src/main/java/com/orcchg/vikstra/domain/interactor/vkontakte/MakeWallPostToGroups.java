package com.orcchg.vikstra.domain.interactor.vkontakte;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.exception.vkontakte.Api14VkUseCaseException;
import com.orcchg.vikstra.domain.exception.vkontakte.Api220VkUseCaseException;
import com.orcchg.vikstra.domain.exception.vkontakte.Api5VkUseCaseException;
import com.orcchg.vikstra.domain.exception.vkontakte.Api6VkUseCaseException;
import com.orcchg.vikstra.domain.executor.PausableThreadPoolExecutor;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.vk.sdk.api.model.VKAttachments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import timber.log.Timber;

public class MakeWallPostToGroups extends MultiUseCase<GroupReportEssence, List<Ordered<GroupReportEssence>>> {

    public static class Parameters implements IParameters {
        final VKAttachments attachments;
        final List<Group> groups;
        final String message;

        Parameters(Builder builder) {
            this.attachments = builder.attachments;
            this.groups = builder.groups;
            this.message = builder.message;
        }

        public VKAttachments getAttachments() {
            return attachments;
        }
        public List<Group> getGroups() {
            return groups;
        }
        public String getMessage() {
            return message;
        }

        public static class Builder {
            VKAttachments attachments;
            List<Group> groups;
            String message;

            public Builder addAttachment(VKAttachments.VKApiAttachment attachment) {
                VKAttachments attachments = new VKAttachments();
                attachments.add(attachment);
                return addAttachments(attachments);
            }

            public Builder addAttachments(VKAttachments attachments) {
                if (this.attachments == null) return setAttachments(attachments);
                for (VKAttachments.VKApiAttachment attachment : attachments) {
                    this.attachments.add(attachment);
                }
                return this;
            }

            public Builder setAttachments(VKAttachments attachments) {
                this.attachments = attachments;
                return this;
            }

            public Builder setGroups(List<Group> groups) {
                this.groups = groups;
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

    private Parameters parameters;

    @Inject @SuppressWarnings("unchecked")
    public MakeWallPostToGroups(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(0, threadExecutor, postExecuteScheduler);  // total count will be set later
        setAllowedErrors(Api6VkUseCaseException.class);
        setSuspendErrors(Api14VkUseCaseException.class);
        setTerminalErrors(Api5VkUseCaseException.class, Api220VkUseCaseException.class);
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

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }

    /* Thread pool */
    // ------------------------------------------
    @Override
    protected PausableThreadPoolExecutor createHighloadThreadPoolExecutor() {
        // completely overridden method
        BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>();
        PausableThreadPoolExecutor pool = new PausableThreadPoolExecutor(10, 10, 3, TimeUnit.SECONDS, queue);
        pool.allowCoreThreadTimeOut(true);
        return pool;
    }
}
