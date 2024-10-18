package com.accenture.dansmarue.utils;

import android.app.Application;
import android.util.Log;

import com.accenture.dansmarue.R;
import com.accenture.dansmarue.mvp.models.Category;
import com.accenture.dansmarue.services.models.CategoryResponse;
import com.accenture.dansmarue.services.models.equipements.CategoryEquipementResponse;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.GsonBuilder;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by PK on 29/03/2017.
 * Helper class to manipulate {@link Category}
 */
public final class CategoryHelper {

    public static final String ID_CATEGORIE_RAMEN = "6666";

    public static Map<String, Integer> CAT_ICONS = new HashMap<>();
    public static Map<String, Integer> MAP_ICONS = new HashMap<>();
    public static Map<String, Integer> MAP_ICONS_RESOLVED = new HashMap<>();
    public static Map<String, Integer> MAP_GENERIC_PICTURES = new HashMap<>();


    private static final String TAG = CategoryHelper.class.getName();
    private static Map<String, Category> ALL_CATEGORIES;


    static {

        //CAT_ICONS.put(ID_CATEGORIE_RAMEN, R.drawable.ic_type_svg_obj_lost_1000);
        CAT_ICONS.put("12159", R.drawable.ic_type_svg_obj_lost_1000);
        CAT_ICONS.put("12310", R.drawable.ic_type_svg_chantier_2000);
        CAT_ICONS.put("12158", R.drawable.ic_type_svg_garbage_3000);
        CAT_ICONS.put("12160", R.drawable.ic_type_svg_garbage_3100);
        CAT_ICONS.put("12161", R.drawable.ic_type_svg_banksy_3600);
        //CAT_ICONS.put("4000", R.drawable.ic_type_svg_voirie_4000);
        //CAT_ICONS.put("5000", R.drawable.ic_type_svg_voirie_4000);
        CAT_ICONS.put("12283", R.drawable.ic_type_svg_light_default_6000);
        CAT_ICONS.put("12293", R.drawable.ic_type_svg_furniture_7000);
        //CAT_ICONS.put("8000", R.drawable.ic_type_svg_voirie_4000);
        CAT_ICONS.put("12162", R.drawable.ic_type_svg_tree_9000);
        //CAT_ICONS.put("10000", R.drawable.ic_type_svg_tree_9000);
        CAT_ICONS.put("12327", R.drawable.ic_type_svg_water_11000);
        //CAT_ICONS.put("12005", R.drawable.icon_12005);
        //CAT_ICONS.put("12000", R.drawable.icon_12005);
        //CAT_ICONS.put("12008", R.drawable.icon_12008);

        //MAP_ICONS.put(ID_CATEGORIE_RAMEN, R.drawable.ic_ano_1000);
        MAP_ICONS.put("12159", R.drawable.ic_ano_1000);
        MAP_ICONS.put("12310", R.drawable.ic_ano_2000);
        MAP_ICONS.put("12158", R.drawable.ic_ano_3000);
        MAP_ICONS.put("12160", R.drawable.ic_ano_3100);
        MAP_ICONS.put("12161", R.drawable.ic_ano_3600);
        //MAP_ICONS.put("4000", R.drawable.ic_ano_4000);
        //MAP_ICONS.put("5000", R.drawable.ic_ano_4000);
        MAP_ICONS.put("12283", R.drawable.ic_ano_6000);
        MAP_ICONS.put("12293", R.drawable.ic_ano_7000);
        //MAP_ICONS.put("8000", R.drawable.ic_ano_4000);
        MAP_ICONS.put("12162", R.drawable.ic_ano_9000);
        //MAP_ICONS.put("10000", R.drawable.ic_ano_9000);
        MAP_ICONS.put("12327", R.drawable.ic_ano_11000);
        //MAP_ICONS.put("12005", R.drawable.ano_12005);
        //MAP_ICONS.put("12000", R.drawable.ano_12005);
        //MAP_ICONS.put("12008", R.drawable.ic_ano_12008);

        //MAP_ICONS_RESOLVED.put(ID_CATEGORIE_RAMEN, R.drawable.ic_ano_done_1000);
        MAP_ICONS_RESOLVED.put("12159", R.drawable.ic_ano_done_1000);
        MAP_ICONS_RESOLVED.put("12310", R.drawable.ic_ano_done_2000);
        MAP_ICONS_RESOLVED.put("12158", R.drawable.ic_ano_done_3000);
        MAP_ICONS_RESOLVED.put("12160", R.drawable.ic_ano_done_3100);
        MAP_ICONS_RESOLVED.put("12161", R.drawable.ic_ano_done_3600);
        //MAP_ICONS_RESOLVED.put("4000", R.drawable.ic_ano_done_4000);
        //MAP_ICONS_RESOLVED.put("5000", R.drawable.ic_ano_done_4000);
        MAP_ICONS_RESOLVED.put("12283", R.drawable.ic_ano_done_6000);
        MAP_ICONS_RESOLVED.put("12293", R.drawable.ic_ano_done_7000);
        //MAP_ICONS_RESOLVED.put("8000", R.drawable.ic_ano_done_4000);
        MAP_ICONS_RESOLVED.put("12162", R.drawable.ic_ano_done_9000);
        //MAP_ICONS_RESOLVED.put("10000", R.drawable.ic_ano_done_9000);
        MAP_ICONS_RESOLVED.put("12327", R.drawable.ic_ano_done_11000);
        //MAP_ICONS_RESOLVED.put("12005", R.drawable.ano_done_12005);
        //MAP_ICONS_RESOLVED.put("12000", R.drawable.ano_done_12005);
        //MAP_ICONS_RESOLVED.put("12008", R.drawable.ic_ano_done_12008);

        MAP_GENERIC_PICTURES.put(ID_CATEGORIE_RAMEN, R.drawable.ic_type_svg_obj_lost_1000_grey);
        MAP_GENERIC_PICTURES.put("12159", R.drawable.ic_type_svg_obj_lost_1000_grey);
        MAP_GENERIC_PICTURES.put("12310", R.drawable.ic_type_svg_chantier_2000_grey);
        MAP_GENERIC_PICTURES.put("12158", R.drawable.ic_type_svg_garbage_3000_grey);
        MAP_GENERIC_PICTURES.put("12160", R.drawable.ic_type_svg_garbage_3100_grey);
        MAP_GENERIC_PICTURES.put("12161", R.drawable.ic_type_svg_banksy_3600_grey);
        //MAP_GENERIC_PICTURES.put("4000", R.drawable.ic_type_svg_voirie_4000_grey);
        //MAP_GENERIC_PICTURES.put("5000", R.drawable.ic_type_svg_voirie_4000_grey);
        MAP_GENERIC_PICTURES.put("12283", R.drawable.ic_type_svg_light_default_6000_grey);
        MAP_GENERIC_PICTURES.put("12293", R.drawable.ic_type_svg_furniture_7000_grey);
        //MAP_GENERIC_PICTURES.put("8000", R.drawable.ic_type_svg_voirie_4000_grey);
        MAP_GENERIC_PICTURES.put("12162", R.drawable.ic_type_svg_tree_9000_grey);
        //MAP_GENERIC_PICTURES.put("10000", R.drawable.ic_type_svg_tree_9000_grey);
        MAP_GENERIC_PICTURES.put("12327", R.drawable.ic_type_svg_water_11000_grey);
        //MAP_GENERIC_PICTURES.put("12005", R.drawable.image_12005);
        //MAP_GENERIC_PICTURES.put("12000", R.drawable.image_12005);
        //MAP_GENERIC_PICTURES.put("12008", R.drawable.image_12008);

    }


    private CategoryHelper() {
        //private constructor, no instanciation
    }


    /**
     * Retrieve the first parent id of a category (different than Category.ID_FIRST_PARENT)
     *
     * @param childId       category id
     * @param allCategories map of all the categories
     * @return his "first" parent id
     */
    public static String getFirstParent(final String childId, final Map<String, Category> allCategories) {
        if (allCategories.get(childId) != null) {
            final String parentId = allCategories.get(childId).getParentId();
            Log.i(TAG, "getFirstParent: parentID " + parentId);
            if (parentId != null && !Category.ID_FIRST_PARENT.equals(parentId)) {
                return getFirstParent(parentId, allCategories);
            }
        }
        return childId;
    }

    /**
     * Retrieve all the categories from the json file {@link Constants#FILE_CATEGORIES_JSON}
     *
     * @param application application context
     * @return map of all the categories
     */
    public static Map<String, Category> getAllCategories(final Application application) {
        return getAllCategories(application, false,"");
    }


    public static Map<String, Category> getAllCategories(final Application application, boolean isTypeEquipement, String typeEquipementId) {
        try {

            PrefManager prefManager = new PrefManager(application.getApplicationContext());

            if (isTypeEquipement) {

                Log.i(TAG, "load getAllCategories: equipements");

                if(null!=prefManager.getCatsInDoor()){
                    ALL_CATEGORIES = prefManager.getCatsInDoor();
                }else {
                    FileInputStream in;
                    in = application.getApplicationContext().openFileInput(Constants.FILE_LIST_ANOS_PAR_EQUIPEMENT);
                    byte[] buffer = new byte[in.available()];
                    in.read(buffer);
                    final String bufferStr = new String(buffer);
                    Map<String,CategoryResponse.Answer.Category> test = new GsonBuilder().create().fromJson(bufferStr, CategoryEquipementResponse.Answer.class).getCategories().get(typeEquipementId);
                    ALL_CATEGORIES = CategoryHelper.answerToCategories(test);
                    prefManager.setCatsInDoor(ALL_CATEGORIES);
                    in.close();
                }

                for(String i:ALL_CATEGORIES.keySet()) Log.i(TAG, "getAllCategories: "+ALL_CATEGORIES.get(i).getName()+" - "+ALL_CATEGORIES.get((i)).getParentId() + "image"+ALL_CATEGORIES.get(i).getImageMobile());


            } else {

                Log.i(TAG, "load getAllCategories: outdoor");

                if(null!=prefManager.getCatsOuDoor()){
                    ALL_CATEGORIES = prefManager.getCatsOuDoor();
                }else {
                    FileInputStream in;
                    in = application.getApplicationContext().openFileInput(Constants.FILE_CATEGORIES_JSON);
                    byte[] buffer = new byte[in.available()];
                    in.read(buffer);
                    final String bufferStr = new String(buffer);
                    ALL_CATEGORIES = CategoryHelper.answerToCategories(new GsonBuilder().create().fromJson(bufferStr, CategoryResponse.Answer.class).getCategories());
                    prefManager.setCatsOutDoor(ALL_CATEGORIES);
                    in.close();
                }

                for(String i:ALL_CATEGORIES.keySet()) Log.i(TAG, "getAllCategories: "+ALL_CATEGORIES.get(i).getName()+" - "+ALL_CATEGORIES.get((i)).getParentId());

            }


        } catch (IOException e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
            Log.e(TAG, e.getMessage(), e);
        }

        return ALL_CATEGORIES;
    }


    /**
     * Transform a Map<String,CategoryEquipementResponse.Answer.Category> returned by the rest WS to a Map of {@link Category}
     *
     * @param answer WS answer
     * @return a map with key=categoryId and value = categorie
     */
    public static Map<String, Category> answerToCategories(Map<String, CategoryResponse.Answer.Category> answer) {
        if (MapUtils.isNotEmpty(answer)) {
            final Map<String, Category> result = new HashMap<>(answer.size());
            for (String id : answer.keySet()) {
                final CategoryResponse.Answer.Category catResponse = answer.get(id);
                final Category cat = CategoryHelper.catAnswerToCategory(id, catResponse);
                if (CollectionUtils.isNotEmpty(catResponse.getChildrenIds())) {
                    final List<Category> children = new ArrayList<>(catResponse.getChildrenIds().size());
                    for (String childId : catResponse.getChildrenIds()) {
                        if (answer.get(childId) != null) {
                            children.add(CategoryHelper.catAnswerToCategory(childId, answer.get(childId)));
                        }
                    }
                    cat.setChildren(children);
                }
                result.put(id, cat);
            }
            return result;
        }
        return null;
    }

    private static Category catAnswerToCategory(String id, CategoryResponse.Answer.Category catAnswer) {
        final Category cat = new Category();
        cat.setId(id);
        cat.setName(catAnswer.getName());
        cat.setIsAgent(catAnswer.isAgent());
        cat.setHasMessageHorsDMR(catAnswer.isHasMessageHorsDMR());
        cat.setMessageHorsDMR(catAnswer.getMessageHorsDMR());
        cat.setParentId(catAnswer.getParentId());
        cat.setAlias(catAnswer.getAlias());
        cat.setImage(catAnswer.getImage());
        cat.setImageMobile(catAnswer.getImageMobile());
        return cat;
    }


    /**
     * Extract the cildren of a category which id is passed
     *
     * @param parentId   id of the category to retrieve children of
     * @param categories map of all the categories
     * @return the children of the category,<br>
     * null if none found
     */
    public static List<Category> extractChildren(String parentId, Map<String, Category> categories) {
        if (MapUtils.isNotEmpty(categories)) {
            if (categories.get(parentId) != null) {
                final List<Category> result = new ArrayList<>(categories.get(parentId).getChildren().size());
                result.addAll(categories.get(parentId).getChildren());
                for(Category x :categories.get(parentId).getChildren() ){
                    Log.i(TAG, "extractChildren: "+x.getName());
                }
                return result;
            }
        }
        return null;
    }

    public static boolean hasChildren(String categorieId) {
        List<Category> listChildren = extractChildren(categorieId, ALL_CATEGORIES);
        if(listChildren != null && ! listChildren.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getCompleteCategoryHierarchy(Category category) {
        StringBuilder sbRes = new StringBuilder(category.getName());
        Category currentCategory = category;
        int currentCategoryParentId = Integer.parseInt(currentCategory.getParentId());
        while ( currentCategoryParentId > 0) {
            currentCategory = ALL_CATEGORIES.get(currentCategory.getParentId());
            if(currentCategory == null) {
                return "";
            }
            currentCategoryParentId = Integer.parseInt(currentCategory.getParentId());
            sbRes.insert(0,currentCategory.getName() + ">");
        }
        return sbRes.toString();
    }




}
