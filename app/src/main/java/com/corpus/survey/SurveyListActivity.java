package com.corpus.survey;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.corpus.survey.com.corpus.survey.db.SurveySQLiteHelper;

public class SurveyListActivity extends AppCompatActivity {

    SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);

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
        if (null != getSupportActionBar())
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mSurveyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = ((SimpleCursorAdapter) parent.getAdapter()).getItem(position);
                Log.d("SurveyList", "Object at position " + position + " is " + item);
                Toast.makeText(SurveyListActivity.this, item + " selected", Toast.LENGTH_LONG).show();

                Intent contentDetailsIntent = new Intent(SurveyListActivity.this, SurveyDetailsActivity.class);
                startActivity(contentDetailsIntent);
            }
        });
    }
}

