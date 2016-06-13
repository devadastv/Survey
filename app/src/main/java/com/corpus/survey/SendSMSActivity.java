package com.corpus.survey;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class SendSMSActivity extends AppCompatActivity {

    public static String SEND_SMS_SINGLE_TARGET = "sendSMSSingleTaget";
    public static String SEND_SMS_MULTIPLE_TARGETS = "sendSMSMultipleTagets";
    EditText mTagetNumbers;
    private int mMessageSentParts;
    private int mMessageSentTotalParts;
    private int mMessageSentCount;
    private String message;

    private final String SENT = "SMS_SENT";
    private final String DELIVERED = "SMS_DELIVERED";

    private String smsGatewayPref;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_sms);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        String targetMobileNumber = "";
        if (extras != null) {
            targetMobileNumber = extras.getString(SEND_SMS_SINGLE_TARGET);
            Log.d("SendSMS", "targetMobileNumber from SEND_SMS_SINGLE_TARGET = " + targetMobileNumber);
            if (null == targetMobileNumber) {
                targetMobileNumber = extras.getString(SEND_SMS_MULTIPLE_TARGETS);
                Log.d("SendSMS", "targetMobileNumber from SEND_SMS_MULTIPLE_TARGETS = " + targetMobileNumber);
            }
        }

        mTagetNumbers = (EditText) findViewById(R.id.numbers);
        mTagetNumbers.setText(targetMobileNumber);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        smsGatewayPref = sharedPref.getString(SettingsActivity.KEY_PREF_SMS_GATEWAY, "");
        Log.d("SendSMS", "Current SMS gateway pref = " + smsGatewayPref);

        Button mSendSMSButton = (Button) findViewById(R.id.send_sms_button);
        mSendSMSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSendMessages();
            }
        });
    }

    private void startSendMessages() {
        String text = mTagetNumbers.getText().toString();
        String [] numbers = text.split(",");
        trimNumbers(numbers);
        EditText mSMSText = (EditText) findViewById(R.id.sms_text);
        message = mSMSText.getText().toString();
        if (isAtLeastOneValidNumber(numbers)) {
            if (smsGatewayPref.equals(SettingsActivity.PREF_VALUE_SMS_GATEWAY_SIM)) {
                Log.d("SendSMS", "About to send SMSs using SIM balance");
                new SimSMSSendingTask(message).execute(numbers);
            } else if (smsGatewayPref.equals(SettingsActivity.PREF_VALUE_SMS_GATEWAY_ONLINE)) {
                Log.d("SendSMS", "About to send SMSs thru online gateway");
                new OnlineSMSSendingTask().execute(numbers);
            } else {
                Log.e("SendSMS", "Unknown smsGatewayPref value!");
            }

        } else {
            Toast.makeText(this, "At least one mobile number should be there to send SMS", Toast.LENGTH_LONG).show();
        }
    }

    private class SimSMSSendingTask extends AsyncTask<String, Integer, String> {

        private boolean isReceiversRegistered;
        private final String message;
        private String[] numbers;
        private Object lockObj;

        public SimSMSSendingTask(String message)
        {
            this.message = message;
            lockObj = new Object();
        }

        @Override
        protected void onPreExecute() {
            Log.d("SendSMS", "inside onPreExecute of SimSMSSendingTask. About to display the progress dialog");
            progressDialog = new ProgressDialog(SendSMSActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle(getResources().getString(R.string.sending));
            progressDialog.setMessage(getResources().getString(R.string.sending_wait_message));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    SendSMSActivity.SimSMSSendingTask.this.cancel(true);
//                    TextView empty = (TextView) findViewById(R.id.error_message_main_activity);
//                    empty.setText(getResources().getString(R.string.cancelled_loading_active_menu));
                }
            });
            progressDialog.show();
            registerBroadCastReceivers();
        }

        @Override
        protected String doInBackground(String... numbers) {
            this.numbers = numbers;
            mMessageSentCount = 0;
            int targetNumberCount = numbers.length;
            for (int i = 0; i < targetNumberCount; i++) {
                sendSMS(numbers[mMessageSentCount].toString(), message);
                synchronized (lockObj)
                {
                    try{
                        lockObj.wait(10000);
                    } catch (InterruptedException e) {
                        Log.e("SendSMS", "Caught InterruptedException on sending SMS via SIM", e);
                    }
                }
                publishProgress((i + 1) * 100/targetNumberCount);
            }
            SendSMSActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(SendSMSActivity.this, "All SMS have been sent", Toast.LENGTH_SHORT).show();
                }
            });
            return "DONE";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0].intValue());
        }

        @Override
        protected void onCancelled(String result) {
            // TODO: Implement it
        }

        protected void onPostExecute(String result) {
            unregisterBroadcastReceivers();
            progressDialog.dismiss();
        }


        private void sendSMS(final String phoneNumber, String message) {
            if (!isValidPhoneNumber(phoneNumber)) {
                SendSMSActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(SendSMSActivity.this, "SMS to invalid number failed: " + phoneNumber, Toast.LENGTH_SHORT).show();
                    }
                });
                Thread callbackSimulatorThread = new Thread() {
                    @Override
                    public void run() {
                        mMessageSentParts = 0;
                        mMessageSentCount++;
                        synchronized (lockObj)
                        {
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

            PendingIntent sentPI = PendingIntent.getBroadcast(SendSMSActivity.this, 0, new Intent(SENT), 0);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(SendSMSActivity.this, 0, new Intent(DELIVERED), 0);

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
                            Toast.makeText(getBaseContext(), "SMS sent to " + numbers[mMessageSentCount],
                                    Toast.LENGTH_SHORT).show();
                            Log.v("SendSMS", "Sent SMS " + mMessageSentCount + ", part " + mMessageSentParts + " of " + mMessageSentTotalParts);
                            mMessageSentCount++;
                            synchronized (lockObj)
                            {
                                lockObj.notify();
                            }
                        }
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
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
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        private void registerBroadCastReceivers() {
            if (!isReceiversRegistered) {
                Log.d("SendSMS", "registerBroadCastReceivers");
                registerReceiver(smsSentReceiver, new IntentFilter(SENT));
                registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
                isReceiversRegistered = true;
            }
        }

        private void unregisterBroadcastReceivers() {
            if (isReceiversRegistered) {
                Log.d("SendSMS", "unregisterBroadcastReceivers");
                unregisterReceiver(smsSentReceiver);
                unregisterReceiver(smsDeliveredReceiver);
                isReceiversRegistered = false;
            }
        }
    }

    private class OnlineSMSSendingTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            Log.d("SendSMS", "inside onPreExecute of OnlineSMSSendingTask. About to display the progress dialog");
            progressDialog = new ProgressDialog(SendSMSActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setTitle(getResources().getString(R.string.sending));
            progressDialog.setMessage(getResources().getString(R.string.sending_wait_message));
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    SendSMSActivity.OnlineSMSSendingTask.this.cancel(true);
                }
            });
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... targetNumbers) {
            int targetNumbersCount = targetNumbers.length;
            Log.d("SendSMS", "inside doInBackground of OnlineSMSSendingTask with targetNumbersCount = " + targetNumbersCount);
            for (int i = 0; i < targetNumbersCount; i++) {
                sendSMSviaOnlineGateway(targetNumbers[i]);
                publishProgress((i + 1) * 100 / targetNumbersCount);
            }
            return "SUCCESS";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            progressDialog.setProgress(progress[0].intValue());
        }

        @Override
        protected void onCancelled(String result) {
            // TODO: Implement it
        }

        protected void onPostExecute(String result) {
            progressDialog.dismiss();
        }

        private void sendSMSviaOnlineGateway(String mobileNumber) {
            // Replace with your username
            String user = "devadastv";

            // Replace with your API KEY (We have sent API KEY on activation email, also available on panel)
            String apikey = "dfK3GuD02aiwPs0lrHII";

            // Replace with the destination mobile Number to which you want to send sms
            String mobile = mobileNumber;

            // Replace if you have your own Sender ID, else donot change
            String senderid = "MYTEXT";

            // Replace with your Message content
            String message = SendSMSActivity.this.message;

            // For Plain Text, use "txt" ; for Unicode symbols or regional Languages like hindi/tamil/kannada use "uni"
            String type = "txt";

            //Prepare Url
            URLConnection myURLConnection = null;
            URL myURL = null;
            BufferedReader reader = null;

            //encoding message
            String encoded_message = URLEncoder.encode(message);

            //Send SMS API
            String mainUrl = "http://smshorizon.co.in/api/sendsms.php?";

            //Prepare parameter string
            StringBuilder sbPostData = new StringBuilder(mainUrl);
            sbPostData.append("user=" + user);
            sbPostData.append("&apikey=" + apikey);
            sbPostData.append("&message=" + encoded_message);
            sbPostData.append("&mobile=" + mobile);
            sbPostData.append("&senderid=" + senderid);
            sbPostData.append("&type=" + type);

            //final string
            mainUrl = sbPostData.toString();
            try {
                //prepare connection
                myURL = new URL(mainUrl);
                myURLConnection = myURL.openConnection();
                myURLConnection.connect();
                reader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
                //reading response
                String response;
                while ((response = reader.readLine()) != null)
                    //print response
                    Log.d("SendSMS", response);
                System.out.println(response);

                //finally close connection
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void trimNumbers(String[] numbers) {
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = numbers[i].trim();
        }
    }

    private boolean isAtLeastOneValidNumber(String[] numbers) {
        for (int i = 0; i < numbers.length; i++) {
            if (!TextUtils.isEmpty(numbers[i]) && TextUtils.isDigitsOnly(numbers[i])) {
                return true;
            }
        }
        return false;
    }


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



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
