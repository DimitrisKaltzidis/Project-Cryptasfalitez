package jk.dev.cryptomessaging.Utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;


public class Preferences {

    @SuppressLint("LongLogTag")
    static public void savePrefsString(String toBeSaved, String valueToBeSaved,
                                       Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString(toBeSaved, valueToBeSaved);
        edit.apply();
        Log.e("Execute Preference Command", "SAVE STRING PREFERENCE '" + toBeSaved + "' WITH VALUE '" + valueToBeSaved + "'");
    }

    @SuppressLint("LongLogTag")
    static public void savePrefsLong(String toBeSaved, Long valueToBeSaved,
                                    Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putLong(toBeSaved, valueToBeSaved);
        edit.apply();
        Log.e("Execute Preference Command", "SAVE LONG PREFERENCE '" + toBeSaved + "' WITH VALUE '" + valueToBeSaved + "'");
    }

    @SuppressLint("LongLogTag")
    static public void savePrefsInt(String toBeSaved, int valueToBeSaved,
                                    Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt(toBeSaved, valueToBeSaved);
        edit.apply();
        Log.e("Execute Preference Command", "SAVE INT PREFERENCE '" + toBeSaved + "' WITH VALUE '" + valueToBeSaved + "'");
    }

    @SuppressLint("LongLogTag")
    static public int loadPrefsInt(String name, int defaultValue, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        int value = sharedPreferences.getInt(name, defaultValue);
        Log.e("Execute Preference Command", "LOAD INT PREFERENCE '" + name + "' WITH VALUE '" + value + "'");
        return value;
    }

    @SuppressLint("LongLogTag")
    static public String loadPrefsString(String name, String defaultValue, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        String value = sharedPreferences.getString(name, defaultValue);
        Log.e("Execute Preference Command", "LOAD STRING PREFERENCE '" + name + "' WITH VALUE '" + value + "'");
        return value;
    }

    @SuppressLint("LongLogTag")
    static public Long loadPrefsLong(String name, Long defaultValue, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        Long value = sharedPreferences.getLong(name, defaultValue);
        Log.e("Execute Preference Command", "LOAD LONG PREFERENCE '" + name + "' WITH VALUE '" + value + "'");
        return value;
    }
}