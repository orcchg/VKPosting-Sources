package com.orcchg.vikstra.data.source.local.model;

import io.realm.RealmList;
import io.realm.RealmObject;

public class GroupBundleDBO extends RealmObject {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_GROUPS = "groups";
    public static final String COLUMN_KEYWORD_BUNDLE_ID = "keywordBundleId";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TITLE = "title";

    public long id;
    public RealmList<GroupDBO> groups;
    public long keywordBundleId;
    public long timestamp;
    public String title;
}
