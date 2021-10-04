package com.accenture.dansmarue.di.modules;

import android.app.Application;

import com.accenture.dansmarue.di.scopes.ActivityScope;
import com.accenture.dansmarue.mvp.presenters.AddAnomalyEquipementPresenter;
import com.accenture.dansmarue.mvp.presenters.AddAnomalyPresenter;
import com.accenture.dansmarue.mvp.presenters.AnomalyDetailsPresenter;
import com.accenture.dansmarue.mvp.presenters.AnomalyEquipementDetailsPresenter;
import com.accenture.dansmarue.mvp.presenters.CategoryEquipementPresenter;
import com.accenture.dansmarue.mvp.presenters.CategoryPresenter;
import com.accenture.dansmarue.mvp.presenters.LoginPresenter;
import com.accenture.dansmarue.mvp.presenters.MapParisEquipementPresenter;
import com.accenture.dansmarue.mvp.presenters.MapParisPresenter;
import com.accenture.dansmarue.mvp.presenters.PrefProfilePresenter;
import com.accenture.dansmarue.mvp.presenters.ProfilePresenter;
import com.accenture.dansmarue.mvp.presenters.SliderPresenter;
import com.accenture.dansmarue.mvp.presenters.SplashScreenPresenter;
import com.accenture.dansmarue.mvp.presenters.WelcomeMapEquipementPresenter;
import com.accenture.dansmarue.mvp.presenters.WelcomeMapPresenter;
import com.accenture.dansmarue.mvp.views.AddAnomalyEquipementView;
import com.accenture.dansmarue.mvp.views.AddAnomalyView;
import com.accenture.dansmarue.mvp.views.AnomalyDetailsView;
import com.accenture.dansmarue.mvp.views.AnomalyEquipementDetailsView;
import com.accenture.dansmarue.mvp.views.BaseView;
import com.accenture.dansmarue.mvp.views.CategoryEquipementView;
import com.accenture.dansmarue.mvp.views.CategoryView;
import com.accenture.dansmarue.mvp.views.LoginView;
import com.accenture.dansmarue.mvp.views.MapParisEquipementView;
import com.accenture.dansmarue.mvp.views.MapParisView;
import com.accenture.dansmarue.mvp.views.PrefProfileView;
import com.accenture.dansmarue.mvp.views.ProfileView;
import com.accenture.dansmarue.mvp.views.SliderView;
import com.accenture.dansmarue.mvp.views.SplashScreenView;
import com.accenture.dansmarue.mvp.views.WelcomeMapEquipementView;
import com.accenture.dansmarue.mvp.views.WelcomeMapView;
import com.accenture.dansmarue.services.ApiServiceEquipement;
import com.accenture.dansmarue.services.AuthentParisAccountService;
import com.accenture.dansmarue.services.SiraApiService;
import com.accenture.dansmarue.services.SiraApiServiceMock;
import com.accenture.dansmarue.utils.PrefManager;

import dagger.Module;
import dagger.Provides;

/**
 * Created by PK on 22/03/2017.
 * Dagger Module defining the provided injection for the  MVP architecture
 */
@Module
public class PresenterModule {

    /**
     * MVP view
     */
    private final BaseView view;


    public PresenterModule(final BaseView view) {
        this.view = view;
    }


    @Provides
    @ActivityScope
    BaseView provideView() {
        return this.view;
    }

    @Provides
    @ActivityScope
    SplashScreenPresenter provideSplashScreenPresenter(final Application application, final PrefManager prefManager, final SiraApiService service, final SiraApiServiceMock serviceMock) {
        return new SplashScreenPresenter(application, (SplashScreenView) this.view, prefManager, service, serviceMock);
    }

    @Provides
    @ActivityScope
    SliderPresenter provideSliderPresenter() {
        return new SliderPresenter((SliderView) this.view);
    }

    @Provides
    @ActivityScope
    WelcomeMapPresenter provideWelcomeMapPresenter(final PrefManager prefManager, SiraApiService service) {
        return new WelcomeMapPresenter((WelcomeMapView) this.view, prefManager, service);
    }

    @Provides
    @ActivityScope
    WelcomeMapEquipementPresenter provideWelcomeMapEquipementPresenter(final PrefManager prefManager, SiraApiService service) {
        return new WelcomeMapEquipementPresenter((WelcomeMapEquipementView) this.view, prefManager, service);
    }


    @Provides
    @ActivityScope
    AddAnomalyPresenter provideAddAnomalyPresenter(final SiraApiService service, final PrefManager prefManager, final Application application) {
        return new AddAnomalyPresenter((AddAnomalyView) this.view, service, prefManager, application);
    }

    @Provides
    @ActivityScope
    AddAnomalyEquipementPresenter provideAddAnomalyEquipementPresenter(final SiraApiService service, final ApiServiceEquipement apiServiceEquipement, final PrefManager prefManager, final Application application) {
        return new AddAnomalyEquipementPresenter((AddAnomalyEquipementView) this.view, service, apiServiceEquipement, prefManager, application);
    }

    @Provides
    @ActivityScope
    CategoryPresenter provideCategoryPresenter(final Application application, final PrefManager prefManager) {
        return new CategoryPresenter(application, (CategoryView) this.view, prefManager);
    }

    @Provides
    @ActivityScope
    CategoryEquipementPresenter provideCategoryEquipementPresenter(final Application application,final PrefManager prefManager) {
        return new CategoryEquipementPresenter(application, (CategoryEquipementView) this.view, prefManager);
    }


    @Provides
    @ActivityScope
    LoginPresenter provideLoginPresenter(final PrefManager prefManager, final AuthentParisAccountService authenService, final SiraApiService siraService) {
        return new LoginPresenter((LoginView) this.view, prefManager, authenService, siraService);
    }

    @Provides
    @ActivityScope
    PrefProfilePresenter providePrefProfilePresenter(final PrefManager prefManager, final SiraApiService siraService) {
        return new PrefProfilePresenter((PrefProfileView) this.view, siraService, prefManager);
    }

    @Provides
    @ActivityScope
    MapParisPresenter provideMapParisPresenter(final Application application, final SiraApiService service, final PrefManager prefManager) {
        return new MapParisPresenter((MapParisView) this.view, service, application, prefManager);
    }

    @Provides
    @ActivityScope
    MapParisEquipementPresenter provideMapParisEquipementPresenter(final Application application, final SiraApiService service, final PrefManager prefManager, final SiraApiServiceMock serviceMock, final ApiServiceEquipement apiServiceEquipement) {
        return new MapParisEquipementPresenter((MapParisEquipementView) this.view, service, application, prefManager, serviceMock, apiServiceEquipement);
    }


    @Provides
    @ActivityScope
    ProfilePresenter provideProfilePresenter(final Application application, final SiraApiService service, final ApiServiceEquipement apiServiceEquipement, final PrefManager prefManager) {
        return new ProfilePresenter((ProfileView) this.view, service, apiServiceEquipement, application, prefManager);
    }


    @Provides
    @ActivityScope
    AnomalyDetailsPresenter provideAnomalyDetailsPresenter(final PrefManager prefManager, SiraApiService service, final Application application) {
        return new AnomalyDetailsPresenter((AnomalyDetailsView) this.view, prefManager, service, application);
    }

    @Provides
    @ActivityScope
    AnomalyEquipementDetailsPresenter AnomalyEquipementDetailsPresenter(final PrefManager prefManager, SiraApiService service, final ApiServiceEquipement apiServiceEquipement, final Application application) {
        return new AnomalyEquipementDetailsPresenter((AnomalyEquipementDetailsView) this.view, prefManager, service, apiServiceEquipement, application);
    }


}
