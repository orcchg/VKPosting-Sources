package com.orcchg.vikstra.domain.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.PostEssence;

import java.util.List;

public interface IPostRepository extends IRepository {

    /* Create */
    // ------------------------------------------
    @Nullable Post addPost(PostEssence essence);

    /* Read */
    // ------------------------------------------
    @Nullable Post post(long id);
    List<Post> posts();
    List<Post> posts(long... ids);
    List<Post> posts(int limit, int offset);

    /* Update */
    // ------------------------------------------
    boolean updatePost(@NonNull Post post);

    /* Delete */
    // ------------------------------------------
    boolean deletePost(long id);
}
