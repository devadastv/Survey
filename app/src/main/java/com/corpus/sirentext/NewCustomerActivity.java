package com.corpus.sirentext;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.corpus.sirentext.db.SurveySQLiteHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewCustomerActivity extends AppCompatActivity {

    SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);
    private EditText mSurveyPersonName;
    private EditText mMobileNumber;
    private int gender;
    private EditText mDateOfBirth;
    private long dateOfBirthMillis;
    private int selectedCustomerGroupIndex = -1;
    private EditText mCustomerGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSurveyPersonName = (EditText) findViewById(R.id.survey_person_name);
        mSurveyPersonName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mMobileNumber = (EditText) findViewById(R.id.mobile_number);

        mDateOfBirth = (EditText) findViewById(R.id.date_of_birth);
        mDateOfBirth.setInputType(InputType.TYPE_NULL);
        mDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });

        mCustomerGroup = (EditText) findViewById(R.id.customer_group);
        mCustomerGroup.setInputType(InputType.TYPE_NULL);
        mCustomerGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(NewCustomerActivity.this);
                builder.setTitle("Customer Group:");
                builder.setCancelable(true);
                AlertDialog dialog = builder.create();
                dialog.getListView();
                builder.setSingleChoiceItems(dbHelper.getCustomerGroupsArray(), selectedCustomerGroupIndex, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("SurveyList", "User selected " + which);
                        selectedCustomerGroupIndex = which;
                        String selectedCustomerGroup = dbHelper.getCustomerGroup(which + 1);
                        if (null != selectedCustomerGroup) {
                            mCustomerGroup.setText(selectedCustomerGroup);
                        }
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        Button clickButton = (Button) findViewById(R.id.submit);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDataSubmit();
            }
        });
    }

    private void attemptDataSubmit() {
        // Reset errors.
        mSurveyPersonName.setError(null);
        mMobileNumber.setError(null);
        mCustomerGroup.setError(null);

        // Store values at the time of the login attempt.
        String surveyPersonName = mSurveyPersonName.getText().toString();
        String mobileNumber = mMobileNumber.getText().toString();
        String customerGroup = mCustomerGroup.getText().toString();

        boolean cancel = false;
        View focusView = null;
        Log.d("DEBUG", "surveyPersonName = " + surveyPersonName + " surveyPersonName.length = " + surveyPersonName.length()
                + " , surveyPersonName.trim.length = " + surveyPersonName.trim().length() + " TextUtils.isEmpty(surveyPersonName.trim()) = " + TextUtils.isEmpty(surveyPersonName.trim()));
        // Check for a valid name, if the user entered one.
        if (TextUtils.isEmpty(surveyPersonName.trim())) {
            mSurveyPersonName.setError("The name is mandatory");
            focusView = mSurveyPersonName;
            cancel = true;
        }

        // Check for a non-empty mobile number.
        if (!cancel && TextUtils.isEmpty(mobileNumber)) {
            mMobileNumber.setError("Mobile number is mandatory");
            focusView = mMobileNumber;
            cancel = true;
        }

        if (!cancel && !isPhoneNumberValidNumber(mobileNumber)) {
            mMobileNumber.setError("Mobile number should be only digits");
            focusView = mMobileNumber;
            cancel = true;
        }

        if (!cancel && mobileNumber.length() < 10) {
            mMobileNumber.setError("Mobile number should have 10 digits");
            focusView = mMobileNumber;
            cancel = true;
        }

        if (!cancel && mobileNumber.length() > 10) {
            mMobileNumber.setError("Mobile number should have only 10 digits. Do not prefix country code or 0");
            focusView = mMobileNumber;
            cancel = true;
        }

        if (!cancel && TextUtils.isEmpty(customerGroup.trim())) {
            mCustomerGroup.setError("Customer Group is mandatory");
            focusView = mCustomerGroup;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt submit and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            Customer customer = new Customer(surveyPersonName, mobileNumber, gender, System.currentTimeMillis(), selectedCustomerGroupIndex);

            // Set optional fields
            // Email ID
            EditText mEmail = (EditText) findViewById(R.id.customer_email);
            if (mEmail.length() > 0) {
                customer.setEmail(mEmail.getText().toString());
            }

            // Place
            EditText mSurveyPlace = (EditText) findViewById(R.id.place);
            if (mSurveyPlace.length() > 0) {
                customer.setPlace(mSurveyPlace.getText().toString());
            }

            // Date of Birth
            customer.setDateOfBirth(dateOfBirthMillis);

            dbHelper.createSurvey(customer);
            Toast.makeText(this, "This contact is saved.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean isPhoneNumberValidNumber(String mobileNumber) {
        try {
            Long.parseLong(mobileNumber);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

//    public void onRadioButtonClicked(View view) {
//        boolean checked = ((RadioButton) view).isChecked();
//        switch (view.getId()) {
//            case R.id.radio_male:
//                if (checked)
//                    gender = com.corpus.sirentext.Customer.GENDER_MALE;
//                break;
//            case R.id.radio_female:
//                if (checked)
//                    gender = com.corpus.sirentext.Customer.GENDER_FEMALE;
//                break;
//            case R.id.radio_other:
//                if (checked)
//                    gender = com.corpus.sirentext.Customer.GENDER_OTHER;
//                break;
//        }
//        hideKeyboard();
//        view.requestFocus();
//    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (null != inputManager && null != this.getCurrentFocus())
        {
            inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker if there is no date already set in DatePicker
            final Calendar c = Calendar.getInstance();
            NewCustomerActivity activity = (NewCustomerActivity) getActivity();
            if (activity.dateOfBirthMillis >= 1) {
                c.setTimeInMillis(activity.dateOfBirthMillis);
            }
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, day);
            NewCustomerActivity activity = (NewCustomerActivity) getActivity();
            activity.mDateOfBirth.setText(dateFormatter.format(newDate.getTime()));
            activity.dateOfBirthMillis = newDate.getTimeInMillis();
        }
    }
}
