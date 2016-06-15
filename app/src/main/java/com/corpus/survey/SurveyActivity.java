package com.corpus.survey;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.corpus.survey.db.SurveySQLiteHelper;

public class SurveyActivity extends AppCompatActivity {

    SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);
    private EditText mSurveyPersonName;
    private EditText mMobileNumber;
    private int gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSurveyPersonName = (EditText) findViewById(R.id.survey_person_name);
        mSurveyPersonName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mMobileNumber = (EditText) findViewById(R.id.mobile_number);

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

        // Store values at the time of the login attempt.
        String surveyPersonName = mSurveyPersonName.getText().toString();
        String mobileNumber = mMobileNumber.getText().toString();

        boolean cancel = false;
        View focusView = null;
        Log.d("DEBUG", "surveyPersonName = " + surveyPersonName + " surveyPersonName.length = " + surveyPersonName.length()
                + " , surveyPersonName.trim.length = " + surveyPersonName.trim().length() + " TextUtils.isEmpty(surveyPersonName.trim()) = " + TextUtils.isEmpty(surveyPersonName.trim()));
        // Check for a valid name, if the user entered one.
        if (TextUtils.isEmpty(surveyPersonName.trim())) {
            mSurveyPersonName.setError("The name is empty");
            focusView = mSurveyPersonName;
            cancel = true;
        }

        // Check for a valid mobile number.
        if (!cancel && TextUtils.isEmpty(mobileNumber) && !isPhoneNumberValid(mobileNumber)) {
            mMobileNumber.setError("Mobile number should contain at least 10 digits");
            focusView = mMobileNumber;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt submit and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            Survey survey = new Survey(surveyPersonName, mobileNumber, gender, System.currentTimeMillis(), null);
            dbHelper.createSurvey(survey);
            Toast.makeText(this, "This survey is successfully submitted. Thanks!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean isPhoneNumberValid(String mobileNumber) {
        return mobileNumber.length() > 10;
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radio_male:
                if (checked)
                    gender = Survey.GENDER_MALE;
                break;
            case R.id.radio_female:
                if (checked)
                    gender = Survey.GENDER_FEMALE;
                break;
            case R.id.radio_other:
                if (checked)
                    gender = Survey.GENDER_OTHER;
                break;
        }
        hideKeyboard();
        view.requestFocus();
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
