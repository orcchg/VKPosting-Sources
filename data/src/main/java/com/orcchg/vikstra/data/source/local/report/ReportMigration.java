package com.orcchg.vikstra.data.source.local.report;

import com.orcchg.vikstra.data.source.local.model.GroupReportDBO;
import com.orcchg.vikstra.data.source.local.model.GroupReportBundleDBO;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;
import timber.log.Timber;

/**
 * Migration for {@link GroupReportDBO} and {@link GroupReportBundleDBO} in {@link io.realm.Realm}.
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
         * Version 1, 2, 3
         * --------------------------------------
         *     long id;
         *  ++ boolean cancelled;
         *     int errorCode;
         *     GroupDBO group;
         *     long timestamp;
         *     long wallPostId;
         *
         * Version 4, 5
         * --------------------------------------
         *     long id;
         *     boolean cancelled;
         *     int errorCode;
         *     GroupDBO group;
         *  ++ boolean reverted;
         *     long timestamp;
         *     long wallPostId;
         */
        if (oldVersion == 0) {
            RealmObjectSchema objectSchema = schema.get("GroupReportDBO");
            objectSchema.addField(GroupReportDBO.COLUMN_CANCELLED, boolean.class);
            oldVersion = 3;
        }

        if (oldVersion < 4) {
            RealmObjectSchema objectSchema = schema.get("GroupReportDBO");
            objectSchema.addField(GroupReportDBO.COLUMN_REVERTED, boolean.class);
            oldVersion = 4;
        }

        // --------------------------------------
        /**
         * {@link GroupReportBundleDBO} schema migration.
         *
         * Version 0, 1, 2, 3, 4
         * --------------------------------------
         *     long id;
         *     RealmList<GroupReportDBO> groupReports
         *     long timestamp
         *
         * Version 5
         * --------------------------------------
         *     long id;
         *     RealmList<GroupReportDBO> groupReports
         *  ++ long keywordBundleId
         *  ++ long postId
         *     long timestamp
         */
        if (oldVersion < 5) {
            RealmObjectSchema objectSchema = schema.get("GroupReportBundleDBO");
            objectSchema.addField(GroupReportBundleDBO.COLUMN_KEYWORD_BUNDLE_ID, long.class);
            objectSchema.addField(GroupReportBundleDBO.COLUMN_POST_ID, long.class);
            oldVersion = 5;
        }
    }
}
