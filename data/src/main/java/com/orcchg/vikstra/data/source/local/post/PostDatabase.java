package com.orcchg.vikstra.data.source.local.post;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.data.source.local.model.PostDBO;
import com.orcchg.vikstra.data.source.local.model.mapper.PostToDboMapper;
import com.orcchg.vikstra.data.source.local.model.populator.PostToDboPopulator;
import com.orcchg.vikstra.data.source.repository.RepoUtility;
import com.orcchg.vikstra.data.source.repository.post.IPostStorage;
import com.orcchg.vikstra.domain.model.Post;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;

@Singleton
public class PostDatabase implements IPostStorage {

    private final PostToDboMapper postToDboMapper;
    private final PostToDboPopulator postToDboPopulator;

    @Inject
    PostDatabase(PostToDboMapper postToDboMapper, PostToDboPopulator postToDboPopulator) {
        this.postToDboMapper = postToDboMapper;
        this.postToDboPopulator = postToDboPopulator;
    }

    /* Create */
    // ------------------------------------------
    @DebugLog @Override
    public Post addPost(Post post) {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction((xrealm) -> {
            PostDBO dbo = xrealm.createObject(PostDBO.class);
            postToDboPopulator.populate(post, dbo);
        });
        realm.close();
        return post;
    }

    /* Read */
    // ------------------------------------------
    @DebugLog @Override
    public long getLastId() {
        Realm realm = Realm.getDefaultInstance();
        Number number = realm.where(PostDBO.class).max(PostDBO.COLUMN_ID);
        long lastId = number != null ? number.longValue() : Constant.INIT_ID;
        realm.close();
        return lastId;
    }

    @DebugLog @Nullable @Override
    public Post post(long id) {
        if (id != Constant.BAD_ID) {
            Realm realm = Realm.getDefaultInstance();
            Post model = null;
            PostDBO dbo = realm.where(PostDBO.class).equalTo(PostDBO.COLUMN_ID, id).findFirst();
            if (dbo != null) model = postToDboMapper.mapBack(dbo);
            realm.close();
            return model;
        }
        return null;
    }

    @DebugLog @Override
    public List<Post> posts(int limit, int offset) {
        RepoUtility.checkLimitAndOffset(limit, offset);
        Realm realm = Realm.getDefaultInstance();
        RealmResults<PostDBO> dbos = realm.where(PostDBO.class).findAll();
        List<Post> models = new ArrayList<>();
        int size = limit < 0 ? dbos.size() : limit;
        RepoUtility.checkListBounds(offset + size - 1, dbos.size());
        for (int i = offset; i < offset + size; ++i) {
            models.add(postToDboMapper.mapBack(dbos.get(i)));
        }
        realm.close();
        return models;
    }

    /* Update */
    // ------------------------------------------
    @DebugLog @Override
    public boolean updatePost(@NonNull Post post) {
        boolean result = false;
        Realm realm = Realm.getDefaultInstance();
        PostDBO dbo = realm.where(PostDBO.class).equalTo(PostDBO.COLUMN_ID, post.id()).findFirst();
        if (dbo != null) {
            realm.executeTransaction((xrealm) -> {
                postToDboPopulator.populate(post, dbo);
            });
            result = true;
        }
        realm.close();
        return result;
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean deletePost(long id) {
        if (id == Constant.BAD_ID) return false;
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction((xrealm) -> {
            PostDBO dbo = xrealm.where(PostDBO.class).equalTo(PostDBO.COLUMN_ID, id).findFirst();
            dbo.deleteFromRealm();
        });
        realm.close();
        return true;
    }
}
