package com.corpus.sirentext.sms;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.corpus.sirentext.SettingsActivity;

/**
 * Created by devadas.vijayan on 6/13/16.
 */
public class SmsSendingTaskFactory {

    public static BaseSmsSendingTask getSmsSendingTask(String prefValue, String message, Activity activity)
    {
        BaseSmsSendingTask asyncTask;
        switch (prefValue)
        {
            case SettingsActivity.PREF_VALUE_SMS_GATEWAY_SIM:
                asyncTask = new SimSmsSendingTask(message, activity);
                break;
            case SettingsActivity.PREF_VALUE_SMS_GATEWAY_ONLINE:
                asyncTask = new OnlineSMSSendingTask(message, activity);
                break;
            default:
                Log.e("SendSMS", "Unknown smsGatewayPref value!");
                Toast.makeText(activity, "App Error: To solve, please set the SMS Gateway preference in Settings. Now using SIM balance to send SMS", Toast.LENGTH_LONG).show();
                asyncTask = new SimSmsSendingTask(message, activity);
        }
        return asyncTask;
    }
}
