package com.orcchg.vikstra.data.source.local.model;

import io.realm.RealmObject;

public class GroupDBO extends RealmObject {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CAN_POST = "can_post";
    public static final String COLUMN_IS_SELECTED = "is_selected";
    public static final String COLUMN_KEYWORD = "keyword";
    public static final String COLUMN_MEMBERS_COUNT = "members_count";
    public static final String COLUMN_NAME = "name";

    public long id;
    public boolean canPost;
    public boolean isSelected;
    public KeywordDBO keyword;
    public int membersCount;
    public String name;
}
