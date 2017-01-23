package com.orcchg.vikstra.domain.interactor.vkontakte.batch;

import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.vkontakte.MakeWallPostToGroups;
import com.orcchg.vikstra.domain.model.essense.GroupReportEssence;
import com.orcchg.vikstra.domain.util.Constant;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKBatchRequest;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKWallPostResult;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MakeWallPostToGroupsBatch extends VkBatchUseCase<GroupReportEssence, List<GroupReportEssence>> {

    private MakeWallPostToGroups.Parameters parameters;

    @Inject
    public MakeWallPostToGroupsBatch(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
    }

    protected MakeWallPostToGroupsBatch() {
        super();
    }

    public void setParameters(MakeWallPostToGroups.Parameters parameters) {
        this.parameters = parameters;
    }

    @Override
    protected VKBatchRequest prepareVkBatchRequest() {
        if (parameters == null) throw new NoParametersException();
        int size = parameters.getGroups().size();
        VKRequest[] requests = new VKRequest[size];
        for (int i = 0; i < size; ++i) {
            VKParameters params = new VKParameters();
            params.put(VKApiConst.OWNER_ID, Long.toString(parameters.getGroups().get(i).id()));  // destination user / community id
            params.put(VKApiConst.MESSAGE, parameters.getMessage());
            params.put(VKApiConst.ATTACHMENTS, parameters.getAttachments());
            params.put(VKApiConst.EXTENDED, 1);
            requests[i] = VKApi.wall().post(params);
        }
        return new VKBatchRequest(requests);
    }

    @Override
    protected List<GroupReportEssence> parseVkBatchResponse() {
        List<GroupReportEssence> list = new ArrayList<>();
        for (int i = 0; i < vkBatchResponse.length; ++i) {
            VKWallPostResult data = (VKWallPostResult) vkBatchResponse[i].parsedModel;
            GroupReportEssence item = GroupReportEssence.builder()
                    .setCancelled(false)  // ignore cancellation for successful result
                    .setErrorCode(Constant.NO_ERROR)
                    .setGroup(parameters.getGroups().get(i))
                    .setWallPostId(data.post_id)
                    .build();
            list.add(item);
        }
        return list;
    }
}
