package com.orcchg.vikstra.data.source.repository.keyword;

import com.orcchg.vikstra.data.source.local.keyword.KeywordDatabase;
import com.orcchg.vikstra.data.source.remote.keyword.KeywordCloud;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.sample.KeywordBundleProvider;
import com.orcchg.vikstra.domain.util.Constant;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.matchers.GreaterThan;

import javax.inject.Inject;

import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KeywordRepositoryImplTest {

    @Mock KeywordCloud keywordCloud;
    @Mock KeywordDatabase keywordDatabase;

    private KeywordRepositoryImpl keywordRepository;
    private Matcher<Long> notBadIdMatcher;

    @Inject KeywordBundleProvider provider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        keywordRepository = new KeywordRepositoryImpl(keywordCloud, keywordDatabase);
        notBadIdMatcher = new GreaterThan<>(Constant.BAD_ID);
    }

    /* Tests */
    // --------------------------------------------------------------------------------------------

    /* Create */
    // ------------------------------------------

    /* Read */
    // ------------------------------------------
    @Test
    public void keywords_SomeValidId_GetValidKeywordBundle() {
        // Given
        KeywordBundle initOne   = provider.keywordBundle_oneWord();
        KeywordBundle initTwo   = provider.keywordBundle_twoWords();
        KeywordBundle initThree = provider.keywordBundle_threeWords();
        KeywordBundle initFour  = provider.keywordBundle_fourWords();
        KeywordBundle initFive  = provider.keywordBundle_fiveWords();
        KeywordBundle initSix   = provider.keywordBundle_sixWords();
        KeywordBundle initSeven = provider.keywordBundle_sevenWords();

        when(keywordDatabase.keywords(longThat(notBadIdMatcher)))
                .thenReturn(initOne, initTwo, initThree, initFour, initFive, initSix, initSeven);

        // When
        KeywordBundle one   = keywordRepository.keywords(1001);
        KeywordBundle two   = keywordRepository.keywords(1002);
        KeywordBundle three = keywordRepository.keywords(1003);
        KeywordBundle four  = keywordRepository.keywords(1004);
        KeywordBundle five  = keywordRepository.keywords(1005);
        KeywordBundle six   = keywordRepository.keywords(1006);
        KeywordBundle seven = keywordRepository.keywords(1007);

        // Then
        verify(keywordDatabase).keywords(1001);
        verify(keywordDatabase).keywords(1002);
        verify(keywordDatabase).keywords(1003);
        verify(keywordDatabase).keywords(1004);
        verify(keywordDatabase).keywords(1005);
        verify(keywordDatabase).keywords(1006);
        verify(keywordDatabase).keywords(1007);

        Assert.assertEquals(initOne, one);
        Assert.assertEquals(initTwo, two);
        Assert.assertEquals(initThree, three);
        Assert.assertEquals(initFour, four);
        Assert.assertEquals(initFive, five);
        Assert.assertEquals(initSix, six);
        Assert.assertEquals(initSeven, seven);
    }

    @Test
    public void keywords_BadId_GetNullKeywordBundle() {
        // Given
        when(keywordDatabase.keywords(Constant.BAD_ID)).thenReturn(null);

        // When
        KeywordBundle none = keywordRepository.keywords(Constant.BAD_ID);

        // Then
        verify(keywordDatabase).keywords(Constant.BAD_ID);

        Assert.assertNull(none);
    }

    @Test
    public void keywords_Limit10Offset5_GetListOfSpecificKeywordBundles() {
        // Given

        // When

        // Then
    }

    @Test
    public void keywords_NoLimitNoOffset_GetListOfAllKeywordBundles() {
        // Given

        // When

        // Then
    }

    /* Update */
    // ------------------------------------------

    /* Delete */
    // ------------------------------------------
}