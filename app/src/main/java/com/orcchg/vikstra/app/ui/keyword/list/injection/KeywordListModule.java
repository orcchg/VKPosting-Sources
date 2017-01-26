package com.orcchg.vikstra.app.ui.keyword.list.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.base.adapter.BaseSelectAdapter;
import com.orcchg.vikstra.app.ui.common.injection.ListModule;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListPresenter;
import com.orcchg.vikstra.app.ui.viewobject.mapper.KeywordBundleToVoMapper;
import com.orcchg.vikstra.domain.interactor.keyword.DeleteKeywordBundle;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundles;

import dagger.Module;
import dagger.Provides;

@Module
public class KeywordListModule extends ListModule {

    public KeywordListModule(@BaseSelectAdapter.SelectMode int selectMode) {
        super(selectMode);
    }

    @Provides @PerActivity
    protected KeywordListPresenter provideKeywordListPresenter(GetKeywordBundles getKeywordBundlesUseCase,
           DeleteKeywordBundle deleteKeywordBundleUseCase, KeywordBundleToVoMapper keywordBundleToVoMapper) {
        return new KeywordListPresenter(selectMode, getKeywordBundlesUseCase, deleteKeywordBundleUseCase, keywordBundleToVoMapper);
    }
}
