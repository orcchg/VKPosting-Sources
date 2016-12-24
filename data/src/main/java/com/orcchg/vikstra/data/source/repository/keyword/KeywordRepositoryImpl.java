package com.orcchg.vikstra.data.source.repository.keyword;

import android.support.annotation.NonNull;

import com.orcchg.vikstra.domain.model.Keyword;
import com.orcchg.vikstra.domain.model.KeywordBundle;
import com.orcchg.vikstra.domain.repository.IKeywordRepository;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class KeywordRepositoryImpl implements IKeywordRepository {

    private final IKeywordStorage cloudSource;
    private final IKeywordStorage localSource;

    @Inject
    KeywordRepositoryImpl(@Named("keywordCloud") IKeywordStorage cloudSource,
                          @Named("keywordDatabase") IKeywordStorage localSource) {
        this.cloudSource = cloudSource;
        this.localSource = localSource;
    }

    /* Create */
    // ------------------------------------------
    @Override
    public KeywordBundle addKeywords(String title, Collection<Keyword> keywords) {
        // TODO: impl cloudly
        long lastId = localSource.getLastId();
        KeywordBundle bundle = KeywordBundle.builder()
                .setId(++lastId)
                .setKeywords(keywords)
                .setTimestamp(System.currentTimeMillis())
                .setTitle(title)
                .build();

        return localSource.addKeywords(bundle);
    }

    @Override
    public boolean addKeywordToBundle(long id, Keyword keyword) {
        // TODO: impl cloudly
        return localSource.addKeywordToBundle(id, keyword);
    }

    /* Read */
    // ------------------------------------------
    @Override
    public KeywordBundle keywords(long id) {
        // TODO: impl cloudly
        return localSource.keywords(id);
    }

    @Override
    public List<KeywordBundle> keywords() {
        return keywords(-1, 0);
    }

    @Override
    public List<KeywordBundle> keywords(int limit, int offset) {
        // TODO: impl cloudly
        return localSource.keywords(limit, offset);
    }

    /* Update */
    // ------------------------------------------
    @Override
    public boolean updateKeywords(@NonNull KeywordBundle keywords) {
        // TODO: impl cloudly
        return localSource.updateKeywords(keywords);
    }

    /* Delete */
    // ------------------------------------------
    @Override
    public boolean deleteKeywords(long id) {
        // TODO: impl cloudly
        return localSource.deleteKeywords(id);
    }
}
