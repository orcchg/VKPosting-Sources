package com.orcchg.vikstra.data.source.local.model.mapper;

import com.orcchg.vikstra.data.source.local.model.MediaDBO;
import com.orcchg.vikstra.data.source.local.model.PostDBO;
import com.orcchg.vikstra.data.source.local.model.populator.PostToDboPopulator;
import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.mapper.DuplexMapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PostToDboMapper implements DuplexMapper<Post, PostDBO> {

    private final MediaToDboMapper mediaToDboMapper;
    private final PostToDboPopulator postToDboPopulator;

    @Inject
    public PostToDboMapper(MediaToDboMapper mediaToDboMapper, PostToDboPopulator postToDboPopulator) {
        this.mediaToDboMapper = mediaToDboMapper;
        this.postToDboPopulator = postToDboPopulator;
    }

    /* Direct mapping */
    // ------------------------------------------
    @Override
    public PostDBO map(Post object) {
        PostDBO dbo = new PostDBO();
        postToDboPopulator.populate(object, dbo);
        return dbo;
    }

    @Override
    public List<PostDBO> map(List<Post> list) {
        List<PostDBO> mapped = new ArrayList<>();
        for (Post item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }

    /* Backward mapping */
    // ------------------------------------------
    @Override
    public Post mapBack(PostDBO object) {
        List<Media> media = new ArrayList<>();
        for (MediaDBO dbo : object.media) {
            media.add(mediaToDboMapper.mapBack(dbo));
        }
        return Post.builder()
                .setId(object.id)
                .setDescription(object.description)
                .setMedia(media)
                .setTimestamp(object.timestamp)
                .setTitle(object.title)
                .build();
    }

    @Override
    public List<Post> mapBack(List<PostDBO> list) {
        List<Post> mapped = new ArrayList<>();
        for (PostDBO item : list) {
            mapped.add(mapBack(item));
        }
        return mapped;
    }
}
