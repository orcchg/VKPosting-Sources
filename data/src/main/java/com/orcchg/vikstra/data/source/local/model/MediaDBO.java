package com.orcchg.vikstra.data.source.local.model;

import io.realm.RealmObject;

public class MediaDBO extends RealmObject {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_URL = "url";

    public long id;
    public String url;
}
