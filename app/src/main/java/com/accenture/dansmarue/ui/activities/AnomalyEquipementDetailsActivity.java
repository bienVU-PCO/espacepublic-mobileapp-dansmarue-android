package com.accenture.dansmarue.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.accenture.dansmarue.BuildConfig;
import com.accenture.dansmarue.R;
import com.accenture.dansmarue.app.DansMaRueApplication;
import com.accenture.dansmarue.di.components.DaggerPresenterComponent;
import com.accenture.dansmarue.di.modules.PresenterModule;
import com.accenture.dansmarue.mvp.models.Incident;
import com.accenture.dansmarue.mvp.presenters.AnomalyEquipementDetailsPresenter;
import com.accenture.dansmarue.mvp.views.AnomalyEquipementDetailsView;
import com.accenture.dansmarue.utils.BitmapScaler;
import com.accenture.dansmarue.utils.Constants;
import com.accenture.dansmarue.utils.MiscTools;
import com.bumptech.glide.Glide;
import com.google.firebase.crashlytics.FirebaseCrashlytics;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;


public class AnomalyEquipementDetailsActivity extends BaseActivity implements AnomalyEquipementDetailsView {

    private static final String TAG = AnomalyEquipementDetailsActivity.class.getCanonicalName();

    private final static int REQUEST_CODE_LOGIN = 7;
    private static final int TAKE_PICTURE_REQUEST_CODE = 1980;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int CHOOSE_FROM_GALLERY_REQUEST_CODE = 1981;
    String mCurrentPhotoPath = "";

    @Inject
    protected AnomalyEquipementDetailsPresenter anomalyEquipementDetailsPresenter;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.anomaly_details_picture)
    protected ImageView imageView;
    @BindView(R.id.anomaly_address)
    protected TextView anomalyAdress;

    @BindView(R.id.anomaly_category)
    protected TextView anomalyCategory;
    @BindView(R.id.anomaly_details)
    protected TextView anomalyDetail;
    @BindView(R.id.anomaly_nb_followers)
    protected TextView nbFollowers;
    @BindView(R.id.anomaly_nb_greetings)
    protected TextView nbGreetings;
    @BindView(R.id.anomaly_time)
    protected TextView anomalyDateTime;
    @BindView(R.id.anomaly_layout_followers)
    protected LinearLayout layoutFollowers;

    @BindView(R.id.fab_details_ano)
    protected FloatingActionButton fabDetailsAno;
    @BindView(R.id.resolve)
    protected Button resolveBtn;

    @BindView(R.id.photo_service_fait_layout)
    protected LinearLayout photoServiceFaitLayout;

    //Pictures
    @BindView(R.id.image_choose_picture)
    protected ImageView choosePicture;
    @BindView(R.id.myImageChoice)
    protected ImageView image1;

    @BindView(R.id.add_anomaly_photo)
    protected ImageButton addPictureButton;

    @BindView(R.id.myImageChoiceLayout)
    protected RelativeLayout myImageChoiceLayout;
    @BindView(R.id.myImageChoiceClose)
    protected ImageView myImageChoiceClose;
    @BindView(R.id.add_anomaly_photo_layout)
    protected RelativeLayout addAnomalyPhotoLayout;

    @BindView(R.id.navLeft)
    protected TextView leftPicNavigation;
    @BindView(R.id.navRight)
    protected TextView rightPicNavigation;

    private boolean fabDetailsAnoFollowed;
    private boolean congratulated;

    private Incident incident;
    private int indexPicture = 0;
    private int maxIndex = 0;

    private int nbFollowersDisplay = 0;
    private int nbGreetingsDisplay = 0;

    private boolean incidentComeFromNotif;

    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        Log.i(TAG, "onViewReady: detail equipement");

        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);

//        Long incidentByNotif = intent.getLongExtra(Constants.EXTRA_INCIDENT_ID, 0);
//        final Long incidentId = incidentByNotif;
//        if (incidentByNotif!=0) incidentComeFromNotif =true;
//        final String source = intent.getStringExtra(Constants.EXTRA_INCIDENT_SOURCE);


        final Long incidentId = intent.getLongExtra(Constants.EXTRA_INCIDENT_ID, 0);
        String incidentByNotif = intent.getStringExtra(Constants.EXTRA_INCIDENT_TYPE);
        if (null != incidentByNotif) incidentComeFromNotif = true;

        final String source = intent.getStringExtra(Constants.EXTRA_INCIDENT_SOURCE);

        //TODO Pay Attention
        if (incidentId == 0) {
            finish();
        }

        anomalyEquipementDetailsPresenter.loadIncident(incidentId, source);


        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        if (null != toolbar) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            getSupportActionBar().setTitle("Détail");
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (incidentComeFromNotif) {

                        // By default, after push come back OUTDOOR
                        startActivity(new Intent(AnomalyEquipementDetailsActivity.this, WelcomeMapActivity.class));
                        finish();
                    } else {
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_CANCELED, returnIntent);
                        finish();
                    }

                }
            });
        }
    }

    @Override
    protected void resolveDaggerDependency() {
        DaggerPresenterComponent.builder()
                .applicationComponent(((DansMaRueApplication) getApplication()).getApplicationComponent())
                .presenterModule(new PresenterModule(this))
                .build()
                .inject(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected int getContentView() {
        return R.layout.anomaly_details_activity_layout;
    }


    @OnClick(R.id.fab_details_ano)
    public void fabClicked() {
        //Should always be true
        if (incident != null) {

            if (incident.isResolu()) {
                congratulate();
            } else {

                if (!incident.isIncidentFollowedByUser()) {

                    anomalyEquipementDetailsPresenter.followAnomaly(String.valueOf(incident.getId()));
                } else {
                    anomalyEquipementDetailsPresenter.unfollowAnomaly(String.valueOf(incident.getId()));
                }

            }
        }

    }


    public void displayFollow() {
        incident.setIncidentFollowedByUser(true);
        fabDetailsAno.setImageResource(R.drawable.ic_followed);
        Snackbar.make(findViewById(R.id.rl_add_ano_details), R.string.follow_anomaly, Snackbar.LENGTH_LONG).show();
        nbFollowersDisplay++;
        nbFollowers.setText(nbFollowersDisplay + " ");
    }

    public void displayFollowFailure() {
        Snackbar.make(findViewById(R.id.rl_add_ano_details), R.string.follow_anomaly_failure, Snackbar.LENGTH_LONG).show();
    }

    public void displayUnfollow() {
        incident.setIncidentFollowedByUser(false);
        fabDetailsAno.setImageResource(R.drawable.ic_follow);
        Snackbar.make(findViewById(R.id.rl_add_ano_details), R.string.unfollow_anomaly, Snackbar.LENGTH_LONG).show();
        nbFollowersDisplay--;
        nbFollowers.setText(nbFollowersDisplay + " ");
    }

    public void displayUnfollowFailure() {
        Snackbar.make(findViewById(R.id.rl_add_ano_details), R.string.unfollow_anomaly_failure, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void populateFields(final Incident loadedIncident) {
        if (null == loadedIncident) {
            Toast.makeText(getApplicationContext(), "Erreur lors de la récuperation des informations du signalement", Toast.LENGTH_SHORT).show();
        } else {
            incident = loadedIncident;

            maxIndex = incident.getAllPictures().size() - 1;
            if (maxIndex == -1) {
                if (null != leftPicNavigation) leftPicNavigation.setVisibility(View.GONE);
                if (null != rightPicNavigation) rightPicNavigation.setVisibility(View.GONE);
            }

            if (indexPicture == 0) {
                leftPicNavigation.setVisibility(View.GONE);
            }

            if (indexPicture == maxIndex) {
                rightPicNavigation.setVisibility(View.GONE);
            }

            if (incident.isFromRamen()) {
                //TODO normalement on reçoit une imgae générique pour ramen
                Glide.with(this).load(incident.getPictures().getGenericPictureId()).into(imageView);
            } else if (incident.getFirstAvailablePicture() != null) {
                Glide.with(this)
                        .load(incident.getFirstAvailablePicture())
                        .fallback(R.drawable.ic_broken_image)
                        .error(R.drawable.ic_broken_image)
                        .into(imageView);
            } else {
                imageView.setImageBitmap(MiscTools.base64ToBitmap(incident.getIconIncident(), 512));
            }

            if (incident.isFromRamen()) {
                layoutFollowers.setVisibility(View.GONE);
                anomalyDetail.setText(incident.getRamenDescription());
                anomalyCategory.setText(R.string.desc_ramen);
                anomalyAdress.setTextColor(getResources().getColor(R.color.grey_icon));
            } else {
                anomalyDetail.setText(incident.getDescriptive());
                anomalyCategory.setText(incident.getAlias());
            }

            anomalyAdress.setText(incident.getAddress());
            anomalyDateTime.setText(incident.getFormatedDate());


            if (incident.isResolu()) {

                TextView txtOverPicture = (TextView) findViewById(R.id.txt_over_picture);
                txtOverPicture.setBackgroundResource(R.drawable.round_corner_green);
                txtOverPicture.setText(R.string.anomaly_resolved_toast);
                txtOverPicture.setVisibility(View.VISIBLE);

                if (incident.isFromRamen()) {
                    fabDetailsAno.setImageResource(R.drawable.ic_greetings_grey);
                    fabDetailsAno.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_grey)));
                } else {
                    fabDetailsAno.setImageResource(R.drawable.ic_greetings_white);
                    fabDetailsAno.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_green)));
                }
            } else {

                TextView txtOverPicture = (TextView) findViewById(R.id.txt_over_picture);
                txtOverPicture.setBackgroundResource(R.drawable.round_corner_orange);
                txtOverPicture.setText(R.string.anomaly_in_progress);
                txtOverPicture.setVisibility(View.VISIBLE);

                if (incident.isFromRamen()) {
                    fabDetailsAno.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.grey_icon)));
                } else if (incident.isIncidentFollowedByUser()) {
                    fabDetailsAno.setImageResource(R.drawable.ic_followed);
                    fabDetailsAnoFollowed = true;
                } else {
                    fabDetailsAno.setImageResource(R.drawable.ic_follow);
                    fabDetailsAnoFollowed = false;
                }

                fabDetailsAno.setClickable(!incident.isFromRamen());
//                if (!incident.isFromRamen() && anomalyEquipementDetailsPresenter.isResolvable(incident.getReporterGuid())) {
                if (!incident.isFromRamen() && incident.isResolvable()) {
                    photoServiceFaitLayout.setVisibility(View.VISIBLE);
                    resolveBtn.setVisibility(View.VISIBLE);
                } else {
                    resolveBtn.setVisibility(View.GONE);
                    photoServiceFaitLayout.setVisibility(View.GONE);
                }

            }
        }

        nbFollowersDisplay = incident.getFollowers();
        nbFollowers.setText(nbFollowersDisplay + " ");

        nbGreetingsDisplay = incident.getCongratulations();
        nbGreetings.setText(nbGreetingsDisplay + " ");

    }


    private void congratulate() {
        if (!congratulated) {



            fabDetailsAno.setImageResource(R.drawable.ic_greetings_grey);
            fabDetailsAno.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_grey)));

            Snackbar.make(findViewById(R.id.anomaly_line2), R.string.greetings_ok, Snackbar.LENGTH_LONG)
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            switch (event) {
                                case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                    anomalyEquipementDetailsPresenter.congratulateAnomalie(String.valueOf(incident.getId()));
                                    congratulated = true;
                                    break;
                            }
                        }
                    })
                    .setAction(R.string.greetings_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fabDetailsAno.setImageResource(R.drawable.ic_greetings_white);
                            fabDetailsAno.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_green)));
                        }
                    })
                    .show();
        }
    }

    @OnClick(R.id.resolve)
    public void resolve() {
        Snackbar.make(findViewById(R.id.anomaly_line2), R.string.resolve_ok, Snackbar.LENGTH_LONG)
                .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        switch (event) {
                            case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                anomalyEquipementDetailsPresenter.resolveIncident(String.valueOf(incident.getId()));

                                if (image1.getVisibility() == View.VISIBLE)
                                    anomalyEquipementDetailsPresenter.uploadPictureServiceFait(String.valueOf(incident.getId()));


                                break;
                        }
                    }
                })
                .setAction(R.string.resolve_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                })
                .show();
    }

    @Override
    public void displayGreetingsOk() {
        Log.d(TAG, "displayGreetingsOk: ");
        nbGreetingsDisplay++;
        nbGreetings.setText(nbGreetingsDisplay + " ");
        fabDetailsAno.setVisibility(View.INVISIBLE);
    }

    @Override
    public void displayGreetingsKo() {
        Snackbar.make(findViewById(R.id.navigation), R.string.dmr_error, Snackbar.LENGTH_LONG).show();
        congratulated = false;
        fabDetailsAno.setImageResource(R.drawable.ic_greetings_green);
        fabDetailsAno.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
    }

    @Override
    public void inviteToLogin() {
        startActivityForResult(new Intent(AnomalyEquipementDetailsActivity.this, LoginActivity.class), REQUEST_CODE_LOGIN);
    }

    @Override
    public void displayResolveKo() {
        //TODO quelle message afficher
        Toast.makeText(this, "Erreur lors de la prise en copmpte de la déclaration", Toast.LENGTH_LONG).show();
    }

    @Override
    public void displayResolveOk() {
        final Toast toastMessage = Toast.makeText(this, "Cette anomalie a été clôturée", Toast.LENGTH_LONG);
        toastMessage.setGravity(Gravity.TOP, 5, 5);
        toastMessage.show();
        incident.setState(Incident.STATE_RESOLVED);
        populateFields(incident);
        photoServiceFaitLayout.setVisibility(View.INVISIBLE);
        resolveBtn.setVisibility(View.INVISIBLE);
    }

    @OnClick(R.id.navLeft)
    public void rollPictureLeft() {
        if (indexPicture > 0) {
            indexPicture--;
            Glide.with(this).load(incident.getPicture(indexPicture)).fallback(incident.getPictures().getGenericPictureId()).error(incident.getPictures().getGenericPictureId()).into(imageView);
        }

        if (indexPicture == 0) {
            leftPicNavigation.setVisibility(View.GONE);
        }
        rightPicNavigation.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.navRight)
    public void rollPictureRight() {
        if (indexPicture < maxIndex) {
            indexPicture++;
            Glide.with(this).load(incident.getPicture(indexPicture)).fallback(incident.getPictures().getGenericPictureId()).error(incident.getPictures().getGenericPictureId()).into(imageView);
        }

        if (indexPicture == maxIndex) {
            rightPicNavigation.setVisibility(View.GONE);
        }
        leftPicNavigation.setVisibility(View.VISIBLE);
    }

    @Override
    public void closeActvity() {
        finish();
        startActivity(new Intent(this, WelcomeMapEquipementActivity.class));
    }

    /**
     * Evolution photo service fait
     */


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

    //TODO remettre au propre et PAS en dur
    public void selectImage() {
        final CharSequence[] items = {"Prendre une photo", "Choisir dans la galerie", "Annuler"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ("Prendre une photo".equals(items[which])) {
                    cameraIntent();
                } else if ("Choisir dans la galerie".equals(items[which])) {
                    galleryIntent();

                } else {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    private void cameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().log(e.getMessage());
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, TAKE_PICTURE_REQUEST_CODE);
            }
        }
    }

    private void galleryIntent() {
        final Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Sélectionnez une photo"), CHOOSE_FROM_GALLERY_REQUEST_CODE);
    }

    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
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

            default:
                Log.d(TAG, "onActivityResult : " + requestCode);
                break;
        }
    }

    private void rotateResizeAndCompress() {
        // Rotate, Scrale and compress
        Bitmap resizedBitmap = BitmapScaler.scaleToFitTheGoodOne(MiscTools.rotateBitmapOrientation(mCurrentPhotoPath));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
        File resizedFile = new File(mCurrentPhotoPath);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(resizedFile);
            fos.write(bytes.toByteArray());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showPicture(final String fileName) {

        addPictureButton.setVisibility(View.GONE);
        addAnomalyPhotoLayout.setVisibility(View.GONE);

        anomalyEquipementDetailsPresenter.addPictureToModel(fileName);


        //first picture set
        choosePicture.setImageResource(R.drawable.ic_check_circle_pink_24px);
        myImageChoiceLayout.setVisibility(View.VISIBLE);
        image1.setVisibility(View.VISIBLE);
        Glide.with(this).load(fileName).fitCenter().into(image1);
        myImageChoiceClose.setVisibility(View.VISIBLE);

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

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

    @OnClick(R.id.myImageChoiceClose)
    public void disablePic1() {
        anomalyEquipementDetailsPresenter.removePicture1();
        myImageChoiceLayout.setVisibility(View.GONE);
        image1.setVisibility(View.GONE);
        myImageChoiceClose.setVisibility(View.GONE);
        addAnomalyPhotoLayout.setVisibility(View.VISIBLE);
        addPictureButton.setVisibility(View.VISIBLE);
        testPicturesAndGreyCheck();
    }

    private void addPictureToPlaceHolder(Bitmap thumbnail) {
        anomalyEquipementDetailsPresenter.savePictureInFile(thumbnail);
    }

    public void testPicturesAndGreyCheck() {
        if (image1.getVisibility() == View.GONE) {
            choosePicture.setImageResource(R.drawable.ic_check_circle_grey_24px);
        }
    }


}
