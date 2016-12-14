package com.orcchg.vikstra.data.source.local.model.mapper;

import com.orcchg.vikstra.data.source.local.model.MediaDBO;
import com.orcchg.vikstra.data.source.local.model.populator.MediaToDboPopulator;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.mapper.DuplexMapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MediaToDboMapper implements DuplexMapper<Media, MediaDBO> {

    private final MediaToDboPopulator mediaToDboPopulator;

    @Inject
    public MediaToDboMapper(MediaToDboPopulator mediaToDboPopulator) {
        this.mediaToDboPopulator = mediaToDboPopulator;
    }

    /* Direct mapping */
    // ------------------------------------------
    @Override
    public MediaDBO map(Media object) {
        MediaDBO dbo = new MediaDBO();
        mediaToDboPopulator.populate(object, dbo);
        return dbo;
    }

    @Override
    public List<MediaDBO> map(List<Media> list) {
        List<MediaDBO> mapped = new ArrayList<>();
        for (Media item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }

    /* Backward mapping */
    // ------------------------------------------
    @Override
    public Media mapBack(MediaDBO object) {
        return Media.builder()
                .setId(object.id)
                .setUrl(object.url)
                .build();
    }
}
