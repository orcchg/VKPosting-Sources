package com.orcchg.vikstra.app;

import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
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

import io.fabric.sdk.android.Fabric;
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
        initializeCrashlytics();
        initializeInjector();
        initializeLeakDetection();
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
    private void initializeCrashlytics() {
        CrashlyticsCore core = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());
    }

    private void initializeInjector() {
        applicationComponent = DaggerApplicationComponent.builder()
                .cloudModule(new CloudModule(this))
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    private void initializeLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return getPackageName() + ":" + super.createStackElementTag(element) + ":" + element.getLineNumber();
                }
            });
        } else {
            Timber.plant(new CrashlyticsTree());
        }
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

    /* Crashlytics */
    // --------------------------------------------------------------------------------------------
    /**
     * {@see https://blog.xmartlabs.com/2015/07/09/Android-logging-with-Crashlytics-and-Timber/}
     *
     * Comment: {@link Timber.Tree} only supplies the tag when it was explicitly set.
     * In most cases, tag will be null. If you want the tag to be extracted from the log,
     * you need to extend {@link Timber.DebugTree} instead.
     */
    public class CrashlyticsTree extends Timber.DebugTree {
        private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
        private static final String CRASHLYTICS_KEY_TAG = "tag";
        private static final String CRASHLYTICS_KEY_MESSAGE = "message";

        @Override
        protected void log(int priority, @Nullable String tag, @Nullable String message, @Nullable Throwable t) {
            if (priority == Log.VERBOSE) {
                return;
            }

            Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
            Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag);
            Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);

            if (t == null) {
                Crashlytics.log(priority, tag, message);
            } else {
                Crashlytics.logException(t);
            }
        }
    }
}
