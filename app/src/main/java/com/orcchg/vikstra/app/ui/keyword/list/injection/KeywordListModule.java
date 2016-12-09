package com.orcchg.vikstra.app.ui.keyword.list.injection;

import com.orcchg.vikstra.app.injection.PerActivity;
import com.orcchg.vikstra.app.ui.keyword.list.KeywordListPresenter;
import com.orcchg.vikstra.domain.interactor.keyword.GetKeywordBundles;

import dagger.Module;
import dagger.Provides;

@Module
public class KeywordListModule {

    private final boolean isListItemSelectable;

    public KeywordListModule(boolean isListItemSelectable) {
        this.isListItemSelectable = isListItemSelectable;
    }

    @Provides @PerActivity
    protected KeywordListPresenter provideKeywordListPresenter(GetKeywordBundles getKeywordBundlesUseCase) {
        return new KeywordListPresenter(isListItemSelectable, getKeywordBundlesUseCase);
    }
}
