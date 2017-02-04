package com.orcchg.vikstra.app;

import android.app.Application;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.orcchg.vikstra.BuildConfig;
import com.orcchg.vikstra.R;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.injection.component.DaggerApplicationComponent;
import com.orcchg.vikstra.app.injection.module.ApplicationModule;
import com.orcchg.vikstra.data.injection.remote.CloudModule;
import com.orcchg.vikstra.domain.DomainConfig;
import com.squareup.leakcanary.LeakCanary;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

import io.realm.Realm;
import timber.log.Timber;

public class AndroidApplication extends Application {

    private ApplicationComponent applicationComponent;

    private VKAccessTokenTracker vkAccessTokenTracker;

    private String TOAST_ACCESS_TOKEN_INVALID;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("Application onCreate");
        initResources();
        initializeInjector();
//        initializeLeakDetection();
        initializeLogger();
        initializeRealmEngine();
        initializeVkontakteSdk();

        printConfigs();  // after logger initialization
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    private void printConfigs() {
        Timber.i("%s", DomainConfig.INSTANCE.toString());
        Timber.i("%s", AppConfig.INSTANCE.toString());
    }

    /* Initialization */
    // --------------------------------------------------------------------------------------------
    private void initializeInjector() {
        applicationComponent = DaggerApplicationComponent.builder()
                .cloudModule(new CloudModule(this))
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    private void initializeLogger() {
//        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return getPackageName() + ":" + super.createStackElementTag(element) + ":" + element.getLineNumber();
                }
            });
//        }
    }

    private void initializeLeakDetection() {
        if (BuildConfig.DEBUG) {
            LeakCanary.install(this);
        }
    }

    private void initializeRealmEngine() {
        Realm.init(this);
    }

    private void initializeVkontakteSdk() {
        vkAccessTokenTracker = new VKAccessTokenTracker() {
            @Override
            public void onVKAccessTokenChanged(@Nullable VKAccessToken oldToken, @Nullable VKAccessToken newToken) {
                if (newToken == null) {
                    Timber.w("Access Token has exhausted !");
                    Toast.makeText(getApplicationContext(), TOAST_ACCESS_TOKEN_INVALID, Toast.LENGTH_LONG).show();
                }
            }
        };
        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(getApplicationContext());
    }

    /* Resources */
    // --------------------------------------------------------------------------------------------
    private void initResources() {
        TOAST_ACCESS_TOKEN_INVALID = getResources().getString(R.string.toast_access_token_has_expired);
    }
}
