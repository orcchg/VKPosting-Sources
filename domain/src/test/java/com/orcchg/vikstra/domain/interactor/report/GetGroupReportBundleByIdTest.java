package com.orcchg.vikstra.domain.interactor.report;

import com.orcchg.vikstra.domain.BaseTest;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.repository.IReportRepository;
import com.orcchg.vikstra.domain.sample.GroupBundleProvider;
import com.orcchg.vikstra.domain.sample.ReportProvider;
import com.orcchg.vikstra.domain.util.Constant;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.inject.Inject;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class GetGroupReportBundleByIdTest extends BaseTest {

    @Mock IReportRepository reportRepository;
    @Mock ThreadExecutor threadExecutor;
    @Mock PostExecuteScheduler postExecuteScheduler;

    private GetGroupReportBundleById useCase;

    @Inject GroupBundleProvider groupsProvider;
    @Inject ReportProvider reportsProvider;

    @Before
    public void setUp() throws Exception {
        super.setUp();
//        testComponent.inject(this);
        groupsProvider = new GroupBundleProvider();
        reportsProvider = new ReportProvider(groupsProvider);
        MockitoAnnotations.initMocks(this);
        useCase = new GetGroupReportBundleById(Constant.BAD_ID, reportRepository, threadExecutor, postExecuteScheduler);
    }

    /* Tests */
    // --------------------------------------------------------------------------------------------
    @Test
    public void execute_SomeValidId_GetValidGroupReportBundle() {
        // Given
        GroupReportBundle bundle = reportsProvider.reportBundle();
        when(reportRepository.groupReports(bundle.id())).thenReturn(bundle);

        // When
        useCase.setGroupReportId(bundle.id());
        GroupReportBundle result = useCase.doAction();

        // Then
        verify(reportRepository).groupReports(bundle.id());
        verifyNoMoreInteractions(reportRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertEquals(bundle, result);
    }

    @Test
    public void execute_BadId_GetNullGroupReportBundle() {
        // Given
        when(reportRepository.groupReports(Constant.BAD_ID)).thenReturn(null);

        // When
        GroupReportBundle result = useCase.doAction();

        // Then
        verify(reportRepository).groupReports(Constant.BAD_ID);
        verifyNoMoreInteractions(reportRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertNull(result);
    }
}
