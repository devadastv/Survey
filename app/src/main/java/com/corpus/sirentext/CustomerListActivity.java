package com.corpus.sirentext;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.corpus.sirentext.db.SurveySQLiteHelper;
import com.corpus.sirentext.sms.SendSMSActivity;

public class CustomerListActivity extends AppCompatActivity {

    SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);

    public static final String TAG = "CustomerList";
    public static final String SURVEY_ITEM_INDEX = "survey_item_index";


    // This is the Adapter being used to display the list's data
    private Cursor currentFilteredCursor;

    private SimpleCursorAdapter mAdapter;

    private String selection;
    private String[] selectionArgs;
    private String orderBy;
    private int orderByIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSendSMSActivity();
            }
        });
        Bundle extras = getIntent().getExtras();
        int selectedCustomerGroup = -1;
        if (extras != null) {
            selectedCustomerGroup = extras.getInt(CustomerGroupListActiviy.SELECTED_CUSTOMER_GROUP);
            Log.d(TAG, "selectedCustomerGroup from CustomerGroupListActiviy = " + selectedCustomerGroup);
        }

        ListView mSurveyList = (ListView) findViewById(R.id.survey_list);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {SurveySQLiteHelper.SURVEY_COLUMN_NAME, SurveySQLiteHelper.SURVEY_COLUMN_PHONE};
        int[] toViews = {android.R.id.text1, android.R.id.text2};

        if (selectedCustomerGroup != -1) {
            selection = SurveySQLiteHelper.SURVEY_COLUMN_CONTACT_GROUP + "=" + selectedCustomerGroup;
//            selectionArgs = new String[]{dbHelper.getCustomerGroup(selectedCustomerGroup)};
        }
        Log.d(TAG, "selection in CustomerList = " + selection);
        currentFilteredCursor = dbHelper.getFilteredList(selection, selectionArgs, orderBy);
        Log.d(TAG, "No of contacts in currentFilteredCursor obtained in CustomerList = " + currentFilteredCursor.getCount());
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, currentFilteredCursor,
                fromColumns, toViews, 0);

        mSurveyList.setAdapter(mAdapter);
        mSurveyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent contentDetailsIntent = new Intent(CustomerListActivity.this, CustomerDetailsActivity.class);
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
        currentFilteredCursor = dbHelper.getFilteredList(selection, selectionArgs, orderBy);
        mAdapter.changeCursor(currentFilteredCursor);
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

        if (id == R.id.action_sendSMS) {
            startSendSMSActivity();
            return true;
        } else if (id == R.id.action_sort) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Sort by:");
            builder.setCancelable(true);
            AlertDialog dialog = builder.create();
            dialog.getListView();
            builder.setSingleChoiceItems(R.array.sort_options, orderByIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.d("SurveyList", "User selected " + which);
                    setSortOrder(which);
                    dialog.dismiss();
                }
            });
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startSendSMSActivity() {
        String formattedTargetMobileNumbers = getFormattedTargetMobileNumbers();
        if (TextUtils.isEmpty(formattedTargetMobileNumbers)) {
            Toast.makeText(this, "The SMS can be sent only with a non-empty list", Toast.LENGTH_SHORT).show();
        } else {
            Intent sendSMSIntent = new Intent(CustomerListActivity.this, SendSMSActivity.class);
            Bundle extras = new Bundle();
            extras.putString(SendSMSActivity.SEND_SMS_MULTIPLE_TARGETS, formattedTargetMobileNumbers);
            sendSMSIntent.putExtras(extras);
            startActivity(sendSMSIntent);
        }
    }

    private String getFormattedTargetMobileNumbers() {
        Cursor filteredCursor = null;
        StringBuilder buffer = new StringBuilder();
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

    public void setSortOrder(int order) {
        this.orderByIndex = order;
        orderBy = getSortOrderString(order);
        updateCurrentFilterCursor();
    }

    public String getSortOrderString(int sortOrderIndex) {
        String sortOrderString;
        switch (sortOrderIndex) {
            case SurveySQLiteHelper.SORT_PURCHASE_DATE:
                sortOrderString = SurveySQLiteHelper.SURVEY_COLUMN_CREATED_DATE + " DESC";
                break;
            case SurveySQLiteHelper.SORT_NAME:
                sortOrderString = SurveySQLiteHelper.SURVEY_COLUMN_NAME + " ASC";
                break;
            case SurveySQLiteHelper.SORT_CONTACT_GROUP:
                sortOrderString = SurveySQLiteHelper.SURVEY_COLUMN_CONTACT_GROUP + " ASC";
                break;
            case SurveySQLiteHelper.SORT_DATE_OF_BIRTH:
                sortOrderString = SurveySQLiteHelper.SURVEY_COLUMN_DATE_OF_BIRTH + " ASC";
                break;
            case SurveySQLiteHelper.SORT_PLACE:
                sortOrderString = SurveySQLiteHelper.SURVEY_COLUMN_PLACE + " ASC";
                break;
            default:
                sortOrderString = SurveySQLiteHelper.SURVEY_COLUMN_CREATED_DATE + " DESC";
        }
        return sortOrderString;
    }
}

