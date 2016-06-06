package com.corpus.survey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.corpus.survey.com.corpus.survey.db.SurveySQLiteHelper;

public class SurveyListActivity extends AppCompatActivity {

    SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);

    public static final String SURVEY_ITEM_INDEX = "survey_item_index";

    // This is the Adapter being used to display the list's data
    SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_list);
        ListView mSurveyList = (ListView) findViewById(R.id.survey_list);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {SurveySQLiteHelper.SURVEY_COLUMN_NAME, SurveySQLiteHelper.SURVEY_COLUMN_PHONE};
        int[] toViews = {android.R.id.text1, android.R.id.text2}; // The TextView in simple_list_item_1
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, dbHelper.getAllSurveyList(),
                fromColumns, toViews, 0);

        mSurveyList.setAdapter(mAdapter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (null != getSupportActionBar())
        {
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

