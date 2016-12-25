package com.orcchg.vikstra.data.source.local.model;

import io.realm.RealmList;
import io.realm.RealmObject;

public class KeywordBundleDBO extends RealmObject {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_COUNT_SELECTED_GROUPS = "countSelectedGroups";
    public static final String COLUMN_COUNT_TOTAL_GROUPS = "countTotalGroups";
    public static final String COLUMN_GROUP_BUNDLE_ID = "groupBundleId";
    public static final String COLUMN_KEYWORDS = "keywords";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TITLE = "title";

    public long id;
    public int countSelectedGroups;
    public int countTotalGroups;
    public long groupBundleId;
    public RealmList<KeywordDBO> keywords;
    public long timestamp;
    public String title;
}
