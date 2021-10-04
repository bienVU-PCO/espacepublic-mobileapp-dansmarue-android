package com.accenture.dansmarue.di.components;

import com.accenture.dansmarue.di.modules.PresenterModule;
import com.accenture.dansmarue.di.scopes.ActivityScope;
import com.accenture.dansmarue.ui.activities.AddAnomalyActivity;
import com.accenture.dansmarue.ui.activities.AddAnomalyEquipementActivity;
import com.accenture.dansmarue.ui.activities.AnomalyDetailsActivity;
import com.accenture.dansmarue.ui.activities.AnomalyEquipementDetailsActivity;
import com.accenture.dansmarue.ui.activities.CategoryActivity;
import com.accenture.dansmarue.ui.activities.CategoryEquipementActivity;
import com.accenture.dansmarue.ui.activities.LoginActivity;
import com.accenture.dansmarue.ui.activities.PrefProfilActivity;
import com.accenture.dansmarue.ui.activities.SliderActivity;
import com.accenture.dansmarue.ui.activities.SplashScreenActivity;
import com.accenture.dansmarue.ui.activities.WelcomeMapActivity;
import com.accenture.dansmarue.ui.activities.WelcomeMapEquipementActivity;
import com.accenture.dansmarue.ui.fragments.MapParisEquipementFragment;
import com.accenture.dansmarue.ui.fragments.MapParisFragment;
import com.accenture.dansmarue.ui.fragments.ProfileFragment;

import dagger.Component;

/**
 * Created by PK on 22/03/2017.
 * Component Interface linked to the {@link PresenterModule}
 */
@ActivityScope
@Component(modules = PresenterModule.class, dependencies = ApplicationComponent.class)
public interface PresenterComponent {

    /**
     * Inject dependencies to the view
     *
     * @param activity view to inject dependecies to
     */
    void inject(SplashScreenActivity activity);

    /**
     * Inject dependencies to the view
     *
     * @param activity view to inject dependecies to
     */
    void inject(SliderActivity activity);


    /**
     * Inject dependencies to the view
     *
     * @param activity view to inject dependecies to
     */
    void inject(CategoryActivity activity);

    void inject(CategoryEquipementActivity activity);


    /**
     * Inject dependencies to the view
     *
     * @param activity view to inject dependecies to
     */
    void inject(AddAnomalyActivity activity);

    void inject(AddAnomalyEquipementActivity activity);


    /**
     * Inject dependencies to the view
     *
     * @param activity view to inject dependecies to
     */
    void inject(WelcomeMapActivity activity);

    void inject(WelcomeMapEquipementActivity activity);


    /**
     * Inject dependencies to the view
     *
     * @param activity view to inject dependecies to
     */
    void inject(LoginActivity activity);

    /**
     * Inject dependencies to the view
     *
     * @param fragment view to inject dependecies to
     */
    void inject(MapParisFragment fragment);

    void inject(MapParisEquipementFragment fragment);


    /**
     * Inject dependencies to the view
     *
     * @param activity view to inject dependecies to
     */
    void inject(AnomalyDetailsActivity activity);

    void inject(AnomalyEquipementDetailsActivity activity);


    /**
     * Inject dependencies to the view
     *
     * @param fragment view to inject dependecies to
     */
    void inject(ProfileFragment fragment);

    void inject(PrefProfilActivity prefProfilActivity);
}
