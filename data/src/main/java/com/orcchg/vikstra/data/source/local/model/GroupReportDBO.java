package com.orcchg.vikstra.data.source.local.model;

import io.realm.RealmObject;

public class GroupReportDBO extends RealmObject {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CANCELLED = "cancelled";
    public static final String COLUMN_ERROR_CODE = "errorCode";
    public static final String COLUMN_GROUP = "group";
    public static final String COLUMN_REVERTED = "reverted";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_WALL_POST_ID = "wallPostId";

    public long id;
    public boolean cancelled;
    public int errorCode;
    public GroupDBO group;
    public boolean reverted;
    public long timestamp;
    public long wallPostId;
}
