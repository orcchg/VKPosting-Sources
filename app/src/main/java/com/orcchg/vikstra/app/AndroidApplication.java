package com.orcchg.vikstra.app;

import android.app.Application;
import android.support.annotation.Nullable;

import com.orcchg.vikstra.BuildConfig;
import com.orcchg.vikstra.app.injection.component.ApplicationComponent;
import com.orcchg.vikstra.app.injection.component.DaggerApplicationComponent;
import com.orcchg.vikstra.app.injection.module.ApplicationModule;
import com.orcchg.vikstra.data.source.remote.injection.CloudModule;
import com.squareup.leakcanary.LeakCanary;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

import io.realm.Realm;
import timber.log.Timber;

public class AndroidApplication extends Application {

    private ApplicationComponent applicationComponent;

    private VKAccessTokenTracker vkAccessTokenTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeInjector();
//        initializeLeakDetection();
        initializeLogger();
        initializeRealmEngine();
        initializeVkontakteSdk();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    private void initializeInjector() {
        applicationComponent = DaggerApplicationComponent.builder()
                .cloudModule(new CloudModule(this))
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    private void initializeLogger() {
        Timber.plant(new Timber.DebugTree() {
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return getPackageName() + ":" + super.createStackElementTag(element) + ":" + element.getLineNumber();
            }
        });
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
                    // TODO: access token is invalid
                }
            }
        };
        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(getApplicationContext());
    }
}
