package com.orcchg.vikstra.domain.interactor.vkontakte;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.interactor.vkontakte.model.VkSimpleResponseModel;
import com.orcchg.vikstra.domain.model.GroupReport;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;

import timber.log.Timber;

public abstract class ProcessWallPost extends VkUseCase<VkSimpleResponseModel> {

    public static class Parameters implements IParameters {
        GroupReport groupReport;

        public Parameters(GroupReport groupReport) {
            this.groupReport = groupReport;
        }

        @Override
        public String toString() {
            return new StringBuilder("ProcessWallPost.Parameters {groupReport=")
                    .append(groupReport)
                    .append("}").toString();
        }
    }

    private Parameters parameters;

    public ProcessWallPost(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    /**
     * For internal use within another {@link UseCase} and synchronous calls only
     */
    protected ProcessWallPost() {
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    protected abstract VKRequest createVkRequest(VKParameters parameters);

    protected VKParameters prepareVKParameters() {
        if (parameters == null) throw new NoParametersException();
        Timber.d(parameters.toString());
        VKParameters params = new VKParameters();
        params.put(VKApiConst.OWNER_ID, -parameters.groupReport.group().id());  // destination user / community id
        params.put(VKApiConst.POST_ID, parameters.groupReport.wallPostId());
        return params;
    }

    @Override
    protected VKRequest prepareVkRequest() {
        return createVkRequest(prepareVKParameters());
    }

    @Override
    protected VkSimpleResponseModel parseVkResponse() {
        return new Gson().fromJson(vkResponse.responseString, VkSimpleResponseModel.class);
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return parameters;
    }
}
