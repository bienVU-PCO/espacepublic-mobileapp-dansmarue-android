package com.accenture.dansmarue.ui.activities;

import android.Manifest;
import android.app.Dialog;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.app.DansMaRueApplication;
import com.accenture.dansmarue.di.components.DaggerPresenterComponent;
import com.accenture.dansmarue.di.modules.PresenterModule;
import com.accenture.dansmarue.mvp.models.Incident;
import com.accenture.dansmarue.mvp.models.MessageServiceFait;
import com.accenture.dansmarue.mvp.presenters.AnomalyDetailsPresenter;
import com.accenture.dansmarue.mvp.views.AnomalyDetailsView;
import com.accenture.dansmarue.utils.BitmapScaler;
import com.accenture.dansmarue.utils.Constants;
import com.accenture.dansmarue.utils.MiscTools;
import com.accenture.dansmarue.utils.NetworkUtils;
import com.accenture.dansmarue.utils.PrefManager;
import com.bumptech.glide.Glide;
import com.google.firebase.crashlytics.FirebaseCrashlytics;


import java.io.File;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * AnomalyDetailsActivity
 *  Activity to display incident detail.
 */
public class AnomalyDetailsActivity extends BaseAnomalyActivity implements AnomalyDetailsView {

    private static final String TAG = AnomalyDetailsActivity.class.getCanonicalName();

    private final static int REQUEST_CODE_LOGIN = 7;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final int CHOOSE_TYPE_REQUEST_CODE = 1982;
    private static final int SET_COMMENTAIRE_AGENT_REQUALIFICATION_REQUEST_CODE = 1983;
    private static final int CHOOSE_MESSAGE_SF_REQUEST_CODE = 1984;



    @Inject
    protected AnomalyDetailsPresenter anomalyDetailsPresenter;

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
    @BindView(R.id.anomaly_commentaire_agent)
    @Nullable
    protected TextView anomalyCommentaireAgentTerrain;
    //@BindView(R.id.anomaly_nb_followers)
    //protected TextView nbFollowers;
    @BindView(R.id.anomaly_nb_greetings)
    protected TextView nbGreetings;
    @BindView(R.id.anomaly_time)
    protected TextView anomalyDateTime;
    @BindView(R.id.anomaly_number)
    protected TextView anomalyNumber;
    @BindView(R.id.anomaly_layout_followers)
    protected LinearLayout layoutFollowers;

    //@BindView(R.id.fab_details_ano)
    //protected FloatingActionButton fabDetailsAno;
    @BindView(R.id.resolve)
    protected Button resolveBtn;

    //AGENT LAYOUT

    //requalification
    @BindView(R.id.linear_layout_requalification)
    @Nullable
    protected LinearLayout requalificationLayout;
    @BindView(R.id.linear_layout_requalification2)
    @Nullable
    protected LinearLayout requalification2Layout;
    @BindView(R.id.image_languette_requalification)
    @Nullable
    protected ImageButton pictoLanguetteRequalification;

    //Type
    @BindView(R.id.image_choose_type)
    @Nullable
    protected ImageView chooseType;
    @BindView(R.id.text_choose_type_subtitle)
    @Nullable
    protected TextView chooseTypeSubtitle;

    @BindView(R.id.requalification_photo)
    @Nullable
    protected ImageButton pictureRequalificationButton;

    @BindView(R.id.requalification_photo_layout)
    @Nullable
    protected RelativeLayout photoRequalificationLayout;

    @BindView(R.id.requalification_choose_picture)
    @Nullable
    protected ImageView requalificationChoosePicture;

    @BindView(R.id.requalificationImageChoiceLayout)
    @Nullable
    protected RelativeLayout requalificationImageChoiceLayout;

    @BindView(R.id.requalificationImageChoice)
    @Nullable
    protected ImageView requalificationImage;

    @BindView(R.id.requalificationImageChoiceClose)
    @Nullable
    protected ImageView requalificationImageChoiceClose;

    //Commentaire agent
    @BindView(R.id.text_commentagent_subtitle)
    @Nullable
    protected AppCompatTextView textCommentAgent;
    @BindView(R.id.commentagent_choose_type)
    @Nullable
    protected ImageView chooseCommentAgent;

    @BindView(R.id.button_requalification)
    @Nullable
    protected Button resqualificationBtn;

    //requalification fin

    // service fait
    @BindView(R.id.linear_layout_service_fait)
    @Nullable
    protected LinearLayout serviceFaitLayout;
    @BindView(R.id.linear_layout_service_fait2)
    @Nullable
    protected LinearLayout serviceFait2Layout;
    @BindView(R.id.image_languette_service_fait)
    @Nullable
    protected ImageButton pictoLanguetteServiceFait;
    @BindView(R.id.photo_service_fait_layout)
    protected LinearLayout photoServiceFaitLayout;

    @BindView(R.id.image_choose_type_sf)
    @Nullable
    protected ImageView chooseMessageServiceFaitImage;

    @BindView(R.id.layout_choose_message_sf)
    @Nullable
    protected LinearLayout messageServiceFait;

    @BindView(R.id.text_choose_type_subtitle_sf)
    @Nullable
    protected TextView chooseMessageServiceFait;

    //FIN AGENT LAYOUT

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
    private MessageServiceFait messageServiceFaitSelect;
    private int indexPicture = 0;
    private int maxIndex = 0;

    private int nbFollowersDisplay = 0;
    private int nbGreetingsDisplay = 0;

    private boolean incidentComeFromNotif;
    private boolean isLayoutAgent;
    private boolean isRequalificationPhoto;
    private boolean isMessageServiceFaitSelect;

    private HashMap<Integer,MessageServiceFait> messagesServiceFait;

    private Dialog bePatientDialog;


    @Override
    protected void onViewReady(Bundle savedInstanceState, Intent intent) {
        super.onViewReady(savedInstanceState, intent);

        Log.i(TAG, "onViewReady: detail");

        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);

        final Long incidentId = intent.getLongExtra(Constants.EXTRA_INCIDENT_ID, 0);

        isMessageServiceFaitSelect = false;

        String incidentByNotif = intent.getStringExtra(Constants.EXTRA_INCIDENT_TYPE);
        if(null!=incidentByNotif) incidentComeFromNotif =true;

        final String source = intent.getStringExtra(Constants.EXTRA_INCIDENT_SOURCE);

        if (incidentId == 0) {
            finish();
        }

        anomalyDetailsPresenter.loadIncident(incidentId, source);


        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        if (null != toolbar) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
            getSupportActionBar().setTitle("Détail");

            // tips to fix weird behavior
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (incidentComeFromNotif) {
                        startActivity(new Intent(AnomalyDetailsActivity.this, WelcomeMapActivity.class));
                        finish();
                    } else {
                        onBackPressed();
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

        PrefManager prefManager = new PrefManager(getApplicationContext());

        if(prefManager.getIsAgent())  {
            isLayoutAgent = true;
            return R.layout.anomaly_details_activity_agent_layout;
        } else {
            isLayoutAgent = false;
            return R.layout.anomaly_details_activity_layout;
        }
    }


    //@OnClick(R.id.fab_details_ano)
    /**public void fabClicked() {
        //Should always be true
        if (incident != null) {

            if (incident.isResolu()) {
                congratulate();
            } else {

                if (!incident.isIncidentFollowedByUser()) {
                    anomalyDetailsPresenter.followAnomaly(String.valueOf(incident.getId()));
                } else {
                    anomalyDetailsPresenter.unfollowAnomaly(String.valueOf(incident.getId()));
                }

            }
        }

    }**/


    /**
     * On display fallow success
     */
    public void displayFollow() {
        incident.setIncidentFollowedByUser(true);
        //fabDetailsAno.setImageResource(R.drawable.ic_followed);
        Snackbar.make(findViewById(R.id.rl_add_ano_details), R.string.follow_anomaly, Snackbar.LENGTH_LONG).show();
        nbFollowersDisplay++;
        //nbFollowers.setText(nbFollowersDisplay + " ");
    }

    /**
     * On display fallow failure
     */
    public void displayFollowFailure() {
        Snackbar.make(findViewById(R.id.rl_add_ano_details), R.string.follow_anomaly_failure, Snackbar.LENGTH_LONG).show();
    }

    /**
     * On display unfallow success
     */
    public void displayUnfollow() {
        incident.setIncidentFollowedByUser(false);
        //fabDetailsAno.setImageResource(R.drawable.ic_follow);
        Snackbar.make(findViewById(R.id.rl_add_ano_details), R.string.unfollow_anomaly, Snackbar.LENGTH_LONG).show();
        nbFollowersDisplay--;
        //nbFollowers.setText(nbFollowersDisplay + " ");
    }

    /**
     * On display unfallow failure
     */
    public void displayUnfollowFailure() {
        Snackbar.make(findViewById(R.id.rl_add_ano_details), R.string.unfollow_anomaly_failure, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void populateFields(final Incident loadedIncident) {
        if (null == loadedIncident) {
            Toast.makeText(getApplicationContext(), "Erreur lors de la récuperation des informations du signalement", Toast.LENGTH_SHORT).show();
        } else {
            incident = loadedIncident;

            messageServiceFaitSelect =null;
            maxIndex = incident.getAllPictures().size() - 1;
            if (maxIndex == -1 && leftPicNavigation != null && rightPicNavigation != null) {
                leftPicNavigation.setVisibility(View.GONE);
                rightPicNavigation.setVisibility(View.GONE);
            }

            if (indexPicture == 0 && leftPicNavigation != null) {
                leftPicNavigation.setVisibility(View.GONE);
            }

            if (indexPicture == maxIndex && rightPicNavigation != null) {
                rightPicNavigation.setVisibility(View.GONE);
            }

            if (incident.isFromRamen()) {
                //TODO normalement on reçoit une imgae générique pour ramen
                Glide.with(this)
                        .load(incident.getPictures().getGenericPictureId())
                        .into(imageView);
            } else if (incident.getFirstAvailablePicture() != null) {
                    Glide.with(this)
                            .load(incident.getFirstAvailablePicture())
                            .fallback(incident.getPictures().getGenericPictureId())
                            .error(incident.getPictures().getGenericPictureId())
                            .into(imageView);

            } else {
                Glide.with(this).load(incident.getPictures().getGenericPictureId()).into(imageView);
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
            anomalyNumber.setText(incident.getReference());


            if (incident.isResolu()) {

                TextView txtOverPicture = (TextView) findViewById(R.id.txt_over_picture);
                txtOverPicture.setBackgroundResource(R.drawable.round_corner_green);
                txtOverPicture.setText(R.string.anomaly_resolved_toast);
                txtOverPicture.setVisibility(View.VISIBLE);

                /**if (incident.isFromRamen()) {
                    fabDetailsAno.setImageResource(R.drawable.ic_greetings_grey);
                    fabDetailsAno.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_grey)));
                } else {
                    fabDetailsAno.setImageResource(R.drawable.ic_greetings_white);
                    fabDetailsAno.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_green)));
                }**/

                //isAgentLayout and isResolve
                if(isLayoutAgent) {
                    requalificationLayout.setVisibility(View.GONE);
                    serviceFaitLayout.setVisibility(View.GONE);
                }


            } else {

                TextView txtOverPicture = (TextView) findViewById(R.id.txt_over_picture);
                txtOverPicture.setBackgroundResource(R.drawable.round_corner_orange);
                txtOverPicture.setText(R.string.anomaly_in_progress);
                txtOverPicture.setVisibility(View.VISIBLE);

                 if(isLayoutAgent) {
                     serviceFaitLayout.setVisibility(View.VISIBLE);
                 } else {
                     resolveBtn.setVisibility(View.GONE);
                     photoServiceFaitLayout.setVisibility(View.GONE);
                 }

            }

            if (isLayoutAgent) {
                anomalyCommentaireAgentTerrain.setText(incident.getCommentaireAgent());
                if (Incident.STATE_TIERS.equals(incident.getState())){
                    requalificationLayout.setVisibility(View.GONE);
                }
            }
        }

        nbFollowersDisplay = incident.getFollowers();
        //nbFollowers.setText(nbFollowersDisplay + " ");

        nbGreetingsDisplay = incident.getCongratulations();
        nbGreetings.setText(nbGreetingsDisplay + " ");


    }

    public void populateMessageServiceFaitGeneric(List<MessageServiceFait> messages) {
        messagesServiceFait = new HashMap<Integer,MessageServiceFait>();
        int index = 0;
        for(MessageServiceFait message : messages) {
            message.setIsGeneric(true);
            messagesServiceFait.put(index,message);
            index++;
        }
    }

    public void populateMessageServiceFaitType(List<MessageServiceFait> messages) {
        int index = messagesServiceFait.size();
        for(MessageServiceFait message : messages) {
            message.setIsGeneric(false);
            messagesServiceFait.put(index,message);
            index++;
        }
    }

    private void congratulate() {
        if (!congratulated) {

            //fabDetailsAno.setImageResource(R.drawable.ic_greetings_grey);
            //fabDetailsAno.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_grey)));

            Snackbar.make(findViewById(R.id.anomaly_line2), R.string.greetings_ok, Snackbar.LENGTH_LONG)
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            switch (event) {
                                case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                    anomalyDetailsPresenter.congratulateAnomalie(String.valueOf(incident.getId()));
                                    congratulated = true;
                                    break;
                            }
                        }
                    })
                    .setAction(R.string.greetings_undo, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                           // fabDetailsAno.setImageResource(R.drawable.ic_greetings_white);
                           // fabDetailsAno.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.greetings_green)));
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
                                resolveBtn.setEnabled(false);

                                if(image1.getVisibility()==View.VISIBLE) {
                                    anomalyDetailsPresenter.uploadPicture(String.valueOf(incident.getId()), false, "done");
                                    // after call resolveIncident
                                } else {
                                   callResolveIncident();
                                }

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
    public void callResolveIncident() {
        anomalyDetailsPresenter.resolveIncident(String.valueOf(incident.getId()), messageServiceFaitSelect);
    }

    @Override
    public void displayGreetingsOk() {
        Log.d(TAG, "displayGreetingsOk: ");

        nbGreetingsDisplay++;
        nbGreetings.setText(nbGreetingsDisplay+" ");
        //fabDetailsAno.setVisibility(View.INVISIBLE);

    }

    @Override
    public void displayGreetingsKo() {
        Snackbar.make(findViewById(R.id.navigation), R.string.dmr_error, Snackbar.LENGTH_LONG).show();
        congratulated = false;
        //fabDetailsAno.setImageResource(R.drawable.ic_greetings_green);
        //fabDetailsAno.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.white)));
    }

    @Override
    public void inviteToLogin() {
        startActivityForResult(new Intent(AnomalyDetailsActivity.this, LoginActivity.class), REQUEST_CODE_LOGIN);
    }

    @Override
    public void displayResolveKo() {
        //TODO quelle message afficher
        Toast.makeText(this, "Erreur lors de la prise en copmpte de la déclaration", Toast.LENGTH_LONG).show();
        if(isLayoutAgent) {
            resolveBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.framboise));
            resolveBtn.setEnabled(true);
        }
    }

    @Override
    public void displayResolveOk() {
        final Toast toastMessage = Toast.makeText(this, "Cette anomalie a été clôturée", Toast.LENGTH_LONG);
        if (isLayoutAgent) {
            finish();
            startActivity(getIntent());
        }
        else {
            toastMessage.setGravity(Gravity.TOP, 5, 5);
            toastMessage.show();
            incident.setState(Incident.STATE_RESOLVED);
            populateFields(incident);
            photoServiceFaitLayout.setVisibility(View.INVISIBLE);
            resolveBtn.setVisibility(View.INVISIBLE);
        }
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
        startActivity(new Intent(this, WelcomeMapActivity.class));
    }


    @Optional
    @OnClick({R.id.linear_layout_requalification, R.id.image_languette_requalification})
    public void showhideLayoutRequalification() {

        if (requalification2Layout.getVisibility() == View.VISIBLE) {
            requalification2Layout.setVisibility(View.GONE);
            pictoLanguetteRequalification.setImageResource(R.drawable.ic_arrow_right);

        } else {
            requalification2Layout.setVisibility(View.VISIBLE);
            pictoLanguetteRequalification.setImageResource(R.drawable.ic_arrow_drop_down);
        }
    }

    @Optional
    @OnClick({R.id.linear_layout_service_fait, R.id.image_languette_service_fait})
    public void showhideLayoutServicefait() {

        if (serviceFait2Layout.getVisibility() == View.VISIBLE) {
            serviceFait2Layout.setVisibility(View.GONE);
            photoServiceFaitLayout.setVisibility(View.GONE);
            messageServiceFait.setVisibility(View.GONE);
            resolveBtn.setVisibility(View.GONE);
            pictoLanguetteServiceFait.setImageResource(R.drawable.ic_arrow_right);

        } else {
            serviceFait2Layout.setVisibility(View.VISIBLE);
            photoServiceFaitLayout.setVisibility(View.VISIBLE);
            if (!incident.isAnonyme() ) {
                messageServiceFait.setVisibility(View.VISIBLE);
                if(isMessageServiceFaitSelect) {
                    resolveBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.framboise));
                    resolveBtn.setEnabled(true);
                } else {
                    resolveBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.grey_icon));
                    resolveBtn.setEnabled(false);
                }
            }  else if (incident.isAnonyme()) {
                //anonymous report
                resolveBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.framboise));
                resolveBtn.setEnabled(true);
            }
            resolveBtn.setVisibility(View.VISIBLE);
            pictoLanguetteServiceFait.setImageResource(R.drawable.ic_arrow_drop_down);
        }
    }

    @Optional
    @OnClick(R.id.layout_choose_type)
    public void chooseType() {
        startActivityForResult(new Intent(this, CategoryActivity.class), CHOOSE_TYPE_REQUEST_CODE);
    }

    @Optional
    @OnClick({R.id.layout_commentagent, R.id.commentagent_layout_parent})
    public void setDescription() {
        final Intent intent = new Intent(AnomalyDetailsActivity.this, SetDescriptionActivity.class);
        intent.putExtra(Constants.EXTRA_COMMENTAIRE_AGENT_REQUALIFICATION, anomalyDetailsPresenter.getRequalificationComment());
        intent.putExtra(Constants.EXTRA_CALLING_ACTIVITY, Constants.ACTIVITY_DETAILS_ANOMALY);
        startActivityForResult(intent, SET_COMMENTAIRE_AGENT_REQUALIFICATION_REQUEST_CODE);
    }

    @Optional
    @OnClick(R.id.button_requalification)
    public void onRequalification() {
        if (NetworkUtils.isConnected(getApplicationContext())) {
            pleaseBePatientDialog();
            anomalyDetailsPresenter.doRequalification(incident);
        } else {
            Toast.makeText(this, R.string.info_no_network, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Requalification work
     */
    public void requalificationSuccess() {

        if(requalificationImage.getVisibility()==View.VISIBLE) {
            String typePhoto = "far";
            if(!incident.getPictures().getFar().isEmpty() && incident.getPictures().getClose().isEmpty()) {
                typePhoto = "close";
            }
            anomalyDetailsPresenter.uploadPicture(String.valueOf(incident.getId()),true, typePhoto);
        } else {
            uploadRequalificationDone();
        }
    }

    public void uploadRequalificationDone() {
        bePatientDialog.dismiss();
        // reload activity with new type
        finish();
        startActivity(getIntent());
    }

    /**
     * Requalification fail
     */
    public void requalificationFaillure() {
        bePatientDialog.dismiss();
        Toast.makeText(this, R.string.dmr_error_requalification,Toast.LENGTH_LONG).show();
    }

    @OnClick(R.id.add_anomaly_photo)
    public void takePhotoOrViewGallery() {
        isRequalificationPhoto = false;
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

    @Optional
    @OnClick(R.id.requalification_photo)
    public void takePhotoOrViewGalleryRequalification() {
        isRequalificationPhoto = true;
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

    @Optional
    @OnClick(R.id.layout_choose_message_sf)
    public void chooseMessageServiceFait() {
        Intent intent = new Intent(this, MessageSFActivity.class);
        intent.putExtra(Constants.EXTRA_LIST_MESSAGE_SERVICE_FAIT,messagesServiceFait);
        startActivityForResult(intent, CHOOSE_MESSAGE_SF_REQUEST_CODE);
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

    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    rotateResizeAndCompress();
                    if (isRequalificationPhoto) {
                        showPictureRequalification(mCurrentPhotoPath);
                    } else {
                        showPicture(mCurrentPhotoPath);
                    }
                }
                break;
            case CHOOSE_FROM_GALLERY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    onSelectFromGalleryResult(data, isRequalificationPhoto);
                }
                break;
            case CHOOSE_TYPE_REQUEST_CODE:
                onTypeResult(resultCode, data);
                break;

            case SET_COMMENTAIRE_AGENT_REQUALIFICATION_REQUEST_CODE:
                onCommentaireAgentRequalificationResult(resultCode, data);
                break;
            case CHOOSE_MESSAGE_SF_REQUEST_CODE:
                onMessageServiceFaitResult(resultCode, data);
                break;
            default:
                Log.d(TAG, "onActivityResult : " + requestCode);
                break;
        }
    }

    public void showPicture(final String fileName) {

        addPictureButton.setVisibility(View.GONE);
        addAnomalyPhotoLayout.setVisibility(View.GONE);

        anomalyDetailsPresenter.addPictureToModel(fileName);


        //first picture set
        choosePicture.setImageResource(R.drawable.ic_check_circle_pink_24px);
        myImageChoiceLayout.setVisibility(View.VISIBLE);
        image1.setVisibility(View.VISIBLE);
        Glide.with(this).load(fileName).fitCenter().into(image1);
        myImageChoiceClose.setVisibility(View.VISIBLE);

    }

    public void showPictureRequalification (final String fileName) {

        pictureRequalificationButton.setVisibility(View.GONE);
        photoRequalificationLayout.setVisibility(View.GONE);

        anomalyDetailsPresenter.addPictureRequalificationToModel(fileName);

        requalificationChoosePicture.setImageResource(R.drawable.ic_check_circle_pink_24px);
        requalificationImageChoiceLayout.setVisibility(View.VISIBLE);
        requalificationImage.setVisibility(View.VISIBLE);
        Glide.with(this).load(fileName).fitCenter().into(requalificationImage);
        requalificationImageChoiceClose.setVisibility(View.VISIBLE);
    }

    private void onSelectFromGalleryResult(final Intent data, final boolean isForRequalification) {
        if (data != null) {
            try {
                Bitmap thumbnail = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                addPictureToPlaceHolder(thumbnail, isForRequalification);
            } catch (IOException e) {
                FirebaseCrashlytics.getInstance().log(e.getMessage());
                Log.e(TAG, e.getMessage(), e);
            }
        }

    }

    private void onTypeResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            typeTreatment(data.getStringExtra(Constants.EXTRA_CATEGORY_NAME), data.getStringExtra(Constants.EXTRA_CATEGORY_ID));
        }
    }

    private void onMessageServiceFaitResult(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            messageServiceFaitSelect = (MessageServiceFait) data.getSerializableExtra(Constants.EXTRA_MESSAGE_SERVICE_FAIT_SELECT);
            chooseMessageServiceFait.setText(messageServiceFaitSelect.getMessage());
            chooseMessageServiceFait.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.pink));
            chooseMessageServiceFaitImage.setImageResource(R.drawable.ic_check_circle_pink_24px);
            resolveBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.framboise));
            resolveBtn.setEnabled(true);
            isMessageServiceFaitSelect = true;
        }
    }

    /**
     * Callback after SET_COMMENTAIRE_AGENT_REQUALIFICATION_REQUEST_CODE
     *
     * @param resultCode result
     * @param data       intent containig the description
     */
    private void onCommentaireAgentRequalificationResult(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            String txtCommentaireAgent = data.getStringExtra(Constants.EXTRA_COMMENTAIRE_AGENT_REQUALIFICATION);
            String txtCommentaireAgentLimit = txtCommentaireAgent;
            if (txtCommentaireAgent.length() > 40) {
                txtCommentaireAgentLimit = txtCommentaireAgent.substring(0, 40) + "...";
            }
            textCommentAgent.setText(txtCommentaireAgentLimit);
            if (txtCommentaireAgent.length() > 0) {
                chooseCommentAgent.setImageResource(R.drawable.ic_check_circle_pink_24px);
            } else {
                chooseCommentAgent.setImageResource(R.drawable.ic_check_circle_grey_24px);
            }
            anomalyDetailsPresenter.setRequalificationComment(data.getStringExtra(Constants.EXTRA_COMMENTAIRE_AGENT_REQUALIFICATION));
        }
    }

    private void typeTreatment(String typeSubtitle, String categoryType) {
        chooseTypeSubtitle.setText(typeSubtitle);
        anomalyDetailsPresenter.setCategory(categoryType);
        chooseType.setImageResource(R.drawable.ic_check_circle_pink_24px);
        if(! incident.getAlias().equals(typeSubtitle)) {
            resqualificationBtn.setEnabled(true);
            resqualificationBtn.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.pink));
        }
    }


    @OnClick(R.id.myImageChoiceClose)
    public void disablePic1() {
        anomalyDetailsPresenter.removePicture1();
        myImageChoiceLayout.setVisibility(View.GONE);
        image1.setVisibility(View.GONE);
        myImageChoiceClose.setVisibility(View.GONE);
        addAnomalyPhotoLayout.setVisibility(View.VISIBLE);
        addPictureButton.setVisibility(View.VISIBLE);
        testPicturesAndGreyCheck();
    }

    @Optional
    @OnClick(R.id.requalificationImageChoiceClose)
    public void disablePicRequalification() {
        anomalyDetailsPresenter.removePictureRequalification();
        requalificationImageChoiceLayout.setVisibility(View.GONE);
        requalificationImage.setVisibility(View.GONE);
        requalificationImageChoiceLayout.setVisibility(View.GONE);
        photoRequalificationLayout.setVisibility(View.VISIBLE);
        pictureRequalificationButton.setVisibility(View.VISIBLE);
        requalificationChoosePicture.setImageResource(R.drawable.ic_check_circle_grey_24px);
    }

    private void addPictureToPlaceHolder(Bitmap thumbnail, boolean isForRequalification) {
        anomalyDetailsPresenter.savePictureInFile(thumbnail, isForRequalification);
    }

    public void testPicturesAndGreyCheck() {
        if (image1.getVisibility() == View.GONE) {
            choosePicture.setImageResource(R.drawable.ic_check_circle_grey_24px);
        }
    }

    /**
     * Show Be patient dialog
     */
    private void pleaseBePatientDialog() {

        bePatientDialog = new Dialog(this);
        bePatientDialog.setContentView(R.layout.greetings_dialog_send_incident);
        bePatientDialog.setCancelable(false);

        if (bePatientDialog.getWindow() != null) {
            bePatientDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Fix Dialog Size
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(bePatientDialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            bePatientDialog.getWindow().setAttributes(lp);
            bePatientDialog.show();
        }

    }

}
