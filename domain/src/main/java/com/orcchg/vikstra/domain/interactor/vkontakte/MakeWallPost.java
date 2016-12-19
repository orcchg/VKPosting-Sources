package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.Post;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKWallPostResult;

import javax.inject.Inject;

public class MakeWallPost extends VkUseCase<VKWallPostResult> {

    public static class Parameters {
        String ownerId;
        String message;
        Post post;

        Parameters(Builder builder) {
            this.ownerId = builder.ownerId;
            this.message = builder.message;
            this.post = builder.post;
        }

        public static class Builder {
            String ownerId;
            String message;
            Post post;

            public Builder setOwnerId(String ownerId) {
                this.ownerId = ownerId;
                return this;
            }

            public Builder setMessage(String message) {
                this.message = message;
                return this;
            }

            public Builder setAttachments(Post post) {
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
        params.put(VKApiConst.OWNER_ID, parameters.ownerId);  // destination user / community id
        params.put(VKApiConst.MESSAGE, parameters.message);
        params.put(VKApiConst.ATTACHMENTS, convert(parameters.post));
        params.put(VKApiConst.EXTENDED, 1);
        return VKApi.wall().post(params);
    }

    @Override
    protected VKWallPostResult parseVkResponse() {
//        return new Gson().fromJson(vkResponse.responseString, VKWallPostResult.class);
        return (VKWallPostResult) vkResponse.parsedModel;
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private VKAttachments convert(Post post) {
        // TODO: convert post to attach
        return new VKAttachments();
    }
}
