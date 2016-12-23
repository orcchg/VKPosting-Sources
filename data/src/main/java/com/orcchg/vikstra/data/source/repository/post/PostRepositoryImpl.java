package com.orcchg.vikstra.data.source.repository.post;

import android.support.annotation.NonNull;

import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.PostEssense;
import com.orcchg.vikstra.domain.model.essense.mapper.PostEssenseMapper;
import com.orcchg.vikstra.domain.repository.IPostRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class PostRepositoryImpl implements IPostRepository {

    private final IPostStorage cloudSource;
    private final IPostStorage localSource;

    @Inject
    PostRepositoryImpl(@Named("postCloud") IPostStorage cloudSource,
                       @Named("postDatabase") IPostStorage localSource) {
        this.cloudSource = cloudSource;
        this.localSource = localSource;
    }

    /* Create */
    // ------------------------------------------
    @Override
    public long addPost(PostEssense essense) {
        // TODO: impl cloudly
        long lastId = localSource.getLastId();
        PostEssenseMapper mapper = new PostEssenseMapper(++lastId, System.currentTimeMillis());
        return localSource.addPost(mapper.map(essense));
    }

    /* Read */
    // ------------------------------------------
    @Override
    public Post post(long id) {
        // TODO: impl cloudly
        return localSource.post(id);
    }

    @Override
    public List<Post> posts() {
       return posts(-1, 0);
    }

    @Override
    public List<Post> posts(int limit, int offset) {
        // TODO: impl cloudly
        return localSource.posts(limit, offset);
    }

    /* Update */
    // ------------------------------------------
    @Override
    public boolean updatePost(@NonNull Post post) {
        // TODO: impl cloudly
        return localSource.updatePost(post);
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean deletePost(long id) {
        // TODO: impl cloudly
        return localSource.deletePost(id);
    }
}
