package com.orcchg.vikstra.data.source.local.model;

import io.realm.RealmList;
import io.realm.RealmObject;

public class KeywordBundleDBO extends RealmObject {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_KEYWORDS = "keywords";

    public long id;
    public String title;
    public RealmList<KeywordDBO> keywords;
}
