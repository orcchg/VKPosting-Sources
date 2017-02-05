package com.orcchg.vikstra.data.source.direct.vkontakte.migration;

import com.orcchg.vikstra.data.source.direct.vkontakte.model.VkApiPhotoDBO;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Migration for {@link VkApiPhotoDBO} in {@link io.realm.Realm}.
 *
 * Example:
 * {@see https://github.com/realm/realm-java/blob/master/examples/migrationExample/src/main/java/io/realm/examples/realmmigrationexample/model/Migration.java}
 */
public class VkAttachMigration implements RealmMigration {

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        /**
         * {@link VkApiPhotoDBO} schema migration.
         *
         * Version 0
         * --------------------------------------
         *     long id;
         *     String attachString;
         *
         *
         * Version 1
         * --------------------------------------
         *     long id;
         *  ++ String path;
         *     String attachString;
         */
        if (oldVersion == 0) {
            RealmObjectSchema objectSchema = schema.get("VkApiPhotoDBO");
            objectSchema.addField(VkApiPhotoDBO.COLUMN_PATH, String.class);
            ++oldVersion;
        }
    }
}
