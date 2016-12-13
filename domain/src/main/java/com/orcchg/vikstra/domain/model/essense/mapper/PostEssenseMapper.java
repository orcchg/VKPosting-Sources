package com.orcchg.vikstra.domain.model.essense.mapper;

import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.PostEssense;
import com.orcchg.vikstra.domain.model.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PostEssenseMapper implements Mapper<PostEssense, Post> {

    private final long postId;
    private final long timestamp;

    @Inject
    public PostEssenseMapper(long postId, long timestamp) {
        this.postId = postId;
        this.timestamp = timestamp;
    }

    @Override
    public Post map(PostEssense object) {
        return Post.builder()
                .setId(postId)
                .setDescription(object.description())
                .setMedia(object.media())
                .setTimestamp(timestamp)
                .setTitle(object.title())
                .build();
    }

    @Override
    public List<Post> map(List<PostEssense> list) {
        List<Post> mapped = new ArrayList<>();
        for (PostEssense item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }
}
