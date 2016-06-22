package com.corpus.survey;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.corpus.survey.db.SurveySQLiteHelper;
import com.corpus.survey.sms.SendSMSActivity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SurveyDetailsActivity extends AppCompatActivity {

    SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        Bundle extras = getIntent().getExtras();
        int surveyItemIndex = 0;
        if (extras != null) {
            surveyItemIndex = extras.getInt(SurveyListActivity.SURVEY_ITEM_INDEX);
        }
        mViewPager.setCurrentItem(surveyItemIndex);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "SMS compose screen should be launched now", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent sendSMSIntent = new Intent(SurveyDetailsActivity.this, SendSMSActivity.class);
                Bundle extras = new Bundle();
                Survey currentSurvey = dbHelper.getSurvey(mViewPager.getCurrentItem() + 1);
                extras.putString(SendSMSActivity.SEND_SMS_SINGLE_TARGET, currentSurvey.getPhoneNumber());
                sendSMSIntent.putExtras(extras);
                startActivity(sendSMSIntent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_survey_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sendSMS) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_SURVEY_OBJECT = "survey_object";
        private static final String ARG_TOTAL_COUNT = "total_count";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, int totalCount, Survey survey) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putInt(ARG_TOTAL_COUNT, totalCount);
            args.putSerializable(ARG_SURVEY_OBJECT, survey);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_survey_details, container, false);

            Survey survey = (Survey) getArguments().getSerializable(ARG_SURVEY_OBJECT);
            int sectionNumber = (int)getArguments().getInt(ARG_SECTION_NUMBER);
            int totalCount = (int)getArguments().getInt(ARG_TOTAL_COUNT);

            TextView mSurveyEntryCount = (TextView) rootView.findViewById(R.id.survey_entry_count);
            TextView mUserName = (TextView) rootView.findViewById(R.id.user_name);
            TextView mPhoneNumber = (TextView) rootView.findViewById(R.id.phone_number);
            TextView mContactsGroup = (TextView) rootView.findViewById(R.id.details_contact_group);
            TextView mEmail = (TextView) rootView.findViewById(R.id.details_email_id);
            TextView mGender = (TextView) rootView.findViewById(R.id.details_gender);
            TextView mPlace = (TextView) rootView.findViewById(R.id.details_place);
            TextView mDateOfBirth = (TextView) rootView.findViewById(R.id.date_of_birth);
            TextView mDateOfShopping = (TextView) rootView.findViewById(R.id.date_of_shopping);

            mSurveyEntryCount.setText("Contact " + (sectionNumber + 1) + " of " + totalCount);
            mUserName.setText(survey.getUserName());
            mPhoneNumber.setText(survey.getPhoneNumber());
            mContactsGroup.setText(survey.getContactGroup());
            mEmail.setText(survey.getEmail());
            mGender.setText(survey.getGenderText());
            mPlace.setText(survey.getPlace());

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(survey.getDateOfBirth());
            mDateOfBirth.setText("Date of Birth: " + dateFormatter.format(calendar.getTime()));

            calendar.setTimeInMillis(survey.getCreatedDate());
            mDateOfShopping.setText("Date of shopping: " + dateFormatter.format(calendar.getTime()));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Survey currentSurvey = dbHelper.getSurvey(position + 1);
            return PlaceholderFragment.newInstance(position, getCount(), currentSurvey);
        }

        @Override
        public int getCount() {
            return dbHelper.getNumberOfSurveyEntries();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Survey Entry : " + (position + 1);
        }
    }
}
