package com.accenture.dansmarue.mvp.presenters;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.mvp.models.Category;
import com.accenture.dansmarue.mvp.views.AddAnomalyView;
import com.accenture.dansmarue.mvp.views.BaseView;
import com.accenture.dansmarue.services.SiraApiService;
import com.accenture.dansmarue.services.models.ProcessWorkflowRequest;
import com.accenture.dansmarue.services.models.SaveIncidentRequest;
import com.accenture.dansmarue.services.models.SaveIncidentResponse;
import com.accenture.dansmarue.services.models.SiraSimpleResponse;
import com.accenture.dansmarue.utils.CategoryHelper;
import com.accenture.dansmarue.utils.Constants;
import com.accenture.dansmarue.utils.DateUtils;
import com.accenture.dansmarue.utils.NetworkUtils;
import com.accenture.dansmarue.utils.PrefManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;


/**
 * Created by PK on 02/04/2017.
 */

public class AddAnomalyPresenter extends BasePresenter<AddAnomalyView> implements SingleObserver<SaveIncidentResponse> {

    private static final String TAG = AddAnomalyPresenter.class.getName();

    private AddAnomalyView view;
    private SiraApiService service;
    private PrefManager prefManager;
    private Application application;

    private long timeStamp = new Date().getTime();
    private SaveIncidentRequest request;
    private long draftId;

    private String picture1;
    private String picture2;

    private int nbPhotos;
    private int nbPhotosSent;
    private int nbPhotosSentWithError;
    private Integer incidentId;

    @Inject
    public AddAnomalyPresenter(final AddAnomalyView view, final SiraApiService service, final PrefManager prefManager, final Application application) {
        this.view = view;
        this.service = service;
        this.prefManager = prefManager;
        this.application = application;
    }

    public void setCategory(final String categoryId) {

        Category category = CategoryHelper.getAllCategories(application).get(categoryId);
        if(category != null) {
            getRequest().getIncident().setCategoryId(categoryId);
            getRequest().getIncident().setAlias(category.getName());

            final String idParentCategory = CategoryHelper.getFirstParent(getRequest().getIncident().getCategoryId(), CategoryHelper.getAllCategories(application));


            getRequest().getIncident().setIconIncidentSignalement(CategoryHelper.MAP_GENERIC_PICTURES.get(idParentCategory));
        }

    }

    public void openDraft(final long draftId) {
        this.draftId = draftId;
    }


    public void setAdress(final String adresse, final LatLng latLng) {
        getRequest().getIncident().setAddress(adresse);
        getRequest().getPosition().setLatitude(latLng.latitude);
        getRequest().getPosition().setLongitude(latLng.longitude);
        getRequest().getIncident().setLat(String.valueOf(latLng.latitude));
        getRequest().getIncident().setLng(String.valueOf(latLng.longitude));
    }

    public void setDescription(String description) {
        getRequest().getIncident().setDescriptive(description);
    }

    public void setCommentaireAgent(String description) {
        getRequest().getIncident().setCommentaireAgent(description);
    }

    public void setPriority(Integer priorityId) {
        getRequest().getIncident().setPriorityId(priorityId);
    }

    public void createIncident() {
        if (prefManager.isConnected()) {
            getRequest().setGuid(prefManager.getGuid());
            getRequest().setUserToken(prefManager.getUserToken());
            getRequest().setEmail(prefManager.getEmail());

            Log.i(TAG, "createIncident: guid "+prefManager.getGuid());
            Log.i(TAG, "createIncident: udid "+prefManager.getUdid());
            Log.i(TAG, "createIncident: token "+prefManager.getUserToken());


        }
        getRequest().setUdid(prefManager.getUdid());
        service.saveIncident(getRequest())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

    /**
     * Set the email in the incident request after a validation
     *
     * @param email email to set
     * @return true if the email is valid <br>
     * false otherwise
     */
    public boolean setEmail(final String email) {
        if (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //prefManager.setConnected(email,null);
            getRequest().setEmail(email);
            return true;
        }

        return false;
    }

    /**
     * Publish action from the view, we check if we know the user email and ask the view to show the greetings dialog
     */
    public void onPublish() {
        if (prefManager.isConnected()) {
            getRequest().setEmail(prefManager.getEmail());
            getRequest().setGuid(prefManager.getGuid());
            view.showGreetingsDialog(false);
        } else {
            view.showGreetingsDialog(true);
        }
    }

    public boolean isIncidentValid() {
        boolean isValid = getRequest().getPosition().getLatitude() != 0d;
        isValid = isValid && getRequest().getPosition().getLongitude() != 0d;
        isValid = isValid && getRequest().getIncident().getAddress() != null;
        isValid = isValid && getRequest().getIncident().getCategoryId() != null;
        isValid = isValid && getRequest().getIncident().getFirstAvailablePicture() != null;
        return isValid;
    }

    public SaveIncidentRequest getRequest() {
        if (request == null) {
            request = new SaveIncidentRequest();
        }
        return request;
    }


    @Override
    public void onError(final Throwable e) {
        if (NetworkUtils.isConnected(application.getApplicationContext())){
            Log.i(TAG, "DMR is off");
            view.displayDialogDmrOffline();
        } else {
            Log.e(TAG, "onError", e);
            view.showDialogErrorSaveDraft();
        }

    }


    @Override
    public void onSubscribe(Disposable d) {
    }


    @Override
    public void onSuccess(SaveIncidentResponse saveIncidentResponse) {
        if (null != saveIncidentResponse && null != saveIncidentResponse.getAnswer() && (saveIncidentResponse.getAnswer().getStatus()).equals(Constants.STATUT_WS_OK)) {
            Log.i(TAG, "onSuccess: save incident");
            //let know the view about the creation
            view.onIncidentCreated(saveIncidentResponse.getAnswer().getIncidentId());
        } else if (null != saveIncidentResponse && saveIncidentResponse.getErrorMessage() != null) {
            Log.i(TAG, "onError: save incident");
            view.showDialogErrorSaveDraft();
        } else {
            Log.i(TAG, "DMR is off");
            view.displayDialogDmrOffline();
        }
    }

    /**
     * Upload pictures linked to the incident
     *
     * @param incidentId incident id
     *                   second pic (can be null)
     */
    public void uploadPictures(@NonNull final Integer incidentId) {

        nbPhotos = 0;
        nbPhotosSent = 0;
        nbPhotosSentWithError = 0;
        this.incidentId = incidentId;


        if (picture1 != null) {
            ++nbPhotos;
            uploadPicture(incidentId, picture1, "close");
        }

        if (picture2 != null) {
            ++nbPhotos;
            uploadPicture(incidentId, picture2, "far");
        }

    }

    public String getPicture1State() {
        return picture1;
    }

    public String getPicture2State() {
        return picture2;
    }


    /**
     * Upload of a picture linked to an incident
     *
     * @param incidentId incident id
     * @param pictureUrl fileUrl of the picture to upload
     * @param type       far or close FIXME do we need that ?
     */
    private void uploadPicture(@NonNull final Integer incidentId, @NonNull final String pictureUrl, final String type) {
        final File picture = new File(pictureUrl);
        final RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), picture);
        final Map<String, String> headers = new HashMap<>();
        headers.put("udid", prefManager.getUdid());
        headers.put("incident_id", incidentId.toString());
        headers.put("type", type);
        headers.put("INCIDENT_CREATION", Boolean.TRUE.toString());
        service.uploadPicture(headers, requestFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).
                subscribe(new UploadPictureObserver());
    }


    /**
     * Call Lutece Workflow DMR to initialiaze state incident
     * @param incidentId
     *          id incident
     */
    private void callWorkFlow(@NonNull final Integer incidentId) {
        ProcessWorkflowRequest request = new ProcessWorkflowRequest();
        request.setId(incidentId.toString());
        service.processWorkflow(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CallWorkFlowObserver());

    }

    private class CallWorkFlowObserver implements SingleObserver<SiraSimpleResponse> {


        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(SiraSimpleResponse value) {
            Log.i(TAG, "onSuccess: workflow");
            //we start by removing the potential draft
            removeDraft();
            // remove the model so its not savable anymore (and no more draft well be created after the onquit action)
            request = null;
            view.showIncidentAndPhotosOkThankYou(true);
        }

        @Override
        public void onError(Throwable e) {
            Log.i(TAG, "onError: workflow");
            view.showIncidentAndPhotosOkThankYou(false);
        }
    }


    public String getLastUsedEmail() {
        return prefManager.getEmail();
    }

    /**
     * Save in a json file the incident currently in creation
     */
    public void saveDraft() {

        try {
            removeDraft();
            final Long prefix;
            if (draftId != 0L) {
                prefix = draftId;
            } else {
                prefix = timeStamp;
            }
            getRequest().getIncident().setId(prefix);
            final Date now = new Date();
            getRequest().getIncident().setDate(DateUtils.extractDate(now));
            getRequest().getIncident().setHour(DateUtils.extractTime(now));

            // add id icon parent for draft icon
            String idParentCategory = CategoryHelper.getFirstParent(getRequest().getIncident().getCategoryId(), CategoryHelper.getAllCategories(application));
            getRequest().getIncident().setIconParentId(idParentCategory);

            if (getRequest().getIncident().getIconIncidentSignalement()==0) {
                getRequest().getIncident().getPictures().setGenericPictureId(R.drawable.ic_streetview_grey_24dp);
            }

            FileOutputStream fos = application.getApplicationContext().openFileOutput(prefix + Constants.FILE_DRAFT_SUFFIXE, Context.MODE_PRIVATE);
            final String jsonIncident = getRequest().getIncident().toJson();

            Writer out = new OutputStreamWriter(fos);
            out.write(jsonIncident);
            out.close();
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Called when the view is quitting.<br>
     * Save or propose a draft depending on the request state (blank ?)and if the action comes from the user.
     *
     * @param byUser true if it's a user action
     *               false otherwise
     */
    public void onQuit(boolean byUser) {
        if (isIncidentSavable()) {
            if (byUser) {
                view.proposeDraft();
            } else {
                saveDraft();
            }
        } else if (byUser) {
            view.navigateBack();
        }
    }

    /**
     * Check if the incident is "savable" ie : at least one field has been filled by the user
     *
     * @return true is at least one field is filled <br>
     * false otherwise
     */
    private boolean isIncidentSavable() {
        boolean isSavable = getRequest().getIncident().getCategoryId() != null;
        isSavable = isSavable || getRequest().getIncident().getFirstAvailablePicture() != null;
        isSavable = isSavable || !"".equals(getRequest().getIncident().getDescriptive());
        return isSavable;
    }

    public void removeDraft() {
        final Long prefix;
        if (draftId != 0L) {
            prefix = draftId;
        } else {
            prefix = timeStamp;
        }
        application.getApplicationContext().deleteFile(prefix + Constants.FILE_DRAFT_SUFFIXE);
    }


    public void savePictureInFile(final Bitmap thumbnail) {
        try {
            final String fileName = new Date().getTime() + Constants.FILE_PICTURE_SUFFIXE;
            FileOutputStream fos = application.getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            // 100 is max value
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            thumbnail.recycle();
            fos.close();
            view.showPicture(application.getFileStreamPath(fileName).getAbsolutePath());
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public void addPictureToModel(final String path) {
        if (getRequest().getIncident().getPicture1() == null) {
            getRequest().getIncident().addPicture1(path);
            picture1 = path;
        } else {
            getRequest().getIncident().addPicture2(path);
            picture2 = path;
        }
    }

    public void removePicture1() {
        getRequest().getIncident().deletePicture1();
        application.deleteFile(picture1.substring(picture1.lastIndexOf('/') + 1, picture1.length()));
        picture1 = null;
    }

    public void removePicture2() {
        getRequest().getIncident().deletePicture2();
        application.deleteFile(picture2.substring(picture2.lastIndexOf('/') + 1, picture2.length()));
        picture2 = null;
    }

    public void isAgentConnected () {
        view.showHideAgentCommentaryField(prefManager.getIsAgent());
    }

    @Override
    protected BaseView getView() {
        return view;
    }

    private void callWorkFlowTest() {

        Log.i(TAG, "callWorkFlow: " + "nbPhotos = " + nbPhotos + " || " + " sent : " + nbPhotosSent + " error : " + nbPhotosSentWithError);

        if (nbPhotos == nbPhotosSent + nbPhotosSentWithError) {
            Log.i(TAG, "callWorkFlow: OK");
            callWorkFlow(incidentId);


        } else {
            Log.i(TAG, "callWorkFlow: NOT YET");


        }

    }

    private class UploadPictureObserver implements SingleObserver<ResponseBody> {

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(ResponseBody value) {

            if (null != value) {
                if (picture1 != null) {
                    Log.i(TAG, "onSuccess: photo 1");
                    new File(picture1).delete();
                    getRequest().getIncident().deletePicture1();
                    ++nbPhotosSent;
                    callWorkFlowTest();

                } else if (picture2 != null) {
                    Log.i(TAG, "onSuccess: photo 2");
                    new File(picture2).delete();
                    getRequest().getIncident().deletePicture2();
                    ++nbPhotosSent;
                    callWorkFlowTest();
                }
            }


        }

        @Override
        public void onError(Throwable e) {
            networkError();
            Log.e(TAG, e.getMessage(), e);

            ++nbPhotosSentWithError;
            view.showIncidentAndPhotosOkThankYou(false);


        }
    }
}
