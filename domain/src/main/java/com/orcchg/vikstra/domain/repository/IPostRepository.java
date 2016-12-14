package com.orcchg.vikstra.domain.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.PostEssense;

import java.util.List;

public interface IPostRepository {

    /* Create */
    // ------------------------------------------
    boolean addPost(PostEssense essense);

    /* Read */
    // ------------------------------------------
    @Nullable Post post(long id);
    List<Post> posts();
    List<Post> posts(int limit, int offset);

    /* Update */
    // ------------------------------------------
    boolean updatePost(@NonNull Post post);

    /* Delete */
    // ------------------------------------------
}
