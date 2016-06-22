package com.corpus.survey;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by devadas.vijayan on 6/22/16.
 */
public class CustomerManager {
    private static CustomerManager instance;

    public String CUSTOMER_GROUP_DELIMITER = "_";

    public static CustomerManager getInstance() {
        if (null == instance) {
            instance = new CustomerManager();
        }
        return instance;
    }

    public String[] getCustomerGroupArray(Context context) {
        String customerGroupString = getCustomerGroupInPersistence(context);
        return customerGroupString.split(CUSTOMER_GROUP_DELIMITER);
    }

    public String getCustomerGroupAtIndex(int index, Context context) {
        String[] customerGroupArray = getCustomerGroupArray(context);
        if (index <= customerGroupArray.length) {
            return customerGroupArray[index];
        } else {
            return null;
        }
    }

    public boolean addCustomerGroup(String newCustomerGroup, final Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String customerGroupString = sharedPref.getString(SettingsActivity.KEY_PREF_CUSTOMER_GROUP, "");

        Log.d("CM", "customerGroupString = " + customerGroupString);
        List currentCustomerGroupList = new ArrayList(); //(ArrayList) Arrays.asList(customerGroupString.split(CUSTOMER_GROUP_DELIMITER));
        currentCustomerGroupList.addAll(Arrays.asList(customerGroupString.split(CUSTOMER_GROUP_DELIMITER)));
        if (!currentCustomerGroupList.contains(newCustomerGroup)) {
            currentCustomerGroupList.add(newCustomerGroup);
        } else {
            Log.e("CM", "The new customer group to be added already exist there in the list. Ignoring the request.");
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, "The customer group to be added already exist in the list. Ignoring the request!", Toast.LENGTH_LONG).show();
                }
            });
            return false;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentCustomerGroupList.size(); i++) {
            sb.append(currentCustomerGroupList.get(i)).append(CUSTOMER_GROUP_DELIMITER);
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        Log.d("CM", "Updated customerGroupString = " + sb.toString());
        editor.putString(SettingsActivity.KEY_PREF_CUSTOMER_GROUP, sb.toString());
        boolean result = editor.commit();
        return result;
    }

    //TODO: Take care of updating the SQL database with moving all contacts in the removed group to
    // default group. Also update the group index.
    public boolean removeCustomerGroup(String customerGroupToRemove, Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String customerGroupString = sharedPref.getString(SettingsActivity.KEY_PREF_CUSTOMER_GROUP, "");
        List currentCustomerGroupList = new ArrayList();
        currentCustomerGroupList.addAll(Arrays.asList(customerGroupString.split(CUSTOMER_GROUP_DELIMITER)));
        if (currentCustomerGroupList.contains(customerGroupToRemove)) {
            currentCustomerGroupList.remove(customerGroupToRemove);
        } else {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < currentCustomerGroupList.size(); i++) {
            sb.append(currentCustomerGroupList.get(i)).append(CUSTOMER_GROUP_DELIMITER);
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        Log.d("CM", "Updated customerGroupString = " + sb.toString());
        editor.putString(SettingsActivity.KEY_PREF_CUSTOMER_GROUP, sb.toString());
        boolean result = editor.commit();
        return result;
    }

    private String getCustomerGroupInPersistence(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String customerGroupString = sharedPref.getString(SettingsActivity.KEY_PREF_CUSTOMER_GROUP, "");
        Log.d("CM", "customerGroupString = " + customerGroupString);
        return customerGroupString;
    }
}
