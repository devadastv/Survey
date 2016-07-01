package com.corpus.sirentext;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.corpus.sirentext.db.SurveySQLiteHelper;

public class CustomerGroupListActiviy extends AppCompatActivity {

    SurveySQLiteHelper dbHelper = new SurveySQLiteHelper(this);

    public static final String SELECTED_CUSTOMER_GROUP = "selected_customer_group";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_group_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: Logic to add a new customer group
            }
        });

        ListView mCustomerGroupList = (ListView) findViewById(R.id.customer_group_list);

        final String[] customerGroups =  dbHelper.getCustomerGroupsArray();
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, customerGroups);
        mCustomerGroupList.setAdapter(mAdapter);
        mCustomerGroupList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CustomerGroupListActiviy.this, CustomerListActivity.class);
                Bundle extras = new Bundle();
                extras.putInt(SELECTED_CUSTOMER_GROUP, position);
                intent.putExtras(extras);
                startActivity(intent);
            }
        });
    }
}
