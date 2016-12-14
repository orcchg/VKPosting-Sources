package com.orcchg.vikstra.app.ui.post.view.injection;

import com.orcchg.vikstra.app.ui.common.injection.PostModule;

import dagger.Module;

@Module
public class PostViewModule extends PostModule {

    public PostViewModule(long postId) {
        super(postId);
    }
}
