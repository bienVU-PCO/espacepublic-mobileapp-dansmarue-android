package com.accenture.dansmarue.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;


import com.accenture.dansmarue.BuildConfig;
import com.accenture.dansmarue.R;
import com.accenture.dansmarue.di.components.DaggerPresenterComponent;
import com.accenture.dansmarue.di.modules.PresenterModule;
import com.accenture.dansmarue.mvp.presenters.SplashScreenPresenter;
import com.accenture.dansmarue.mvp.views.SplashScreenView;
import com.accenture.dansmarue.utils.Constants;
import com.accenture.dansmarue.utils.NetworkUtils;
import com.google.firebase.analytics.FirebaseAnalytics;


import javax.inject.Inject;


/**
 * Launch SplashScreen, wait and show slider the first time
 * or redirect to the next Activity
 */
public class SplashScreenActivity extends BaseActivity implements SplashScreenView {

    private String TAG = SplashScreenActivity.class.getCanonicalName();

    private Long incidentId = null;

    private String anomalyType ="";

   // private com.mapbox.mapboxsdk.maps.MapView mapViewMapbox;

    private FirebaseAnalytics mFirebaseAnalytics;

    @SuppressWarnings("WeakerAccess")
    @Inject
    protected SplashScreenPresenter presenter;


    @Override
    protected int getContentView() {
        //Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        return R.layout.splashscreen_activity_layout;
    }

    @Override
    protected void resolveDaggerDependency() {
        DaggerPresenterComponent.builder()
                .applicationComponent(getApplicationComponent())
                .presenterModule(new PresenterModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //MapBox Start
        /**mapViewMapbox = (com.mapbox.mapboxsdk.maps.MapView) this.findViewById(R.id.mapboxView);
        mapViewMapbox.onCreate(savedInstanceState);
        mapViewMapbox.getMapAsync(new com.mapbox.mapboxsdk.maps.OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                    }
                });
            }
        });**/
        //MapBox end

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }


    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        final String anomalyId = intent.getStringExtra(Constants.NOTIF_ANOMALY_ID_KEY);
        anomalyType = intent.getStringExtra(Constants.NOTIF_ANOMALY_TYPE);

        if (anomalyId != null) {
            incidentId = Long.valueOf(anomalyId);
        }

        if (NetworkUtils.isConnected(this)) {
            presenter.dmrIsOnline();
        } else {
            //check if user need update application
            presenter.checkVersion();
        }


    }


    /**
     * Display popup to ask for dowload last version of the application.
     */
    public void displayPopupUpdateNotMandory() {

        final String packageDMRFinal = BuildConfig.APPLICATION_ID.endsWith(".debug") ? BuildConfig.APPLICATION_ID.substring(0, BuildConfig.APPLICATION_ID.length() - 6) : BuildConfig.APPLICATION_ID;

        AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this, R.style.MyDialogTheme);
        builder
                .setMessage(getString(R.string.popup_msg))
                .setCancelable(false)
                .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=" + packageDMRFinal));
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(R.string.label_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                        presenter.checkAndLoadCategories();
                    }
                });

        builder.create().show();

    }

    /**
     * Display popup to force update.
     */
    public void displayPopupUpdateMandory() {

        final String packageDMRFinal = BuildConfig.APPLICATION_ID.endsWith(".debug") ? BuildConfig.APPLICATION_ID.substring(0, BuildConfig.APPLICATION_ID.length() - 6) : BuildConfig.APPLICATION_ID;

        AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this, R.style.MyDialogTheme);
        builder
                .setMessage(getString(R.string.popup_msg_mandatory))
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=" + packageDMRFinal));
                        startActivity(intent);
                        finish();
                    }
                });

        builder.create().show();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onFirstTimeLaunch() {
        startActivity(new Intent(SplashScreenActivity.this, SliderActivity.class));
        // close this activity
        finish();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void onLaunch() {
        if (incidentId != null) {

            Intent intent;

            if(anomalyType.equals("OUTDOOR")){
                intent = new Intent(SplashScreenActivity.this, AnomalyDetailsActivity.class);
            } else {
                intent = new Intent(SplashScreenActivity.this, AnomalyEquipementDetailsActivity.class);
            }
            intent.putExtra(Constants.EXTRA_INCIDENT_ID, incidentId);
            intent.putExtra(Constants.EXTRA_INCIDENT_TYPE, anomalyType);
            startActivity(intent);
        } else {
            startActivity(new Intent(SplashScreenActivity.this, RuntimeGeolocPermissionRequestActivity.class));
        }

        // close this activity

        finish();
    }

    public void endCauseNoCategories() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this, R.style.MyDialogTheme);
        builder
                .setMessage(getString(R.string.popup_no_categories))
                .setCancelable(false)
                .setNegativeButton("Quitter", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                        finish();
                    }
                });

        builder.create().show();
    }


    @Override
    public void dataReady() {
        presenter.onDataReady();
    }

    /**
     * Display alert dialog if back office is down.
     */
    public void displayDialogDmrOffline() {

        AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this, R.style.MyDialogTheme);
        builder
                .setTitle(R.string.information)
                .setMessage(getString(R.string.dmr_offline))
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                        finish();
                    }
                });

        builder.create().show();
    }

    @Override
    public void displayDialogMessageInformation( String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashScreenActivity.this, R.style.MyDialogTheme);
        builder
                .setMessage(message.replaceFirst("\\.","\n\n"))
                .setCancelable(false)
                .setNegativeButton("Fermer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();
                        presenter.checkVersion();
                    }
                });
        builder.create().show();
    }


}

