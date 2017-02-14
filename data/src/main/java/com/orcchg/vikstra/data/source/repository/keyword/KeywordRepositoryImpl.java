package com.orcchg.vikstra.data.source.repository.keyword;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.domain.executor.ReadWriteReentrantLock;
import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;
import com.orcchg.vikstra.domain.util.Constant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class KeywordRepositoryImpl implements IKeywordRepository {

    private final IKeywordStorage cloudSource;
    private final IKeywordStorage localSource;
    private ReadWriteReentrantLock lock = new ReadWriteReentrantLock();

    @Inject
    KeywordRepositoryImpl(@Named("keywordCloud") IKeywordStorage cloudSource,
                          @Named("keywordDatabase") IKeywordStorage localSource) {
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
    public KeywordBundle addKeywords(String title, Collection<Keyword> keywords) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                long lastId = getLastId();
                KeywordBundle bundle = KeywordBundle.builder()
                        .setId(++lastId)
                        .setKeywords(new ArrayList<>(keywords))  // turn collection into ordered list
                        .setTimestamp(System.currentTimeMillis())
                        .setTitle(title)
                        .build();

                return localSource.addKeywords(bundle);
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    public boolean addKeywordToBundle(long id, Keyword keyword) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.addKeywordToBundle(id, keyword);
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    /* Read */
    // ------------------------------------------
    @Nullable @Override
    public KeywordBundle keywords(long id) {
        try {
            lock.lockRead();
            try {
                // TODO: impl cloudly
                return localSource.keywords(id);
            } finally {
                lock.unlockRead();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    @Override
    public List<KeywordBundle> keywords() {
        return keywords(-1, 0);
    }

    @Override
    public List<KeywordBundle> keywords(long... ids) {
        try {
            lock.lockRead();
            try {
                // TODO: impl cloudly
                return localSource.keywords(ids);
            } finally {
                lock.unlockRead();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new ArrayList<>();
    }

    @Override
    public List<KeywordBundle> keywords(int limit, int offset) {
        try {
            lock.lockRead();
            try {
                // TODO: impl cloudly
                return localSource.keywords(limit, offset);
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
    public boolean updateKeywords(@NonNull KeywordBundle keywords) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.updateKeywords(keywords);
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    @Override
    public boolean updateKeywordsTitle(long id, String newTitle) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.updateKeywordsTitle(id, newTitle);
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
    public boolean deleteKeywords(long id) {
        try {
            lock.lockWrite();
            try {
                // TODO: impl cloudly
                return localSource.deleteKeywords(id);
            } finally {
                lock.unlockWrite();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
