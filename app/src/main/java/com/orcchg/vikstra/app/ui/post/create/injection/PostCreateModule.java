package com.orcchg.vikstra.app.ui.post.create.injection;

import com.orcchg.vikstra.app.ui.common.injection.PostModule;

import dagger.Module;

@Module
public class PostCreateModule extends PostModule {

    public PostCreateModule(long postId) {
        super(postId);
    }
}
