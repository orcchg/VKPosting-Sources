package com.orcchg.vikstra.domain.model;

import com.orcchg.vikstra.domain.BaseTest;
import com.orcchg.vikstra.domain.sample.GroupBundleProvider;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import javax.inject.Inject;

public class GroupBundleTest extends BaseTest {

    @Inject GroupBundleProvider provider;

    @Before
    public void setUp() throws Exception {
        super.setUp();
//        testComponent.inject(this);
        provider = new GroupBundleProvider();
    }

    /* Tests */
    // --------------------------------------------------------------------------------------------
    @Test
    public void selectedCount() throws Exception {
        GroupBundle one = provider.groupBundle_oneKeyword();
        int index = 0;
        for (Group group : one.groups()) {
            ++index;
            if (index % 3 == 0) group.setSelected(true);
        }
        Assert.assertEquals(one.groups().size() / 3, one.selectedCount());
    }

    @Test
    public void splitGroupsByKeywords() throws Exception {
        // Given
        GroupBundle one   = provider.groupBundle_oneKeyword();
        GroupBundle two   = provider.groupBundle_twoKeywords();
        GroupBundle three = provider.groupBundle_threeKeywords();
        GroupBundle four  = provider.groupBundle_fourKeywords();
        GroupBundle five  = provider.groupBundle_fiveKeywords();

        // When
        List<List<Group>> splitOne   = one.splitGroupsByKeywords();
        List<List<Group>> splitTwo   = two.splitGroupsByKeywords();
        List<List<Group>> splitThree = three.splitGroupsByKeywords();
        List<List<Group>> splitFour  = four.splitGroupsByKeywords();
        List<List<Group>> splitFive  = five.splitGroupsByKeywords();

        // Then
        Assert.assertEquals(1, splitOne.size());
        Assert.assertEquals(2, splitTwo.size());
        Assert.assertEquals(3, splitThree.size());
        Assert.assertEquals(4, splitFour.size());
        Assert.assertEquals(5, splitFive.size());

        // sum of sizes of separated lists must be equal to the total Group-s in each GroupBundle
        Assert.assertEquals(one.groups().size(),   splitOne.get(0).size());
        Assert.assertEquals(two.groups().size(),   splitTwo.get(0).size()   + splitTwo.get(1).size());
        Assert.assertEquals(three.groups().size(), splitThree.get(0).size() + splitThree.get(1).size() + splitThree.get(2).size());
        Assert.assertEquals(four.groups().size(),  splitFour.get(0).size()  + splitFour.get(1).size()  + splitFour.get(2).size() + splitFour.get(3).size());
        Assert.assertEquals(five.groups().size(),  splitFive.get(0).size()  + splitFive.get(1).size()  + splitFive.get(2).size() + splitFive.get(3).size() + splitFive.get(4).size());

        // all Group-s within the same list must have the same Keyword-s
        for (Group group : splitOne.get(0)) Assert.assertEquals(GroupBundleProvider.KEYWORD_11, group.keyword().keyword());

        for (Group group : splitTwo.get(0)) Assert.assertEquals(GroupBundleProvider.KEYWORD_21, group.keyword().keyword());
        for (Group group : splitTwo.get(1)) Assert.assertEquals(GroupBundleProvider.KEYWORD_22, group.keyword().keyword());

        for (Group group : splitThree.get(0)) Assert.assertEquals(GroupBundleProvider.KEYWORD_31, group.keyword().keyword());
        for (Group group : splitThree.get(1)) Assert.assertEquals(GroupBundleProvider.KEYWORD_32, group.keyword().keyword());
        for (Group group : splitThree.get(2)) Assert.assertEquals(GroupBundleProvider.KEYWORD_33, group.keyword().keyword());

        for (Group group : splitFour.get(0)) Assert.assertEquals(GroupBundleProvider.KEYWORD_41, group.keyword().keyword());
        for (Group group : splitFour.get(1)) Assert.assertEquals(GroupBundleProvider.KEYWORD_42, group.keyword().keyword());
        for (Group group : splitFour.get(2)) Assert.assertEquals(GroupBundleProvider.KEYWORD_43, group.keyword().keyword());
        for (Group group : splitFour.get(3)) Assert.assertEquals(GroupBundleProvider.KEYWORD_44, group.keyword().keyword());

        for (Group group : splitFive.get(0)) Assert.assertEquals(GroupBundleProvider.KEYWORD_51, group.keyword().keyword());
        for (Group group : splitFive.get(1)) Assert.assertEquals(GroupBundleProvider.KEYWORD_52, group.keyword().keyword());
        for (Group group : splitFive.get(2)) Assert.assertEquals(GroupBundleProvider.KEYWORD_53, group.keyword().keyword());
        for (Group group : splitFive.get(3)) Assert.assertEquals(GroupBundleProvider.KEYWORD_54, group.keyword().keyword());
        for (Group group : splitFive.get(4)) Assert.assertEquals(GroupBundleProvider.KEYWORD_55, group.keyword().keyword());
    }
}
