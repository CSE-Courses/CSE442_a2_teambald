package com.teambald.cse442_project_team_bald.Encryption;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefUtils {
    private static final String SECRET_KEY = "QGJEI3WithUbmdSHA1";

    public static final PrefUtils prefUtils = new PrefUtils();
    public static SharedPreferences myPrefs = null;

    public static PrefUtils getInstance(Context context) {
        if (null == myPrefs)
            myPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefUtils;
    }

    public void saveSecretKey(String value) {
        SharedPreferences.Editor editor = myPrefs.edit();
        editor.putString(SECRET_KEY, value);
        editor.commit();
    }

    public String getSecretKey() {
        return myPrefs.getString(SECRET_KEY, null);
    }
}
