package com.corpus.survey;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

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

    public String[] getCurrentCustomerGroupArray(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String customerGroupString = sharedPref.getString(SettingsActivity.KEY_PREF_CUSTOMER_GROUP, "");
        String[] customerGroupArray = customerGroupString.split(CUSTOMER_GROUP_DELIMITER);
        return customerGroupArray;
    }

    public boolean addCustomerGroup(String newCustomerGroup, Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String customerGroupString = sharedPref.getString(SettingsActivity.KEY_PREF_CUSTOMER_GROUP, "");

        Log.d("CM", "customerGroupString = " + customerGroupString);
        List currentCustomerGroupList = new ArrayList(); //(ArrayList) Arrays.asList(customerGroupString.split(CUSTOMER_GROUP_DELIMITER));
        currentCustomerGroupList.addAll(Arrays.asList(customerGroupString.split(CUSTOMER_GROUP_DELIMITER)));
        currentCustomerGroupList.add(newCustomerGroup);

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

        Log.d("CM", "customerGroupString = " + customerGroupString);
        List currentCustomerGroupList = new ArrayList(); //(ArrayList) Arrays.asList(customerGroupString.split(CUSTOMER_GROUP_DELIMITER));
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

}
