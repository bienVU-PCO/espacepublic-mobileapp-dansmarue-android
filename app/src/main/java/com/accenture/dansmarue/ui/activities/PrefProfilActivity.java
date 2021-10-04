package com.accenture.dansmarue.ui.activities;


import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.accenture.dansmarue.R;

import com.accenture.dansmarue.di.components.DaggerPresenterComponent;
import com.accenture.dansmarue.di.modules.PresenterModule;
import com.accenture.dansmarue.mvp.presenters.PrefProfilePresenter;
import com.accenture.dansmarue.mvp.views.PrefProfileView;

import com.accenture.dansmarue.utils.PrefManager;

import javax.inject.Inject;

import butterknife.BindView;



public class PrefProfilActivity extends BaseActivity implements PrefProfileView {

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.profil_email_label)
    protected TextView labelEmail;
    @BindView(R.id.profil_email)
    protected TextView email;
    @BindView(R.id.profil_enter_my_mail)
    protected TextView labelInputEmail;
    @BindView(R.id.input_my_mail)
    protected EditText inputMyEmail;

    @BindView(R.id.button_connect)
    protected Button buttonConnect;

    @Inject
    protected PrefProfilePresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        if (null != toolbar) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.pref_profil_activity_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24px);
        }

        PrefManager prefManager = new PrefManager(getApplicationContext());
        if (prefManager.getEmail() != null && prefManager.getEmail().trim().length() > 0) {
            email.setText(prefManager.getEmail());
            labelEmail.setVisibility(View.VISIBLE);
            email.setVisibility(View.VISIBLE);
            labelInputEmail.setVisibility(View.INVISIBLE);
            inputMyEmail.setVisibility(View.INVISIBLE);
            buttonConnect.setVisibility(View.INVISIBLE);
        } else {
            labelEmail.setVisibility(View.INVISIBLE);
            email.setVisibility(View.INVISIBLE);
            labelInputEmail.setVisibility(View.VISIBLE);
            inputMyEmail.setVisibility(View.VISIBLE);
            buttonConnect.setVisibility(View.VISIBLE);
        }

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailEnter = inputMyEmail.getText().toString();
                if (!TextUtils.isEmpty(emailEnter) && Patterns.EMAIL_ADDRESS.matcher(emailEnter).matches()) {
                    prefManager.setConnected(emailEnter,null);
                    presenter.isEmailAgent(emailEnter);
                } else {
                    Toast.makeText(getApplicationContext(),"L'adresse mail n'est pas valide.",Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    @Override
    protected void resolveDaggerDependency() {
        DaggerPresenterComponent.builder()
                .applicationComponent(getApplicationComponent())
                .presenterModule(new PresenterModule(this))
                .build().inject(this);

    }

    @Override
    protected int getContentView() {
        return R.layout.pref_profil_activity_layout;
    }

    @Override
    public void isEmailAgentCallback() {
        finish();
    }
}
