package com.orcchg.vikstra.data.source.remote.post;

import android.support.annotation.NonNull;

import com.orcchg.vikstra.data.source.repository.post.IPostStorage;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.List;

import javax.inject.Inject;

public class PostCloud implements IPostStorage {

    @Inject
    PostCloud() {
    }

    /* Create */
    // ------------------------------------------
    @Override
    public Post addPost(Post post) {
        return null;
    }

    /* Read */
    // ------------------------------------------
    @Override
    public long getLastId() {
        return 0;
    }

    @Override
    public Post post(long id) {
        if (id != Constant.BAD_ID) {
            // TODO: cloud impl
        }
        return null;
    }

    @Override
    public List<Post> posts(int limit, int offset) {
        return null;
    }

    /* Update */
    // ------------------------------------------
    @Override
    public boolean updatePost(@NonNull Post post) {
        return false;
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean deletePost(long id) {
        if (id == Constant.BAD_ID) return false;
        // TODO: cloud impl
        return false;
    }
}
