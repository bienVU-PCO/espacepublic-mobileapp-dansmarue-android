package com.accenture.dansmarue.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.app.DansMaRueApplication;
import com.accenture.dansmarue.di.components.DaggerPresenterComponent;
import com.accenture.dansmarue.di.modules.PresenterModule;
import com.accenture.dansmarue.mvp.models.FavoriteAddress;
import com.accenture.dansmarue.mvp.models.Incident;
import com.accenture.dansmarue.mvp.presenters.WelcomeMapPresenter;
import com.accenture.dansmarue.mvp.views.WelcomeMapView;
import com.accenture.dansmarue.ui.adapters.RecyclerViewAdapter;
import com.accenture.dansmarue.ui.fragments.MapParisFragment;
import com.accenture.dansmarue.ui.fragments.ProfileFragment;
import com.accenture.dansmarue.utils.Constants;
import com.accenture.dansmarue.utils.MiscTools;
import com.accenture.dansmarue.utils.PrefManager;
import com.accenture.dansmarue.utils.RecyclerItemClickListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.LatLng;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * WelcomeMapActivity
 * Activity for main view
 */
public class WelcomeMapActivity extends BaseActivity implements WelcomeMapView, MapParisFragment.OnMapParisFragmentInteractionListener, ProfileFragment.OnProfileFragmentInteractionListener {

    private static final String TAG = WelcomeMapActivity.class.getCanonicalName();

    private static final int LOGIN_REQUEST_CODE = 1982;

    @Inject
    protected WelcomeMapPresenter presenter;

    private MapParisFragment mapParisFragment;

    @BindView(R.id.showAnomalyLinearLayout)
    protected LinearLayout showAnomalyLinearLayout;

    @BindView(R.id.avoid_duplicate)
    protected Button avoidDuplicateButton;

    @BindView(R.id.wbsa_fab)
    protected FloatingActionButton addAnomalyFloatingButton;
    //@BindView(R.id.follow_fab)
    //protected FloatingActionButton followAnomalyFloatingButton;
    @BindView(R.id.greetings_fab)
    protected FloatingActionButton greetingsAnomalyFloatingButton;
    @BindView(R.id.wbsa_coordinator)
    protected CoordinatorLayout coordinatorLayout;
    @BindView(R.id.wbsa_bottom_sheet)
    protected View bottomSheet;
    @BindView(R.id.bottom_sheet_my_adress)
    protected TextView selectedAdress;
    @BindView(R.id.title_bottom_sheet_my_incident)
    protected TextView selectedIncidentTitle;
    @BindView(R.id.image_bottom_sheet_my_incident)
    protected ImageView selectedIncidentPic;
    @BindView(R.id.adress_bottom_sheet_my_incident)
    protected TextView selectedIncidentAdress;
    @BindView(R.id.layout_bottom_sheet_my_adress)
    protected LinearLayout layoutSelectedAdress;
    @BindView(R.id.layout_bottom_sheet_my_incident)
    protected LinearLayout layoutSelectedIncident;
    @BindView(R.id.image_bottom_sheet_my_adress)
    protected ImageButton buttonSelectedAdress;
    @BindView(R.id.image_bottom_sheet_favorite_address)
    protected ImageButton buttonFavoriteAdress;
    @BindView(R.id.navigation)
    protected BottomNavigationView bottomNavigationView;
    @BindView(R.id.my_recycler_view)
    protected RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;

    @BindView(R.id.bottom_sheet_add_anomaly)
    protected LinearLayout layoutAddAnomaly;
    @BindView(R.id.bottom_sheet_anomaly_position_precision)
    protected LinearLayout layoutPrecisePosition;

    // setup bottom sheet
    // To handle FAB animation upon entrance and exit
    private Animation growAnimation;
    private Animation shrinkAnimation;

    //BottomSheet behavior
    private BottomSheetBehavior behavior;

    private boolean showFAB = true;
    private boolean fabFollowed = false;
    private boolean preciseMode = false;

    private ProfileFragment profileFragment;

    private Incident summarizedIncident;

    private final static int REQUEST_CODE_LOGIN = 7;

    private boolean congratulated;

    private String selectedAddressWithCodePostal;

    @Override
    protected void resolveDaggerDependency() {
        DaggerPresenterComponent.builder()
                .applicationComponent(((DansMaRueApplication) getApplication()).getApplicationComponent())
                .presenterModule(new PresenterModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        //askForOptinsPermission();
        initAnimations();
        initBottomSheet();
        initBottomNavigationView();

        // setup fragment and hide or not bottom sheet
        mapParisFragment = MapParisFragment.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, mapParisFragment);
        fragmentTransaction.commit();


    }

    @Override
    protected int getContentView() {
        return R.layout.welcome_map_activity_layout;
    }


    /**private void askForOptinsPermission() {

        //BEING COMPLIANT WITH GDPR
        // Manage opt-ins
        AdtagInitializer adtagInitializer = AdtagInitializer.getInstance();
        if (adtagInitializer.optinsNeverAsked()) {
            // ask the user to validate the optins
            new AlertDialog.Builder(this).setMessage(getString(R.string.ask_optins_permission_message))
                    .setCancelable(true)
                    .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adtagInitializer.updateOptin(OPTIN.USER_DATA, true);
                            adtagInitializer.allOptinsAreUpdated();
                            dialog.dismiss();
                        }
                    }).setNegativeButton("Non",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    adtagInitializer.updateOptin(OPTIN.USER_DATA, false);
                    adtagInitializer.allOptinsAreUpdated();
                    dialog.dismiss();
                }
            }).show();
        }
    }**/

    /**
     * Menu map or profile
     */
    private void initBottomNavigationView() {

        PrefManager prefManager = new PrefManager(getApplicationContext());
        
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_menu_map:
                        coordinatorLayout.setVisibility(View.VISIBLE);
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frameLayout, mapParisFragment);
                        fragmentTransaction.commitAllowingStateLoss();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                            showAnomalyLinearLayout.setAccessibilityTraversalBefore(R.id.bottom_menu_map);
                        }
                        break;
                    case R.id.bottom_menu_profile:
                            presenter.profileClicked();
                        break;
                }
                return true;
            }
        });
    }

    private void initAnimations() {
        growAnimation = AnimationUtils.loadAnimation(this, R.anim.simple_grow);
        shrinkAnimation = AnimationUtils.loadAnimation(this, R.anim.simple_shrink);

        addAnomalyFloatingButton.setVisibility(View.VISIBLE);
        addAnomalyFloatingButton.startAnimation(growAnimation);

        shrinkAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                addAnomalyFloatingButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * Init bottom sheet application.
     */
    private void initBottomSheet() {
        behavior = BottomSheetBehavior.from(bottomSheet);

        addAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.framboise)));
        //followAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.framboise)));
        greetingsAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_green)));

        behavior.setPeekHeight((int) ((198) * Resources.getSystem().getDisplayMetrics().density));

        avoidDuplicateButton.setOnClickListener(view -> {
            expandListOfActions();
            slideToTop();
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setPeekHeight(Math.round((198) * Resources.getSystem().getDisplayMetrics().density));
            bottomSheet.scrollTo(0, 0);
        });

        showAnomalyLinearLayout.setOnClickListener(view -> {
            expandListOfActions();
            slideToTop();
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setPeekHeight(Math.round((198) * Resources.getSystem().getDisplayMetrics().density));
            bottomSheet.scrollTo(0, 0);
        });

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull final View bottomSheet, int newState) {

                switch (newState) {

                    case BottomSheetBehavior.STATE_DRAGGING:
                        if (showFAB) {
                            if (addAnomalyFloatingButton.getVisibility() == View.VISIBLE)
                                addAnomalyFloatingButton.startAnimation(shrinkAnimation);
                            //if (followAnomalyFloatingButton.getVisibility() == View.VISIBLE)
                            //    followAnomalyFloatingButton.startAnimation(shrinkAnimation);
                            if (greetingsAnomalyFloatingButton.getVisibility() == View.VISIBLE)
                                greetingsAnomalyFloatingButton.startAnimation(shrinkAnimation);
                        }
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                        collapseListOfActions();
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        expandListOfActions();
                        break;

                    default:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        // RecyclerView de la BottomSheet
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RecyclerViewAdapter(this, R.layout.recycler_view_item, null);
        recyclerView.setAdapter(adapter);
        // Magic option to keep recyclerview at the right place
        recyclerView.setFocusable(false);

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        behavior.setPeekHeight((int) ((198) * Resources.getSystem().getDisplayMetrics().density));
                        bottomSheet.scrollTo(0,0);

                        presenter.onThumbnailClicked(adapter.getData(position));
                    }
                })
        );

    }

    /**
     * On expand bottom sheet.
     */
    private void expandListOfActions() {
        showFAB = false;

        layoutSelectedAdress.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.framboise));
        selectedAdress.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        buttonSelectedAdress.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.framboise));
        buttonSelectedAdress.setImageResource(R.drawable.ic_close);

        layoutSelectedIncident.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.framboise));
        selectedIncidentAdress.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.light_grey));
        selectedIncidentTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

        layoutSelectedAdress.setVisibility(View.VISIBLE);
        layoutSelectedIncident.setVisibility(View.GONE);

        showAnomalyLinearLayout.setVisibility(View.GONE);
        addAnomalyFloatingButton.setVisibility(View.GONE);
        //followAnomalyFloatingButton.setVisibility(View.GONE);
        greetingsAnomalyFloatingButton.setVisibility(View.GONE);

    }

    private void slideToTop() {
        Animation slide = null;
        slide = new TranslateAnimation(0f, 0f, 5f, 0f);
        slide.setDuration(500);
        bottomSheet.startAnimation(slide);

        slide.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // bottomSheet.clearAnimation();
                bottomSheet.setContentDescription(getString(R.string.text_around_incident));
                bottomSheet.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    bottomSheet.setAccessibilityHeading(true);
                }
            }

        });
    }

    /**
     * On collapse bottom sheet.
     */
    private void collapseListOfActions() {
        if (showFAB) {
            layoutSelectedAdress.setVisibility(View.VISIBLE);
            layoutSelectedIncident.setVisibility(View.GONE);
        } else {
            showFAB = true;
        }

        showAnomalyLinearLayout.setVisibility(View.VISIBLE);
        layoutPrecisePosition.setVisibility(View.VISIBLE);
        layoutAddAnomaly.setVisibility(View.GONE);
        //followAnomalyFloatingButton.setVisibility(View.GONE);
        greetingsAnomalyFloatingButton.setVisibility(View.GONE);
        addAnomalyFloatingButton.setVisibility(View.VISIBLE);
        addAnomalyFloatingButton.startAnimation(growAnimation);

        layoutSelectedIncident.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        selectedIncidentAdress.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_tranparent));
        selectedIncidentTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));


        layoutSelectedAdress.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        selectedAdress.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
        buttonSelectedAdress.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        buttonSelectedAdress.setImageResource(R.drawable.ic_geoloc_blue_circle);
        displayIconFavoriteAddress(selectedAddressWithCodePostal);

        addAnomalyFloatingButton.setImageResource(R.drawable.ic_add_anomaly_fab);
        addAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.framboise)));

       /** if (fabFollowed) {
            followAnomalyFloatingButton.setImageResource(R.drawable.ic_followed);
            followAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.framboise)));
        } else {
            followAnomalyFloatingButton.setImageResource(R.drawable.ic_follow);
            followAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.framboise)));
        }**/

        behavior.setPeekHeight((int) ((198) * Resources.getSystem().getDisplayMetrics().density));

    }

    @OnClick({R.id.bottom_sheet_my_adress, R.id.adress_bottom_sheet_my_incident, R.id.title_bottom_sheet_my_incident})
    public void onClickMyAdress() {

        // Depends on bottom sheet position

        if (behavior.getPeekHeight() == (int) ((198) * Resources.getSystem().getDisplayMetrics().density)) {
            layoutSelectedAdress.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.framboise));
            selectedAdress.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            buttonSelectedAdress.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.framboise));
            addAnomalyFloatingButton.setImageResource(R.drawable.ic_add_pink_24px);
            addAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));

            layoutSelectedIncident.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.framboise));
            selectedIncidentAdress.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.light_grey));
            selectedIncidentTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            behavior.setPeekHeight((int) ((354) * Resources.getSystem().getDisplayMetrics().density));

            /**if (fabFollowed) {
                followAnomalyFloatingButton.setImageResource(R.drawable.ic_followed_pink);
                followAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
            } else {
                followAnomalyFloatingButton.setImageResource(R.drawable.ic_follow_pink);
                followAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
            }**/

            if (congratulated) {
                greetingsAnomalyFloatingButton.setImageResource(R.drawable.ic_greetings_grey);
                greetingsAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_grey)));
            } else {
                greetingsAnomalyFloatingButton.setImageResource(R.drawable.ic_greetings_green);
                greetingsAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
            }


        } else {
            layoutSelectedAdress.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            selectedAdress.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            buttonSelectedAdress.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            addAnomalyFloatingButton.setImageResource(R.drawable.ic_add_anomaly_fab);
            addAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.framboise)));


            /**if (fabFollowed) {
                followAnomalyFloatingButton.setImageResource(R.drawable.ic_followed);
                followAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.framboise)));
            } else {
                followAnomalyFloatingButton.setImageResource(R.drawable.ic_follow);
                followAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.framboise)));
            }**/

            layoutSelectedIncident.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            selectedIncidentAdress.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_tranparent));
            selectedIncidentTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));

            if (congratulated) {
                greetingsAnomalyFloatingButton.setImageResource(R.drawable.ic_greetings_grey);
                greetingsAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_grey)));
            } else {
                greetingsAnomalyFloatingButton.setImageResource(R.drawable.ic_greetings_white);
                greetingsAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_green)));
            }

            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            behavior.setPeekHeight((int) ((198) * Resources.getSystem().getDisplayMetrics().density));
        }
    }

    @OnClick(R.id.image_bottom_sheet_my_adress)
    public void onClickCross() {
        if (preciseMode) {
            onClickPrecisePosition();
        } else {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            behavior.setPeekHeight((int) ((198) * Resources.getSystem().getDisplayMetrics().density));
        }
    }

    @OnClick(R.id.bottom_sheet_anomaly_position_precision)
    public void onClickPrecisePosition() {


        mapParisFragment = (MapParisFragment) getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if (null != mapParisFragment && mapParisFragment.isAdded()) {
            preciseMode = !preciseMode;
            mapParisFragment.precisePositionModeFunction(preciseMode);

            layoutSelectedAdress.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            selectedAdress.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.black));
            buttonSelectedAdress.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            addAnomalyFloatingButton.setImageResource(R.drawable.ic_add_anomaly_fab);
            addAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.framboise)));

            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            behavior.setPeekHeight((int) ((198) * Resources.getSystem().getDisplayMetrics().density));
        }
    }

    @OnClick({R.id.wbsa_fab, R.id.bottom_sheet_add_anomaly})
    public void addAnomalyClicked() {
        // Add another check about the address
        if (mapParisFragment != null && mapParisFragment.isValidLocation() && !selectedAdress.getText().equals("")) {

            // Descendre la bottom sheet avant l'ajout d'ano suivant si la bottom-sheet est à mi-hauteur ou on top
            if (behavior.getPeekHeight() == (int) ((354) * Resources.getSystem().getDisplayMetrics().density)) {
                Log.i(TAG, "mi-hauteur");
                onClickMyAdress();
            } else {
                Log.i(TAG, "on top");
                onClickCross();
                bottomSheet.scrollTo(0, 0);
            }

            // Send my current position (geoloc || search bar)
            Log.i(TAG, "ma position avant + : "+mapParisFragment.getMyCurrentLocationPosition().latitude+" longitude : "+mapParisFragment.getMyCurrentLocationPosition().longitude);

            Intent intentAddAnomaly = new Intent();
            intentAddAnomaly.setClass(WelcomeMapActivity.this, AddAnomalyActivity.class);
            intentAddAnomaly.putExtra(Constants.EXTRA_CURRENT_LOCATION, (mapParisFragment.getMyCurrentLocationPosition()));
            if (mapParisFragment.isSearchBarMode()) {
                intentAddAnomaly.putExtra(Constants.EXTRA_SEARCH_BAR_ADDRESS, (mapParisFragment.getSearchBarAdress()));
            } else if (mapParisFragment.getFavoriteAddressSelect().length() > 0) {
                intentAddAnomaly.putExtra(Constants.EXTRA_SEARCH_BAR_ADDRESS, (mapParisFragment.getFavoriteAddressSelect()));
            }

            startActivityForResult(intentAddAnomaly,9821);

            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            if (mapParisFragment.isNotConnectedMode()) {
                Toast.makeText(getApplicationContext(), R.string.add_anomaly_offline_mode, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.add_anomaly_no_location, Toast.LENGTH_SHORT).show();
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                presenter.profileClicked();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                bottomNavigationView.setSelectedItemId(R.id.bottom_menu_map);
            }
        }
    }

    @Override
    public void showProfile() {
        coordinatorLayout.setVisibility(View.GONE);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        if (profileFragment == null) {
            profileFragment = ProfileFragment.newInstance();
        }
        fragmentTransaction.replace(R.id.frameLayout, profileFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void showIncidentDetails(Incident incident) {
        final Intent intent = new Intent(WelcomeMapActivity.this, AnomalyDetailsActivity.class);
        intent.putExtra(Constants.EXTRA_INCIDENT_ID, incident.getId());
        intent.putExtra(Constants.EXTRA_INCIDENT_SOURCE, incident.getSource());
        startActivityForResult(intent,9281);
    }


    @Override
    public void onUpdateClosestIncidents(List<Incident> closestIncidents, boolean reset) {

        if (reset) {
            adapter = new RecyclerViewAdapter(getApplicationContext(), R.layout.recycler_view_item, null);
        } else {
            if (CollectionUtils.isNotEmpty(closestIncidents)) {
                if (adapter != null && adapter.getData()!=null) {
                    //remove duplicate incident Begin
                    Map<Long,Incident> mapOrigin = new LinkedHashMap<>();
                    for( Incident incident : adapter.getData()) {
                        mapOrigin.put(incident.getId(),incident);
                    }
                    Map<Long,Incident> mapNew = new LinkedHashMap<>();
                    for( Incident incident : closestIncidents) {
                        mapNew.put(incident.getId(),incident);
                    }
                    mapOrigin.putAll(mapNew);
                    //remove duplicate incident End
                    closestIncidents.clear();
                    closestIncidents.addAll(mapOrigin.values());
                }
                adapter = new RecyclerViewAdapter(getApplicationContext(), R.layout.recycler_view_item, closestIncidents);
            }
        }
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onUpdateLocation(LatLng location, String myAddr) {
        layoutSelectedIncident.setVisibility(View.GONE);
        greetingsAnomalyFloatingButton.setVisibility(View.GONE);
        //followAnomalyFloatingButton.setVisibility(View.GONE);

        layoutSelectedAdress.setVisibility(View.VISIBLE);
        addAnomalyFloatingButton.setVisibility(View.VISIBLE);

        boolean containsCity = false;
        List<String> listCity = Arrays.asList(getString(R.string.city_name).toUpperCase().split(","));
        for (String city : listCity) {
            if(myAddr.toUpperCase().contains(city)) {
                containsCity = true;
                break;
            }
        }

        if ("".equals(myAddr)) {
            selectedAdress.setText(location.latitude + ", " + location.longitude);
        } else if (!containsCity) {
            selectedAdress.setText("");
        } else {
            selectedAdress.setText(MiscTools.reformatArrondissement(myAddr));
            selectedAddressWithCodePostal = myAddr;
            displayIconFavoriteAddress(myAddr);
        }
        Log.i(TAG, "onUpdateLocation: adresse ok");
    }

    private void displayIconFavoriteAddress( String address) {

        PrefManager prefManager = new PrefManager(getApplicationContext());

        if(prefManager.getFavoriteAddress().containsKey(address)) {
            buttonFavoriteAdress.setImageResource(R.drawable.ic_full_star);
        } else {
            buttonFavoriteAdress.setImageResource(R.drawable.ic_add_star);
        }

        buttonFavoriteAdress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!prefManager.getFavoriteAddress().containsKey(address)) {
                    // add favorite address
                    prefManager.setFavorisAddress(new FavoriteAddress(address,mapParisFragment.getMyCurrentLocationPosition()),false);
                    buttonFavoriteAdress.setImageResource(R.drawable.ic_full_star);
                } else {
                    // delete favorite address
                    prefManager.setFavorisAddress(new FavoriteAddress(address,mapParisFragment.getMyCurrentLocationPosition()),true);
                    buttonFavoriteAdress.setImageResource(R.drawable.ic_add_star);
                }
            }
        });
    }

    // Avoid user to quit the app
    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onMarkerClicked(final Incident incident) {

        congratulated = false;

        if (!preciseMode && incident != null) {

            // Collapse BottomSheet and init address bar
            collapseListOfActions();

            summarizedIncident = incident;

            layoutPrecisePosition.setVisibility(View.GONE);
            layoutAddAnomaly.setVisibility(View.VISIBLE);

            layoutSelectedAdress.setVisibility(View.GONE);
            layoutSelectedIncident.setVisibility(View.VISIBLE);

            selectedIncidentAdress.setText(summarizedIncident.getAddress());
            selectedIncidentTitle.setText(summarizedIncident.getAlias());
            if(summarizedIncident.isFromRamen()) {
                selectedIncidentTitle.setText(R.string.desc_ramen);
            }

            addAnomalyFloatingButton.setVisibility(View.GONE);

            if (summarizedIncident.isFromRamen()) {
                greetingsAnomalyFloatingButton.setVisibility(View.GONE);
                //followAnomalyFloatingButton.setVisibility(View.VISIBLE);
                //followAnomalyFloatingButton.setImageResource(R.drawable.ic_follow);
                //followAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_grey)));

            } else {

                if (summarizedIncident.isResolu()) {

                    //followAnomalyFloatingButton.setVisibility(View.GONE);
                    greetingsAnomalyFloatingButton.setVisibility(View.VISIBLE);

                    if (congratulated) {
                        greetingsAnomalyFloatingButton.setImageResource(R.drawable.ic_greetings_grey);
                        greetingsAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_grey)));
                    } else {
                        greetingsAnomalyFloatingButton.setImageResource(R.drawable.ic_greetings_white);
                        greetingsAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_green)));
                    }

                } else {

                    greetingsAnomalyFloatingButton.setVisibility(View.GONE);
                    //followAnomalyFloatingButton.setVisibility(View.VISIBLE);

                    if (summarizedIncident.isIncidentFollowedByUser()) {
                        //followAnomalyFloatingButton.setImageResource(R.drawable.ic_followed);
                        fabFollowed = true;
                    } else {
                        //followAnomalyFloatingButton.setImageResource(R.drawable.ic_follow);
                        fabFollowed = false;

                    }

                }
            }

            if (summarizedIncident.getFirstAvailablePicture() != null) {
                Glide.with(this).load(summarizedIncident.getFirstAvailablePicture()).fallback(summarizedIncident.getPictures().getGenericPictureId()).error(summarizedIncident.getPictures().getGenericPictureId()).into(selectedIncidentPic);
            } else {
                Glide.with(this).load(summarizedIncident.getPictures().getGenericPictureId()).into(selectedIncidentPic);
            }
        }
    }

    @OnClick(R.id.image_bottom_sheet_my_incident)
    public void onThumbnailClicked() {
        presenter.onThumbnailClicked(summarizedIncident);
    }


    /**
     * Fab Buttons
     */

   /** @OnClick(R.id.follow_fab)
    public void followClicked() {
        //Should always be true
        if (summarizedIncident != null) {
            if (!summarizedIncident.isFromRamen()) {
                if (!summarizedIncident.isIncidentFollowedByUser()) {
                    presenter.followAnomaly(String.valueOf(summarizedIncident.getId()));
                } else {
                    presenter.unfollowAnomaly(String.valueOf(summarizedIncident.getId()));
                }
            }
        }
    }**/

    @OnClick(R.id.greetings_fab)
    public void congratulate() {
        if (null != summarizedIncident) {
            if (!congratulated) {
                greetingsAnomalyFloatingButton.setImageResource(R.drawable.ic_greetings_grey);
                greetingsAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_grey)));

                Snackbar.make(findViewById(R.id.navigation), R.string.greetings_ok, Snackbar.LENGTH_LONG)
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                switch (event) {
                                    case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                        presenter.congratulateAnomalie(String.valueOf(summarizedIncident.getId()));
                                        congratulated = true;
                                        break;
                                }
                            }
                        })
                        .setAction(R.string.greetings_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                changeGreetingsColorButton();
                            }
                        })
                        .show();
            }
        }
    }

    private void changeGreetingsColorButton() {
        if (behavior.getPeekHeight() == (int) ((198) * Resources.getSystem().getDisplayMetrics().density)) {
            greetingsAnomalyFloatingButton.setImageResource(R.drawable.ic_greetings_white);
            greetingsAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_green)));
        } else {
            greetingsAnomalyFloatingButton.setImageResource(R.drawable.ic_greetings_green);
            greetingsAnomalyFloatingButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
        }
    }

    /**
     * WS Results
     */

    public void displayFollow() {

        mapParisFragment.refreshDatas();
        //followAnomalyFloatingButton.setImageResource(R.drawable.ic_followed);
        Snackbar.make(findViewById(R.id.navigation), R.string.follow_anomaly, Snackbar.LENGTH_LONG).show();
        summarizedIncident.setIncidentFollowedByUser(true);
    }

    public void displayFollowFailure() {
        Snackbar.make(findViewById(R.id.navigation), R.string.follow_anomaly_failure, Snackbar.LENGTH_LONG).show();
    }

    public void displayUnfollow() {
        mapParisFragment.refreshDatas();
        //followAnomalyFloatingButton.setImageResource(R.drawable.ic_follow);
        Snackbar.make(findViewById(R.id.navigation), R.string.unfollow_anomaly, Snackbar.LENGTH_LONG).show();
        summarizedIncident.setIncidentFollowedByUser(false);
    }

    public void displayUnfollowFailure() {
        Snackbar.make(findViewById(R.id.navigation), R.string.unfollow_anomaly_failure, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void inviteToLogin() {
        startActivityForResult(new Intent(WelcomeMapActivity.this, LoginActivity.class), REQUEST_CODE_LOGIN);
    }

    @Override
    public void displayGreetingsOk() {
        Log.d(TAG, "displayGreetingsOk: ");
    }

    @Override
    public void displayGreetingsKo() {
        Snackbar.make(findViewById(R.id.navigation), R.string.dmr_error, Snackbar.LENGTH_LONG).show();
        congratulated = false;
        changeGreetingsColorButton();
    }

}
