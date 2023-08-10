package com.accenture.dansmarue.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.app.DansMaRueApplication;
import com.accenture.dansmarue.di.components.ApplicationComponent;

import butterknife.ButterKnife;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;


/**
 * Created by PK on 24/03/2017.
 * Base activity extendable by all the activities : Manage ButterKnife et Dagger injections
 */
public abstract class BaseActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        resolveDaggerDependency();
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        ButterKnife.bind(this);


        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        changeStatusBarColor();

        onViewReady(savedInstanceState, getIntent());

    }

    // Making notification bar transparent
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(getApplicationContext(), R.color.framboise));
        }

    }


    @CallSuper
    void onViewReady(Bundle savedInstanceState, Intent intent) {
        //To be used by child activities
    }

    @Override
    protected void onDestroy() {
        ButterKnife.bind(this).unbind();
        super.onDestroy();
    }

    // pass context to Calligraphy to use Custom fonts
    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(context));
    }

    void resolveDaggerDependency() {
    }


    ApplicationComponent getApplicationComponent() {
        return ((DansMaRueApplication) getApplication()).getApplicationComponent();
    }


    protected abstract int getContentView();


    public void displayConnectionError() {
        final Toast toastMessage = Toast.makeText(this, R.string.info_no_network, Toast.LENGTH_LONG);
        toastMessage.setGravity(Gravity.TOP, 5, 5);
//      toastMessage.show();
    }


}
