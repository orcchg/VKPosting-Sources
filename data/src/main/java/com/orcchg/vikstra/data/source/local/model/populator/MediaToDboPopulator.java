package com.orcchg.vikstra.data.source.local.model.populator;

import com.orcchg.vikstra.data.source.local.model.MediaDBO;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.mapper.Populator;

import javax.inject.Inject;

public class MediaToDboPopulator implements Populator<Media, MediaDBO> {

    @Inject
    public MediaToDboPopulator() {
    }

    @Override
    public void populate(Media object, MediaDBO dbo) {
        dbo.id = object.id();
        dbo.url = object.url();
    }
}
