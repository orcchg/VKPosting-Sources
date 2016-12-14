package com.orcchg.vikstra.app.ui.viewobject.mapper;

import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.mapper.Mapper;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PostToSingleGridVoMapper implements Mapper<Post, PostSingleGridItemVO> {

    private final MediaToVoMapper mediaToVoMapper;

    @Inject
    public PostToSingleGridVoMapper(MediaToVoMapper mediaToVoMapper) {
        this.mediaToVoMapper = mediaToVoMapper;
    }

    @Override
    public PostSingleGridItemVO map(Post object) {
        return PostSingleGridItemVO.builder()
                .setId(object.id())
                .setMedia(mediaToVoMapper.map(object.media().get(0)))  // use first media item as primary
                .build();
    }

    @Override
    public List<PostSingleGridItemVO> map(List<Post> list) {
        List<PostSingleGridItemVO> mapped = new ArrayList<>();
        for (Post item : list) {
            mapped.add(map(item));
        }
        return mapped;
    }
}
