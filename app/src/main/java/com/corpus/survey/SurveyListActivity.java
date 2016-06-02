package com.corpus.survey;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

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
    }

//    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
//        String item = (String) getListAdapter().getItem(position);
//        Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
    }
}

