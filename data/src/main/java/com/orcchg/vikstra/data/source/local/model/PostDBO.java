package com.orcchg.vikstra.data.source.local.model;

import io.realm.RealmList;
import io.realm.RealmObject;

public class PostDBO extends RealmObject {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_MEDIA = "media";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TITLE = "title";

    public long id;
    public String description;
    public RealmList<MediaDBO> media;
    public long timestamp;
    public String title;
}
