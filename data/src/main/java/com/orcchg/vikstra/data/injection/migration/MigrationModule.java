package com.orcchg.vikstra.data.injection.migration;

import com.orcchg.vikstra.data.source.direct.vkontakte.migration.VkAttachMigration;
import com.orcchg.vikstra.data.source.local.post.PostMigration;
import com.orcchg.vikstra.data.source.local.report.ReportMigration;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.RealmConfiguration;

@Module
public class MigrationModule {

    // TODO: solve singleton problem
    private static RealmConfiguration sRealmConfiguration;

    @Provides @Singleton
    PostMigration providePostMigration() {
        return new PostMigration();
    }

    @Provides @Singleton
    ReportMigration provideReportMigration() {
        return new ReportMigration();
    }

    @Provides @Singleton
    VkAttachMigration provideVkAttachMigration() {
        return new VkAttachMigration();
    }

    @Provides @Singleton
    Migration provideMigration(PostMigration postMigration, ReportMigration reportMigration, VkAttachMigration vkAttachMigration) {
        return new Migration(postMigration, reportMigration, vkAttachMigration);
    }

    @Provides @Singleton
    RealmConfiguration provideRealmConfiguration(Migration migration) {
        if (sRealmConfiguration == null) {
            sRealmConfiguration = new RealmConfiguration.Builder()
                    .schemaVersion(3)  // bumped version up, previous: 2
                    .migration(migration)
                    .build();
        }
        return sRealmConfiguration;
    }
}
