package com.orcchg.vikstra.domain.interactor.vkontakte;

import android.text.TextUtils;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.util.Constant;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.model.VKApiCommunityArray;
import com.vk.sdk.api.model.VKApiCommunityFull;

import javax.inject.Inject;

public class GetGroupsByKeyword extends VkUseCase<VKApiCommunityArray> {

    private final Keyword keyword;

    @Inject
    public GetGroupsByKeyword(Keyword keyword, ThreadExecutor threadExecutor,
                              PostExecuteScheduler postExecuteScheduler) {
        super(threadExecutor, postExecuteScheduler);
        this.keyword = keyword;
    }

    protected GetGroupsByKeyword(Keyword keyword) {
        this.keyword = keyword;
    }

    @Override
    protected VKRequest prepareVkRequest() {
        String[] fields = new String[]{VKApiCommunityFull.CAN_POST, VKApiCommunityFull.MEMBERS_COUNT,
                                       VKApiCommunityFull.SITE};
        VKParameters params = VKParameters.from(
                VKApiConst.Q, keyword.keyword(),
                VKApiConst.COUNT, Integer.toString(Constant.GROUPS_COUNT_PER_GET_REQUEST),
                VKApiConst.FIELDS, TextUtils.join(",", fields));
        return VKApi.groups().search(params);
    }

    @Override
    protected VKApiCommunityArray parseVkResponse() {
//        return new Gson().fromJson(vkResponse.responseString, VKApiCommunityArray.class);
        return (VKApiCommunityArray) vkResponse.parsedModel;
    }
}

