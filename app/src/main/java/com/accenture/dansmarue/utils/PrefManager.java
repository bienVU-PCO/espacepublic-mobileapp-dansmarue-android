package com.accenture.dansmarue.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import androidx.annotation.NonNull;

import com.accenture.dansmarue.mvp.models.Category;
import com.accenture.dansmarue.mvp.models.FavoriteAddress;
import com.accenture.dansmarue.mvp.models.equipementsMunicipaux.TypeEquipement;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by d4v1d on 17/03/2017.
 * <p>
 * Use sharedPreferences to stock datas
 * ex : first launch of application
 */

public class PrefManager {
    private final SharedPreferences pref;
    private final Context _context;

    // shared pref mode
    private static final int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "dansmarue";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    private static final String EMAIL_IN_SHP = "EmailInSharedPref";
    private static final String IS_CONNECTED = "isConnected";
    private static final String GUID = "guid";
    private static final String FIRST_NAME = "firstname";
    private static final String LAST_NAME = "lastname";
    private static final String IS_AGENT = "isAgent";

    private static final String LAST_MENU = "lastmenu";

    private static final String FAVORIS_ITEMS = "favorisitem";
    private static final String FAVORIS_ADDRESS = "favorisaddress";


    private static final String CATEGORIES_VERSION = "categoriesVersion";
    private static final String DEFAULT_CATEGORIES_VERSION = "0";
    private String udid;

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.apply();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }


    public void setCategoriesVersion(final String version) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(CATEGORIES_VERSION, version);
        editor.apply();
    }

    public String getCategoriesVersion() {
        return pref.getString(CATEGORIES_VERSION, DEFAULT_CATEGORIES_VERSION);
    }

    public void setTypesEquipement(List<TypeEquipement> listeObjets) {
        Gson gson = new Gson();
        String maListe = gson.toJson(listeObjets);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("LISTE_TYPES_EQUIPEMENT", maListe);
        editor.apply();
    }

    public ArrayList<TypeEquipement> getTypesEquipement() {
        Gson gson = new Gson();
        String maListe = pref.getString("LISTE_TYPES_EQUIPEMENT", null);
        ArrayList<TypeEquipement> mesTypes = new ArrayList<>();
        mesTypes = gson.fromJson(maListe, new TypeToken<ArrayList<TypeEquipement>>() {
        }.getType());
        return mesTypes;
    }

    public void setEquipementTypeByDefault(TypeEquipement equipementByDefault) {
        Gson gson = new Gson();
        String myEquipementByDefault = gson.toJson(equipementByDefault);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("TYPE_EQUIPEMENT_BY_DEFAULT", myEquipementByDefault);
        editor.apply();
    }

    public void setEquipementIdMapTypeEquipementId(Map<String,String> maMap){
        Gson gson = new Gson();
        String maMapStr = gson.toJson(maMap);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("EQUIPEMENTID_MAP_TYPEEQUIPEMENTID", maMapStr);
        editor.apply();
    }

    public Map<String,String> getEquipementIdMapTypeEquipementId(){
        Gson gson = new Gson();
        String maMapStr = pref.getString("EQUIPEMENTID_MAP_TYPEEQUIPEMENTID", null);
        Map<String,String> maMap= gson.fromJson(maMapStr, new TypeToken<Map<String,String>>(){}.getType());
        return maMap;
    }

    // Save All Cats Outdoor
    public void setCatsOutDoor(Map<String,Category> catOutDoor){
        Gson gson = new Gson();
        String str = gson.toJson(catOutDoor);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("CAT_OUTDOOR", str);
        editor.apply();
    }

    public Map<String,Category> getCatsOuDoor(){
        Gson gson = new Gson();
        String str = pref.getString("CAT_OUTDOOR", null);
        Map<String,Category> catOutDoor= gson.fromJson(str, new TypeToken<Map<String,Category>>(){}.getType());
        return catOutDoor;
    }

    // Save All Cats Indoor
    public void setCatsInDoor(Map<String,Category> catInDoor){
        Gson gson = new Gson();
        String str = gson.toJson(catInDoor);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("CAT_INDOOR", str);
        editor.apply();
    }

    public Map<String,Category> getCatsInDoor(){
        Gson gson = new Gson();
        String str = pref.getString("CAT_INDOOR", null);
        Map<String,Category> catInDoor= gson.fromJson(str, new TypeToken<Map<String,Category>>(){}.getType());
        return catInDoor;
    }






    public void setEquipementIdSelected(String id) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("EQUIPEMENT_ID_SELECTED", "" + id);
        editor.apply();
    }

    public int getEquipementIdSelected() {
        String idStr = pref.getString("EQUIPEMENT_ID_SELECTED", null);
        return Integer.parseInt(idStr);
    }

    public TypeEquipement getEquipementTypeByDefault() {
        Gson gson = new Gson();
        String myEquipementByDefaultStr = pref.getString("TYPE_EQUIPEMENT_BY_DEFAULT", null);
        TypeEquipement myEquipementByDefault = gson.fromJson(myEquipementByDefaultStr, TypeEquipement.class);
        return myEquipementByDefault;
    }


    //    Save Email
    public void setEmail(@NonNull final String email) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(EMAIL_IN_SHP, email);
        editor.apply();
    }

    public String getEmail() {
        return pref.getString(EMAIL_IN_SHP, "");
    }

    public void setFirstName(@NonNull final String firstName) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(FIRST_NAME, firstName);
        editor.apply();
    }

    public String getFirstName() {
        return pref.getString(FIRST_NAME, "");
    }


    public void setIsAgent(@NonNull final Boolean isAgent) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean (IS_AGENT, isAgent);
        editor.apply();
    }

    public Boolean getIsAgent() {
        return pref.getBoolean (IS_AGENT, false);
    }

    public void setLastMenu(int idMenu) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(LAST_MENU, idMenu);
        editor.apply();
    }

    public int getLastMenu() {
        return pref.getInt(LAST_MENU,0);
    }


    public void setLastName(@NonNull final String lastName) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(LAST_NAME, lastName);
        editor.apply();
    }

    public String getLastName() {
        return pref.getString(LAST_NAME, "");
    }

    public void setConnected(@NonNull final String email, @NonNull final String guid) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_CONNECTED, Boolean.TRUE);
        editor.putString(EMAIL_IN_SHP, email);
        editor.putString(GUID, guid);
        editor.apply();
    }

    //    Save email as login
    public void disconnect() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(IS_CONNECTED, Boolean.FALSE);
        editor.putBoolean(IS_AGENT, Boolean.FALSE);
        editor.remove(GUID);
        editor.remove(FIRST_NAME);
        editor.remove(LAST_NAME);
        editor.apply();
    }

    public String getGuid() {
        return pref.getString(GUID, null);
    }


    public boolean isConnected() {
        return pref.getBoolean(IS_CONNECTED, Boolean.FALSE);
    }


    public String getUdid() {
        if (udid == null) {
            udid = Settings.Secure.getString(_context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        return udid;
    }


    /**
     * Get the firebase user token (used for the push notification)
     *
     * @return actual firebase user token for the user
     */
    public String getUserToken() {
        return FirebaseMessaging.getInstance().getToken().getResult();
    }


    // User favoris items
    public void setFavorisItem(List<Category> categories) {

        Map<String, Category> mapFavoriteCategory = new LinkedHashMap<>();

        for(Category category : categories ) {
            mapFavoriteCategory.put(category.getId(), category);
        }

        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        String str = gson.toJson(mapFavoriteCategory);
        editor.putString(FAVORIS_ITEMS, str);
        editor.apply();
    }

    public void setFavorisItem(Category category, boolean remove) {
        Map<String,Category> mapFavorisItems = getFavorisItems();

        if (remove) {
            mapFavorisItems.remove(category.getId());
        } else {
            mapFavorisItems.put(category.getId(), category);
        }

        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        String str = gson.toJson(mapFavorisItems);
        editor.putString(FAVORIS_ITEMS, str);
        editor.apply();

    }

    // get User Favoris Items
    public Map<String,Category> getFavorisItems() {

        String str = pref.getString(FAVORIS_ITEMS, null);
        Gson gson = new Gson();
        Map<String,Category> mapFavorisItems = gson.fromJson(str, new TypeToken<Map<String,Category>>(){}.getType());
        if(mapFavorisItems == null) {
            mapFavorisItems = new HashMap<>();
        }
        return mapFavorisItems;
    }


    // User favorite address
    public void setFavorisAddress(List<FavoriteAddress> favAddress) {
        Map<String, FavoriteAddress> mapFavoriteAddress = new LinkedHashMap<>();

        for(FavoriteAddress address : favAddress ) {
            mapFavoriteAddress.put(address.getAddress(), address);
        }

        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        String str = gson.toJson(mapFavoriteAddress);
        editor.putString(FAVORIS_ADDRESS, str);
        editor.apply();
    }

    public void setFavorisAddress(FavoriteAddress favAddress, boolean remove) {
        Map<String, FavoriteAddress> mapFavoriteAddress = getFavoriteAddress();

        if (remove) {
            mapFavoriteAddress.remove(favAddress.getAddress());
        } else {
            mapFavoriteAddress.put(favAddress.getAddress(), favAddress);
        }

        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        String str = gson.toJson(mapFavoriteAddress);
        editor.putString(FAVORIS_ADDRESS, str);
        editor.apply();

    }

    // get User FavoriteAddress
    public Map<String,FavoriteAddress> getFavoriteAddress() {

        String str = pref.getString(FAVORIS_ADDRESS, null);
        Gson gson = new Gson();
        Map<String,FavoriteAddress> mapFavoriteAddress = gson.fromJson(str, new TypeToken<Map<String,FavoriteAddress>>(){}.getType());
        if(mapFavoriteAddress == null) {
            mapFavoriteAddress = new HashMap<>();
        }
        return mapFavoriteAddress;
    }

}