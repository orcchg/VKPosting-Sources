package com.orcchg.vikstra.data.source.direct.vkontakte;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orcchg.vikstra.data.source.direct.vkontakte.migration.VkAttachMigration;
import com.orcchg.vikstra.data.source.direct.vkontakte.model.VkApiPhotoDBO;
import com.orcchg.vikstra.data.source.direct.vkontakte.model.mapper.VkApiPhotoToDboMapper;
import com.orcchg.vikstra.data.source.direct.vkontakte.model.populator.VkApiPhotoToDboPopulator;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.util.Constant;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmConfiguration;

@Singleton
public class VkAttachLocalCache {

    private final VkApiPhotoToDboMapper vkApiPhotoToDboMapper;
    private final VkApiPhotoToDboPopulator vkApiPhotoToDboPopulator;

    private final RealmConfiguration realmConfiguration;

    @Inject
    VkAttachLocalCache(VkApiPhotoToDboMapper vkApiPhotoToDboMapper,
                       VkApiPhotoToDboPopulator vkApiPhotoToDboPopulator) {
        this.vkApiPhotoToDboMapper = vkApiPhotoToDboMapper;
        this.vkApiPhotoToDboPopulator = vkApiPhotoToDboPopulator;
        this.realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(1)  // bumped version up, previous: 0
                .migration(new VkAttachMigration())
                .build();
    }

    /* API */
    // --------------------------------------------------------------------------------------------
    void retain(List<Media> media, List<Media> cached, List<Media> retained) {
        for (Media item : media) {
            if (!hasPhoto(item.id())) {
                retained.add(item);
            } else {
                cached.add(item);
            }
        }
    }

    /**
     * Searches for photo stored in cache corresponding to the {@param path} and returns it's id
     * assigned by Vkontakte if this photo had been previously uploaded.
     */
    @DebugLog
    public long getIdByPhotoPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            Realm realm = Realm.getInstance(realmConfiguration);
            VkApiPhotoDBO dbo = realm.where(VkApiPhotoDBO.class).equalTo(VkApiPhotoDBO.COLUMN_PATH, path).findFirst();
            realm.close();
            if (dbo != null) return dbo.id;
        }
        return Constant.BAD_ID;
    }

    /* Read */
    // ------------------------------------------
    @DebugLog
    boolean hasPhoto(long mediaId) {
        if (mediaId != Constant.BAD_ID) {
            Realm realm = Realm.getInstance(realmConfiguration);
            VkApiPhotoDBO dbo = realm.where(VkApiPhotoDBO.class).equalTo(VkApiPhotoDBO.COLUMN_ID, mediaId).findFirst();
            realm.close();
            return dbo != null;
        }
        return false;
    }

    @DebugLog @Nullable
    VKApiPhoto readPhoto(long mediaId) {
        if (mediaId != Constant.BAD_ID) {
            Realm realm = Realm.getInstance(realmConfiguration);
            VKApiPhoto model = null;
            VkApiPhotoDBO dbo = realm.where(VkApiPhotoDBO.class).equalTo(VkApiPhotoDBO.COLUMN_ID, mediaId).findFirst();
            if (dbo != null) model = vkApiPhotoToDboMapper.mapBack(dbo);
            realm.close();
            return model;
        }
        return null;
    }

    @DebugLog
    VKAttachments readPhotos(List<Media> cache) {
        VKAttachments attachments = new VKAttachments();
        if (cache != null && !cache.isEmpty()) {
            for (Media media : cache) {
                VKApiPhoto photo = readPhoto(media.id());
                if (photo != null) attachments.add(photo);
            }
        }
        return attachments;
    }

    /* Write */
    // ------------------------------------------
    /**
     * Image will be stored in cache only if it has been uploaded to Vkontakte, so it has some id.
     */
    @DebugLog
    void writePhoto(String path, VKApiPhoto vkPhoto) {
        Realm realm = Realm.getInstance(realmConfiguration);
        realm.executeTransaction((xrealm) -> {
            VkApiPhotoDBO dbo = xrealm.createObject(VkApiPhotoDBO.class);
            vkApiPhotoToDboPopulator.populate(vkPhoto, dbo);
            dbo.path = path;
        });
        realm.close();
    }
}
