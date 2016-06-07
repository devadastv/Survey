package com.corpus.survey;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class SendSMSActivity extends AppCompatActivity {

    public static String SEND_SMS_SINGLE_TARGET = "sendSMSSingleTaget";
    public static String SEND_SMS_MULTIPLE_TARGETS = "sendSMSMultipleTagets";
    EditText mTagetNumbers;
    private int mMessageSentParts;
    private int mMessageSentTotalParts;
    private int mMessageSentCount;
    private String message;
    private String[] numbers;

    private final String SENT = "SMS_SENT";
    private final String DELIVERED = "SMS_DELIVERED";
    private boolean isReceiversRegistered;


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
        unregisterBroadcastReceivers();
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
        numbers = text.split(",");
        trimNumbers(numbers);
        EditText mSMSText = (EditText) findViewById(R.id.sms_text);
        message = mSMSText.getText().toString();
        if (isAtLeastOneValidNumber(numbers)) {
            registerBroadCastReceivers();
            mMessageSentCount = 0;
            sendSMS(numbers[mMessageSentCount].toString(), message);
        } else {
            Toast.makeText(this, "At least one mobile number should be there to send SMS", Toast.LENGTH_LONG).show();
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

    private void sendNextMessage() {
        if (thereAreSmsToSend()) {
            sendSMS(numbers[mMessageSentCount].toString(), message);
        } else {
            this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(SendSMSActivity.this, "All SMS have been sent", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean thereAreSmsToSend() {
        return mMessageSentCount < numbers.length;
    }


    private void sendSMS(final String phoneNumber, String message) {
        if (!isValidPhoneNumber(phoneNumber)) {
            this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(SendSMSActivity.this, "SMS to invalid number failed: " + phoneNumber, Toast.LENGTH_SHORT).show();
                }
            });
            Thread callbackSimulatorThread = new Thread() {
                @Override
                public void run() {
                    mMessageSentCount++;
                    sendNextMessage();
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

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        for (int j = 0; j < mMessageSentTotalParts; j++) {
            sentIntents.add(sentPI);
            deliveryIntents.add(deliveredPI);
        }

        mMessageSentParts = 0;
        sms.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, deliveryIntents);
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
                        sendNextMessage();
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
            registerReceiver(smsSentReceiver, new IntentFilter(SENT));
            registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
            isReceiversRegistered = true;
        }
    }

    private void unregisterBroadcastReceivers() {
        if (isReceiversRegistered) {
            unregisterReceiver(smsSentReceiver);
            unregisterReceiver(smsDeliveredReceiver);
            isReceiversRegistered = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcastReceivers();
    }
}
