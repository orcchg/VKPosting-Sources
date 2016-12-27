package com.orcchg.vikstra.domain.model.essense.mapper;

import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.PostEssence;
import com.orcchg.vikstra.domain.model.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PostEssenceMapper implements Mapper<PostEssence, Post> {

    private final long postId;
    private final long timestamp;

    @Inject
    public PostEssenceMapper(long postId, long timestamp) {
        this.postId = postId;
        this.timestamp = timestamp;
    }

    @Override
    public Post map(PostEssence object) {
        return Post.builder()
                .setId(postId)
                .setDescription(object.description())
                .setMedia(object.media())
                .setTimestamp(timestamp)
                .setTitle(object.title())
                .build();
    }

    @Override
    public List<Post> map(List<PostEssence> list) {
        List<Post> mapped = new ArrayList<>();
        for (PostEssence item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }
}
