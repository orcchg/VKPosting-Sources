package com.orcchg.vikstra.data.source.local.report;

import com.orcchg.vikstra.data.source.local.model.GroupReportDBO;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import timber.log.Timber;

/**
 * Migration for {@link GroupReportDBO} in {@link io.realm.Realm}.
 */
@Singleton
public class ReportMigration implements RealmMigration {

    @Inject
    public ReportMigration() {
        Timber.d("ReportMigration ctor");
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        /**
         * {@link GroupReportDBO} schema migration.
         *
         * Version 0
         * --------------------------------------
         *     long id;
         *     int errorCode;
         *     GroupDBO group;
         *     long timestamp;
         *     long wallPostId;
         *
         * Version 1
         * --------------------------------------
         *     long id;
         *  ++ boolean cancelled;
         *     int errorCode;
         *     GroupDBO group;
         *     long timestamp;
         *     long wallPostId;
         */
        if (oldVersion == 0) {
            RealmObjectSchema objectSchema = schema.get("GroupReportDBO");
            objectSchema.addField(GroupReportDBO.COLUMN_CANCELLED, boolean.class);
            ++oldVersion;
        }
    }
}
