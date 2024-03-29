package com.orcchg.vikstra.data.injection.migration;

import com.orcchg.vikstra.data.source.direct.vkontakte.migration.VkAttachMigration;
import com.orcchg.vikstra.data.source.local.post.PostMigration;
import com.orcchg.vikstra.data.source.local.report.ReportMigration;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import timber.log.Timber;

@Singleton
public class Migration implements RealmMigration {

    PostMigration postMigration;
    ReportMigration reportMigration;
    VkAttachMigration vkAttachMigration;

    @Inject
    Migration(PostMigration postMigration, ReportMigration reportMigration, VkAttachMigration vkAttachMigration) {
        Timber.d("Migration ctor");
        this.postMigration = postMigration;
        this.reportMigration = reportMigration;
        this.vkAttachMigration = vkAttachMigration;
    }

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        postMigration.migrate(realm, oldVersion, newVersion);
        reportMigration.migrate(realm, oldVersion, newVersion);
        vkAttachMigration.migrate(realm, oldVersion, newVersion);
    }

    @Override
    public int hashCode() {
        return 17;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Migration);
    }
}
