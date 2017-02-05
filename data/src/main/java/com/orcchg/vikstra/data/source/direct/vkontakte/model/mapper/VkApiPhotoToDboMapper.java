package com.orcchg.vikstra.data.source.direct.vkontakte.model.mapper;

import com.orcchg.vikstra.data.source.direct.vkontakte.model.VkApiPhotoDBO;
import com.orcchg.vikstra.data.source.direct.vkontakte.model.populator.VkApiPhotoToDboPopulator;
import com.orcchg.vikstra.domain.model.mapper.DuplexMapper;
import com.vk.sdk.api.model.VKApiPhoto;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class VkApiPhotoToDboMapper implements DuplexMapper<VKApiPhoto, VkApiPhotoDBO> {

    private final VkApiPhotoToDboPopulator vkApiPhotoToDboPopulator;

    @Inject
    public VkApiPhotoToDboMapper(VkApiPhotoToDboPopulator vkApiPhotoToDboPopulator) {
        this.vkApiPhotoToDboPopulator = vkApiPhotoToDboPopulator;
    }

    /* Direct mapping */
    // ------------------------------------------
    @Override
    public VkApiPhotoDBO map(VKApiPhoto object) {
        VkApiPhotoDBO dbo = new VkApiPhotoDBO();
        vkApiPhotoToDboPopulator.populate(object, dbo);
        return dbo;
    }

    @Override
    public List<VkApiPhotoDBO> map(List<VKApiPhoto> list) {
        List<VkApiPhotoDBO> mapped = new ArrayList<>();
        for (VKApiPhoto item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }

    /* Backward mapping */
    // ------------------------------------------
    @Override
    public VKApiPhoto mapBack(VkApiPhotoDBO object) {
        return new VKApiPhoto(object.attachString);
    }

    @Override
    public List<VKApiPhoto> mapBack(List<VkApiPhotoDBO> list) {
        List<VKApiPhoto> mapped = new ArrayList<>();
        for (VkApiPhotoDBO item : list) {
            mapped.add(mapBack(item));
        }
        return mapped;
    }
}
