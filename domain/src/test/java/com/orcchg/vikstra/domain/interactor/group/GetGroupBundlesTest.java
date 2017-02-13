package com.orcchg.vikstra.domain.interactor.group;

import com.orcchg.vikstra.domain.BaseTest;
import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.repository.IGroupRepository;
import com.orcchg.vikstra.domain.sample.GroupBundleProvider;

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

public class GetGroupBundlesTest extends BaseTest {

    @Mock IGroupRepository groupRepository;
    @Mock ThreadExecutor threadExecutor;
    @Mock PostExecuteScheduler postExecuteScheduler;

    private GetGroupBundles useCase;

    @Inject GroupBundleProvider provider;

    @Before
    public void setUp() throws Exception {
        super.setUp();
//        testComponent.inject(this);
        provider = new GroupBundleProvider();
        MockitoAnnotations.initMocks(this);
        useCase = new GetGroupBundles(groupRepository, threadExecutor, postExecuteScheduler);
    }

    /* Tests */
    // --------------------------------------------------------------------------------------------
    @Test
    public void execute_DefaultParameters_GetListOfAllGroupBundles() {
        // Given
        List<GroupBundle> list = provider.groupBundles();
        when(groupRepository.groups(-1, 0)).thenReturn(list);

        // When
        List<GroupBundle> result = useCase.doAction();

        // Then
        verify(groupRepository).groups(-1, 0);
        verifyNoMoreInteractions(groupRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertNotNull(result);
        Assert.assertEquals(list.size(), result.size());
        for (int i = 0; i < list.size(); ++i) {
            Assert.assertEquals(list.get(i), result.get(i));
        }
    }

    @Test
    public void execute_Limit3Offset2_GetListOfSpecificGroupBundles() {
        // Given
        int limit = 3;
        int offset = 2;
        List<GroupBundle> list = provider.groupBundles();
        when(groupRepository.groups(limit, offset)).thenReturn(list.subList(offset, offset + limit));

        // When
        GetGroupBundles.Parameters parameters = new GetGroupBundles.Parameters.Builder()
                .setLimit(limit).setOffset(offset).build();
        useCase.setParameters(parameters);
        List<GroupBundle> result = useCase.doAction();

        // Then
        verify(groupRepository).groups(limit, offset);
        verifyNoMoreInteractions(groupRepository);
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
        List<GroupBundle> list = provider.groupBundles();
        when(groupRepository.groups()).thenReturn(list);
        useCase.setParameters(null);
        useCase.doAction();
    }
}