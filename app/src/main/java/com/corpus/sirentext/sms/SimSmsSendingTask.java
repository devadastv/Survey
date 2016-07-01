package com.corpus.sirentext.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.corpus.sirentext.NewCustomerActivity;

import java.util.ArrayList;

/**
 * Created by devadas.vijayan on 6/13/16.
 */
class SimSmsSendingTask extends BaseSmsSendingTask {

    private static final String TAG = "BaseSmsSendingTask";
    private final String SENT = "SMS_SENT";
    private final String DELIVERED = "SMS_DELIVERED";

    private boolean isReceiversRegistered;
    private Object lockObj;
    private int mMessageSentCount;
    private int mMessageSentTotalParts;
    private int mMessageSentParts;

    public SimSmsSendingTask(String message, Activity activity) {
        super(message, activity);
    }

    @Override
    protected void onPreExecute() {
        Log.d("SendSMS", "inside onPreExecute of SimSMSSendingTask. About to display the progress dialog");
        super.onPreExecute();
        lockObj = new Object();
        registerBroadCastReceivers();
    }

    @Override
    String performBackgroundTask() {
        mMessageSentCount = 0;
        int targetNumberCount = numbers.length;
        for (int i = 0; i < targetNumberCount; i++) {
            sendSMS(numbers[mMessageSentCount].toString(), message);
            synchronized (lockObj) {
                try {
                    lockObj.wait(10000);
                } catch (InterruptedException e) {
                    Log.e("SendSMS", "Caught InterruptedException on sending SMS via SIM", e);
                }
            }
            publishProgress((i + 1) * 100 / targetNumberCount);
        }
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, "All SMS have been sent", Toast.LENGTH_SHORT).show();
            }
        });
        return "DONE";
    }


    protected void onPostExecute(String result) {
        unregisterBroadcastReceivers();
//        progressDialog.dismiss();
        if (activity instanceof NewCustomerActivity)
        {
            activity.finish();
        }
        activity = null;
    }


    private void sendSMS(final String phoneNumber, String message) {
        if (!isValidPhoneNumber(phoneNumber)) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(activity, "SMS to invalid number failed: " + phoneNumber, Toast.LENGTH_SHORT).show();
                }
            });
            Thread callbackSimulatorThread = new Thread() {
                @Override
                public void run() {
                    mMessageSentParts = 0;
                    mMessageSentCount++;
                    synchronized (lockObj) {
                        lockObj.notify();
                    }
                }
            };
            callbackSimulatorThread.start();
            return;
        }
        SmsManager sms = SmsManager.getDefault();
        ArrayList<String> parts = sms.divideMessage(message);
        mMessageSentTotalParts = parts.size();

        Log.i("Message Count", "Message Count: " + mMessageSentTotalParts);

        ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();

        PendingIntent sentPI = PendingIntent.getBroadcast(activity, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(activity, 0, new Intent(DELIVERED), 0);

        for (int j = 0; j < mMessageSentTotalParts; j++) {
            sentIntents.add(sentPI);
            deliveryIntents.add(deliveredPI);
        }
        mMessageSentParts = 0;
        sms.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveryIntents);
    }


    private BroadcastReceiver smsSentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    mMessageSentParts++;
                    if (mMessageSentParts == mMessageSentTotalParts) {
                        Toast.makeText(activity, "SMS sent to " + numbers[mMessageSentCount], Toast.LENGTH_SHORT).show();
                        Log.v("SendSMS", "Sent SMS " + mMessageSentCount + ", part " + mMessageSentParts + " of " + mMessageSentTotalParts);
                        mMessageSentCount++;
                        synchronized (lockObj) {
                            lockObj.notify();
                        }
                    }
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(activity, "Generic failure",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(activity, "No service",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(activity, "Null PDU",
                            Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(activity, "Radio off",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private BroadcastReceiver smsDeliveredReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {

                case Activity.RESULT_OK:
                    Toast.makeText(activity, "SMS delivered",
                            Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(activity, "SMS not delivered",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private boolean isValidPhoneNumber(String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber) && isNumber(phoneNumber.trim())) {
            if (phoneNumber.startsWith("0") && phoneNumber.length() == 11) {
                Log.d("SendSMS", "isValidPhoneNumber with " + phoneNumber + ": starts with 0 and has length 11 and hence treated as valid");
                return true;
            } else if (!phoneNumber.startsWith("0") && phoneNumber.length() == 10) {
                Log.d("SendSMS", "isValidPhoneNumber with " + phoneNumber + ": doesn't starts with 0 and has length 10 and hence treated as valid");
                return true;
            }
            Log.d("SendSMS", "isValidPhoneNumber with " + phoneNumber + ": is not empty but doesn't satisfy the validation filters. Hence not valid");
        } else {
            Log.d("SendSMS", "isValidPhoneNumber with " + phoneNumber + ": is empty or is containing characters. Hence not valid");
        }
        return false;
    }

    public static boolean isNumber(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }

    private void registerBroadCastReceivers() {
        Log.d(TAG, "Inside registerBroadCastReceivers with isReceiversRegistered = " + isReceiversRegistered);
        if (!isReceiversRegistered) {
            activity.registerReceiver(smsSentReceiver, new IntentFilter(SENT));
            activity.registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
            isReceiversRegistered = true;
        }
    }

    private void unregisterBroadcastReceivers() {
        Log.d(TAG, "Inside unregisterBroadcastReceivers with isReceiversRegistered = " + isReceiversRegistered);
        if (isReceiversRegistered) {
            activity.unregisterReceiver(smsSentReceiver);
            activity.unregisterReceiver(smsDeliveredReceiver);
            isReceiversRegistered = false;
        }
    }
}
