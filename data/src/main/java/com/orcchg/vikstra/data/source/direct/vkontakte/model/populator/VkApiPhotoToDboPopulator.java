package com.orcchg.vikstra.data.source.direct.vkontakte.model.populator;

import com.orcchg.vikstra.data.source.direct.vkontakte.model.VkApiPhotoDBO;
import com.orcchg.vikstra.domain.model.mapper.Populator;
import com.vk.sdk.api.model.VKApiPhoto;

import javax.inject.Inject;

public class VkApiPhotoToDboPopulator implements Populator<VKApiPhoto, VkApiPhotoDBO> {

    @Inject
    public VkApiPhotoToDboPopulator() {
    }

    @Override
    public void populate(VKApiPhoto object, VkApiPhotoDBO dbo) {
        dbo.id = object.getId();
        dbo.attachString = object.toAttachmentString().toString();
    }
}
