package com.orcchg.vikstra.domain.interactor.group;

import com.orcchg.vikstra.domain.BaseTest;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.repository.IGroupRepository;
import com.orcchg.vikstra.domain.sample.GroupBundleProvider;
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

public class GetGroupBundleByIdTest extends BaseTest {

    @Mock IGroupRepository groupRepository;
    @Mock ThreadExecutor threadExecutor;
    @Mock PostExecuteScheduler postExecuteScheduler;

    private GetGroupBundleById useCase;

    @Inject GroupBundleProvider provider;

    @Before
    public void setUp() throws Exception {
        super.setUp();
//        testComponent.inject(this);
        provider = new GroupBundleProvider();
        MockitoAnnotations.initMocks(this);
        useCase = new GetGroupBundleById(Constant.BAD_ID, groupRepository, threadExecutor, postExecuteScheduler);
    }

    /* Tests */
    // --------------------------------------------------------------------------------------------
    @Test
    public void execute_SomeValidId_GetValidGroupBundle() {
        // Given
        GroupBundle one = provider.groupBundle_oneKeyword();
        when(groupRepository.groups(one.id())).thenReturn(one);

        // When
        useCase.setGroupBundleId(one.id());
        GroupBundle result = useCase.doAction();

        // Then
        verify(groupRepository).groups(one.id());
        verifyNoMoreInteractions(groupRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertEquals(one, result);
    }

    @Test
    public void execute_BadId_GetNullGroupBundle() {
        // Given
        when(groupRepository.groups(Constant.BAD_ID)).thenReturn(null);

        // When
        GroupBundle result = useCase.doAction();

        // Then
        verify(groupRepository).groups(Constant.BAD_ID);
        verifyNoMoreInteractions(groupRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertNull(result);
    }
}
