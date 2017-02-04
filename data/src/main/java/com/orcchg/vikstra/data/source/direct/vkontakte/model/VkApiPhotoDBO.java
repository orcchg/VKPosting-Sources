package com.orcchg.vikstra.data.source.direct.vkontakte.model;

import io.realm.RealmObject;

public class VkApiPhotoDBO extends RealmObject {
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PATH = "path";
    public static final String COLUMN_ATTACH = "attachString";

    public long id;
    public String path;
    public String attachString;
}
