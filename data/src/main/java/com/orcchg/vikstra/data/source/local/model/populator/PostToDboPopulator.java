package com.orcchg.vikstra.data.source.local.model.populator;

import com.orcchg.vikstra.data.source.local.model.PostDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.MediaToDboMapper;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.mapper.Populator;

import java.util.List;

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
        dbo.link = object.link();
        dbo.media = new RealmList<>();
        dbo.timestamp = object.timestamp();
        dbo.title = object.title();
        List<Media> medias = object.media();
        if (medias != null) {
            for (Media media : medias) {
                dbo.media.add(mediaToDboMapper.map(media));
            }
        }
    }
}
