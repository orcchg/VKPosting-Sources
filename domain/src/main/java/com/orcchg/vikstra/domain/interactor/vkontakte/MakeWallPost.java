package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKWallPostResult;

import javax.inject.Inject;

public class MakeWallPost extends VkUseCase<GroupReport> {

    public static class Parameters {
        long ownerId;
        String message;
        VKAttachments attachments;

        Parameters(Builder builder) {
            this.ownerId = builder.ownerId;
            this.message = builder.message;
            this.attachments = builder.attachments;
        }

        public static class Builder {
            long ownerId;
            String message;
            VKAttachments attachments;

            public Builder setOwnerId(long ownerId) {
                this.ownerId = ownerId;
                return this;
            }

            public Builder setMessage(String message) {
                this.message = message;
                return this;
            }

            public Builder setAttachments(VKAttachments attachments) {
                this.attachments = attachments;
                return this;
            }

            public Parameters build() {
                return new Parameters(this);
            }
        }
    }

    Parameters parameters;

    @Inject
    public MakeWallPost(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    protected MakeWallPost() {
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    protected VKRequest prepareVkRequest() {
        if (parameters == null) throw new NoParametersException();
        VKParameters params = new VKParameters();
        params.put(VKApiConst.OWNER_ID, Long.toString(parameters.ownerId));  // destination user / community id
        params.put(VKApiConst.MESSAGE, parameters.message);
        params.put(VKApiConst.ATTACHMENTS, parameters.attachments);
        params.put(VKApiConst.EXTENDED, 1);
        return VKApi.wall().post(params);
    }

    @Override
    protected GroupReport parseVkResponse() {
//        return new Gson().fromJson(vkResponse.responseString, VKWallPostResult.class);
        VKWallPostResult data = (VKWallPostResult) vkResponse.parsedModel;
        return GroupReport.builder()
                .setGroupId(parameters.ownerId)
                .setWallPostId(data.post_id)
                .build();
    }
}
