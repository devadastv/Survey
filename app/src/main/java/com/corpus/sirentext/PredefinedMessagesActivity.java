package com.corpus.sirentext;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.corpus.sirentext.db.SurveySQLiteHelper;

public class PredefinedMessagesActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_PICK_MESSAGE = 1;

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
                AlertDialog.Builder builder = new AlertDialog.Builder(getSupportActionBar().getThemedContext());
                LayoutInflater inflater = PredefinedMessagesActivity.this.getLayoutInflater();
                final View rootView = inflater.inflate(R.layout.dialog_new_predefined_message, null);
                builder.setView(rootView).
                        setTitle(R.string.new_predefined_message).
                        setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                TextView mNewPredefinedMessage = (TextView) rootView.findViewById(R.id.new_predefined_message);
                                String newPredefinedMessage = mNewPredefinedMessage.getText().toString();
                                if (null != newPredefinedMessage && newPredefinedMessage.trim().length() != 0) {
                                    dbHelper.createPredefinedMessage(newPredefinedMessage);
                                    updateMessageList();
                                } else {
                                    PredefinedMessagesActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            Toast.makeText(PredefinedMessagesActivity.this, "The message is empty and is ignored !", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        ListView mMessageList = (ListView) findViewById(R.id.predefined_messages_list);

        // For the cursor adapter, specify which columns go into which views
        String[] fromColumns = {SurveySQLiteHelper.PREDEFINED_MESSAGE_COLUMN_MESSAGE};
        int[] toViews = {android.R.id.text1}; // The TextView in simple_list_item_1
        currentFilteredCursor = dbHelper.getAllPredefinedMessagesCursor();
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, currentFilteredCursor,
                fromColumns, toViews, 0);

        mMessageList.setAdapter(mAdapter);
        mMessageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != getCallingActivity()) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("messageIndex", position + 1);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        });
    }

    private void updateMessageList() {
        currentFilteredCursor = dbHelper.getAllPredefinedMessagesCursor();
        mAdapter.changeCursor(currentFilteredCursor);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != currentFilteredCursor && !currentFilteredCursor.isClosed()) {
            currentFilteredCursor.close();
        }
    }
}
