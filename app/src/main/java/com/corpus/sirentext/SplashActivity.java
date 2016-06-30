package com.corpus.sirentext;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.corpus.sirentext.db.SurveySQLiteHelper;
import com.corpus.sirentext.sms.SendSMSActivity;
import com.corpus.sirentext.usermanagement.SaveSharedPreference;
import com.corpus.sirentext.usermanagement.UserProfileManager;

/**
 * Created by devadas.vijayan on 6/20/16.
 */
public class SplashActivity extends AppCompatActivity {

    private SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            initPredefinedMessages();
            initCustomerGroups();

            UserProfileManager.getInstance().setCredentialStorage(new SaveSharedPreference(this));
            Intent intent = null;
            if (UserProfileManager.getInstance().isUserAlreadyLoggedIn()) {
                intent = new Intent(this, SendSMSActivity.class);
            } else {
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected()) {
                    displayNetworkWarning();
                } else {
                    intent = new Intent(this, LoginActivity.class);
                }
            }
            if (null != intent) {
                Thread.sleep(700);
                startActivity(intent);
                finish();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initCustomerGroups() {
        if (dbHelper.getNumberOfCustomerGroups() == 0) {
            dbHelper.createNewCustomerGroup(getResources().getString(R.string.customer_group_sample_1));
            dbHelper.createNewCustomerGroup(getResources().getString(R.string.customer_group_sample_2));
            dbHelper.createNewCustomerGroup(getResources().getString(R.string.customer_group_sample_3));
        }
    }

    private void initPredefinedMessages() {
        if (dbHelper.getNumberOfPredefinedMessages() == 0) {
            dbHelper.createPredefinedMessage(getResources().getString(R.string.predefined_message_sample_1));
            dbHelper.createPredefinedMessage(getResources().getString(R.string.predefined_message_sample_2));
            dbHelper.createPredefinedMessage(getResources().getString(R.string.predefined_message_sample_3));
        }
    }

    private void displayNetworkWarning() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        builder.setCancelable(false);
        builder.setTitle(getString(R.string.not_connected_warning_title));
        builder.setMessage(getString(R.string.not_connected_warning_message));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.show();
    }


}
