package com.accenture.dansmarue.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.di.components.DaggerPresenterComponent;
import com.accenture.dansmarue.di.modules.PresenterModule;
import com.accenture.dansmarue.mvp.models.Category;
import com.accenture.dansmarue.mvp.presenters.CategoryPresenter;
import com.accenture.dansmarue.mvp.views.CategoryView;
import com.accenture.dansmarue.ui.adapters.CategoryAdapter;
import com.accenture.dansmarue.ui.adapters.CategorySearchAdapter;

import com.accenture.dansmarue.utils.Constants;



import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class CategoryActivity extends BaseActivity implements AdapterView.OnItemClickListener, CategoryView {

    private static final String TAG = CategoryActivity.class.getName();

    private final static int REQUEST_FAVORITE_ITEM = 200;

    @Inject
    protected CategoryPresenter presenter;

    @BindView(R.id.search_bar_category)
    protected EditText searchBarCategory;

    @BindView(R.id.categories)
    protected ListView listView;

    @BindView(R.id.text_title_category)
    protected TextView titleCategory;

    @BindView(R.id.listview_category)
    protected ListView listViewSearchCategory;


    private CategoryAdapter adapter;
    private CategorySearchAdapter categorySearchAdapter;
    private String idPreviousParent;
    private Category lastParentItemSelected;

    @Override
    protected void onViewReady(final Bundle savedInstanceState, final Intent intent) {
        super.onViewReady(savedInstanceState, intent);
        listView.setOnItemClickListener(this);

        Log.i(TAG, "onViewReady: load type outdoor");
        presenter.loadCategories();
        idPreviousParent = null;
        lastParentItemSelected = null;

        initSearchBarCategory();

    }

    private void initSearchBarCategory() {

        categorySearchAdapter = new CategorySearchAdapter(this, presenter.loadCategoriesForSearchBar());
        listViewSearchCategory.setAdapter(categorySearchAdapter);

        listViewSearchCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                presenter.onCategorySelected(((Category) categorySearchAdapter.getItem(i)).getId());
            }
        });

        searchBarCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newText = searchBarCategory.getText().toString();
                if (newText.equals("")) {
                    listViewSearchCategory.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
                if (newText != null && newText.trim().length() > 2) {
                    String text = newText;
                    listViewSearchCategory.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                    categorySearchAdapter.filter(text);
                } else {
                    listViewSearchCategory.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.i(TAG, "onBackPressed: ");
    }

    @OnClick(R.id.arrow_back_type)
    public void backType() {

        if (adapter.getDisplayFavoriteItems()) {
            if(lastParentItemSelected ==null) {
                titleCategory.setText(R.string.text_type);
                presenter.extractChildren(Category.ID_FIRST_PARENT);
            } else {
                titleCategory.setText(lastParentItemSelected.getName());
                presenter.extractChildren(lastParentItemSelected.getId());
            }

        } else {
            Log.i(TAG, "customBack: " + idPreviousParent);
            if (idPreviousParent == null) {
                finish();
            } else {
                presenter.onCategorySelected(idPreviousParent);
            }
        }
    }

    @OnClick(R.id.favoris)
    public void openFavorisItems() {
        final Intent intent = new Intent(CategoryActivity.this, FavoriteCategoryActivity.class);
        startActivityForResult(intent, REQUEST_FAVORITE_ITEM);
    }


    @Override
    protected int getContentView() {
        return R.layout.category_activity_layout;
    }


    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        Log.i(TAG, "onItemClick: " + idPreviousParent);
        if (adapter.getItem(position) != null)
            presenter.onCategorySelected(adapter.getItem(position).getId());
    }

    @Override
    protected void resolveDaggerDependency() {
        DaggerPresenterComponent.builder()
                .applicationComponent(getApplicationComponent())
                .presenterModule(new PresenterModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void updateListView(final List<Category> categories, boolean displayFavoriteItems) {
        if (adapter == null) {
            adapter = new CategoryAdapter(this, R.layout.category, categories);
            listView.setAdapter(adapter);
        } else {
            adapter.clear();
            adapter.addAll(categories);
        }
        adapter.setDisplayFavoriteItems(displayFavoriteItems);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onChildSelected(final Category category) {
        final Intent intent = new Intent(CategoryActivity.this, AddAnomalyActivity.class);
        intent.putExtra(Constants.EXTRA_CATEGORY_ID, category.getId());
        if (null != category.getAlias()) {
            intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getAlias());
        } else {
            intent.putExtra(Constants.EXTRA_CATEGORY_NAME, category.getName());
        }

        if(category.getMessageHorsDMR() != null) {
            intent.putExtra(Constants.EXTRA_MESSAGE_HORS_DMR, category.getMessageHorsDMR());
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onParentSelected(final Category catParent) {
        idPreviousParent = catParent.getParentId();
        lastParentItemSelected = catParent;
        if (catParent.getName() != null ) {
            titleCategory.setText(catParent.getName());
        } else {
            titleCategory.setText(R.string.text_type);
        }
        presenter.extractChildren(catParent.getId());
    }

    @Override
    public void categoriesReady() {
        presenter.extractChildren(Category.ID_FIRST_PARENT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode ==RESULT_OK) {
            Category favoriteCategory = new Category();
            favoriteCategory.setId(data.getStringExtra(Constants.EXTRA_CATEGORY_ID));
            favoriteCategory.setName(data.getStringExtra(Constants.EXTRA_CATEGORY_NAME));
            if (data.hasExtra(Constants.EXTRA_MESSAGE_HORS_DMR)) {
                favoriteCategory.setMessageHorsDMR(data.getStringExtra(Constants.EXTRA_MESSAGE_HORS_DMR));
            }
            onChildSelected(favoriteCategory);
        }
    }
}
