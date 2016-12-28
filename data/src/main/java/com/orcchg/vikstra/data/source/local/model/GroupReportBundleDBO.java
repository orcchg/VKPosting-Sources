package com.orcchg.vikstra.data.source.local.model;

import io.realm.RealmList;
import io.realm.RealmObject;

public class GroupReportBundleDBO extends RealmObject {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_GROUP_REPORTS = "group_reports";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    public long id;
    public RealmList<GroupReportDBO> groupReports;
    public long timestamp;
}
