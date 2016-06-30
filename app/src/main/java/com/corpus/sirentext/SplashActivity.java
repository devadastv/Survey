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

import com.corpus.sirentext.sms.SendSMSActivity;
import com.corpus.sirentext.usermanagement.SaveSharedPreference;
import com.corpus.sirentext.usermanagement.UserProfileManager;

/**
 * Created by devadas.vijayan on 6/20/16.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.getListView();
                builder.setTitle(getString(R.string.not_connected_warning_title));
                builder.setMessage(getString(R.string.not_connected_warning_message));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.show();
            } else {
                UserProfileManager.getInstance().setCredentialStorage(new SaveSharedPreference(this));
                Intent intent;
                if (UserProfileManager.getInstance().isUserAlreadyLoggedIn())
                {
                    intent = new Intent(this, SummaryActivity.class);
                }
                else
                {
                    intent = new Intent(this, LoginActivity.class);
                }

                startActivity(intent);
                finish();
            }
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
