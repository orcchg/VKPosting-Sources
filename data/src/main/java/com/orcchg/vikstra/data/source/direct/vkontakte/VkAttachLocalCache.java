package com.orcchg.vikstra.data.source.direct.vkontakte;

import android.support.annotation.Nullable;

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

@Singleton
public class VkAttachLocalCache {

    private final VkApiPhotoToDboMapper vkApiPhotoToDboMapper;
    private final VkApiPhotoToDboPopulator vkApiPhotoToDboPopulator;

    @Inject
    VkAttachLocalCache(VkApiPhotoToDboMapper vkApiPhotoToDboMapper,
                       VkApiPhotoToDboPopulator vkApiPhotoToDboPopulator) {
        this.vkApiPhotoToDboMapper = vkApiPhotoToDboMapper;
        this.vkApiPhotoToDboPopulator = vkApiPhotoToDboPopulator;
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

    /* Read */
    // ------------------------------------------
    @DebugLog
    boolean hasPhoto(long mediaId) {
        if (mediaId != Constant.BAD_ID) {
            Realm realm = Realm.getDefaultInstance();
            VkApiPhotoDBO dbo = realm.where(VkApiPhotoDBO.class).equalTo(VkApiPhotoDBO.COLUMN_ID, mediaId).findFirst();
            realm.close();
            return dbo != null;
        }
        return false;
    }

    @DebugLog @Nullable
    VKApiPhoto readPhoto(long mediaId) {
        if (mediaId != Constant.BAD_ID) {
            Realm realm = Realm.getDefaultInstance();
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
    @DebugLog
    void writePhoto(long mediaId, VKApiPhoto vkPhoto) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction((xrealm) -> {
            VkApiPhotoDBO dbo = xrealm.createObject(VkApiPhotoDBO.class);
            vkApiPhotoToDboPopulator.populate(vkPhoto, dbo);
        });
        realm.close();
    }
}
