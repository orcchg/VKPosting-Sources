package com.orcchg.vikstra.domain.interactor.keyword;

import com.orcchg.vikstra.domain.BaseTest;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;
import com.orcchg.vikstra.domain.sample.KeywordBundleProvider;
import com.orcchg.vikstra.domain.util.Constant;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class GetKeywordBundleByIdTest extends BaseTest {

    @Mock IKeywordRepository keywordRepository;
    @Mock ThreadExecutor threadExecutor;
    @Mock PostExecuteScheduler postExecuteScheduler;

    private GetKeywordBundleById useCase;

    @Inject KeywordBundleProvider provider;

    @Before
    public void setUp() throws Exception {
        super.setUp();
//        testComponent.inject(this);
        provider = new KeywordBundleProvider();
        MockitoAnnotations.initMocks(this);
        useCase = new GetKeywordBundleById(Constant.BAD_ID, keywordRepository, threadExecutor, postExecuteScheduler);
    }

    /* Tests */
    // --------------------------------------------------------------------------------------------
    @Test
    public void execute_SomeValidId_GetValidKeywordBundle() {
        // Given
        KeywordBundle one = provider.keywordBundle_oneWord();
        when(keywordRepository.keywords(one.id())).thenReturn(one);

        // When
        useCase.setKeywordBundleId(one.id());
        KeywordBundle result = useCase.doAction();

        // Then
        verify(keywordRepository).keywords(one.id());
        verifyNoMoreInteractions(keywordRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertEquals(one, result);
    }

    @Test
    public void execute_BadId_GetNullKeywordBundle() {
        // Given
        when(keywordRepository.keywords(Constant.BAD_ID)).thenReturn(null);

        // When
        KeywordBundle result = useCase.doAction();

        // Then
        verify(keywordRepository).keywords(Constant.BAD_ID);
        verifyNoMoreInteractions(keywordRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertNull(result);
    }
}
