package com.orcchg.vikstra.domain.interactor.vkontakte;

import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.exception.vkontakte.Api5VkUseCaseException;
import com.orcchg.vikstra.domain.exception.vkontakte.Api6VkUseCaseException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.MultiUseCase;
import com.orcchg.vikstra.domain.interactor.base.Ordered;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.model.Keyword;
import com.vk.sdk.api.model.VKApiCommunityArray;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GetGroupsByKeywordsList extends MultiUseCase<VKApiCommunityArray, List<Ordered<VKApiCommunityArray>>> {

    private final List<Keyword> keywords;

    @Inject @SuppressWarnings("unchecked")
    public GetGroupsByKeywordsList(List<Keyword> keywords, ThreadExecutor threadExecutor,
                                   PostExecuteScheduler postExecuteScheduler) {
        super(keywords.size(), threadExecutor, postExecuteScheduler);
        this.keywords = keywords;
        setAllowedErrors(Api6VkUseCaseException.class);
        setTerminalErrors(Api5VkUseCaseException.class);
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

    /* Parameters */
    // --------------------------------------------------------------------------------------------
    public static class Parameters implements IParameters {
        private final List<Keyword> keywords;

        private Parameters(List<Keyword> keywords) {
            this.keywords = keywords;
        }

        public List<Keyword> keywords() {
            return keywords;
        }
    }

    @Nullable @Override
    protected IParameters getInputParameters() {
        return new Parameters(keywords);
    }
}
