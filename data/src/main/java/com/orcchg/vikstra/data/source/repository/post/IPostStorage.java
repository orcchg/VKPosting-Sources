package com.orcchg.vikstra.data.source.repository.post;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.repository.IStorage;
import com.orcchg.vikstra.domain.model.Post;

import java.util.List;

public interface IPostStorage extends IStorage {

    /* Create */
    // ------------------------------------------
    Post addPost(Post post);

    /* Read */
    // ------------------------------------------
    @Nullable Post post(long id);
    List<Post> posts(long... ids);
    List<Post> posts(int limit, int offset);

    /* Update */
    // ------------------------------------------
    boolean updatePost(@NonNull Post post);

    /* Delete */
    // ------------------------------------------
    boolean deletePost(long id);
}
