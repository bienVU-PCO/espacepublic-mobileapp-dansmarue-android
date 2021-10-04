package com.accenture.dansmarue.mvp.presenters;

import android.app.Application;
import android.util.Log;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.mvp.models.Category;
import com.accenture.dansmarue.mvp.models.Incident;
import com.accenture.dansmarue.mvp.models.equipementsMunicipaux.TypeEquipement;
import com.accenture.dansmarue.mvp.views.BaseView;
import com.accenture.dansmarue.mvp.views.ProfileView;
import com.accenture.dansmarue.services.ApiServiceEquipement;
import com.accenture.dansmarue.services.SiraApiService;
import com.accenture.dansmarue.services.models.FollowRequest;
import com.accenture.dansmarue.services.models.GetIncidentsByUserRequest;
import com.accenture.dansmarue.services.models.GetIncidentsByUserResponse;
import com.accenture.dansmarue.services.models.SiraSimpleResponse;
import com.accenture.dansmarue.services.models.UnfollowRequest;
import com.accenture.dansmarue.utils.CategoryHelper;
import com.accenture.dansmarue.utils.Constants;
import com.accenture.dansmarue.utils.IncidentComparator;
import com.accenture.dansmarue.utils.PrefManager;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.GsonBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

/**
 * Created by PK on 11/05/2017.
 */

public class ProfilePresenter extends BasePresenter<ProfileView> implements SingleObserver<GetIncidentsByUserResponse> {


    private ProfileView view;
    private SiraApiService service;
    private ApiServiceEquipement apiServiceEquipement;
    private Application application;
    private PrefManager prefManager;

    private String filterState;

    @Inject
    public ProfilePresenter(final ProfileView view, final SiraApiService service, final ApiServiceEquipement apiServiceEquipement, final Application application, final PrefManager prefManager) {
        this.view = view;
        this.service = service;
        this.application = application;
        this.prefManager = prefManager;
        this.apiServiceEquipement = apiServiceEquipement;
    }

    /**
     * Load draft incidents save on device.
     */
    public void loadDrafts() {
        final List<Incident> drafts = new ArrayList<>();
        if (application.getApplicationContext().fileList().length > 0) {
            for (String file :
                    application.getApplicationContext().fileList()) {
                if (file.endsWith(Constants.FILE_DRAFT_SUFFIXE)) {
                    try {
                        FileInputStream in = application.getApplicationContext().openFileInput(file);
                        byte[] buffer = new byte[in.available()];
                        in.read(buffer);
                        final String bufferStr = new String(buffer);
                        drafts.add(new GsonBuilder().create().fromJson(bufferStr, Incident.class));
                    } catch (IOException e) {
                        FirebaseCrashlytics.getInstance().log(e.getMessage());
                        view.showDrafts(null);
                    }
                }
            }
            Collections.sort(drafts, IncidentComparator.getInstance());
            view.showDrafts(drafts);
        } else {
            view.showDrafts(null);
        }
    }

    public void deleteDraft(final String draftId) {
        application.deleteFile(draftId + Constants.FILE_DRAFT_SUFFIXE);
    }

    public void loadIncidentsByUser(String filterState) {
        if (prefManager.isConnected()) {
            this.filterState= filterState;
            if (prefManager.getTypesEquipement() != null) {
                final GetIncidentsByUserRequest request = new GetIncidentsByUserRequest();
                request.setEmail(prefManager.getEmail());
                request.setFilterIncidentStatus(this.filterState);
                apiServiceEquipement.getIncidentsByUser(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this);
            } else {
                callWsAnosOutdoor(new ArrayList<Incident>(),new ArrayList<Incident>());
            }

        } else {
            view.showUnsolvedIncidents(null);
            view.showSolvedIncidents(null);
        }

    }

    /**
     * Init user name.
     */
    public void initUser() {
        view.updateUserName(prefManager.getEmail(), "");
    }

    public void oldPositionMenu() {
        onMenuClicked(prefManager.getLastMenu());
    }

    public void onMenuClicked(int idMenu) {
        prefManager.setLastMenu(idMenu);

        switch (idMenu) {
            case R.id.menu_anos_drafts:
                view.showMenuDrafts();
                break;
            case R.id.menu_anos_unresolved:
                view.loadIncidents(Incident.STATE_OPEN);
                view.showMenuUnresolved();
                break;
            case R.id.menu_anos_resolved:
                view.loadIncidents(Incident.STATE_RESOLVED);
                view.showMenuResolved();
                break;
            default:
                view.showMenuDrafts();
                break;
        }
    }

    public void onPreferenceClicked() {
        view.showPreferences();
    }

    public void onItemClicked(boolean isDraft, final Incident item) {
        if (item != null) {
            if (isDraft) {
                view.modifyDraft(item);
            } else {
                view.showAnomalyDetails(item);
            }
        }
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onSuccess(GetIncidentsByUserResponse value) {
        if (value.getAnswer() != null && value.getAnswer().getStatus().equals(Constants.STATUT_WS_OK)) {
            if (value.getAnswer().getIncidents() != null) {

                final List<Incident> resolvedIncidents = new ArrayList<>();
                final List<Incident> unresolvedIncidents = new ArrayList<>();
                final List<TypeEquipement> maListeDeTypesDequipements = prefManager.getTypesEquipement();
                int index = 0;

                for (Incident incident : value.getAnswer().getIncidents()) {

                    if (null != incident.getEquipementId()) {

                        Log.i(TAG, "incident infos " + incident.toString());

                        final String idParentCategory = CategoryHelper.getFirstParent(incident.getCategoryId(), CategoryHelper.getAllCategories(application, true, prefManager.getEquipementIdMapTypeEquipementId().get(incident.getEquipementId())));

                        // Pay attention index of list is different from id ;
                        for (int i = 0; i < maListeDeTypesDequipements.size(); i++) {
                            if (maListeDeTypesDequipements.get(i).getIdTypEquipement().equals(prefManager.getEquipementIdMapTypeEquipementId().get(incident.getEquipementId()))) {
                                index = i;
                            }
                        }

                        incident.setIconParentId(idParentCategory);
                        incident.setTypeEquipementName(maListeDeTypesDequipements.get(index).getNomTypeEquipement());

                        Map<String, Category> mesCat = CategoryHelper.getAllCategories(application, true, maListeDeTypesDequipements.get(index).getIdTypEquipement());

                        final String idParentCategory2 = CategoryHelper.getFirstParent(incident.getCategoryId(), mesCat);
                        incident.setIconIncident(mesCat.get(idParentCategory2).getImageMobile());


                        if (incident.isResolu()) {
                            resolvedIncidents.add(incident);
                        } else {
                            unresolvedIncidents.add(incident);
                        }
                    }
                }

                callWsAnosOutdoor(resolvedIncidents, unresolvedIncidents);
            }
        } else {
            view.showSolvedIncidents(null);
            view.showUnsolvedIncidents(null);
        }
    }

    @Override
    public void onError(Throwable e) {
        view.showFailedLoading();
    }


    private void callWsAnosOutdoor(List<Incident> resolvedIncidents, List<Incident> unresolvedIncidents) {

        final GetIncidentsByUserRequest request = new GetIncidentsByUserRequest();
        request.setEmail(prefManager.getEmail());
        request.setFilterIncidentStatus(filterState);
        service.getIncidentsByUser(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new RequestGetIncidentsByUserOutDoorObserver(resolvedIncidents, unresolvedIncidents));


    }

    public void unFollowDraft(Incident incident) {
        if (null != incident.getEquipementId()) {
            UnfollowRequest request = new UnfollowRequest(String.valueOf(incident.getId()));
            request.setGuid(prefManager.getGuid());
            request.setUdid(prefManager.getUdid());
            apiServiceEquipement.unfollow(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new UnFollowObserver());
        } else {
            UnfollowRequest request = new UnfollowRequest(String.valueOf(incident.getId()));
            request.setGuid(prefManager.getGuid());
            request.setUdid(prefManager.getUdid());
            service.unfollow(request)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new UnFollowObserver());
        }
    }


    private class UnFollowObserver implements SingleObserver<SiraSimpleResponse> {


        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(SiraSimpleResponse value) {
            if (value.getAnswer() != null && value.getAnswer().getStatus().equals(Constants.STATUT_WS_OK) && FollowRequest.SERVICE_NAME.equals(value.getRequest())) {
                if ("0".equals(value.getAnswer().getStatus())) {
                    Log.i(TAG, "onSuccess: follow");
                } else {
                    Log.i(TAG, "onError: follow");
                }
            }

        }

        @Override
        public void onError(Throwable e) {
            Log.i(TAG, "onError Network: follow");
        }
    }


    private class RequestGetIncidentsByUserOutDoorObserver implements SingleObserver<GetIncidentsByUserResponse> {

        private List<Incident> resolvedIncidents;
        private List<Incident> unresolvedIncidents;

        public RequestGetIncidentsByUserOutDoorObserver(List<Incident> resolvedIncidents, List<Incident> unresolvedIncidents) {
            this.resolvedIncidents = resolvedIncidents;
            this.unresolvedIncidents = unresolvedIncidents;
        }

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(GetIncidentsByUserResponse value) {
            if (value.getAnswer() != null && value.getAnswer().getStatus().equals(Constants.STATUT_WS_OK)) {
                if (value.getAnswer().getIncidents() != null) {


                    for (Incident incident :
                            value.getAnswer().getIncidents()) {

                        final String idParentCategory = CategoryHelper.getFirstParent(incident.getCategoryId(), CategoryHelper.getAllCategories(application));

                        if (incident.isResolu()) {
                            incident.getPictures().setGenericPictureId(CategoryHelper.MAP_GENERIC_PICTURES.get(idParentCategory));
                        } else {
                            incident.getPictures().setGenericPictureId(CategoryHelper.MAP_GENERIC_PICTURES.get(idParentCategory));
                        }

                        Log.i(TAG, "outdoor");
                        Log.i(TAG, "incident id : " + incident.getId());
                        Log.i(TAG, "load incident > id-Parent-Category : " + idParentCategory);
                        Log.i(TAG, "load incident > get First Parent   : " + CategoryHelper.getFirstParent(idParentCategory, CategoryHelper.getAllCategories(application)));
                        Log.i(TAG, "load incident > nom : " + incident.getDescriptive());
                        Log.i(TAG, "load incident > id equipement : " + incident.getEquipementId());

                        incident.setIconParentId(idParentCategory);
                        if (incident.isResolu()) {
                            resolvedIncidents.add(incident);
                        } else {
                            unresolvedIncidents.add(incident);
                        }
                    }

                    if( Incident.STATE_RESOLVED.equals(filterState)) {
                        Collections.sort(resolvedIncidents, IncidentComparator.getInstance());
                        view.showSolvedIncidents(resolvedIncidents);
                    }

                    if( Incident.STATE_OPEN.equals(filterState)) {
                        Collections.sort(unresolvedIncidents, IncidentComparator.getInstance());
                        view.showUnsolvedIncidents(unresolvedIncidents);
                    }

                }
            } else {
                view.showSolvedIncidents(null);
                view.showUnsolvedIncidents(null);
            }

        }

        @Override
        public void onError(Throwable e) {

        }
    }


    @Override
    protected BaseView getView() {
        return view;
    }


}
