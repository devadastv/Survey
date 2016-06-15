package com.corpus.survey;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.corpus.survey.db.SurveySQLiteHelper;
import com.corpus.survey.sms.SendSMSActivity;

public class SurveyListActivity extends AppCompatActivity {

    SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);

    public static final String SURVEY_ITEM_INDEX = "survey_item_index";
    private final int FILTER_ALL_SURVEYS = 1;
    private final int FILTER_SURVEYS_LAST_DAY = 2;

    private int currentFilter = FILTER_ALL_SURVEYS;

    // This is the Adapter being used to display the list's data
    private SimpleCursorAdapter mAdapter;
    private Cursor currentFilteredCursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentFilter = FILTER_ALL_SURVEYS;
        setContentView(R.layout.activity_survey_list);
        ListView mSurveyList = (ListView) findViewById(R.id.survey_list);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {SurveySQLiteHelper.SURVEY_COLUMN_NAME, SurveySQLiteHelper.SURVEY_COLUMN_PHONE};
        int[] toViews = {android.R.id.text1, android.R.id.text2}; // The TextView in simple_list_item_1
        updateCurrentFilterCursor();
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, currentFilteredCursor,
                fromColumns, toViews, 0);

        mSurveyList.setAdapter(mAdapter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mSurveyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent contentDetailsIntent = new Intent(SurveyListActivity.this, SurveyDetailsActivity.class);
                Bundle extras = new Bundle();
                extras.putInt(SURVEY_ITEM_INDEX, position);
                contentDetailsIntent.putExtras(extras);
                startActivity(contentDetailsIntent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentFilterCursor();
    }

    private void updateCurrentFilterCursor() {

        switch (currentFilter) {
            case FILTER_ALL_SURVEYS:
                currentFilteredCursor =  dbHelper.getAllSurveyList();
            case FILTER_SURVEYS_LAST_DAY:
                currentFilteredCursor = dbHelper.getAllSurveyList(); // TODO: Implement later
            default:
                currentFilteredCursor = dbHelper.getAllSurveyList();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_survey_list, menu);
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
            String formattedTargetMobileNumbers = getFormattedTargetMobileNumbers();
            if (TextUtils.isEmpty(formattedTargetMobileNumbers)) {
                Toast.makeText(this, "The SMS can be sent only with a non-empty list", Toast.LENGTH_SHORT).show();
            } else {
                Intent sendSMSIntent = new Intent(SurveyListActivity.this, SendSMSActivity.class);
                Bundle extras = new Bundle();
                extras.putString(SendSMSActivity.SEND_SMS_MULTIPLE_TARGETS, formattedTargetMobileNumbers);
                sendSMSIntent.putExtras(extras);
                startActivity(sendSMSIntent);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getFormattedTargetMobileNumbers() {
        Cursor filteredCursor = null;
        StringBuffer buffer = new StringBuffer();
        try {
            filteredCursor = currentFilteredCursor;
            filteredCursor.moveToFirst();
            if (filteredCursor.getCount() > 0) {
                do {
                    String phoneNumber = filteredCursor.getString(filteredCursor.getColumnIndexOrThrow(SurveySQLiteHelper.SURVEY_COLUMN_PHONE));
                    buffer.append(phoneNumber);
                    buffer.append(", ");
                } while (filteredCursor.moveToNext());
            }
        } finally {
            if (null != filteredCursor && !filteredCursor.isClosed()) {
                filteredCursor.close();
            }
        }
        return buffer.toString().trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != currentFilteredCursor && !currentFilteredCursor.isClosed()) {
            currentFilteredCursor.close();
        }
    }
}

