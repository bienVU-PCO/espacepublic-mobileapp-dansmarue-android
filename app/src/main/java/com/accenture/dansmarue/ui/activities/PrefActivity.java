package com.accenture.dansmarue.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.utils.PrefManager;

import butterknife.BindView;


public class PrefActivity extends BaseActivity {

    private static final int LOGIN_REQUEST_CODE = 1982;

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.listPrefs)
    protected ListView listPrefs;

    @BindView(R.id.btn_deco)
    protected Button btnDeco;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        if (null != toolbar) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(R.string.pref_activity_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24px);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }


        // Setup list pref
        final String[] itemsMenu = getResources().getStringArray(R.array.pref_items_menu);
        prefManager = new PrefManager(getApplicationContext());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemsMenu);
        listPrefs.setAdapter(adapter);

        listPrefs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                switch (position) {

                    //  profil
                    case 0:
                        startActivity(new Intent(PrefActivity.this, PrefProfilActivity.class));
                        break;
                    //  préfs
                    case 1:
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);

                        break;

                    //  CGU
                    case 2:
                        startActivity(new Intent(PrefActivity.this, PrefCGUActivity.class));
                        break;

                    //  About
                    case 3:
                        startActivity(new Intent(PrefActivity.this, PrefAboutActivity.class));
                        break;

                    default:
                        break;

                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOGIN_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    btnDeco.setVisibility(View.VISIBLE);
                    finish();
                }
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (prefManager.isConnected()) {
            btnDeco.setVisibility(View.VISIBLE);
            btnDeco.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prefManager.disconnect();
                    prefManager.setEmail(null);
                    recreate();
                }
            });
        } else {
            btnDeco.setVisibility(View.GONE);
        }
    }

    // Avoid user to quit the app
    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected int getContentView() {
        return R.layout.pref_activity_layout;
    }
}
