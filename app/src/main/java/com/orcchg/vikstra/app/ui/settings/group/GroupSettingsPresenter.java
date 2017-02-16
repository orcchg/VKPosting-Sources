package com.orcchg.vikstra.app.ui.settings.group;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.app.ui.base.BasePresenter;
import com.orcchg.vikstra.app.ui.settings.SettingsFactory;
import com.orcchg.vikstra.app.ui.settings.group.model.GroupFilter;
import com.orcchg.vikstra.app.ui.settings.group.model.GroupLoadLimit;
import com.orcchg.vikstra.app.ui.settings.group.model.GroupSelector;
import com.orcchg.vikstra.app.ui.settings.group.model.PostingInterval;
import com.orcchg.vikstra.domain.executor.PostExecuteScheduler;
import com.orcchg.vikstra.domain.executor.ThreadExecutor;
import com.orcchg.vikstra.domain.interactor.base.IParameters;
import com.orcchg.vikstra.domain.interactor.base.UseCase;
import com.orcchg.vikstra.domain.util.Constant;

import javax.inject.Inject;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class GroupSettingsPresenter extends BasePresenter<GroupSettingsContract.View>
        implements GroupSettingsContract.Presenter {
    private static final int PrID = Constant.PresenterId.SETTINGS_GROUP_PRESENTER;

    private final LoadSettings loadSettingsUseCase;
    private final SaveSettings saveSettingsUseCase;

    private GroupFilter groupFilterSetting;
    private GroupLoadLimit groupLoadLimitSetting;
    private GroupSelector groupSelectorSetting;
    private PostingInterval postingIntervalSetting;

    private boolean hasChanges = false;

    @Inject
    GroupSettingsPresenter(LoadSettings loadSettingsUseCase, SaveSettings saveSettingsUseCase) {
        this.loadSettingsUseCase = loadSettingsUseCase;
        this.loadSettingsUseCase.setPostExecuteCallback(createLoadSettingsCallback());
        this.saveSettingsUseCase = saveSettingsUseCase;
        this.saveSettingsUseCase.setPostExecuteCallback(createSaveSettingsCallback());
    }

    /* Contract */
    // --------------------------------------------------------------------------------------------
    @Override
    public void onBackPressed() {
        Timber.i("onBackPressed");
        if (isViewAttached()) {
            if (hasChanges()) {
                getView().openSaveChangesDialog();
            } else {
                getView().closeView();
            }
        } else {
            Timber.w("No View is attached");
        }
    }

    @Override
    public void onSavePressed() {
        Timber.i("onSavePressed");
        saveSettingsUseCase.execute();
    }

    @Override
    public void retry() {
        freshStart();  // nothing to be restored
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    @Override
    protected void freshStart() {
        if (isViewAttached()) getView().showLoading(GroupSettingsActivity.RV_TAG);
        loadSettingsUseCase.execute();
    }

    @Override
    protected void onRestoreState() {
        freshStart();  // nothing to be restored
    }

    @DebugLog
    private boolean hasChanges() {
        return hasChanges;
    }

    /* Use Case */
    // --------------------------------------------------------------------------------------------
    final class LoadSettings extends UseCase<Boolean> {
        @Inject
        LoadSettings(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
            super(threadExecutor, postExecuteScheduler);
        }

        @Nullable @Override
        protected Boolean doAction() {
            groupFilterSetting     = (GroupFilter)     sharedPrefsManagerComponent.sharedPrefsManager().getSetting(GroupFilter.TAG);
            groupLoadLimitSetting  = (GroupLoadLimit)  sharedPrefsManagerComponent.sharedPrefsManager().getSetting(GroupLoadLimit.TAG);
            groupSelectorSetting   = (GroupSelector)   sharedPrefsManagerComponent.sharedPrefsManager().getSetting(GroupSelector.TAG);
            postingIntervalSetting = (PostingInterval) sharedPrefsManagerComponent.sharedPrefsManager().getSetting(PostingInterval.TAG);

            if (groupFilterSetting     == null) groupFilterSetting     = (GroupFilter)     SettingsFactory.create(GroupFilter.TAG);
            if (groupLoadLimitSetting  == null) groupLoadLimitSetting  = (GroupLoadLimit)  SettingsFactory.create(GroupLoadLimit.TAG);
            if (groupSelectorSetting   == null) groupSelectorSetting   = (GroupSelector)   SettingsFactory.create(GroupSelector.TAG);
            if (postingIntervalSetting == null) postingIntervalSetting = (PostingInterval) SettingsFactory.create(PostingInterval.TAG);
            return true;
        }

        @Nullable @Override
        protected IParameters getInputParameters() {
            return null;
        }
    }

    // ------------------------------------------
    final class SaveSettings extends UseCase<Boolean> {
        @Inject
        SaveSettings(ThreadExecutor threadExecutor, PostExecuteScheduler postExecuteScheduler) {
            super(threadExecutor, postExecuteScheduler);
        }

        @Nullable @Override
        protected Boolean doAction() {
            sharedPrefsManagerComponent.sharedPrefsManager().putSetting(groupFilterSetting);
            sharedPrefsManagerComponent.sharedPrefsManager().putSetting(groupLoadLimitSetting);
            sharedPrefsManagerComponent.sharedPrefsManager().putSetting(groupSelectorSetting);
            sharedPrefsManagerComponent.sharedPrefsManager().putSetting(postingIntervalSetting);
            return true;
        }

        @Nullable @Override
        protected IParameters getInputParameters() {
            return null;
        }
    }

    /* Callback */
    // --------------------------------------------------------------------------------------------
    private UseCase.OnPostExecuteCallback<Boolean> createLoadSettingsCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @DebugLog @Override
            public void onFinish(@Nullable Boolean values) {
                // TODO: fill view
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(GroupSettingsActivity.RV_TAG);
            }
        };
    }

    private UseCase.OnPostExecuteCallback<Boolean> createSaveSettingsCallback() {
        return new UseCase.OnPostExecuteCallback<Boolean>() {
            @DebugLog @Override
            public void onFinish(@Nullable Boolean values) {
                if (isViewAttached()) getView().closeView(Activity.RESULT_OK);
            }

            @DebugLog @Override
            public void onError(Throwable e) {
                if (isViewAttached()) getView().showError(GroupSettingsActivity.RV_TAG);
            }
        };
    };
}
