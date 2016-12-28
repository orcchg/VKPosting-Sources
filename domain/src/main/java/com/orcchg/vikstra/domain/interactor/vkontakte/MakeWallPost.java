package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.util.Constant;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKWallPostResult;

import javax.inject.Inject;

public class MakeWallPost extends VkUseCase<GroupReportEssence> {

    public static class Parameters {
        VKAttachments attachments;
        Group group;
        String message;

        Parameters(Builder builder) {
            this.attachments = builder.attachments;
            this.group = builder.group;
            this.message = builder.message;
        }

        public static class Builder {
            Group group;
            String message;
            VKAttachments attachments;

            public Builder setAttachments(VKAttachments attachments) {
                this.attachments = attachments;
                return this;
            }

            public Builder setDestinationGroup(Group group) {
                this.group = group;
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
        params.put(VKApiConst.OWNER_ID, Long.toString(parameters.group.id()));  // destination user / community id
        params.put(VKApiConst.MESSAGE, parameters.message);
        params.put(VKApiConst.ATTACHMENTS, parameters.attachments);
        params.put(VKApiConst.EXTENDED, 1);
        return VKApi.wall().post(params);
    }

    @Override
    protected GroupReportEssence parseVkResponse() {
//        return new Gson().fromJson(vkResponse.responseString, VKWallPostResult.class);
        VKWallPostResult data = (VKWallPostResult) vkResponse.parsedModel;
        return GroupReportEssence.builder()
                .setErrorCode(Constant.NO_ERROR)
                .setGroup(parameters.group)
                .setWallPostId(data.post_id)
                .build();
    }
}
