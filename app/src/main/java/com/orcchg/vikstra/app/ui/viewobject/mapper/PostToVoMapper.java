package com.orcchg.vikstra.app.ui.viewobject.mapper;

import com.orcchg.vikstra.app.ui.viewobject.PostViewVO;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PostToVoMapper implements Mapper<Post, PostViewVO> {

    private final MediaToVoMapper mediaToVoMapper;

    @Inject
    public PostToVoMapper(MediaToVoMapper mediaToVoMapper) {
        this.mediaToVoMapper = mediaToVoMapper;
    }

    @Override
    public PostViewVO map(Post object) {
        return PostViewVO.builder()
                .setDescription(object.description())
                .setLink(object.link())
                .setMedia(mediaToVoMapper.map(object.media()))
                .build();
    }

    @Override
    public List<PostViewVO> map(List<Post> list) {
        List<PostViewVO> mapped = new ArrayList<>();
        for (Post item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }
}
