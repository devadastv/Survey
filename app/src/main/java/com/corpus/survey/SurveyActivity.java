package com.corpus.survey;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.corpus.survey.com.corpus.survey.db.SurveySQLiteHelper;

public class SurveyActivity extends AppCompatActivity {

    SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);
    private EditText mSurveyPersonName;
    private EditText mMobileNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSurveyPersonName = (EditText) findViewById(R.id.survey_person_name);
        mMobileNumber = (EditText) findViewById(R.id.mobile_number);

        Button clickButton = (Button) findViewById(R.id.submit);
        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDataSubmit();
            }
        });
    }

    private void attemptDataSubmit()
    {
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
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(surveyPersonName.trim())) {
            mSurveyPersonName.setError("The name is empty");
            focusView = mSurveyPersonName;
            cancel = true;
        }

        // Check for a valid email address.
        if (!cancel && TextUtils.isEmpty(mobileNumber)  && !isPhoneNumberValid(mobileNumber)) {
            mMobileNumber.setError("Mobile number should contain at least 10 digits");
            focusView = mMobileNumber;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            Survey survey = new Survey(surveyPersonName, mobileNumber);
            dbHelper.createSurvey(survey);
            Toast.makeText(this, "This survey is successfully submitted. Thanks!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isPhoneNumberValid(String mobileNumber) {
        return mobileNumber.length() > 10;
    }

}
