package com.orcchg.vikstra.data.source.repository.post;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.ReadWriteReentrantLock;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.model.essense.PostEssence;
import com.orcchg.vikstra.domain.model.essense.mapper.PostEssenceMapper;
import com.orcchg.vikstra.domain.repository.IPostRepository;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class PostRepositoryImpl implements IPostRepository {

    private final IPostStorage cloudSource;
    private final IPostStorage localSource;
    private ReadWriteReentrantLock lock = new ReadWriteReentrantLock();

    @Inject
    PostRepositoryImpl(@Named("postCloud") IPostStorage cloudSource,
                       @Named("postDatabase") IPostStorage localSource) {
        this.cloudSource = cloudSource;
        this.localSource = localSource;
    }

    @Override
    public long getLastId() {
        try {
            lock.lockRead();
            try {
                // TODO: impl cloudly
                return localSource.getLastId();
            } finally {
                lock.unlockRead();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Constant.BAD_ID;
    }

    /* Create */
    // ------------------------------------------
    @Nullable @Override
    public Post addPost(PostEssence essence) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                long lastId = getLastId();
                PostEssenceMapper mapper = new PostEssenceMapper(++lastId, System.currentTimeMillis());
                return localSource.addPost(mapper.map(essence));
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    /* Read */
    // ------------------------------------------
    @Nullable @Override
    public Post post(long id) {
        try {
            lock.lockRead();
            try {
                // TODO: impl cloudly
                return localSource.post(id);
            } finally {
                lock.unlockRead();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    public List<Post> posts() {
       return posts(-1, 0);
    }

    @Override
    public List<Post> posts(int limit, int offset) {
        try {
            lock.lockRead();
            try {
                // TODO: impl cloudly
                return localSource.posts(limit, offset);
            } finally {
                lock.unlockRead();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new ArrayList<>();
    }

    /* Update */
    // ------------------------------------------
    @Override
    public boolean updatePost(@NonNull Post post) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.updatePost(post);
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean deletePost(long id) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.deletePost(id);
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
