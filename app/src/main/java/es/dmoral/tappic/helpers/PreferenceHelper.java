package es.dmoral.tappic.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Daniel Morales (GrenderG)
 * on 26/03/15.
 * All rights reserved. For more info:
 * grenderg@gmail.com
 */
public class PreferenceHelper {

    public static final String PREFERENCES = "preferences";
    public static final String PREF_FIRST_BOOT = "preferences.first_boot";

    private static SharedPreferences prefs;

    private PreferenceHelper() {

    }

    public static void initializePrefs(Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(
                    PREFERENCES,
                    Context.MODE_PRIVATE);
        }

    }


    public static void initializePrefs(Context context, boolean forceReinitialising) {
        if (prefs == null || forceReinitialising) {
            prefs = context.getSharedPreferences(
                    PREFERENCES,
                    Context.MODE_PRIVATE);
        }

    }

    public static boolean isPrefsInitialized() {
        return (prefs != null);
    }

    public static String read(String what) {

        return prefs.getString(what, "");
    }

    public static void write(String where, String what) {
        prefs.edit().putString(where, what).apply();
    }

    public static Integer readInteger(String what) {
        return prefs.getInt(what, 0);
    }

    public static Integer readInteger(String what, int defaultInt) {
        return prefs.getInt(what, defaultInt);
    }

    public static void writeInteger(String where, Integer what) {
        prefs.edit().putInt(where, what).apply();
    }

    public static Long readLong(String what, long defaultLong) {
        return prefs.getLong(what, defaultLong);
    }

    public static void writeLong(String where, Long what) {
        prefs.edit().putLong(where, what).apply();
    }

    public static void writeFloat(String where, float what) {
        prefs.edit().putFloat(where, what).apply();
    }

    public static float readFloat(String what, float defaultFloat) {
        return prefs.getFloat(what, defaultFloat);
    }

    public static boolean readBoolean(String what) {
        return prefs.getBoolean(what, true);
    }

    public static boolean readBoolean(String what, boolean defaultState) {
        return prefs.getBoolean(what, defaultState);
    }

    public static void writeBoolean(String where, boolean what) {
        prefs.edit().putBoolean(where, what).apply();
    }

    public static ArrayList<Integer> readIntegerArrayList(String what) {
        String savedString = prefs.getString(what, "");
        List<String> st = Arrays.asList(savedString.split("\\s*,\\s*"));
        ArrayList<Integer> toReturn = new ArrayList<Integer>();
        for (int i = 0; i < st.size(); i++) {
            try {
                toReturn.add(Integer.parseInt(st.get(i)));
            } catch (Exception ignored) {
            }
        }
        return toReturn;
    }

    public static void writeIntegerArrayList(String where, ArrayList<Integer> what) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < what.size(); i++) {
            str.append(what.get(i)).append(",");
        }
        prefs.edit().putString(where, str.toString()).apply();
    }

    public static void removePreferences() {
        prefs.edit().clear().apply();
    }

}

