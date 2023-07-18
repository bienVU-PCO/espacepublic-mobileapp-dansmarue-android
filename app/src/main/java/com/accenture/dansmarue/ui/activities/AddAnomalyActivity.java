package com.accenture.dansmarue.ui.activities;

import static com.accenture.dansmarue.ui.activities.ChoosePriorityActivity.PRIORITIES_IDS;
import static com.accenture.dansmarue.ui.activities.ChoosePriorityActivity.PRIORITIES_LIBELLE;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.di.components.DaggerPresenterComponent;
import com.accenture.dansmarue.di.modules.PresenterModule;
import com.accenture.dansmarue.mvp.models.Incident;
import com.accenture.dansmarue.mvp.presenters.AddAnomalyPresenter;
import com.accenture.dansmarue.mvp.views.AddAnomalyView;
import com.accenture.dansmarue.ui.fragments.MapAnomalyFragment;
import com.accenture.dansmarue.utils.Constants;
import com.accenture.dansmarue.utils.MiscTools;
import com.accenture.dansmarue.utils.NetworkUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * AddAnomalyActivity
 * Activity to create new incident
 */
public class AddAnomalyActivity extends BaseAnomalyActivity implements AddAnomalyView {

    private static final String TAG = AddAnomalyActivity.class.getCanonicalName();
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    private static final int DEFAULT_PRIORITY_ID = R.id.priority_low;

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1976;
    private static final int CHOOSE_TYPE_REQUEST_CODE = 1977;
    private static final int SET_DESCRIPTION_REQUEST_CODE = 1978;
    private static final int CHOOSE_PRIORITY_REQUEST_CODE = 1979;
    private static final int LOGIN_REQUEST_CODE = 1982;
    private static final int SET_COMMENTAIRE_AGENT_REQUEST_CODE = 1983;

    private static final String KEYWORD_ADDRESS_BRIDGE = "pont";

    private static LatLng myLastPosition = null;

    private Dialog greetingsDialog, greetingsDialogSendIncident, greetingsDialogSendError, greetingsDialogSendOkThanks;
    private Dialog emailDialog;

    @Inject
    protected AddAnomalyPresenter presenter;

    //Type
    @BindView(R.id.image_choose_type)
    protected ImageView chooseType;
    @BindView(R.id.text_choose_type)
    protected TextView type;
    @BindView(R.id.text_choose_type_subtitle)
    protected TextView chooseTypeSubtitle;

    //Pictures
    @BindView(R.id.image_choose_picture)
    protected ImageView choosePicture;
    @BindView(R.id.myImageChoice)
    protected ImageView image1;
    @BindView(R.id.myImageChoice2)
    protected ImageView image2;
    @BindView(R.id.add_anomaly_photo_layout)
    protected RelativeLayout addAnomalyPhotoLayout;

    @BindView(R.id.add_anomaly_photo)
    protected ImageButton addPictureButton;

    @BindView(R.id.myImageChoiceLayout)
    protected RelativeLayout myImageChoiceLayout;
    @BindView(R.id.myImageChoiceClose)
    protected ImageView myImageChoiceClose;

    @BindView(R.id.myImageChoice2Layout)
    protected RelativeLayout myImageChoice2Layout;
    @BindView(R.id.myImageChoice2Close)
    protected ImageView myImageChoice2Close;

    //Description
    @BindView(R.id.text_description_subtitle)
    protected AppCompatTextView textDescription;
    @BindView(R.id.description_choose_type)
    protected ImageView chooseDescription;

    //Commentaire Agent
    @BindView(R.id.commentagent_layout_parent)
    protected LinearLayout layoutCommentAgent;
    @BindView(R.id.text_commentagent_subtitle)
    protected AppCompatTextView textCommentAgent;
    @BindView(R.id.commentagent_choose_type)
    protected ImageView chooseCommentAgent;

    //Priority
    @BindView(R.id.text_default)
    protected TextView defaultPriorityLabel;
    @BindView(R.id.text_priority_subtitle)
    protected TextView choosePrioritySubtitle;

    //Submit
    @BindView(R.id.button_publish)
    protected Button publishButton;

    @BindView(R.id.bottom_sheet_my_adress_add_anomaly)
    protected TextView bottom_sheet_my_adress_add_anomaly;

    private boolean byUser;
    private boolean isValidAddress;
    private boolean isValidAddressWithNumber;

    boolean firstPicLayout;

    String savechooseTypeSubtitle;
    String saveCategoryType;

    String txtDescription;
    String txtDescriptionLimit;

    String txtCommentaireAgent;
    String txtCommentaireAgentLimit;


    int priorityId;

    ImageButton pencilAddress;

    @Override
    protected void resolveDaggerDependency() {
        DaggerPresenterComponent.builder()
                .applicationComponent(getApplicationComponent())
                .presenterModule(new PresenterModule(this))
                .build()
                .inject(this);
    }


    @Override
    protected int getContentView() {
        return R.layout.add_anomaly_activity_layout;
    }

    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        presenter.isAgentConnected();

        //check if we come from a draft
        if (TextUtils.isEmpty(intent.getStringExtra(Constants.EXTRA_DRAFT))) {
            // Retrieve my last position
            if (getIntent().getExtras() != null) {
                myLastPosition = getIntent().getExtras().getParcelable(Constants.EXTRA_CURRENT_LOCATION);
            }
            if (myLastPosition != null) {

                if (NetworkUtils.isConnected(getApplicationContext())) {

                    findAdresseWithLatLng(myLastPosition, getIntent().getStringExtra(Constants.EXTRA_SEARCH_BAR_ADDRESS));

                } else {
                    setUpCurrentAdress("", myLastPosition);
                }
            }

            presenter.setPriority(PRIORITIES_IDS.get(R.id.priority_low));

        } else {
            final String jsonDraft = intent.getStringExtra(Constants.EXTRA_DRAFT);
            final Incident draft = new GsonBuilder().create().fromJson(jsonDraft, Incident.class);
            presenter.openDraft(draft.getId());
            if (!TextUtils.isEmpty(draft.getDescriptive())) {
                onDescriptionResult(RESULT_OK, new Intent().putExtra(Constants.EXTRA_DESCRIPTION, draft.getDescriptive()));
            }

            if (!TextUtils.isEmpty(draft.getCommentaireAgent())) {
                onCommentaireAgentResult(RESULT_OK, new Intent().putExtra(Constants.EXTRA_COMMENTAIRE_AGENT, draft.getCommentaireAgent()));
            }

            onPriorityResult(RESULT_OK, new Intent().putExtra(Constants.EXTRA_PRIORITY_ID, PRIORITIES_IDS.keyAt(PRIORITIES_IDS.indexOfValue(draft.getPriorityId()))));

            if (draft.getLat() != null && draft.getLng() != null) {
                setUpCurrentAdress(draft.getAddress(), new LatLng(Double.valueOf(draft.getLat()), Double.valueOf(draft.getLng())));
            }

            if (!TextUtils.isEmpty(draft.getCategoryId())) {
                onTypeResult(RESULT_OK, new Intent().putExtra(Constants.EXTRA_CATEGORY_ID, draft.getCategoryId()).putExtra(Constants.EXTRA_CATEGORY_NAME, draft.getAlias()));
            }
            if (draft.getPicture1() != null) {
                showPicture(draft.getPicture1());
            }

            if (draft.getPicture2() != null) {
                showPicture(draft.getPicture2());
            }

            isValidAddressWithNumber = draft.isValidAddressWithNumber();

        }


        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        if (null != toolbar) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.anomaly_screen_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        }
        // TODO refactor Clic on pen to search address
        pencilAddress = (ImageButton) findViewById(R.id.pen_add_anomaly);
        pencilAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findPlace(v);

            }
        });
        enableCreateIncident();
    }

    /**
     * Update adress on bottom sheet.
     *
     * @param adress   adress
     * @param location Location latitude longitude.
     */
    private void setUpCurrentAdress(String adress, LatLng location) {
        // setup fragment and hide or not bottom sheet
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        MapAnomalyFragment mapAnomalyFragment = MapAnomalyFragment.newInstance(location);
        fragmentTransaction.replace(R.id.framelayout_add_anomaly, mapAnomalyFragment);
        fragmentTransaction.commitNowAllowingStateLoss();

        if (!adress.equals("")) {
            bottom_sheet_my_adress_add_anomaly.setText(MiscTools.reformatArrondissement(adress));
            isValidAddress = true;
        } else {
            bottom_sheet_my_adress_add_anomaly.setText(location.latitude + ", " + location.longitude);
            isValidAddress = false;
        }

        // Format address
        Log.i(TAG, "Formatage adresse selon pattern BO : " + MiscTools.reformatArrondissement(adress));
        presenter.setAdress(MiscTools.reformatArrondissement(adress), location);

    }

    @OnClick(R.id.add_anomaly_photo)
    public void takePhotoOrViewGallery() {
        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            storagePermission = Manifest.permission.READ_MEDIA_IMAGES;
        }
        if (ContextCompat.checkSelfPermission(this, storagePermission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{storagePermission}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        } else {
            selectImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectImage();
                }
            }
        }
    }

    @OnClick(R.id.layout_choose_type)
    public void chooseType() {
        startActivityForResult(new Intent(AddAnomalyActivity.this, CategoryActivity.class), CHOOSE_TYPE_REQUEST_CODE);
    }

    @OnClick({R.id.layout_choose_priority, R.id.priority_layout_parent})
    public void choosePriority() {
        startActivityForResult(new Intent(AddAnomalyActivity.this, ChoosePriorityActivity.class), CHOOSE_PRIORITY_REQUEST_CODE);
    }

    @OnClick({R.id.layout_description, R.id.description_layout_parent})
    public void setDescription() {
        final Intent intent = new Intent(AddAnomalyActivity.this, SetDescriptionActivity.class);
        if (presenter.getRequest().getIncident().getDescriptive() != null) {
            intent.putExtra(Constants.EXTRA_DESCRIPTION, presenter.getRequest().getIncident().getDescriptive());
        }
        startActivityForResult(intent, SET_DESCRIPTION_REQUEST_CODE);
    }

    @OnClick({R.id.layout_commentagent, R.id.commentagent_layout_parent})
    public void setCommentAgent() {
        final Intent intent = new Intent(AddAnomalyActivity.this, SetDescriptionActivity.class);
        if (presenter.getRequest().getIncident().getCommentaireAgent() != null) {
            intent.putExtra(Constants.EXTRA_COMMENTAIRE_AGENT, presenter.getRequest().getIncident().getCommentaireAgent());
        }
        startActivityForResult(intent, SET_COMMENTAIRE_AGENT_REQUEST_CODE);
    }

    /**
     * Search place address
     *
     * @param view activity view
     */
    public void findPlace(View view) {

        if (!Places.isInitialized()) {
            try {
                Bundle metaData = getPackageManager().getApplicationInfo(getApplicationContext().getPackageName(), PackageManager.GET_META_DATA).metaData;
                Places.initialize(getApplicationContext(), metaData.getString("com.google.android.geo.API_KEY"));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG))
                .setCountry("Fr")
                .setTypeFilter(TypeFilter.ADDRESS)
                .setLocationRestriction(RectangularBounds.newInstance(
                        new LatLng(48.896794, 2.308851),
                        new LatLng(48.986503, 2.413853)))
                .build(this);

        startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
    }

    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = Autocomplete.getPlaceFromIntent(data);

                    Log.i(TAG, "onActivityResult: " + place.getAddress().toString());

                    boolean containsCity = false;
                    List<String> listCity = Arrays.asList(getString(R.string.city_name).toUpperCase().split(","));
                    for (String city : listCity) {
                        if (place.getAddress().toString().toUpperCase().contains(city)) {
                            containsCity = true;
                            break;
                        }
                    }

                    if (!containsCity) {
                        new AlertDialog.Builder(AddAnomalyActivity.this)
                                .setMessage(R.string.not_in_paris)
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                        pencilAddress.performClick();
                                    }
                                }).show();


                    } else {
                        myLastPosition = place.getLatLng();
                        if (NetworkUtils.isConnected(getApplicationContext())) {
                            findAdresseWithLatLng(place.getLatLng(), place.getAddress().toString());
                        } else {
                            setUpCurrentAdress(place.getAddress().toString(), place.getLatLng());
                        }
                    }

                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(data);
                    // TODO: Handle the error.
                    Log.i(TAG, status.getStatusMessage());
                }
                break;
            case CHOOSE_TYPE_REQUEST_CODE:
                onTypeResult(resultCode, data);
                break;
            case SET_DESCRIPTION_REQUEST_CODE:
                onDescriptionResult(resultCode, data);
                break;
            case SET_COMMENTAIRE_AGENT_REQUEST_CODE:
                onCommentaireAgentResult(resultCode, data);
                break;
            case CHOOSE_PRIORITY_REQUEST_CODE:
                onPriorityResult(resultCode, data);
                break;
            case TAKE_PICTURE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    rotateResizeAndCompress();
                    showPicture(mCurrentPhotoPath);
                }
                break;
            case CHOOSE_FROM_GALLERY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    onSelectFromGalleryResult(data);
                }
                break;
            case LOGIN_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    pleaseBePatientDialog();
                    presenter.createIncident();
                }
                break;
            default:
                Log.d(TAG, "onActivityResult : " + requestCode);
                break;
        }
    }


    private void onTypeResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data.hasExtra(Constants.EXTRA_MESSAGE_HORS_DMR)) {
                Intent intentAddAnomaly = new Intent(AddAnomalyActivity.this, SimpleMessageActivity.class);
                intentAddAnomaly.putExtra(Constants.EXTRA_MESSAGE_HORS_DMR, data.getStringExtra(Constants.EXTRA_MESSAGE_HORS_DMR));
                startActivity(intentAddAnomaly);
            } else {
                typeTreatment(data.getStringExtra(Constants.EXTRA_CATEGORY_NAME), data.getStringExtra(Constants.EXTRA_CATEGORY_ID));
            }
        }
        enableCreateIncident();
    }

    private void typeTreatment(String typeSubtitle, String categoryType) {
        presenter.setCategory(categoryType);
        if (presenter.getRequest().getIncident().getCategoryId() != null) {
            chooseTypeSubtitle.setText(typeSubtitle);
            chooseType.setImageResource(R.drawable.ic_check_circle_pink_24px);
        }
    }

    /**
     * Callback after SET_DESCRIPTION_REQUEST_CODE
     *
     * @param resultCode result
     * @param data       intent containig the description
     */
    private void onDescriptionResult(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            txtDescription = data.getStringExtra(Constants.EXTRA_DESCRIPTION);
            txtDescriptionLimit = txtDescription;
            if (txtDescription.length() > 40) {
                txtDescriptionLimit = txtDescription.substring(0, 40) + "...";
            }
            textDescription.setText(txtDescriptionLimit);
            if (txtDescription.length() > 0) {
                chooseDescription.setImageResource(R.drawable.ic_check_circle_pink_24px);
            } else {
                chooseDescription.setImageResource(R.drawable.ic_check_circle_grey_24px);
            }
            presenter.setDescription(data.getStringExtra(Constants.EXTRA_DESCRIPTION));
        }
    }

    /**
     * Callback after SET_COMMENTAIRE_AGENT_REQUEST_CODE
     *
     * @param resultCode result
     * @param data       intent containig the description
     */
    private void onCommentaireAgentResult(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            txtCommentaireAgent = data.getStringExtra(Constants.EXTRA_COMMENTAIRE_AGENT);
            txtCommentaireAgentLimit = txtCommentaireAgent;
            if (txtCommentaireAgent.length() > 40) {
                txtCommentaireAgentLimit = txtCommentaireAgent.substring(0, 40) + "...";
            }
            textCommentAgent.setText(txtCommentaireAgentLimit);
            if (txtCommentaireAgent.length() > 0) {
                chooseCommentAgent.setImageResource(R.drawable.ic_check_circle_pink_24px);
            } else {
                chooseCommentAgent.setImageResource(R.drawable.ic_check_circle_grey_24px);
            }
            presenter.setCommentaireAgent(data.getStringExtra(Constants.EXTRA_COMMENTAIRE_AGENT));
        }
    }

    /**
     * Callback after CHOOSE_PRIORITY_REQUEST_CODE
     *
     * @param resultCode result
     * @param data       intent containig the choosen priority
     */
    private void onPriorityResult(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            priorityId = data.getIntExtra(Constants.EXTRA_PRIORITY_ID, DEFAULT_PRIORITY_ID);
            priorityTreatment(priorityId);
        }
    }

    private void priorityTreatment(int thePriority) {
        presenter.setPriority(PRIORITIES_IDS.get(thePriority));
        choosePrioritySubtitle.setText(getString(PRIORITIES_LIBELLE.get(thePriority)));
        if (thePriority == DEFAULT_PRIORITY_ID) {
            defaultPriorityLabel.setVisibility(View.VISIBLE);
        } else {
            defaultPriorityLabel.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Callback after TAKE_PICTURE_REQUEST_CODE
     *
     * @param data intent containig the picture taken
     */
    private void onCaptureImageResult(final Intent data) {
        final Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        addPictureToPlaceHolder(thumbnail);
    }

    /**
     * Callback after CHOOSE_FROM_GALLERY_REQUEST_CODE
     *
     * @param data intent containig the picture choosen
     */
    private void onSelectFromGalleryResult(final Intent data) {
        if (data != null) {
            try {
                Bitmap thumbnail = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                addPictureToPlaceHolder(thumbnail);
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().log(e.getMessage());
                Log.e(TAG, e.getMessage(), e);
            }
        }

    }

    /**
     * Place a thumbnail on the placeholder after compressing the Bitmap.
     *
     * @param thumbnail thumbnail
     */
    private void addPictureToPlaceHolder(Bitmap thumbnail) {
        presenter.savePictureInFile(thumbnail);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {

        // We prefer saving all datas because photo intent may kill app before going back

        if (null != mCurrentPhotoPath)
            outState.putString(Constants.ADD_ANO_CURRENT_PHOTO_PATH, mCurrentPhotoPath);

        if (null != presenter.getPicture1State())
            outState.putString(Constants.ADD_ANO_PICTURE_1, presenter.getPicture1State());
        if (null != presenter.getPicture2State())
            outState.putString(Constants.ADD_ANO_PICTURE_2, presenter.getPicture2State());

        if (null != savechooseTypeSubtitle)
            outState.putString(Constants.ADD_ANO_TYPE_SUBTITLE, savechooseTypeSubtitle);
        if (null != saveCategoryType)
            outState.putString(Constants.ADD_ANO_TYPE_CATEGORY, saveCategoryType);

        if (null != textDescription)
            outState.putString(Constants.ADD_ANO_TXT_DESCRIPTION, txtDescription);
        if (null != textCommentAgent)
            outState.putString(Constants.ADD_ANO_TXT_COMMENTAIRE_AGENT, txtCommentaireAgent);
        if (null != txtDescriptionLimit)
            outState.putString(Constants.ADD_ANO_TXT_DESCRIPTION_SHORT, txtDescriptionLimit);
        if (null != txtCommentaireAgentLimit)
            outState.putString(Constants.ADD_ANO_TXT_COMMENTAIRE_AGENT_SHORT, txtCommentaireAgentLimit);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {

        if (null != state.getString(Constants.ADD_ANO_CURRENT_PHOTO_PATH))
            mCurrentPhotoPath = state.getString(Constants.ADD_ANO_CURRENT_PHOTO_PATH);
        if (null != state.getString(Constants.ADD_ANO_PICTURE_1))
            showPicture(state.getString(Constants.ADD_ANO_PICTURE_1));
        if (null != state.getString(Constants.ADD_ANO_PICTURE_2))
            showPicture(state.getString(Constants.ADD_ANO_PICTURE_2));

        if (null != state.getString(Constants.ADD_ANO_TYPE_SUBTITLE) && null != state.getString(Constants.ADD_ANO_TYPE_CATEGORY)) {
            typeTreatment(state.getString(Constants.ADD_ANO_TYPE_SUBTITLE), state.getString(Constants.ADD_ANO_TYPE_CATEGORY));
        }

        if (null != state.getString(Constants.ADD_ANO_TXT_DESCRIPTION) && null != state.getString(Constants.ADD_ANO_TXT_DESCRIPTION_SHORT)) {
            textDescription.setText(state.getString(Constants.ADD_ANO_TXT_DESCRIPTION_SHORT));
            presenter.setDescription(state.getString(Constants.ADD_ANO_TXT_DESCRIPTION));
            if (state.getString(Constants.ADD_ANO_TXT_DESCRIPTION).length() > 0) {
                chooseDescription.setImageResource(R.drawable.ic_check_circle_pink_24px);
            } else {
                chooseDescription.setImageResource(R.drawable.ic_check_circle_grey_24px);
            }
        }

        if (null != state.getString(Constants.ADD_ANO_TXT_COMMENTAIRE_AGENT) && null != state.getString(Constants.ADD_ANO_TXT_COMMENTAIRE_AGENT_SHORT)) {
            textCommentAgent.setText(state.getString(Constants.ADD_ANO_TXT_COMMENTAIRE_AGENT_SHORT));
            presenter.setCommentaireAgent(state.getString(Constants.ADD_ANO_TXT_COMMENTAIRE_AGENT));
            if (state.getString(Constants.ADD_ANO_TXT_COMMENTAIRE_AGENT).length() > 0) {
                chooseCommentAgent.setImageResource(R.drawable.ic_check_circle_pink_24px);
            } else {
                chooseCommentAgent.setImageResource(R.drawable.ic_check_circle_grey_24px);
            }
        }

        enableCreateIncident();

        super.onRestoreInstanceState(state);
    }


    public void showPicture(final String fileName) {
        presenter.addPictureToModel(fileName);

        firstPicLayout = myImageChoiceLayout.getVisibility() == View.GONE;

        //image1 placeholder is empty
        if (firstPicLayout) {
            //first picture set
            choosePicture.setImageResource(R.drawable.ic_check_circle_pink_24px);
            myImageChoiceLayout.setVisibility(View.VISIBLE);
            image1.setVisibility(View.VISIBLE);
            Glide.with(this).load(fileName).fitCenter().into(image1);
            myImageChoiceClose.setVisibility(View.VISIBLE);
            if (myImageChoice2Layout.getVisibility() == View.VISIBLE) {
                addAnomalyPhotoLayout.setVisibility(View.GONE);
                addPictureButton.setVisibility(View.GONE);
            }
        } else {
            //image2 placeholder is empty then
            myImageChoice2Layout.setVisibility(View.VISIBLE);
            image2.setVisibility(View.VISIBLE);
            Glide.with(this).load(fileName).fitCenter().into(image2);
            myImageChoice2Close.setVisibility(View.VISIBLE);
            //both pictures taken, we can remove the add picture button
            addAnomalyPhotoLayout.setVisibility(View.INVISIBLE);
            addPictureButton.setVisibility(View.INVISIBLE);
        }
        enableCreateIncident();
    }


    @OnClick(R.id.myImageChoiceClose)
    public void disablePic1() {
        presenter.removePicture1();
        myImageChoiceLayout.setVisibility(View.GONE);
        image1.setVisibility(View.GONE);
        myImageChoiceClose.setVisibility(View.GONE);
        addAnomalyPhotoLayout.setVisibility(View.VISIBLE);
        addPictureButton.setVisibility(View.VISIBLE);
        testPicturesAndGreyCheck();
        enableCreateIncident();
    }

    @OnClick(R.id.myImageChoice2Close)
    public void disablePic2() {
        presenter.removePicture2();
        myImageChoice2Layout.setVisibility(View.GONE);
        image2.setVisibility(View.GONE);
        myImageChoice2Close.setVisibility(View.GONE);
        addAnomalyPhotoLayout.setVisibility(View.VISIBLE);
        addPictureButton.setVisibility(View.VISIBLE);
        testPicturesAndGreyCheck();
        enableCreateIncident();
    }

    public void testPicturesAndGreyCheck() {
        if (image1.getVisibility() == View.GONE && image2.getVisibility() == View.GONE) {
            choosePicture.setImageResource(R.drawable.ic_check_circle_grey_24px);
        }
    }


    /**
     * Check if the publish button should be enable by validating the form
     */
    private void enableCreateIncident() {
        if (checkMandatoryFields()) {
            publishButton.setClickable(true);
            publishButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.framboise));
        } else {
            publishButton.setClickable(false);
            publishButton.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_icon));
        }
    }


    /**
     * Find Postal Adresse with latitude longitude.
     *
     * @param latlng           Latitude, Longitude
     * @param searchBarAddress Address enter in search address
     */
    private void findAdresseWithLatLng(LatLng latlng, String searchBarAddress) {

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String currentAddress = "";
        try {
            boolean searchBarMode = searchBarAddress != null && !"".equals(searchBarAddress);
            final List<Address> addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 4); // Here 4 represent max location result to returned, by documents it recommended 1 to 5
            if (addresses != null && !addresses.isEmpty()) {
                final Address addressSelect = MiscTools.selectAddress(addresses, getString(R.string.city_name), searchBarMode, searchBarAddress);

                final String address = addressSelect.getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                Log.i(TAG, "adress " + address);
                final String city = addressSelect.getLocality();
                Log.i(TAG, "city " + city);
                final String country = addressSelect.getCountryName();
                Log.i(TAG, "country " + country);


                final String postalCode = addressSelect.getPostalCode();
                Log.i(TAG, "cp " + postalCode);

                currentAddress = address;

                if (searchBarMode) {
                    if (searchBarAddress.split(",").length > 3) {
                        //fix commercial address
                        searchBarAddress = searchBarAddress.replace(searchBarAddress.substring(0, searchBarAddress.indexOf(",") + 1), "");
                    }
                    currentAddress = currentAddress.replace(currentAddress.substring(0, currentAddress.indexOf(",")), searchBarAddress);
                }
                Log.i(TAG, "current " + currentAddress);

                isValidAddressWithNumber = true;
                if (warningAddress(currentAddress, address)) {
                    Log.i(TAG, "address not started with number");
                    isValidAddressWithNumber = false;
                    presenter.getRequest().getIncident().setValidAddressWithNumber(false);
                }
            }
        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
            Log.e(TAG, e.getMessage(), e);
        }

        setUpCurrentAdress(currentAddress, latlng);
    }

    /**
     * Determine if incomplet address pop up must be display.
     *
     * @return true if display
     */
    private boolean warningAddress(String addressToCheck, String referenceAddress) {
        boolean isBridgeAddress = KEYWORD_ADDRESS_BRIDGE.equals(addressToCheck.split(" ")[0].toLowerCase()) || KEYWORD_ADDRESS_BRIDGE.equals(referenceAddress.split(" ")[0].toLowerCase());
        if (!isBridgeAddress) {
            boolean addressHasANumber = !Character.isDigit(addressToCheck.trim().charAt(0)) && Character.isDigit(referenceAddress.trim().charAt(0));
            return addressHasANumber;
        } else {
            return false;
        }


    }

    private Address findLatLngWithAddress(String addressText) {

        if (NetworkUtils.isConnected(getApplicationContext())) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(addressText, 1);
                return addresses.get(0);
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().log(e.getMessage());
                Log.e(TAG, e.getMessage(), e);
                return null;
            }
        }

        return null;
    }

    /**
     * @return true if all required fields a<br>
     * false otherwise
     */
    private boolean checkMandatoryFields() {
        return presenter.isIncidentValid();
    }


    /**
     * Click action on the publish button
     */
    @OnClick(R.id.button_publish)
    public void onPublish() {

        if (!isValidAddressWithNumber) {
            showDialogError();
        } else {
            if (!isValidAddress && null != myLastPosition) {
                if (NetworkUtils.isConnected(getApplicationContext())) {
                    findAdresseWithLatLng(myLastPosition, null);
                    isValidAddress = true;
                }
            }
            presenter.onPublish();
        }

    }


    /**
     * Open the greeting dialog.
     * The incident is created after the user closes this dialog.
     *
     * @param askEmail true to let the user set an email or connect to his VDP account <br>
     *                 false to just show the greetings
     */
    @Override
    public void showGreetingsDialog(boolean askEmail) {

        //user not connected, we ask him to input an email or to connect to his VDP account
        if (askEmail) {

            // custom dialog
            greetingsDialog = new Dialog(this);
            greetingsDialog.setContentView(R.layout.greetings_dialog_keep_contact);
            greetingsDialog.setCancelable(false);

            // set the custom dialog components - text, image and button
            ImageView closeSave = (ImageView) greetingsDialog.findViewById(R.id.btnCloseSave);
            // Close Button
            closeSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pleaseBePatientDialog();
                    presenter.createIncident();
                }
            });


            TextView cnx2mail = (TextView) greetingsDialog.findViewById(R.id.cnx2mail);

            if (greetingsDialog.getWindow() != null) {
                greetingsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // Fix Dialog Size
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(greetingsDialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                greetingsDialog.getWindow().setAttributes(lp);
                greetingsDialog.show();
            }

            cnx2mail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    recordEmail();
                }
            });

        } else {

            pleaseBePatientDialog();
            presenter.createIncident();
        }


    }

    /**
     * Show dialog error when user click Publish
     * with incomplete address.
     */
    private void showDialogError() {

        Dialog dialog = new Dialog(this);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_incomplete_address, null, false);

        final EditText input = (EditText) viewInflated.findViewById(R.id.input_street_number);
        final Spinner complementAddress = (Spinner) viewInflated.findViewById(R.id.spinner_address_complement);

        Button buttonPublish = (Button) viewInflated.findViewById(R.id.button_publish);
        buttonPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (input.getText().toString().length() > 0) {
                    try {
                        if (Integer.parseInt(input.getText().toString()) > 0) {
                            String newAddress = "";
                            if (complementAddress.getSelectedItem().toString().length() > 0) {
                                newAddress = input.getText().toString() + complementAddress.getSelectedItem().toString().charAt(0) + " " + presenter.getRequest().getIncident().getAddress();
                            } else {
                                newAddress = input.getText().toString() + " " + presenter.getRequest().getIncident().getAddress();
                            }

                            presenter.getRequest().getIncident().setAddress(newAddress.trim());

                            //find new latitude longitude code postal for newAddress
                            Address address = findLatLngWithAddress(newAddress);
                            if (address != null) {
                                presenter.getRequest().getIncident().setLat(String.valueOf(address.getLatitude()));
                                presenter.getRequest().getIncident().setLng(String.valueOf(address.getLongitude()));
                                presenter.getRequest().getPosition().setLatitude(address.getLatitude());
                                presenter.getRequest().getPosition().setLongitude(address.getLongitude());
                                presenter.getRequest().getIncident().setAddress(newAddress.replaceFirst("75[0-9][0-9][0-9]", address.getPostalCode()).trim());
                            }

                            isValidAddressWithNumber = true;
                            onPublish();
                            dialog.dismiss();
                        }
                    } catch (NumberFormatException e) {
                        input.requestFocus();
                    }
                } else {
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
                dialog.dismiss();
            }
        });

        dialog.setContentView(viewInflated);
        dialog.setCancelable(true);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Fix Dialog Size
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            dialog.getWindow().setAttributes(lp);
            dialog.show();
        }
    }

    /**
     * Patient dialog after publish incident.
     */
    private void pleaseBePatientDialog() {

        if (null != greetingsDialog) greetingsDialog.dismiss();

        greetingsDialogSendIncident = new Dialog(this);
        greetingsDialogSendIncident.setContentView(R.layout.greetings_dialog_send_incident);
        greetingsDialogSendIncident.setCancelable(false);

        if (greetingsDialogSendIncident.getWindow() != null) {
            greetingsDialogSendIncident.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Fix Dialog Size
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(greetingsDialogSendIncident.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            greetingsDialogSendIncident.getWindow().setAttributes(lp);
            greetingsDialogSendIncident.show();
        }

    }

    @Override
    public void displayDialogDmrOffline() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder
                .setTitle(R.string.information)
                .setMessage(getString(R.string.dmr_offline_publish))
                .setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        presenter.saveDraft();
                        dialog.dismiss();
                        navigateBack();
                    }
                });

        builder.create().show();
    }


    @Override
    public void showDialogErrorSaveDraft() {
        new AlertDialog.Builder(this).setMessage(R.string.save_anomaly_failure)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.saveDraft();
                        dialog.dismiss();
                        navigateBack();
                    }
                }).show();
    }


    @Override
    public void showIncidentAndPhotosOkThankYou(boolean smile) {

        greetingsDialogSendIncident.dismiss();

        if (smile) {
            Log.i(TAG, "showIncidentAndPhotosOkThankYou: OK");

            greetingsDialogSendOkThanks = new Dialog(this);
            greetingsDialogSendOkThanks.setContentView(R.layout.greetings_dialog_send_ok_thanks);
            greetingsDialogSendOkThanks.setCancelable(false);

            // set the custom dialog components - text, image and button
            ImageView close = (ImageView) greetingsDialogSendOkThanks.findViewById(R.id.btnCloseSuccess);
            // Close Button
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    greetingsDialogSendOkThanks.dismiss();
                    finish();
                }
            });

            if (greetingsDialogSendOkThanks.getWindow() != null) {
                greetingsDialogSendOkThanks.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // Fix Dialog Size
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(greetingsDialogSendIncident.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                greetingsDialogSendOkThanks.getWindow().setAttributes(lp);
                greetingsDialogSendOkThanks.show();
            }


        } else {
            Log.i(TAG, "showIncidentAndPhotosOkThankYou: KO");

            greetingsDialogSendError = new Dialog(this);
            greetingsDialogSendError.setContentView(R.layout.greetings_dialog_send_error);
            greetingsDialogSendError.setCancelable(false);

            // set the custom dialog components - text, image and button
            ImageView close = (ImageView) greetingsDialogSendError.findViewById(R.id.btnCloseFailure);
            // Close Button
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    greetingsDialogSendError.dismiss();
                }
            });

            if (greetingsDialogSendError.getWindow() != null) {
                greetingsDialogSendError.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                // Fix Dialog Size
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(greetingsDialogSendError.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

                greetingsDialogSendError.getWindow().setAttributes(lp);
                greetingsDialogSendError.show();
            }

        }
    }

    /**
     * Starts the LoginActivity to connect to a VDP account
     */
    private void loginParisianAccount() {
        startActivityForResult(new Intent(AddAnomalyActivity.this, LoginActivity.class), LOGIN_REQUEST_CODE);
    }

    /**
     * Display another dialog to input an email adress
     */
    private void recordEmail() {
        emailDialog = new Dialog(this);
        emailDialog.setContentView(R.layout.email_record_dialog);

        final TextInputEditText inputEmail = (TextInputEditText) emailDialog.findViewById(R.id.input_email);
        final String lastUsedEmail = presenter.getLastUsedEmail();
        if (!TextUtils.isEmpty(lastUsedEmail)) {
            inputEmail.append(lastUsedEmail);
        }

        if (emailDialog.getWindow() != null) {
            emailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Fix Dialog Size
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(emailDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            emailDialog.show();
            emailDialog.getWindow().setAttributes(lp);
        }


        TextView saveEmailPopupCancelBtn = (TextView) emailDialog.findViewById(R.id.save_email_popup_cancel_btn);
        TextView saveEmailPopupConfirmBtn = (TextView) emailDialog.findViewById(R.id.save_email_popup_confirm_btn);


        saveEmailPopupCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emailDialog.dismiss();
            }
        });

        saveEmailPopupConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String txtEmail = inputEmail.getText().toString();
                if (presenter.setEmail(txtEmail)) {
                    emailDialog.dismiss();
                    pleaseBePatientDialog();
                    presenter.createIncident();
                } else {
                    inputEmail.setError(getString(R.string.invalid_email));
                }

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        byUser = true;
        presenter.onQuit(true);
        return true;
    }

    // Avoid user to quit the app
    @Override
    public void onBackPressed() {
        byUser = true;
        presenter.onQuit(true);
    }

    /**
     * Callback after an incident is successfully created.
     * Upload the picture and go back to the main screen
     *
     * @param incidentId id of the incident newly created
     */
    @Override
    public void onIncidentCreated(Integer incidentId) {
        presenter.uploadPictures(incidentId);
    }


    /**
     * Callback after a creation failure
     *
     * @param errorMessage error message received by the WS
     */
    @Override
    public void onIncidentNotCreated(String errorMessage) {
        Toast.makeText(this, "Une erreur technique est survenue pendant la création du signalement.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void proposeDraft() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.popup_error_title)
                .setMessage(R.string.save_draft)
                .setCancelable(true)
                .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.saveDraft();
                        dialog.cancel();
                        navigateBack();
                    }
                }).setNegativeButton(R.string.label_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        presenter.removeDraft();
                        dialog.cancel();
                        navigateBack();
                    }
                }).show();
    }

    public void showHideAgentCommentaryField(final boolean agentConnected) {
        if (agentConnected) {
            layoutCommentAgent.setVisibility(LinearLayout.VISIBLE);
        } else {
            layoutCommentAgent.setVisibility(LinearLayout.GONE);
        }
    }

    @Override
    public void navigateBack() {
        finish();
    }

    @Override
    protected void onStop() {
        if (!byUser) {
            presenter.onQuit(false);
        }

        super.onStop();
    }
}
