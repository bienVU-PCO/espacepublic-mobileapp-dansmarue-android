package com.accenture.dansmarue.mvp.presenters;

import android.app.Application;
import android.util.Log;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.mvp.models.DossierRamen;
import com.accenture.dansmarue.mvp.models.Incident;
import com.accenture.dansmarue.mvp.models.Position;
import com.accenture.dansmarue.mvp.views.BaseView;
import com.accenture.dansmarue.mvp.views.MapParisView;
import com.accenture.dansmarue.services.SiraApiService;
import com.accenture.dansmarue.services.models.GetIncidentsByPositionRequest;
import com.accenture.dansmarue.services.models.GetIncidentsByPositionResponse;
import com.accenture.dansmarue.utils.CategoryHelper;
import com.accenture.dansmarue.utils.Constants;
import com.accenture.dansmarue.utils.PrefManager;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.GsonBuilder;

import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * Created by PK on 19/04/2017.
 */
public class MapParisPresenter extends BasePresenter<MapParisView> implements SingleObserver<GetIncidentsByPositionResponse> {

    private static final String TAG = MapParisPresenter.class.getCanonicalName();

    private MapParisView view;
    private SiraApiService service;
    private Application application;
    private PrefManager prefManager;

    @Inject
    public MapParisPresenter(final MapParisView view, final SiraApiService service, final Application application, PrefManager prefManager) {
        this.view = view;
        this.service = service;
        this.application = application;
        this.prefManager = prefManager;

    }

    /**
     * On selected location change.
     * Call WS to load close incident
     * @param newLocation
     *        selected location
     *
     */
    public void locationChanged(final Position newLocation) {

        view.clearAnomaly();
        view.updateAnomalyList(null,true);

        GetIncidentsByPositionRequest request = new GetIncidentsByPositionRequest();
        request.setPosition(newLocation);
        request.setGuid(prefManager.getGuid());
        if(view.getFindByNumberValue() != null && !"".equals(view.getFindByNumberValue().trim())) {
            request.setSearchByNumber(view.getFindByNumberValue());
        }
        //Get incidents DMR
        service.getIncidentsByPosition(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onSuccess(final GetIncidentsByPositionResponse value) {
        Log.i(TAG,value.toString());
        if (null != value && null != value.getAnswer() && value.getAnswer().getStatus().equals(Constants.STATUT_WS_OK)) {
            if (CollectionUtils.isNotEmpty(value.getAnswer().getClosestIncidents())) {
                displayIncidents(value.getAnswer().getClosestIncidents());
            } else if (CollectionUtils.isNotEmpty(value.getAnswer().getIncident())) {
                displayIncidents(value.getAnswer().getIncident());
                LatLng latlngIncident = new LatLng(Double.valueOf(value.getAnswer().getIncident().get(0).getLat()),Double.valueOf(value.getAnswer().getIncident().get(0).getLng()));
                view.callBackFindByNumber(null, false, latlngIncident);
            }
        } else if ( null != value.getErrorMessage()) {
            view.callBackFindByNumber(value.getErrorMessage(), false,null);
        } else if ( value.getAnswer() == null) {
            view.callBackFindByNumber(null, true,null);
        }
    }

    @Override
    public void onError(final Throwable e) {
        Log.e(TAG, e.getMessage(), e);
        networkError();
    }

    /**
     * Call WebService Find incident by number
     * @param incidentNumber
     */
    public void findByNumber(final String incidentNumber) {
           service.getAnomalieByNumber(incidentNumber)
                   .subscribeOn(Schedulers.io())
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribe(this);
    }

    /**
     * Display on map DMR and Ramen incident.
     * @param incidentsTodisplay
     *          list incident to display on map
     */
    private void displayIncidents(List <Incident> incidentsTodisplay) {

            if (CollectionUtils.isNotEmpty(incidentsTodisplay)) {
                final List<MarkerOptions> markers = new ArrayList<>(incidentsTodisplay.size());
                final List<Incident> anomalyList = new ArrayList<>(incidentsTodisplay.size());
                for (Incident incident : incidentsTodisplay) {
                    LatLng newLatLng = new LatLng(Double.parseDouble(incident.getLat()), Double.parseDouble(incident.getLng()));
                    if (incident.isFromRamen()) {
                        // Category "Encombrants"
                        incident.setCategoryId(CategoryHelper.ID_CATEGORIE_RAMEN);
                    }
                    final String idParentCategory = CategoryHelper.getFirstParent(incident.getCategoryId(), CategoryHelper.getAllCategories(application));
                    if (CategoryHelper.CAT_ICONS.get(idParentCategory) != null) {
                        if (incident.isResolu()) {
                            incident.setIconIncidentSignalement(CategoryHelper.MAP_ICONS_RESOLVED.get(idParentCategory));
                        } else {
                            incident.setIconIncidentSignalement(CategoryHelper.MAP_ICONS.get(idParentCategory));

                            anomalyList.add(incident);
                        }

                        incident.getPictures().setGenericPictureId(CategoryHelper.MAP_GENERIC_PICTURES.get(idParentCategory));

                        // anchor : centre de l'image = position gps
                        MarkerOptions markerOpt = new MarkerOptions()
                                .position(newLatLng)
                                .anchor(0.5f,0.5f)
                                .snippet(incident.toJson())
                                .icon(BitmapDescriptorFactory.fromResource(incident.getIconIncidentSignalement()));

                        if ( view.getFindByNumberValue() != null && incident.getReference().equals( view.getFindByNumberValue())) {
                            //if it's a search by number display only the reporting search  DMR-2146
                            markers.add(markerOpt);
                        } else if ( view.getFindByNumberValue() == null || view.getFindByNumberValue().trim().length() == 0) {
                            markers.add(markerOpt);
                        }



                    }
                }
                view.updateAnomalyMarkers(markers);

                view.updateAnomalyList(anomalyList, false);
                view.updatePosMarker();
            } else {
                view.updatePosMarker();
            }
    }


    /**
     * Check if the given location is inside the given bounds. <br>
     * Callback to a different method in the view wether the location is correct or not.
     *
     * @param location location to check
     * @param bounds   bounds
     */
    public void validateNewLocation(final LatLng location, final LatLngBounds bounds) {
        if (bounds.contains(location)) {
            view.locationChanged(location);
        } else {
            view.invalidLocation();
        }
    }


    @Override
    protected BaseView getView() {
        return view;
    }


}