package com.orcchg.vikstra.domain.interactor.post;

import com.orcchg.vikstra.domain.BaseTest;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.repository.IPostRepository;
import com.orcchg.vikstra.domain.sample.PostProvider;
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

public class GetPostByIdTest extends BaseTest {

    @Mock IPostRepository postRepository;
    @Mock ThreadExecutor threadExecutor;
    @Mock PostExecuteScheduler postExecuteScheduler;

    private GetPostById useCase;

    @Inject PostProvider provider;

    @Before
    public void setUp() throws Exception {
        super.setUp();
//        testComponent.inject(this);
        provider = new PostProvider();
        MockitoAnnotations.initMocks(this);
        useCase = new GetPostById(Constant.BAD_ID, postRepository, threadExecutor, postExecuteScheduler);
    }

    /* Tests */
    // --------------------------------------------------------------------------------------------
    @Test
    public void execute_SomeValidId_GetValidPost() {
        // Given
        Post post = provider.post();
        Post withLink = provider.postWithLink();
        Post withMedia_one = provider.postWithMedia(1);
        Post withMedia_two = provider.postWithMedia(2);
        Post withMedia_three = provider.postWithMedia(3);
        Post withMedia_four = provider.postWithMedia(4);
        Post withMedia_five = provider.postWithMedia(5);
        Post withMedia_six = provider.postWithMedia(6);
        Post withMedia_seven = provider.postWithMedia(7);
        Post withMedia_eight = provider.postWithMedia(8);

        when(postRepository.post(post.id())).thenReturn(post);
        when(postRepository.post(withLink.id())).thenReturn(withLink);
        when(postRepository.post(withMedia_one.id())).thenReturn(withMedia_one);
        when(postRepository.post(withMedia_two.id())).thenReturn(withMedia_two);
        when(postRepository.post(withMedia_three.id())).thenReturn(withMedia_three);
        when(postRepository.post(withMedia_four.id())).thenReturn(withMedia_four);
        when(postRepository.post(withMedia_five.id())).thenReturn(withMedia_five);
        when(postRepository.post(withMedia_six.id())).thenReturn(withMedia_six);
        when(postRepository.post(withMedia_seven.id())).thenReturn(withMedia_seven);
        when(postRepository.post(withMedia_eight.id())).thenReturn(withMedia_eight);

        // When
        useCase.setPostId(post.id());             Post result = useCase.doAction();
        useCase.setPostId(withLink.id());         Post resultLink = useCase.doAction();
        useCase.setPostId(withMedia_one.id());    Post resultMedia_one = useCase.doAction();
        useCase.setPostId(withMedia_two.id());    Post resultMedia_two = useCase.doAction();
        useCase.setPostId(withMedia_three.id());  Post resultMedia_three = useCase.doAction();
        useCase.setPostId(withMedia_four.id());   Post resultMedia_four = useCase.doAction();
        useCase.setPostId(withMedia_five.id());   Post resultMedia_five = useCase.doAction();
        useCase.setPostId(withMedia_six.id());    Post resultMedia_six = useCase.doAction();
        useCase.setPostId(withMedia_seven.id());  Post resultMedia_seven = useCase.doAction();
        useCase.setPostId(withMedia_eight.id());  Post resultMedia_eight = useCase.doAction();

        // Then
        verify(postRepository).post(post.id());
        verify(postRepository).post(withLink.id());
        verify(postRepository).post(withMedia_one.id());
        verify(postRepository).post(withMedia_two.id());
        verify(postRepository).post(withMedia_three.id());
        verify(postRepository).post(withMedia_four.id());
        verify(postRepository).post(withMedia_five.id());
        verify(postRepository).post(withMedia_six.id());
        verify(postRepository).post(withMedia_seven.id());
        verify(postRepository).post(withMedia_eight.id());
        verifyNoMoreInteractions(postRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertEquals(post, result);
        Assert.assertEquals(withLink, resultLink);
        Assert.assertEquals(withMedia_one, resultMedia_one);
        Assert.assertEquals(withMedia_two, resultMedia_two);
        Assert.assertEquals(withMedia_three, resultMedia_three);
        Assert.assertEquals(withMedia_four, resultMedia_four);
        Assert.assertEquals(withMedia_five, resultMedia_five);
        Assert.assertEquals(withMedia_six, resultMedia_six);
        Assert.assertEquals(withMedia_seven, resultMedia_seven);
        Assert.assertEquals(withMedia_eight, resultMedia_eight);
    }

    @Test
    public void execute_BadId_GetNullPost() {
        // Given
        when(postRepository.post(Constant.BAD_ID)).thenReturn(null);

        // When
        Post result = useCase.doAction();

        // Then
        verify(postRepository).post(Constant.BAD_ID);
        verifyNoMoreInteractions(postRepository);
        verifyZeroInteractions(threadExecutor);
        verifyZeroInteractions(postExecuteScheduler);

        Assert.assertNull(result);
    }
}