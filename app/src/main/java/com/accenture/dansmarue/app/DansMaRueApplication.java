package com.accenture.dansmarue.app;


import androidx.multidex.MultiDexApplication;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.di.components.ApplicationComponent;
import com.accenture.dansmarue.di.components.DaggerApplicationComponent;
import com.accenture.dansmarue.di.modules.ApplicationModule;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;


/**
 * Created by PK on 22/03/2017.
 * DansMaRue Application. Used to start dagger dependency injection mechanism
 */
public class DansMaRueApplication extends MultiDexApplication {

    private ApplicationComponent applicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        initializeApplicationComponent();
        initializeCaligraphy();

    }


    private void initializeCaligraphy() {
        // Initialise Calligraphy
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Montserrat-Regular.otf")
                                .setFontAttrId(io.github.inflationx.calligraphy3.R.attr.fontPath)
                                .build()))
                .build());
    }

    private void initializeApplicationComponent() {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
