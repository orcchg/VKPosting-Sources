package com.orcchg.vikstra.domain.sample;

import com.orcchg.vikstra.domain.model.Group;
import com.orcchg.vikstra.domain.model.GroupBundle;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

public class GroupBundleProvider {
    private static long BASE_ID = Constant.INIT_ID;

    private Random rng = new Random();

    @Inject
    public GroupBundleProvider() {
    }

    public static final String KEYWORD_11 = "Universal";
    public static final String KEYWORD_21 = "Fiction";
    public static final String KEYWORD_22 = "Magazine";
    public static final String KEYWORD_31 = "Android";
    public static final String KEYWORD_32 = "iOS";
    public static final String KEYWORD_33 = "Windows";
    public static final String KEYWORD_41 = "Burger";
    public static final String KEYWORD_42 = "Cheese";
    public static final String KEYWORD_43 = "Meat";
    public static final String KEYWORD_44 = "Cola";
    public static final String KEYWORD_51 = "Football";
    public static final String KEYWORD_52 = "Hockey";
    public static final String KEYWORD_53 = "Sprint";
    public static final String KEYWORD_54 = "Slalom";
    public static final String KEYWORD_55 = "Tennis";

    // ------------------------------------------
    public Group group() {
        long id = BASE_ID++;
        String name = "group_" + rng.nextInt() + "_" + id;
        return Group.builder()
                .setId(id)
                .setCanPost(rng.nextBoolean())
                .setKeyword(Keyword.create("keyword_" + rng.nextInt()))
                .setLink("http://" + name + ".com")
                .setMembersCount(rng.nextInt(50_000))
                .setName(name)
                .setScreenName("screen_" + name)
                .setWebSite(rng.nextBoolean() ? null : "http://site_" + name + ".pro")
                .build();
    }

    public Collection<Group> groups(String... keywords) {
        Collection<Group> groups = new ArrayList<>();
        for (int i = 0; i < keywords.length; ++i) {
            for (int j = 0; j < 5 + i; ++j) {
                long id = BASE_ID++ + 1000 * (i + 1) + j + 1;
                String name = "group_" + keywords[i] + "_" + id;
                Group group = Group.builder()
                        .setId(id)
                        .setCanPost(j % 2 == 0)
                        .setKeyword(Keyword.create(keywords[i]))
                        .setLink("http://" + name + ".com")
                        .setMembersCount(rng.nextInt(50_000))
                        .setName(name)
                        .setScreenName("screen_" + name)
                        .setWebSite(j % 4 == 0 ? null : "http://site_" + name + ".pro")
                        .build();
                groups.add(group);
            }
        }
        return groups;
    }

    // ------------------------------------------
    public GroupBundle groupBundle_oneKeyword() {
        return GroupBundle.builder()
                .setId(1001)
                .setGroups(groups(KEYWORD_11))
                .setKeywordBundleId(Constant.BAD_ID)
                .setTimestamp(1_456_789_101)
                .setTitle("Space")
                .build();
    }

    public GroupBundle groupBundle_twoKeywords() {
        return GroupBundle.builder()
                .setId(1002)
                .setGroups(groups(KEYWORD_21, KEYWORD_22))
                .setKeywordBundleId(Constant.BAD_ID)
                .setTimestamp(1_456_789_101)
                .setTitle("Books")
                .build();
    }

    public GroupBundle groupBundle_threeKeywords() {
        return GroupBundle.builder()
                .setId(1003)
                .setGroups(groups(KEYWORD_31, KEYWORD_32, KEYWORD_33))
                .setKeywordBundleId(Constant.BAD_ID)
                .setTimestamp(1_456_789_101)
                .setTitle("Mobile platforms")
                .build();
    }

    public GroupBundle groupBundle_fourKeywords() {
        return GroupBundle.builder()
                .setId(1004)
                .setGroups(groups(KEYWORD_41, KEYWORD_42, KEYWORD_43, KEYWORD_44))
                .setKeywordBundleId(Constant.BAD_ID)
                .setTimestamp(1_456_789_101)
                .setTitle("Food & Drink")
                .build();
    }

    public GroupBundle groupBundle_fiveKeywords() {
        return GroupBundle.builder()
                .setId(1005)
                .setGroups(groups(KEYWORD_51, KEYWORD_52, KEYWORD_53, KEYWORD_54, KEYWORD_55))
                .setKeywordBundleId(Constant.BAD_ID)
                .setTimestamp(1_456_789_101)
                .setTitle("Sport")
                .build();
    }

    public List<GroupBundle> groupBundles() {
        List<GroupBundle> list = new ArrayList<>();
        list.add(groupBundle_oneKeyword());
        list.add(groupBundle_twoKeywords());
        list.add(groupBundle_threeKeywords());
        list.add(groupBundle_fourKeywords());
        list.add(groupBundle_fiveKeywords());
        return list;
    }
}
