package com.orcchg.vikstra.domain.interactor.post;

import com.orcchg.vikstra.domain.BaseTest;
import com.orcchg.vikstra.domain.exception.NoParametersException;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.repository.IPostRepository;
import com.orcchg.vikstra.domain.sample.PostProvider;

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

public class GetPostsTest extends BaseTest {

    @Mock IPostRepository postRepository;
    @Mock ThreadExecutor threadExecutor;
    @Mock PostExecuteScheduler postExecuteScheduler;

    private GetPosts useCase;

    @Inject PostProvider provider;

    @Before
    public void setUp() throws Exception {
        super.setUp();
//        testComponent.inject(this);
        provider = new PostProvider();
        MockitoAnnotations.initMocks(this);
        useCase = new GetPosts(postRepository, threadExecutor, postExecuteScheduler);
    }

    /* Tests */
    // --------------------------------------------------------------------------------------------
    @Test
    public void execute_DefaultParameters_GetListOfAllPosts() {
        // Given
        List<Post> list = provider.posts();
        when(postRepository.posts(-1, 0)).thenReturn(list);

        // When
        List<Post> result = useCase.doAction();

        // Then
        verify(postRepository).posts(-1, 0);
        verifyNoMoreInteractions(postRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertNotNull(result);
        Assert.assertEquals(list.size(), result.size());
        for (int i = 0; i < list.size(); ++i) {
            Assert.assertEquals(list.get(i), result.get(i));
        }
    }

    @Test
    public void execute_Limit3Offset2_GetListOfSpecificPosts() {
        // Given
        int limit = 3;
        int offset = 2;
        List<Post> list = provider.posts();
        when(postRepository.posts(limit, offset)).thenReturn(list.subList(offset, offset + limit));

        // When
        GetPosts.Parameters parameters = new GetPosts.Parameters.Builder()
                .setLimit(limit).setOffset(offset).build();
        useCase.setParameters(parameters);
        List<Post> result = useCase.doAction();

        // Then
        verify(postRepository).posts(limit, offset);
        verifyNoMoreInteractions(postRepository);
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
        List<Post> list = provider.posts();
        when(postRepository.posts()).thenReturn(list);
        useCase.setParameters(null);
        useCase.doAction();
    }
}
