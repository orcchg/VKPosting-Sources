package com.orcchg.vikstra.app.ui.keyword.list.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListAdapter;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListPresenter;
import com.orcchg.vikstra.app.ui.viewobject.mapper.KeywordBundleToVoMapper;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundles;

import dagger.Module;
import dagger.Provides;

@Module
public class KeywordListModule {

    private final @KeywordListAdapter.SelectMode int selectMode;

    public KeywordListModule(@KeywordListAdapter.SelectMode int selectMode) {
        this.selectMode = selectMode;
    }

    @Provides @PerActivity
    protected KeywordListPresenter provideKeywordListPresenter(GetKeywordBundles getKeywordBundlesUseCase,
                                                               KeywordBundleToVoMapper keywordBundleToVoMapper) {
        return new KeywordListPresenter(selectMode, getKeywordBundlesUseCase, keywordBundleToVoMapper);
    }
}
