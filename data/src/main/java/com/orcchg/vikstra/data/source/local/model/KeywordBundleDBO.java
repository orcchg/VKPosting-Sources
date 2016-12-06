package com.orcchg.vikstra.data.source.local.model;

import io.realm.RealmList;
import io.realm.RealmObject;

public class KeywordBundleDBO extends RealmObject {
    public long id;
    public String title;
    public RealmList<KeywordDBO> keywords;
}
