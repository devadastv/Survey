package com.corpus.sirentext.usermanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by devadas.vijayan on 6/29/16.
 */
public class SaveSharedPreference {
    private static final String TAG = "LoginSavePref";

    private static final String PREF_KEY_USER_NAME = "username";
    private static final String PREF_KEY_PASSWORD = "password";
    private static final String PREF_KEY_LOGIN_STATUS = "login_status";

    private static final String PREF_VALUE_NOT_LOGGED_IN = "0";
    private static final String PREF_VALUE_LOGIN_VALID = "1";

    private SharedPreferences.Editor edit;
    private SharedPreferences shared;

    public SaveSharedPreference(Context context) {
        this.shared = PreferenceManager.getDefaultSharedPreferences(context);
        this.edit = this.shared.edit();
    }

    void saveValidCredentials(String userName, String password) {
        this.edit.putString(PREF_KEY_USER_NAME, userName);
        this.edit.putString(PREF_KEY_PASSWORD, password);
        this.edit.putString(PREF_KEY_LOGIN_STATUS, PREF_VALUE_LOGIN_VALID);
        Log.d(TAG, "Updating the login status to persistence");
        this.edit.apply();
    }

    boolean isUserAlreadyLoggedIn() {
        String loginStatus = shared.getString(PREF_KEY_LOGIN_STATUS, "");
        Log.d(TAG, "loginStatus from preference = " + loginStatus);
        return loginStatus.equals(PREF_VALUE_LOGIN_VALID);
    }

    void resetCredentialsOnUserLogout() {
        this.edit.putString(PREF_KEY_USER_NAME, "");
        this.edit.putString(PREF_KEY_PASSWORD, "");
        this.edit.putString(PREF_KEY_LOGIN_STATUS, PREF_VALUE_NOT_LOGGED_IN);
        this.edit.apply();
    }
}
