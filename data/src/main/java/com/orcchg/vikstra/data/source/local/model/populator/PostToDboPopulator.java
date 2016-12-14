package com.orcchg.vikstra.data.source.local.model.populator;

import com.orcchg.vikstra.data.source.local.model.PostDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.MediaToDboMapper;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.mapper.Populator;

import javax.inject.Inject;

import io.realm.RealmList;

public class PostToDboPopulator implements Populator<Post, PostDBO> {

    private final MediaToDboMapper mediaToDboMapper;

    @Inject
    public PostToDboPopulator(MediaToDboMapper mediaToDboMapper) {
        this.mediaToDboMapper = mediaToDboMapper;
    }

    @Override
    public void populate(Post object, PostDBO dbo) {
        dbo.id = object.id();
        dbo.description = object.description();
        dbo.timestamp = object.timestamp();
        dbo.title = object.title();
        dbo.media = new RealmList<>();
        for (Media media : object.media()) {
            dbo.media.add(mediaToDboMapper.map(media));
        }
    }
}
