package com.orcchg.vikstra.app.ui.viewobject.mapper;

import com.orcchg.vikstra.app.ui.viewobject.PostSingleGridItemVO;
import com.orcchg.vikstra.domain.model.Media;
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
        List<Media> media = object.media();
        PostSingleGridItemVO.Builder builder = PostSingleGridItemVO.builder()
                .setId(object.id())
                .setDescription(object.description())
                .setMediaCount(media != null ? media.size() : 0)
                .setTitle(object.title());
        if (media != null && !media.isEmpty()) {
            builder.setMedia(mediaToVoMapper.map(media.get(0)));  // use first media item as primary
        }
        return builder.build();
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
