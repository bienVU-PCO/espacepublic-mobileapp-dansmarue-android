package com.accenture.dansmarue.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.accenture.dansmarue.ui.activities.SplashScreenActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.app.DansMaRueApplication;
import com.accenture.dansmarue.di.components.DaggerPresenterComponent;
import com.accenture.dansmarue.di.modules.PresenterModule;
import com.accenture.dansmarue.mvp.models.FavoriteAddress;
import com.accenture.dansmarue.mvp.models.Incident;
import com.accenture.dansmarue.mvp.models.Position;
import com.accenture.dansmarue.mvp.presenters.MapParisPresenter;
import com.accenture.dansmarue.mvp.views.MapParisView;
import com.accenture.dansmarue.ui.activities.FavoriteAddressActivity;
import com.accenture.dansmarue.ui.activities.TypeEquipementChooser;
import com.accenture.dansmarue.utils.Constants;
import com.accenture.dansmarue.utils.MiscTools;
import com.accenture.dansmarue.utils.NetworkUtils;
import com.accenture.dansmarue.utils.PrefManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapParisFragment.OnMapParisFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapParisFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapParisFragment extends BaseFragment implements MapParisView, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = MapParisFragment.class.getCanonicalName();

    @Inject
    protected MapParisPresenter presenter;
    protected OnMapParisFragmentInteractionListener activity;

    private MapView mMapView;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;

    private PrefManager prefManager;

    private boolean validLocation = true;
    private LatLng myCurrentLocationPosition;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int REQUEST_LOCATION = 199;
    private final static int REQUEST_FAVORITE_ADDRESS = 200;

    private ImageView locationButton;
    private FloatingActionButton myProperFloatActionButton;
    private FloatingActionButton findByNumberButton;
    private BroadcastReceiver gpsReceiver;
    private BroadcastReceiver internetReceiver;

    private Boolean isNetworkOkReceiver;
    private Boolean myPrecisionMode = false;

    private Boolean searchBarMode = false;

    private String searchBarAddress ="";
    private String favoriteAddressSelect="";
    private String searchNumberIncident="";

    private Dialog dialogFindByNumber;

    /**
     * My position (choosen) marker
     */
    private Marker myPosMarker;
    /**
     * Restriction Paris
     */
    private LatLngBounds parisBounds = new LatLngBounds(
            new LatLng(48.896794, 2.308851),
            new LatLng(48.986503, 2.413853));

    private int permissionCheck;

    private Location locationNetwork;

    private boolean longPress;

    public MapParisFragment() {
        Log.d(TAG, "MapParisFragment: ");
        // Required empty public constructor
    }

    public static MapParisFragment newInstance() {
        Log.d(TAG, "newInstance: ");
        // Required empty public constructor
        return new MapParisFragment();
    }

    @Override
    protected void resolveDaggerDependency(DansMaRueApplication application) {
        DaggerPresenterComponent.builder()
                .applicationComponent(application.getApplicationComponent())
                .presenterModule(new PresenterModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {

        if (isAdded()) {
            Log.i(TAG, "onMapReady: ");
            initMap(map);
            isNetworkOkReceiver = NetworkUtils.isConnected(getActivity());
            initAutoCompleteSearchBar();
            initLocationButton();
            intLongPress();
            initFavoriteAddress();
        }

    }

    /**
     * Init favorite address function.
     */
    public void initFavoriteAddress() {
       ImageView imgViewFavoriteAddress =  getView().findViewById(R.id.favoris);
       imgViewFavoriteAddress.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               searchNumberIncident = null;
               Intent intent = new Intent(getActivity(), FavoriteAddressActivity.class);
               startActivityForResult(intent, REQUEST_FAVORITE_ADDRESS);
           }
       });
    }

    /**
     * Init on long press on map function.
     */
    private void intLongPress() {
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                searchNumberIncident = null;
                if (myPrecisionMode == true) activity.onClickPrecisePosition();
                longPress = true;
                searchBarMode = false;
                favoriteAddressSelect="";
                updateLocation(latLng);
            }
        });
    }

    private void popInGps() {
        if (!NetworkUtils.isGpsEnable(getContext())) {
            Log.i(TAG, "popInGps: +1");
            enableLocationViaGooglePopUp();
        }
    }

    /**
     * Init google map.
     * @param map
     */
    private void initMap(GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMap.setOnMarkerClickListener(this);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        if (null == myCurrentLocationPosition || validLocation == false) {
            updateMapToCentralParis();
        } else {
            updateLocation(myCurrentLocationPosition);
        }


    }

    /**
     * Center map on Paris.
     */
    private void updateMapToCentralParis() {
        if (null != googleMap) {
            LatLng centralParis = new LatLng(48.935378, 2.359882);

//        By Default, we center the map on Notre Dame
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centralParis, 12));
        }
    }

    /**
     * Init module auto complete search address.
     */
    private void initAutoCompleteSearchBar() {

        if (!Places.isInitialized()) {
            Places.initialize(getContext(), getString(R.string.google_maps_key));
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(getContext());

        //        Search and place pin
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG,Place.Field.NAME));
//        Be more specific : limit result to a rectangle Meudon > Bobigny
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(parisBounds));
        autocompleteFragment.setHint(getString(R.string.google_searchbar_wording));


//        Limit to France
        autocompleteFragment.setCountry("Fr");
        autocompleteFragment.setTypeFilter(TypeFilter.ADDRESS);

//        Search Results
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                searchNumberIncident = null;
                if (null != place) {
                    searchBarMode = true;
                    favoriteAddressSelect="";
                    LatLng latLngPlace = place.getLatLng();
                    if (myPrecisionMode == true) {
                        precisePositionModeFunction(false);
                        updateLocation(latLngPlace);
                        precisePositionModeFunction(true);
                    } else {
                        updateLocation(latLngPlace);
                    }
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i(TAG, "onError: " + status);
            }
        } );
    }

    /**
     * Init my current position function and find by number.
     */
    private void initLocationButton() {
        permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        //        No Geolocation Button if Permission has not been granted and Gps is NOT enable
        isNetworkOkReceiver = NetworkUtils.isConnected(getContext());
        if (permissionCheck == PackageManager.PERMISSION_GRANTED && ((isNetworkOkReceiver) || NetworkUtils.isGpsEnable(getContext()))) {
            googleMap.setMyLocationEnabled(true);

            // Retreive LastKnowPosition if GPS is off
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationNetwork = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            // Zoom on my position at the beginnning
            if (!searchBarMode && favoriteAddressSelect.length() ==  0 ) searchPositionAndUpdate(false);

            // TODO Check LastLocation
            // How to find, customize and position the location button
            locationButton = (ImageView) getView().findViewWithTag("GoogleMapMyLocationButton");
            if ( null != locationButton) {
                locationButton.setVisibility(View.INVISIBLE);
                locationButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!myPrecisionMode && favoriteAddressSelect.length() ==  0) {
                            searchBarMode = false;
                            searchPositionAndUpdate(true);
                        }
                    }
                });


                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(160, 160); // size of button in dp
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

//            Fix the position of the LocationButton (convert DP to pixels)
                int rightMargin = (int) ((13) * Resources.getSystem().getDisplayMetrics().density);
                int bottomMargin = (int) ((204) * Resources.getSystem().getDisplayMetrics().density);
                params.setMargins(rightMargin, 0, 0, bottomMargin);
                locationButton.setLayoutParams(params);

                myProperFloatActionButton = (FloatingActionButton) getView().findViewById(R.id.my_proper_location_button);
                myProperFloatActionButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.white)));
                myProperFloatActionButton.setVisibility(View.VISIBLE);

                myProperFloatActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        favoriteAddressSelect="";
                        if (!NetworkUtils.isGpsEnable(getContext())) enableLocationViaGooglePopUp();
                        locationButton.performClick();
                    }
                });

                //Find signalement by numero
                findByNumberButton = (FloatingActionButton) getView().findViewById(R.id.find_by_number_button);
                findByNumberButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.white)));
                findByNumberButton.setVisibility(View.VISIBLE);
                findByNumberButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialogFindByNumber(null);
                    }
                });

            }
        }

    }

    /**
     * Search select position.
     * @param popUpEnable
     */
    private void searchPositionAndUpdate(boolean popUpEnable) {
        if (null != googleMap) {

            Double lastLat = null;
            Double lastLong = null;

            try {
                lastLat = googleMap.getMyLocation().getLatitude();
                lastLong = googleMap.getMyLocation().getLongitude();
                Log.i(TAG, "searchPositionAndUpdate: ma position");
            } catch (Exception e) {
                try {
                    lastLat = locationNetwork.getLatitude();
                    lastLong = locationNetwork.getLongitude();
                    Log.i(TAG, "searchPositionAndUpdate: une ancienne position");

                } catch (Exception f) {
                    Log.i(TAG, "searchPositionAndUpdate: rien");

                }
            }

            if (popUpEnable) {
                if (null != lastLat && null != lastLong)
                    updateLocation(new LatLng(lastLat, lastLong));
            } else {
                //avoid double popup
                if (null != lastLat && null != lastLong && parisBounds.contains(new LatLng(lastLat, lastLong)))
                    updateLocation(new LatLng(lastLat, lastLong));
            }
        }

    }


    /**
     * Enable/Disable Anomaly mode precision
     * You can move the map and the marker is over - Uber effect
     */
    public void precisePositionModeFunction(boolean precisePositionMode) {

        searchNumberIncident = null;
        myPrecisionMode = precisePositionMode;

        if (permissionCheck == PackageManager.PERMISSION_GRANTED && (NetworkUtils.isGpsEnable(getContext()) || NetworkUtils.isConnected(getContext()))) {

            ImageView anomalyPinBlack = (ImageView) getView().findViewById(R.id.anomaly_pin_black);
            TextView anomalyHowToPinBlack = (TextView) getView().findViewById(R.id.anomaly_how_to_pin_black);

            TextView anomalyChoiceBox = (TextView) getView().findViewById(R.id.anomaly_choice_box);

            if (precisePositionMode) {

                anomalyPinBlack.setVisibility(View.VISIBLE);
                anomalyHowToPinBlack.setVisibility(View.VISIBLE);
                anomalyChoiceBox.setVisibility(View.GONE);

                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        favoriteAddressSelect="";
                        //Get latlng at the center by calling
                        final LatLng midLatLng = googleMap.getCameraPosition().target;
                        if (myCurrentLocationPosition == null || !myCurrentLocationPosition.equals(midLatLng)) {
                            updateLocation(midLatLng);
                        }

                    }
                });

            } else {
                googleMap.setOnCameraIdleListener(null);

                if (null != myCurrentLocationPosition)
                    myPosMarker = googleMap.addMarker(new MarkerOptions().position(myCurrentLocationPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_pink)));


                anomalyPinBlack.setVisibility(View.GONE);
                anomalyHowToPinBlack.setVisibility(View.GONE);
                if (prefManager.getTypesEquipement() != null) {
                    anomalyChoiceBox.setVisibility(View.VISIBLE);
                }

            }

        }


    }

    /**
     * Check if network or gps is enable.
     * @return true is No network and No Gps
     */
    public boolean isNotConnectedMode() {

        return !NetworkUtils.isConnected(getContext()) && !NetworkUtils.isGpsEnable(getContext());
    }

    public LatLng getMyCurrentLocationPosition() {
        return myCurrentLocationPosition;
    }


    public String getSearchBarAdress() {
        return searchBarAddress;
    }

    public boolean isSearchBarMode() {
        return searchBarMode;
    }

    public String getFavoriteAddressSelect() {
        return favoriteAddressSelect;
    }

    /**
     * Selected location is invalid.
     */
    public void invalidLocation() {
        if (getContext() != null) {

            new AlertDialog.Builder(getContext()).setMessage(R.string.not_in_paris)
                    .setCancelable(true)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            updateMapToCentralParis();
                        }
                    }).show();
        }
        validLocation = false;
        searchBarMode = false;
    }

    public boolean isValidLocation() {
        return validLocation;
    }

    /**
     * Refresh list of incidents by position and markers on the map
     */
    public void refreshDatas() {
        presenter.locationChanged(new Position(myPosMarker.getPosition().latitude, myPosMarker.getPosition().longitude));
    }

    public void locationChanged(LatLng location) {

        //location not changed
        Boolean locationChanged = true;
        if( myCurrentLocationPosition != null && location.latitude == myCurrentLocationPosition.latitude && location.longitude == myCurrentLocationPosition.longitude ) {
            locationChanged = false;
        }

        validLocation = true;
        myCurrentLocationPosition = location;
        Log.i(TAG, "locationChanged: LAT current / location" + myCurrentLocationPosition.latitude + " - " + location.latitude);

        if (myPosMarker != null) {
            myPosMarker.remove();
        }

//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centralParis, 12));

        if (null != googleMap) {
            if (myPrecisionMode == false && longPress == false)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myCurrentLocationPosition, 18));
        }
        longPress = false;

        if (NetworkUtils.isConnected(getContext())) {

            // Pay Attention... My MapFragement has disappeared - click on bottom bar but geocoder is still running ...
            if (getContext() != null) {

                if (Geocoder.isPresent() && null != location) {

                    // Transform my location into adress
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

                    try {

                        String currentAdress = "";
                        final List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 4);
                        if ( addresses != null && !addresses.isEmpty()) {
                            EditText searchBarText = (EditText) getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment).getView().findViewById(R.id.places_autocomplete_search_input);

                            final Address addressSelect = MiscTools.selectAddress(addresses, getString(R.string.city_name), searchBarMode,searchBarText.getText().toString());

                            final String address = addressSelect.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            Log.i(TAG, "adress " + address);
                            final String city = addressSelect.getLocality();
                            Log.i(TAG, "city " + city);

                            final String country = addressSelect.getCountryName();
                            Log.i(TAG, "country " + country);
                            final String postalCode = addressSelect.getPostalCode();
                            Log.i(TAG, "cp " + postalCode);

                            currentAdress = address;
                            Log.i(TAG, "current " + currentAdress);


                            Log.i(TAG, "Map Paris Refactor Adress " + MiscTools.reformatArrondissement(currentAdress));

                            //not in Paris
                            if (locationChanged && city != null && (! Arrays.asList(getString(R.string.city_name).toUpperCase().split(",")).contains(city.toUpperCase()))) {
                                invalidLocation();
                                return;
                            }
                            if (favoriteAddressSelect.length() > 0) {
                                activity.onUpdateLocation(myCurrentLocationPosition, favoriteAddressSelect);
                                searchBarMode = false;
                            }
                            else if (searchBarMode) {
                                if (!searchBarText.getText().toString().isEmpty()) {

                                    if(searchBarText.getText().toString().split(",").length > 1 ) {
                                        //fix commercial address
                                        String commercialAddress = searchBarText.getText().toString();
                                        String noCommercialAddress = searchBarText.getText().toString().replace(commercialAddress.substring(0, commercialAddress.indexOf(",")+1),"");
                                        searchBarText.setText(noCommercialAddress);
                                    }
                                    searchBarAddress = currentAdress.replace(currentAdress.substring(0, currentAdress.indexOf(",")), searchBarText.getText().toString());
                                }
                                activity.onUpdateLocation(myCurrentLocationPosition, searchBarAddress);
                            } else {
                                activity.onUpdateLocation(myCurrentLocationPosition, currentAdress);
                            }
                        }else {
                            activity.onUpdateLocation(myCurrentLocationPosition, currentAdress);
                        }
                    } catch (IOException e) {
                        FirebaseCrashlytics.getInstance().log(e.getMessage());
                        Log.e(TAG, e.getMessage(), e);
                        Log.i(TAG, "locationChanged: " + " Waiting 4 Geocoder");
//                        Toast.makeText(getContext(),"En attente du service de géocoding inversé.\nMerci de votre patience",Toast.LENGTH_SHORT).show();
//                        activity.onUpdateLocation(location, "En cours de résolution");
                    }


                } else {
                    Log.i(TAG, "locationChanged: Geocoder unavailable");
                }
            }
        } else {
            activity.onUpdateLocation(myCurrentLocationPosition, "");
        }

        presenter.locationChanged(new Position(myCurrentLocationPosition.latitude, myCurrentLocationPosition.longitude));
    }

    // Update location : geocoding and populate bottom sheet
    public void updateLocation(LatLng location) {
        presenter.validateNewLocation(location, parisBounds);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(gpsReceiver);
        getContext().unregisterReceiver(internetReceiver);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }
    }


    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {

        if (mGoogleApiClient == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    //.addApi(Places.GEO_DATA_API)
                    //.addApi(Places.PLACE_DETECTION_API)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();
        }
        mGoogleApiClient.connect();


    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();

                Log.i(TAG, "This device is supported");

            } else {

                Log.i(TAG, "This device is NOT supported");

//                Do Something in this case

            }
            return false;
        }
        return true;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) view.findViewById(R.id.map_anomaly_paris);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately
        mMapView.getMapAsync(this);

        // Test GPS and PopInEnableGps via Google Dialog
        popInGps();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View viewFragment = inflater.inflate(R.layout.fragment_map_paris, container, false);

        prefManager = new PrefManager(getActivity());

        TextView anomalyChoiceBox = (TextView) viewFragment.findViewById(R.id.anomaly_choice_box);
        if (prefManager.getTypesEquipement() != null) {
            // chevron inversé
            String libelle = getString(R.string.anomalie_espace_public_libelle) + "  " + Html.fromHtml("&#8744;");
            anomalyChoiceBox.setText(libelle);
            anomalyChoiceBox.setVisibility(View.VISIBLE);
            anomalyChoiceBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "Passage anos equipements");
                    Intent i = new Intent(getActivity(), TypeEquipementChooser.class);
                    startActivity(i);
                }
            });
        } else {
            anomalyChoiceBox.setVisibility(View.GONE);
        }


        return viewFragment;


    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (OnMapParisFragmentInteractionListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());

    }

    @Override
    public void updateAnomalyList(List<Incident> closestIncidents, boolean reset) {
        if (activity != null)
            activity.onUpdateClosestIncidents(closestIncidents, reset);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            if (null != googleMap) googleMap.setMyLocationEnabled(false);
        } catch (SecurityException e) {
        } finally {
            googleMap = null;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        myPrecisionMode = false;
        mMapView.onResume();// needed to get the map to display immediately
        mMapView.getMapAsync(this);

        checkPlayServices();


        /**
         * Receive the information about user action on GPS switch
         */

        gpsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                    //Do your stuff on GPS status change

                    LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);

                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                        if (null != locationButton) {
                            myProperFloatActionButton.setVisibility(View.VISIBLE);
                            findByNumberButton.setVisibility(View.VISIBLE);
                            Log.i(TAG, "Localisation : OK");
                        }

                    } else {
                        if (null != googleMap && null != locationButton && null != googleMap.getMyLocation() && NetworkUtils.isConnected(getContext()) == false) {
                            myProperFloatActionButton.setVisibility(View.GONE);
                            findByNumberButton.setVisibility(View.GONE);
                            Log.i(TAG, "Localisation : KO");
                        }
                    }

                }
            }
        };

        getContext().registerReceiver(gpsReceiver, new IntentFilter("android.location.PROVIDERS_CHANGED"));

        internetReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {


                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {


                    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

                    CardView cardview_place_autocomplete_fragment = (CardView) getView().findViewById(R.id.cardview_place_autocomplete_fragment);

                    if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {

                        Log.i(TAG, "internet : OK");

                        cardview_place_autocomplete_fragment.setVisibility(View.VISIBLE);

                        isNetworkOkReceiver = true;


                        if (null != myCurrentLocationPosition) {
                            locationChanged(myCurrentLocationPosition);
                        }

                        if (null != googleMap && null != locationButton) {
                            myProperFloatActionButton.setVisibility(View.VISIBLE);
                            findByNumberButton.setVisibility(View.VISIBLE);
                        }


                    } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {

                        Log.i(TAG, "internet : KO");

                        isNetworkOkReceiver = false;
                        cardview_place_autocomplete_fragment.setVisibility(View.GONE);
                        if (null != googleMap) {
                            if (null != locationButton && null != googleMap.getMyLocation() && NetworkUtils.isGpsEnable(getContext()) == false) {
                                myProperFloatActionButton.setVisibility(View.GONE);
                                findByNumberButton.setVisibility(View.GONE);
                            }
                        }
                    }

                }

            }
        };

        getContext().registerReceiver(internetReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));


    }


    @Override
    public void updateAnomalyMarkers(List<MarkerOptions> markers) {
        if (googleMap != null) {
            //googleMap.clear();

            for (MarkerOptions marker : markers) {
                googleMap.addMarker(marker);
            }
        }
    }

    public void updatePosMarker() {
        if (!myPrecisionMode && googleMap != null && myCurrentLocationPosition != null)
            myPosMarker = googleMap.addMarker(new MarkerOptions().position(myCurrentLocationPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_pink)));
    }

    @Override
    public void clearAnomaly() {
        if (googleMap != null) {
            googleMap.clear();
            myPosMarker = googleMap.addMarker(new MarkerOptions().position(myCurrentLocationPosition).icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_pink)));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (null != myPosMarker) {
            if (!myPosMarker.equals(marker) && null != activity) {
                final Incident incident = new GsonBuilder().create().fromJson(marker.getSnippet(), Incident.class);
                activity.onMarkerClicked(incident);
            } else {
                Log.i(TAG, "onMarkerClick: activity null");
            }
        }
        return false;
    }

    private void enableLocationViaGooglePopUp() {

        if (null != mGoogleApiClient) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                if (null != getActivity())
                                    status.startResolutionForResult(getActivity(), REQUEST_LOCATION);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                    case Activity.RESULT_CANCELED: {
                        // The user was asked to change settings, but chose not to
                        break;
                    }
                    default: {
                        break;
                    }
                }
            case REQUEST_FAVORITE_ADDRESS: {
               if(Activity.RESULT_OK == resultCode && data.getSerializableExtra(Constants.EXTRA_FAVORITE_ADDRESS_SELECT) != null)  {
                 FavoriteAddress favAddress = (FavoriteAddress) data.getSerializableExtra(Constants.EXTRA_FAVORITE_ADDRESS_SELECT);
                   myCurrentLocationPosition = new LatLng(favAddress.getLatitude(),favAddress.getLongitude());
                   favoriteAddressSelect = favAddress.getAddress();
               }
            }
            default: {
                break;
            }
        }
    }

    /**
     * Display modal dialog find incident by number.
     * @param errorMessage
     */
    private void showDialogFindByNumber(String errorMessage) {

        dialogFindByNumber = new Dialog(getContext());

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_find_by_number,null, false);

        final EditText input = (EditText) viewInflated.findViewById(R.id.input_number);

        final TextView errorMessageText = viewInflated.findViewById(R.id.dialog_message_error);
        if(errorMessage != null) {
            input.setText(searchNumberIncident);
            errorMessageText.setText(errorMessage);
        } else {
            searchNumberIncident = null;
        }

        Button buttonPublish = ( Button ) viewInflated.findViewById(R.id.button_search);
        buttonPublish.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (input.getText().toString().toUpperCase().matches("[BSGA][2][0-9]{3}[A-L][0-9]+")) {
                    searchNumberIncident = input.getText().toString().toUpperCase();
                    presenter.findByNumber(searchNumberIncident);
                } else {
                    errorMessageText.setText(R.string.incorect_number);
                    input.requestFocus();
                }
            }
        });

        // set the custom dialog components - text, image and button
        ImageView close = (ImageView) viewInflated.findViewById(R.id.btnCloseFailure);
        // Close Button
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchNumberIncident = null;
                dialogFindByNumber.dismiss();
            }
        });

        dialogFindByNumber.setContentView(viewInflated);
        dialogFindByNumber.setCancelable(true);
        if (dialogFindByNumber.getWindow() != null) {
            dialogFindByNumber.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Fix Dialog Size
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialogFindByNumber.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            dialogFindByNumber.getWindow().setAttributes(lp);
            dialogFindByNumber.show();
        }
    }

    /**
     * callbak for ws find incident by number.
     * @param errorMessage
     *         ws response is an error message
     * @param dmrOffline
     *          true if backoffice is down
     * @param posIncident
     *          position of the incident find.
     */
    public void callBackFindByNumber(String errorMessage, boolean dmrOffline, LatLng posIncident) {
           dialogFindByNumber.dismiss();
           if (dmrOffline) {
              searchNumberIncident = null;
               AlertDialog.Builder builder = new AlertDialog.Builder( getContext() , R.style.MyDialogTheme);
               builder
                       .setTitle(R.string.information)
                       .setMessage(getString(R.string.dmr_offline))
                       .setCancelable(false)
                       .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                               // User cancelled the dialog
                               dialog.dismiss();
                           }
                       });
               builder.create().show();
           } else if (errorMessage != null) {
               showDialogFindByNumber(errorMessage);
           } else {
               updateLocation(posIncident);
           }
    }

    public String getFindByNumberValue() {
        return searchNumberIncident;
    }

    public interface OnMapParisFragmentInteractionListener {
        void onUpdateClosestIncidents(final List<Incident> closestIncidents, boolean reset);

        void onUpdateLocation(LatLng location, String myAddr);

        void onMarkerClicked(Incident incident);

        void onClickPrecisePosition();
    }
}