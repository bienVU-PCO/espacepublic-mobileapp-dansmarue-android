package com.accenture.dansmarue.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.app.DansMaRueApplication;
import com.accenture.dansmarue.di.components.DaggerPresenterComponent;
import com.accenture.dansmarue.di.modules.PresenterModule;
import com.accenture.dansmarue.mvp.models.Incident;
import com.accenture.dansmarue.mvp.presenters.ProfilePresenter;
import com.accenture.dansmarue.mvp.views.ProfileView;
import com.accenture.dansmarue.ui.activities.AddAnomalyActivity;
import com.accenture.dansmarue.ui.activities.AddAnomalyEquipementActivity;
import com.accenture.dansmarue.ui.activities.AnomalyDetailsActivity;
import com.accenture.dansmarue.ui.activities.AnomalyEquipementDetailsActivity;
import com.accenture.dansmarue.ui.activities.PrefActivity;
import com.accenture.dansmarue.ui.adapters.ProfileSection;
import com.accenture.dansmarue.utils.Constants;
import com.accenture.dansmarue.utils.RecyclerItemClickListener;
import com.google.gson.GsonBuilder;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;


/**
 * create an instance of this fragment.
 */
@SuppressWarnings("deprecation")
public class ProfileFragment extends BaseFragment implements ProfileView {

    private static final String TAG = ProfileFragment.class.getCanonicalName();

    @Inject
    protected ProfilePresenter presenter;
    private OnProfileFragmentInteractionListener activity;

    private RecyclerView recyclerView;
    private SectionedRecyclerViewAdapter adapter;

    private ProfileSection draftSection;
    private ProfileSection resolvedSection;
    private ProfileSection unresolvedSection;


    @BindView(R.id.menu_anos_drafts)
    protected TextView menuDraft;
    @BindView(R.id.menu_anos_unresolved)
    protected TextView menuUnresolved;
    @BindView(R.id.menu_anos_resolved)
    protected TextView menuResolved;

    @BindView(R.id.user_mail_txt)
    protected TextView userMailTxt;


    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        // Required empty public constructor
        return new ProfileFragment();
    }


    @Override
    protected void resolveDaggerDependency(DansMaRueApplication application) {
        DaggerPresenterComponent.builder()
                .applicationComponent(application.getApplicationComponent())
                .presenterModule(new PresenterModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (ProfileFragment.OnProfileFragmentInteractionListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        ButterKnife.bind(this, rootView);

        adapter = new SectionedRecyclerViewAdapter();

        draftSection = new ProfileSection(getContext(), getString(R.string.section_drafts));
        unresolvedSection = new ProfileSection(getContext(), getString(R.string.section_unresolved));
        resolvedSection = new ProfileSection(getContext(), getString(R.string.section_resolved));


        adapter.addSection(Constants.TAG_SECTION_DRAFTS, draftSection);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view_anomaly_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        initRecyclerActions();
        return rootView;

    }


    @Override
    public void onStart() {
        super.onStart();

        loadDraft();
        showMenuDrafts();
    }

    @OnClick(R.id.menu_setup)
    public void onMenuSetupClicked() {
        presenter.onPreferenceClicked();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDraft();
        Log.i(TAG, "onResume: ");
        presenter.initUser();

        presenter.oldPositionMenu();

    }

    private void initRecyclerActions() {
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        try {
                            ProfileSection section = (ProfileSection) adapter.getSectionForPosition(position);
                            if (section.getState() == Section.State.FAILED) {
                                //loadIncidents();
                            } else {
                                int posInSection = adapter.getPositionInSection(position);
                                //the item  with pos = -1 is the header component of the section
                                if (posInSection > -1) {
                                    boolean isDraft;
                                    isDraft = draftSection == section;
                                    presenter.onItemClicked(isDraft, section.getItem(posInSection));
                                }
                            }
                        }catch(IndexOutOfBoundsException e) {
                           Log.e(TAG, "initRecyclerActions: " + e.getMessage()+ " position : " + position);
                        }
                    }
                })
        );

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final int position = viewHolder.getAdapterPosition();
                try {
                    ProfileSection section = (ProfileSection) adapter.getSectionForPosition(position);
                    Log.d(TAG, "getSwipeDirs: " + adapter.getPositionInSection(position));
                    if (draftSection == section || unresolvedSection == section || resolvedSection == section  && adapter.getPositionInSection(position) > -1) {
                        return super.getSwipeDirs(recyclerView, viewHolder);
                    }
                } catch (IndexOutOfBoundsException e) {
                    Log.e(TAG,"getSwipeDirs",e);
                    return 0;
                }
                return 0;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                final int position = viewHolder.getAdapterPosition();
                ProfileSection section = (ProfileSection) adapter.getSectionForPosition(position);

                int posInSection = adapter.getPositionInSection(position);
                final Incident draft = section.getItem(posInSection);

                // Swype for incident = Unfollow

                if(null!=draft) {

                    if (draftSection == section) {
                        presenter.deleteDraft(String.valueOf(draft.getId()));
                        section.deleteItem(posInSection);
                        adapter.notifyItemRemoved(position);
                    }

                    if (unresolvedSection == section || resolvedSection == section) {
                        presenter.unFollowDraft(draft);
                        section.deleteItem(posInSection);
                        adapter.notifyItemRemoved(position);
                    }
                }

            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

    }

    @Override
    public void loadIncidents(final String filterState) {
        resolvedSection.setState(Section.State.LOADING);
        unresolvedSection.setState(Section.State.LOADING);
        adapter.notifyDataSetChanged();
        presenter.loadIncidentsByUser(filterState);
    }

    private void loadDraft() {
        draftSection.setState(Section.State.LOADING);
        adapter.notifyDataSetChanged();
        presenter.loadDrafts();
    }

    @Override
    public void updateUserName(String firstName, String lastName) {
        userMailTxt.setText(firstName + " " + lastName);
    }

    @Override
    public void showDrafts(final List<Incident> drafts) {
        draftSection.setData(drafts);
        draftSection.setState(Section.State.LOADED);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showSolvedIncidents(List<Incident> incidents) {
        resolvedSection.setData(incidents);
        resolvedSection.setState(Section.State.LOADED);
        adapter.notifyDataSetChanged();
    }


    @Override
    public void showUnsolvedIncidents(List<Incident> incidents) {
        unresolvedSection.setData(incidents);
        unresolvedSection.setState(Section.State.LOADED);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showFailedLoading() {
        unresolvedSection.setState(Section.State.FAILED);
        resolvedSection.setState(Section.State.FAILED);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void showMenuAll() {

        adapter.removeAllSections();

        adapter.addSection(Constants.TAG_SECTION_DRAFTS, draftSection);
        adapter.addSection(Constants.TAG_SECTION_UNRESOLVED, unresolvedSection);
        adapter.addSection(Constants.TAG_SECTION_RESOLVED, resolvedSection);

        adapter.notifyDataSetChanged();

        menuDraft.setTextColor(getResources().getColor(R.color.framboise));
        menuUnresolved.setTextColor(getResources().getColor(R.color.framboise));
        menuResolved.setTextColor(getResources().getColor(R.color.framboise));

    }

    @Override
    public void showMenuDrafts() {
        adapter.removeSection(Constants.TAG_SECTION_RESOLVED);
        adapter.removeSection(Constants.TAG_SECTION_UNRESOLVED);
        adapter.addSection(Constants.TAG_SECTION_DRAFTS, draftSection);
        adapter.notifyDataSetChanged();

        menuDraft.setTextColor(getResources().getColor(R.color.framboise));
        menuUnresolved.setTextColor(getResources().getColor(R.color.grey_tranparent));
        menuResolved.setTextColor(getResources().getColor(R.color.grey_tranparent));

    }

    @Override
    public void showMenuUnresolved() {
        adapter.removeSection(Constants.TAG_SECTION_DRAFTS);
        adapter.removeSection(Constants.TAG_SECTION_RESOLVED);
        adapter.addSection(Constants.TAG_SECTION_UNRESOLVED, unresolvedSection);
        adapter.notifyDataSetChanged();

        menuDraft.setTextColor(getResources().getColor(R.color.grey_tranparent));
        menuUnresolved.setTextColor(getResources().getColor(R.color.framboise));
        menuResolved.setTextColor(getResources().getColor(R.color.grey_tranparent));

    }

    @Override
    public void showMenuResolved() {
        adapter.removeSection(Constants.TAG_SECTION_DRAFTS);
        adapter.removeSection(Constants.TAG_SECTION_UNRESOLVED);
        adapter.addSection(Constants.TAG_SECTION_RESOLVED, resolvedSection);

        adapter.notifyDataSetChanged();

        menuDraft.setTextColor(getResources().getColor(R.color.grey_tranparent));
        menuUnresolved.setTextColor(getResources().getColor(R.color.grey_tranparent));
        menuResolved.setTextColor(getResources().getColor(R.color.framboise));

    }

    @OnClick({R.id.menu_anos_drafts, R.id.menu_anos_unresolved, R.id.menu_anos_resolved})
    public void onMenuClicked(final View view) {

        presenter.onMenuClicked(view.getId());
    }

    @Override
    public void modifyDraft(final Incident draft) {

        Intent intent;

        // Add ano equipement or outdoor
        if (null != draft.getEquipementId()) {
            intent = new Intent(getActivity(), AddAnomalyEquipementActivity.class);

        } else {
            intent = new Intent(getActivity(), AddAnomalyActivity.class);
        }
        final String jsonDraft = new GsonBuilder().create().toJson(draft);
        intent.putExtra(Constants.EXTRA_DRAFT, jsonDraft);
        getActivity().startActivityForResult(intent,9281);
    }

    @Override
    public void showAnomalyDetails(final Incident incident) {
        Intent intent;

        if (null != incident.getEquipementId()) {

            intent = new Intent(getActivity(), AnomalyEquipementDetailsActivity.class);
        } else {
            intent = new Intent(getActivity(), AnomalyDetailsActivity.class);
        }

        intent.putExtra(Constants.EXTRA_INCIDENT_ID, incident.getId());
        intent.putExtra(Constants.EXTRA_INCIDENT_SOURCE, incident.getSource());
        getActivity().startActivityForResult(intent,9281);

    }

    @Override
    public void showPreferences() {
        startActivityForResult(new Intent(getContext(), PrefActivity.class),9632);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public interface OnProfileFragmentInteractionListener {
    }

}
