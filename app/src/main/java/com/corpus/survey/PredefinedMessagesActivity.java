package com.corpus.survey;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.corpus.survey.db.SurveySQLiteHelper;

public class PredefinedMessagesActivity extends AppCompatActivity {

    private SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);
    private Cursor currentFilteredCursor;
    private SimpleCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predefined_messages);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbHelper.createPredefinedMessage("Sample message. This will work as a teal aetlkj agafgldkgj dfgdlgk dfgdfgsdfg sdfg dfg dfgsfgdfgfdgd fgdfgdfg dfg 12345");
            }
        });

        ListView mSurveyList = (ListView) findViewById(R.id.predefined_messages_list);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {SurveySQLiteHelper.PREDEFINED_MESSAGE_COLUMN_MESSAGE};
        int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1
        currentFilteredCursor = dbHelper.getAllPredefinedMessagesCursor();
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, currentFilteredCursor,
                fromColumns, toViews, 0);

        mSurveyList.setAdapter(mAdapter);
        mSurveyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent contentDetailsIntent = new Intent(SurveyListActivity.this, SurveyDetailsActivity.class);
//                Bundle extras = new Bundle();
//                extras.putInt(SURVEY_ITEM_INDEX, position);
//                contentDetailsIntent.putExtras(extras);
//                startActivity(contentDetailsIntent);
            }
        });
    }

}
