package com.accenture.dansmarue.mvp.presenters;

import com.accenture.dansmarue.mvp.views.BaseView;
import com.accenture.dansmarue.mvp.views.PrefProfileView;
import com.accenture.dansmarue.mvp.views.ProfileView;
import com.accenture.dansmarue.services.SiraApiService;
import com.accenture.dansmarue.services.models.FollowRequest;
import com.accenture.dansmarue.services.models.IsEmailAgentRequest;
import com.accenture.dansmarue.services.models.SiraSimpleResponse;
import com.accenture.dansmarue.utils.Constants;
import com.accenture.dansmarue.utils.PrefManager;

import javax.inject.Inject;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created on 24/09/2021.
 */

public class PrefProfilePresenter extends BasePresenter<PrefProfileView> implements SingleObserver<SiraSimpleResponse> {

    private PrefProfileView view;
    private SiraApiService service;
    private PrefManager prefManager;

    @Inject
    public PrefProfilePresenter(final PrefProfileView view, final SiraApiService service, final PrefManager prefManager) {
        this.view = view;
        this.service = service;
        this.prefManager = prefManager;
    }

    public void isEmailAgent(String email) {

        IsEmailAgentRequest request = new IsEmailAgentRequest();
        request.setEmail(email);

        service.isEmailAgent(request)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

    @Override
    protected BaseView getView() {
        return view;
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onSuccess(SiraSimpleResponse value) {
        if (value.getAnswer() != null && value.getAnswer().getStatus().equals(Constants.STATUT_WS_OK)) {
           prefManager.setIsAgent(value.getAnswer().isAgent());
           view.isEmailAgentCallback();
        }
    }

    @Override
    public void onError(Throwable e) {

    }
}
