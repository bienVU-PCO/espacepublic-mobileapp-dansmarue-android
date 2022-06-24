package com.accenture.dansmarue.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.accenture.dansmarue.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapAnomalyFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;


    private LatLng currentPos;


    public MapAnomalyFragment() {
        // Required empty public constructor
    }

    public static MapAnomalyFragment newInstance() {
        // Required empty public constructor
        return new MapAnomalyFragment();
    }

    public static MapAnomalyFragment newInstance(LatLng currentPos) {
        MapAnomalyFragment fragment = new MapAnomalyFragment();
        fragment.setCurrentPos(currentPos);
        return fragment;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (isAdded()) {

            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

//        Central Paris > Notre Dame: 48.853152, 2.349891
            LatLng centralParis = new LatLng(48.853152, 2.349891);

//        By Default, we center the map on Notre Dame
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centralParis, 12));

            if (currentPos != null) {
                CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(currentPos.latitude, currentPos.longitude));
                googleMap.moveCamera(center);
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                googleMap.addMarker(new MarkerOptions()
                        .position(currentPos)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_dmr_pin))
                );
            }

        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do other setup activities here too, as described elsewhere in this tutorial.

        // Build the Play services client for use by the Fused Location Provider and the Places API.
        // Use the addApi() method to request the Google Places API and the Fused Location Provider.
        if (mGoogleApiClient == null) {

            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    //.addApi(Places.GEO_DATA_API)
                    //.addApi(Places.PLACE_DETECTION_API)
                    .build();
        }
        mGoogleApiClient.connect();

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MapView mMapView = (MapView) view.findViewById(R.id.map_anomaly);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately
        mMapView.getMapAsync(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_anomaly, container, false);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void setCurrentPos(LatLng currentPos) {
        this.currentPos = currentPos;
    }

}
