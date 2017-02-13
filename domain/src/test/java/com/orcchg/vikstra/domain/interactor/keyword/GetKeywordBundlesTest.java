package com.orcchg.vikstra.domain.interactor.keyword;

import com.orcchg.vikstra.domain.BaseTest;
import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;
import com.orcchg.vikstra.domain.sample.KeywordBundleProvider;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import javax.inject.Inject;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class GetKeywordBundlesTest extends BaseTest {

    @Mock IKeywordRepository keywordRepository;
    @Mock ThreadExecutor threadExecutor;
    @Mock PostExecuteScheduler postExecuteScheduler;

    private GetKeywordBundles useCase;

    @Inject KeywordBundleProvider provider;

    @Before
    public void setUp() throws Exception {
        super.setUp();
//        testComponent.inject(this);
        provider = new KeywordBundleProvider();
        MockitoAnnotations.initMocks(this);
        useCase = new GetKeywordBundles(keywordRepository, threadExecutor, postExecuteScheduler);
    }

    /* Tests */
    // --------------------------------------------------------------------------------------------
    @Test
    public void execute_DefaultParameters_GetListOfAllKeywordBundles() {
        // Given
        List<KeywordBundle> list = provider.keywordBundles();
        when(keywordRepository.keywords(-1, 0)).thenReturn(list);

        // When
        List<KeywordBundle> result = useCase.doAction();

        // Then
        verify(keywordRepository).keywords(-1, 0);
        verifyNoMoreInteractions(keywordRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertNotNull(result);
        Assert.assertEquals(list.size(), result.size());
        for (int i = 0; i < list.size(); ++i) {
            Assert.assertEquals(list.get(i), result.get(i));
        }
    }

    @Test
    public void execute_Limit3Offset2_GetListOfSpecificKeywordBundles() {
        // Given
        int limit = 3;
        int offset = 2;
        List<KeywordBundle> list = provider.keywordBundles();
        when(keywordRepository.keywords(limit, offset)).thenReturn(list.subList(offset, offset + limit));

        // When
        GetKeywordBundles.Parameters parameters = new GetKeywordBundles.Parameters.Builder()
                .setLimit(limit).setOffset(offset).build();
        useCase.setParameters(parameters);
        List<KeywordBundle> result = useCase.doAction();

        // Then
        verify(keywordRepository).keywords(limit, offset);
        verifyNoMoreInteractions(keywordRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertNotNull(result);
        Assert.assertEquals(limit, result.size());
        for (int i = 0; i < limit; ++i) {
            Assert.assertEquals(list.get(offset + i), result.get(i));
        }
    }

    @Test(expected = NoParametersException.class)
    public void execute_NoLimitNoOffset_ThrowException() {
        List<KeywordBundle> list = provider.keywordBundles();
        when(keywordRepository.keywords()).thenReturn(list);
        useCase.setParameters(null);
        useCase.doAction();
    }
}
