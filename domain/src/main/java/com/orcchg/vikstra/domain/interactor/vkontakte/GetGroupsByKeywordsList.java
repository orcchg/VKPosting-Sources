package com.orcchg.vikstra.domain.interactor.vkontakte;

import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.UseCase;
import com.orcchg.vikstra.domain.model.Keyword;
import com.vk.sdk.api.model.VKApiCommunityArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

public class GetGroupsByKeywordsList extends MultiUseCase<VKApiCommunityArray, List<VKApiCommunityArray>> {

    private Collection<Keyword> keywords;

    @Inject
    public GetGroupsByKeywordsList(Collection<Keyword> keywords, ThreadExecutor threadExecutor,
                                   PostExecuteScheduler postExecuteScheduler) {
        super(keywords.size(), threadExecutor, postExecuteScheduler);
        this.keywords = keywords;
//        setAllowedError();  // TODO: allow VkError with code = 6
    }

    @Override
    protected List<? extends UseCase<VKApiCommunityArray>> createUseCases() {
        List<GetGroupsByKeyword> useCases = new ArrayList<>();
        for (Keyword keyword : keywords) {
            GetGroupsByKeyword useCase = new GetGroupsByKeyword(keyword);
            useCases.add(useCase);
        }
        return useCases;
    }
}
