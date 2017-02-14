package com.orcchg.vikstra.domain.interactor.report;

import com.orcchg.vikstra.domain.BaseTest;
import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.GroupReportBundle;
import com.orcchg.vikstra.domain.repository.IReportRepository;
import com.orcchg.vikstra.domain.sample.GroupBundleProvider;
import com.orcchg.vikstra.domain.sample.ReportProvider;

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

public class GetGroupReportBundlesTest extends BaseTest {

    @Mock IReportRepository reportRepository;
    @Mock ThreadExecutor threadExecutor;
    @Mock PostExecuteScheduler postExecuteScheduler;

    private GetGroupReportBundles useCase;

    @Inject GroupBundleProvider groupsProvider;
    @Inject ReportProvider reportsProvider;

    @Before
    public void setUp() throws Exception {
        super.setUp();
//        testComponent.inject(this);
        groupsProvider = new GroupBundleProvider();
        reportsProvider = new ReportProvider(groupsProvider);
        MockitoAnnotations.initMocks(this);
        useCase = new GetGroupReportBundles(reportRepository, threadExecutor, postExecuteScheduler);
    }

    /* Tests */
    // --------------------------------------------------------------------------------------------
    @Test
    public void execute_DefaultParameters_GetListOfAllGroupReportBundles() {
        // Given
        List<GroupReportBundle> list = reportsProvider.reportBundles(5);
        when(reportRepository.groupReports(-1, 0)).thenReturn(list);

        // When
        List<GroupReportBundle> result = useCase.doAction();

        // Then
        verify(reportRepository).groupReports(-1, 0);
        verifyNoMoreInteractions(reportRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertNotNull(result);
        Assert.assertEquals(list.size(), result.size());
        for (int i = 0; i < list.size(); ++i) {
            Assert.assertEquals(list.get(i), result.get(i));
        }
    }

    @Test
    public void execute_Limit3Offset2_GetListOfSpecificGroupReportBundles() {
        // Given
        int limit = 3;
        int offset = 2;
        List<GroupReportBundle> list = reportsProvider.reportBundles(5);
        when(reportRepository.groupReports(limit, offset)).thenReturn(list.subList(offset, offset + limit));

        // When
        GetGroupReportBundles.Parameters parameters = new GetGroupReportBundles.Parameters.Builder()
                .setLimit(limit).setOffset(offset).build();
        useCase.setParameters(parameters);
        List<GroupReportBundle> result = useCase.doAction();

        // Then
        verify(reportRepository).groupReports(limit, offset);
        verifyNoMoreInteractions(reportRepository);
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
        List<GroupReportBundle> list = reportsProvider.reportBundles(5);
        when(reportRepository.groupReports()).thenReturn(list);
        useCase.setParameters(null);
        useCase.doAction();
    }
}