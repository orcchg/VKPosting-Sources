package com.orcchg.vikstra.data.source.local.post;

import com.orcchg.vikstra.data.source.local.model.PostDBO;

import javax.inject.Inject;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import timber.log.Timber;

/**
 * Migration for {@link PostDBO} in {@link io.realm.Realm}.
 */
public class PostMigration implements RealmMigration {

    @Inject
    public PostMigration() {
        Timber.d("PostMigration ctor");
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        /**
         * {@link PostDBO} schema migration.
         *
         * Version 0, 1, 2
         * --------------------------------------
         *     public long id;
         *     String description;
         *     RealmList<MediaDBO> media;
         *     long timestamp;
         *     String title;
         *
         * Version 3
         * --------------------------------------
         *     public long id;
         *     String description;
         *  ++ String link;
         *     RealmList<MediaDBO> media;
         *     long timestamp;
         *     String title;
         */
        if (oldVersion < 3) {
            RealmObjectSchema objectSchema = schema.get("PostDBO");
            objectSchema.addField(PostDBO.COLUMN_LINK, String.class);
            oldVersion = 3;
        }
    }
}
