package com.accenture.dansmarue.ui.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.mvp.models.Category;
import com.accenture.dansmarue.utils.CategoryHelper;
import com.accenture.dansmarue.utils.PrefManager;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class CategorySearchAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater inflater;
    private List<Category> categoryList = null;
    private ArrayList<Category> arraylist;

    private PrefManager prefManager;

    public CategorySearchAdapter(Context context, List<Category> categoryList) {
        mContext = context;
        this.categoryList = categoryList;
        inflater = LayoutInflater.from(mContext);
        this.arraylist = new ArrayList<Category>();
        this.arraylist.addAll(categoryList);
        prefManager = new PrefManager(mContext);
    }

    public class ViewHolder {
        TextView name;
        ImageView iconFavoris;
    }

    @Override
    public int getCount() {
        return categoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return categoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final Category category = categoryList.get(position);
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.category, null);
            // Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.cat_name);
            holder.iconFavoris =  ((ImageView)view.findViewById(R.id.cat_favoris));
            view.setTag(holder);

            ((ImageView)view.findViewById(R.id.cat_icon)).setVisibility(View.GONE);

        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(CategoryHelper.getCompleteCategoryHierarchy(category));
        if(prefManager.getFavorisItems().containsKey(category.getId())) {
            holder.iconFavoris.setImageResource(R.drawable.ic_full_star);
        } else {
            holder.iconFavoris.setImageResource(R.drawable.ic_empty_star);
        }

        holder.iconFavoris.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, "onClick: favoris");
                        if(prefManager.getFavorisItems().containsKey(category.getId())) {
                            // remove fovoris
                            holder.iconFavoris.setImageResource(R.drawable.ic_empty_star);
                            holder.iconFavoris.setContentDescription(mContext.getString(R.string.add_favorite_type));
                            prefManager.setFavorisItem(category,true);
                        } else {
                            // add favoris
                            holder.iconFavoris.setImageResource(R.drawable.ic_full_star);
                            holder.iconFavoris.setContentDescription(mContext.getString(R.string.remove_favorite_type));
                            prefManager.setFavorisItem(category,false);
                        }

                    }
                }
        );
        return view;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase();
        categoryList.clear();
        if (charText.length() == 0) {
            categoryList.addAll(arraylist);
        } else {
            for (Category cat : arraylist) {
                if (CategoryHelper.getCompleteCategoryHierarchy(cat).toLowerCase().contains(charText)) {
                    categoryList.add(cat);
                }
            }
        }
        notifyDataSetChanged();
    }
}

