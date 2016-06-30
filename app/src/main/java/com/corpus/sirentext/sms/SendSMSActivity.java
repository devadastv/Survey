package com.corpus.sirentext.sms;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.corpus.sirentext.CustomerListActivity;
import com.corpus.sirentext.CustomerManager;
import com.corpus.sirentext.LoginActivity;
import com.corpus.sirentext.NewCustomerActivity;
import com.corpus.sirentext.PredefinedMessagesActivity;
import com.corpus.sirentext.R;
import com.corpus.sirentext.SettingsActivity;
import com.corpus.sirentext.db.SurveySQLiteHelper;
import com.corpus.sirentext.usermanagement.UserProfileManager;

public class SendSMSActivity extends AppCompatActivity {

    public static String SEND_SMS_SINGLE_TARGET = "sendSMSSingleTaget";
    public static String SEND_SMS_MULTIPLE_TARGETS = "sendSMSMultipleTagets";


    private SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);
    EditText mTagetNumbers;
    private EditText mCustomerGroup;
    private int selectedCustomerGroupIndex = -1; // TODO: Can be used in future based on pref - to allow user to send messge to single or multiple groups???
    private boolean[] selectedCustomerGroupIndexArray;
    private EditText mMessageText;
    private TextView mMessageCharCount;

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

        mMessageText = (EditText) findViewById(R.id.sms_text);
        mMessageText.addTextChangedListener(mTextEditorWatcher);

        if (targetMobileNumber.trim().equals("")) {
            mTagetNumbers.requestFocus();
        } else {
            mMessageText.requestFocus();
        }
        mMessageCharCount = (TextView) findViewById(R.id.sms_char_count);
        updateCharacterCount(0);

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

        Button mPredefinedMessagesButton = (Button) findViewById(R.id.predefined_message);
        mPredefinedMessagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contentSummaryIntent = new Intent(SendSMSActivity.this, PredefinedMessagesActivity.class);
                startActivityForResult(contentSummaryIntent, PredefinedMessagesActivity.REQUEST_CODE_PICK_MESSAGE);
            }
        });

        Button mClearAllButton = (Button) findViewById(R.id.clear_all);
        mClearAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTagetNumbers.setText("");
                mMessageText.setText("");
                mCustomerGroup.setText("");
                for (int i = 0; i < selectedCustomerGroupIndexArray.length; i++) {
                    selectedCustomerGroupIndexArray[i] = false;
                }
                mTagetNumbers.requestFocus();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_sms, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.new_customer) {
            addNewCustomer();
            return true;
        } else if (id == R.id.customer_list) {
            launchCustomerList();
        } else if (id == R.id.clear_all_contacts) {
            displayClearCustomerListConfirmationDialog();
        } else if (id == R.id.action_settings) {
            Intent contentSummaryIntent = new Intent(this, SettingsActivity.class);
            startActivity(contentSummaryIntent);
        } else if (id == R.id.logout) {
            UserProfileManager.getInstance().resetCredentialsOnUserLogout();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(loginIntent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addNewCustomer() {
        Intent contentSummaryIntent = new Intent(this, NewCustomerActivity.class);
        startActivity(contentSummaryIntent);
    }

    private void launchCustomerList() {
        Intent intent = new Intent(this, CustomerListActivity.class);
        startActivity(intent);
    }

    private void displayClearCustomerListConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Warning: Deleting all customer contacts")
                .setMessage("Are you sure you want to delete all customer contacts created so far? This action can not be undone!")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteAllSurveyEntries();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void startSendMessages() {
        String text = mTagetNumbers.getText().toString();
        String[] numbers = text.split(",");
        trimNumbers(numbers);
        String message = mMessageText.getText().toString();
        if (isAtLeastOneValidNumber(numbers)) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String smsGatewayPref = sharedPref.getString(SettingsActivity.KEY_PREF_SMS_GATEWAY, "");
            Log.d("SendSMS", "Current SMS gateway pref = " + smsGatewayPref);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PredefinedMessagesActivity.REQUEST_CODE_PICK_MESSAGE) {
            if (resultCode == Activity.RESULT_OK) {
                int result = data.getIntExtra("messageIndex", -1);
                if (result != -1) {
                    String selectedMessage = dbHelper.getPredefinedMessage(result);
                    mMessageText.setText(selectedMessage);
                }
            }
        }
    }

    private void updateCharacterCount(int totalCharCount) {
        final int CHAR_COUNT_PER_SMS = 160;
        int smsCount = totalCharCount / CHAR_COUNT_PER_SMS + 1;
        int remainingCharCount = totalCharCount % CHAR_COUNT_PER_SMS;

        StringBuilder builder = new StringBuilder();
        builder.append(getResources().getString(R.string.characters));
        builder.append(remainingCharCount);
        builder.append("/");
        builder.append(smsCount);
        mMessageCharCount.setText(builder.toString());
    }

    private final TextWatcher mTextEditorWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            updateCharacterCount(s.length());
        }
    };
}
