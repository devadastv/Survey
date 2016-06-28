package com.corpus.survey.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.corpus.survey.CustomerManager;
import com.corpus.survey.PredefinedMessagesActivity;
import com.corpus.survey.R;
import com.corpus.survey.SettingsActivity;
import com.corpus.survey.SummaryActivity;
import com.corpus.survey.SurveyActivity;

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
    private String smsGatewayPref;
    private EditText mCustomerGroup;
    private int selectedCustomerGroupIndex = -1; // TODO: Can be used in future based on pref - to allow user to send messge to single or multiple groups???
    private boolean[] selectedCustomerGroupIndexArray;


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

        mCustomerGroup = (EditText) findViewById(R.id.select_customer_group);
        mCustomerGroup.setInputType(InputType.TYPE_NULL);
        final String[] customerGroupArray = CustomerManager.getInstance().getCustomerGroupArray(SendSMSActivity.this);
        selectedCustomerGroupIndexArray = new boolean[customerGroupArray.length];
        mCustomerGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SendSMSActivity.this);
                builder.setTitle("Customer Group:");
                builder.setCancelable(true);
                AlertDialog dialog = builder.create();
                dialog.getListView();


                builder.setMultiChoiceItems(customerGroupArray, selectedCustomerGroupIndexArray, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedCustomerGroupIndexArray[which] = true;
                        } else {
                            selectedCustomerGroupIndexArray[which] = false;
                        }
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < selectedCustomerGroupIndexArray.length; i++) {
                            if (selectedCustomerGroupIndexArray[i]) {
                                String selectedCustomerGroup = CustomerManager.getInstance().getCustomerGroupAtIndex(i, SendSMSActivity.this);
                                if (null != selectedCustomerGroup) {
                                    builder.append(", ");
                                    builder.append(selectedCustomerGroup);
                                }
                            }
                        }
                        if (builder.length() > 1) {
                            builder.delete(0, 1);
                        }
                        mCustomerGroup.setText(builder.toString());
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        smsGatewayPref = sharedPref.getString(SettingsActivity.KEY_PREF_SMS_GATEWAY, "");
        Log.d("SendSMS", "Current SMS gateway pref = " + smsGatewayPref);

        Button mPredefinedMessagesButton = (Button) findViewById(R.id.predefined_message);
        mPredefinedMessagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contentSummaryIntent = new Intent(SendSMSActivity.this, PredefinedMessagesActivity.class);
//                contentSummaryIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(contentSummaryIntent);
            }
        });

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
        String[] numbers = text.split(",");
        trimNumbers(numbers);
        EditText mSMSText = (EditText) findViewById(R.id.sms_text);
        String message = mSMSText.getText().toString();
        if (isAtLeastOneValidNumber(numbers)) {
            BaseSmsSendingTask smsSendingTask = SmsSendingTaskFactory.getSmsSendingTask(smsGatewayPref, message, this);
            smsSendingTask.execute(numbers);
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
}
