package com.orcchg.vikstra.data.source.local.model;

import io.realm.RealmList;
import io.realm.RealmObject;

public class GroupReportBundleDBO extends RealmObject {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_GROUP_REPORTS = "groupReports";
    public static final String COLUMN_KEYWORD_BUNDLE_ID = "keywordBundleId";
    public static final String COLUMN_POST_ID = "postId";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public long id;
    public long userId;
    public RealmList<GroupReportDBO> groupReports;
    public long keywordBundleId;
    public long postId;
    public long timestamp;
}
