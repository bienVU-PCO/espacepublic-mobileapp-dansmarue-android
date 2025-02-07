package com.accenture.dansmarue.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.media.ExifInterface;
import android.util.Base64;
import android.util.Log;


import com.accenture.dansmarue.R;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by d4v1d on 30/03/2017.
 */

public class MiscTools {

    /**
     * FIXME
     *
     * @param address
     * @return
     */
    public static String whichPostalCode(String address) {
        try {
            Pattern p = Pattern.compile("75[0-9][0-9][0-9]");
            Matcher m = p.matcher(address);
            String postalCode;
            int numberPostalCode;

            if (m.find()) {

                postalCode = m.group(0);
                postalCode = postalCode.substring(postalCode.length() - 2);
                numberPostalCode = Integer.parseInt(postalCode);
                if (numberPostalCode == 1) {
                    postalCode = numberPostalCode + "er";
                } else {
                    postalCode = numberPostalCode + "ème";
                }

                Log.i("mab", "whichPostalCode: " + address);
                String endAddress = address.substring(address.indexOf(","), address.length());
                Log.i("mab", "fin : " + endAddress);

                address = address.substring(0, address.indexOf(endAddress)) + "\n" + postalCode;

            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        return address;
    }

    //reformatArrondissement Paris
    /**public static String reformatArrondissement(String address) {
        try {
            Pattern p = Pattern.compile("75[0-9][0-9][0-9]");
            Matcher m = p.matcher(address);
            String postalCode;

            if (m.find()) {
                postalCode = m.group(0) + " PARIS";
                Log.i("mab", "whichPostalCode: " + address);
                String endAddress = address.substring(address.indexOf(","), address.length());
                Log.i("mab", "fin : " + endAddress);
                address = address.substring(0, address.indexOf(endAddress)) + ", " + postalCode;
                Log.i("mab", "adress reformat : " + address);

            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().log(e.getMessage());
        }

        return address;
    }**/

    //Reformat Arrondissement Plaine Commune
    public static String reformatArrondissement(String address) {

        if ( address.indexOf(", France") > 0 ) {
            return address.substring(0, address.indexOf(", France"));
        } else {
            return address;
        }
    }

    public static Bitmap rotateBitmapOrientation(String photoFilePath) {
        // Create and configure BitmapFactory
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        Bitmap bm = BitmapFactory.decodeFile(photoFilePath, opts);
        // Read EXIF Data
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String orientString = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
        // Rotate Bitmap
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bm, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);
        // Return result
        return rotatedBitmap;
    }


    // Convert imageBase64 to Bitmap
    public static Bitmap base64ToBitmap(String imgB64, int size) {
        // Tips : supprimer le début de la chaîne pour rendre la conversion possible

        Log.i(TAG, "base64ToBitmap: " + imgB64);

        Bitmap decodedImage;

        try {
            imgB64 = imgB64.substring(imgB64.indexOf(",") + 1);
            byte[] imageBytes = Base64.decode(imgB64, Base64.DEFAULT);
            decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            decodedImage = BitmapScaler.scaleToFitWidth(decodedImage, size);
        } catch (Exception e) {
            imgB64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAMAAAADACAQAAAD41aSMAAADkklEQVR4Ae2XXUpbURSF15sRf6agMfhU7GAKER2PiDgCfwcUCCgdRLENedRCroXbPPTBtsmBZHNWtub71gDWXueTS9RMAAAAAAAAAAAAAAAAAAAAAAAAdnWqew01VqP2g6fRWEPd6UQ7SsGh7vVT7RrmRbfqaaVs6lKvatc4jS7U0Yro6ataokd15Uef9f3PAeRJRzLT04+/TkBBV0Y2//v4kAd1ZONyxgHkXCYOZ/7yIY0OZOF+zgHkRgZ25/7bRZ61reqcFg4gxx/+A8RHaFioJwNVZ1yoJyNVpynUk4mq05ZCEIAAQxBAEIAAggAEEAQggCAAAQQBCCAIQABBAAIMQQBBAAIIAhBAEIAAggAEEARUCQJep3H2xfur453/ZZqIAn9/dbzzJYUU+Pur454fU+Dvr45/fkSBv786/vkRBf7+6vjnRxT4+6vjnx9R4O+vzqt/fkCBv7860xP8899Rv/wKnPPz98uvwDs/e7/8Ctzzc/fLr8A/P3O//Ar88zP3y6/APz9zv/wK/PMz9yuNAsP8jP1KpMAwP1+/UikwzM/Wr2QKDPNz9SudAsP8TP1KqMAwP0+/EioIzO9P8776lVBBYP6vafrvql8JFQTmt2pDCvz9SqkgMD+qwN+fU0FgflSBvz+hgtj8qAJ/fzIF8fkxBf7+VApi8wMKDP1+Fn6C+PyAAkO/m0WfIDB/dpL1+4mfEJnfZuv3UzzBMD9Zv5/yCY75qfr9lE/wzE/U76d8gmt+mn4/5RN887P0uymcYJmfsN9L4QTL/JT9TgonWOYn7fdROMEyP22/i8IJlvmJ+00UTnDMT91voXiCYX7ufgPRE+Lz8/fXJHZCfH76/toEnqDC/IT9tQk8gWF+gv7aLPEEhvmJ+muzxBMY5ifqr80ST2CYn6i/Nks8gWF+ov7qLPgEhvm5+quz4BOY5/v7/QIIAhBAEIAAggAEEAQggCAAAQQBCCAIQABxCSAIQABBAAIIAhBAEIAAggAEEAQggFSnKZSTiaozLtSTkaozLNSTgapzV6gn16rOSaGe9FWdHb3MKSfP2pKB2zn15EoWempmlJOJ9mXiYkY9OZONjh7/KSdDbchIV09vysk37cnM0RsFPP8n+VFXD2qJhtrTiujoXM2a//I504ZWyoFu9Lym/3ZdaV8p2NaxbjTQSJM1+JsfaaBr9bWlGQAAAAAAAAAAAAAAAAAAAAAAAPwGa9lNiAvpJxgAAAAASUVORK5CYII=";
            imgB64 = imgB64.substring(imgB64.indexOf(",") + 1);
            byte[] imageBytes = Base64.decode(imgB64, Base64.DEFAULT);
            decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            decodedImage = BitmapScaler.scaleToFitWidth(decodedImage, size);
        }

        return decodedImage;
    }


    /**
     * Compares two version strings.
     * <p>
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     * The result is a positive integer if str1 is _numerically_ greater than str2.
     * The result is zero if the strings are _numerically_ equal.
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     */
    public static Integer versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        else {
            return Integer.signum(vals1.length - vals2.length);
        }
    }


    /**
     * Select the most coherent address in the list.
     * @param addresses
     *          List of address
     * @param cityList
     *          List of valid city name
     * @param searchBarMode
     *          true if use AutoComplete Address component
     * @param searchBarAddress
     *         Address type in search bar
     * @return the selected address
     */
    public static Address selectAddress(List<Address> addresses , String cityList, boolean searchBarMode, String searchBarAddress) {

        final String invalidRoadName = "Unnamed Road";

        List<Address> addressesFilter = new ArrayList<>();
        List<String> cityPlaineCommune = Arrays.asList(cityList.toUpperCase().split(","));
        for (Address addresse :addresses) {
            if(cityPlaineCommune.contains(addresse.getLocality().toUpperCase())){
                addressesFilter.add(addresse);
            }
        }

        int index = 0;
        int indexMax = addressesFilter.size() -1;

        while ( index <= indexMax) {

         boolean validAddress =  addressesFilter.get(index).getThoroughfare() != null && ! addressesFilter.get(index).getAddressLine(0).toUpperCase().contains(invalidRoadName.toUpperCase()) ;

         if(validAddress && searchBarMode && searchBarAddress != null) {
             validAddress = searchBarAddress.toUpperCase().contains( addressesFilter.get(index).getThoroughfare().toUpperCase());
             if (! validAddress) {
                 String[] tabThoroughfare = addressesFilter.get(index).getThoroughfare().toUpperCase().split(" ", 2);
                 validAddress = searchBarAddress.toUpperCase().contains( tabThoroughfare[tabThoroughfare.length-1].toUpperCase());
             }

         }

         if (validAddress) {
             return addressesFilter.get(index);
         }

         index ++;

        }
        if(addressesFilter.size() > 0) {
            return addressesFilter.get(indexMax);
        } else {
            return addresses.get(0);
        }
    }
}
