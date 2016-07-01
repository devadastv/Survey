package com.corpus.sirentext.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.corpus.sirentext.Customer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by devadas.vijayan on 5/30/16.
 */
public class SurveySQLiteHelper extends SQLiteOpenHelper {

    private static final int database_VERSION = 1;
    public static final String DATABASE_NAME = "SurveyDB";
    public static final String SURVEY_TABLE_NAME = "survey";
    public static final String SURVEY_COLUMN_ID = "_id";
    public static final String SURVEY_COLUMN_NAME = "name";
    public static final String SURVEY_COLUMN_PHONE = "phone";
    public static final String SURVEY_COLUMN_EMAIL = "email";
    public static final String SURVEY_COLUMN_GENDER = "gender";
    public static final String SURVEY_COLUMN_PLACE = "place";
    public static final String SURVEY_COLUMN_CREATED_DATE = "created_date";
    public static final String SURVEY_COLUMN_DATE_OF_BIRTH = "date_of_birth";
    public static final String SURVEY_COLUMN_CONTACT_GROUP = "contact_group";

    public static final String PREDEFINED_MESSAGE_TABLE_NAME = "PredefinedMessages";
    public static final String PREDEFINED_MESSAGE_COLUMN_ID = "_id";
    public static final String PREDEFINED_MESSAGE_COLUMN_MESSAGE = "message";

    public static final String CUSTOMER_GROUPS_TABLE_NAME = "customer_groups";
    public static final String CUSTOMER_GROUPS_COLUMN_ID = "_id";
    public static final String CUSTOMER_GROUPS_COLUMN_NAME = "group_name";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER ";
    private static final String COMMA_SEP = ",";

    /**
     * NOTE: Modification on this sort list values should only be done with corresponding modification in
     * order of items in 'sort_options' array in strings.xml
     */
    public static final int SORT_PURCHASE_DATE = 0;
    public static final int SORT_NAME = 1;
    public static final int SORT_CONTACT_GROUP = 2;
    public static final int SORT_DATE_OF_BIRTH = 3;
    public static final int SORT_PLACE = 4;


    private static final String SQL_CREATE_ENTRIES_SURVEY =
            "CREATE TABLE " + SURVEY_TABLE_NAME + " (" +
                    SURVEY_COLUMN_ID + INTEGER_TYPE + " PRIMARY KEY," +
                    SURVEY_COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    SURVEY_COLUMN_PHONE + TEXT_TYPE + COMMA_SEP +
                    SURVEY_COLUMN_EMAIL + TEXT_TYPE + COMMA_SEP +
                    SURVEY_COLUMN_GENDER + INTEGER_TYPE + COMMA_SEP +
                    SURVEY_COLUMN_PLACE + TEXT_TYPE + COMMA_SEP +
                    SURVEY_COLUMN_CREATED_DATE + INTEGER_TYPE + COMMA_SEP +
                    SURVEY_COLUMN_DATE_OF_BIRTH + INTEGER_TYPE + COMMA_SEP +
                    SURVEY_COLUMN_CONTACT_GROUP + INTEGER_TYPE +
                    " )";

    private static final String SQL_CREATE_ENTRIES_PREDEFINED_MESSAGES =
            "CREATE TABLE " + PREDEFINED_MESSAGE_TABLE_NAME + " (" +
                    PREDEFINED_MESSAGE_COLUMN_ID + INTEGER_TYPE + " PRIMARY KEY," +
                    PREDEFINED_MESSAGE_COLUMN_MESSAGE + TEXT_TYPE +
                    " )";

    private static final String SQL_CREATE_ENTRIES_CUSTOMER_GROUPS =
            "CREATE TABLE " + CUSTOMER_GROUPS_TABLE_NAME + " (" +
                    CUSTOMER_GROUPS_COLUMN_ID + INTEGER_TYPE + " PRIMARY KEY," +
                    CUSTOMER_GROUPS_COLUMN_NAME + TEXT_TYPE +
                    " )";


    public SurveySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, database_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DBHandler", "Creating DB with command \n" + SQL_CREATE_ENTRIES_SURVEY);
        db.execSQL(SQL_CREATE_ENTRIES_SURVEY);
        db.execSQL(SQL_CREATE_ENTRIES_PREDEFINED_MESSAGES);
        db.execSQL(SQL_CREATE_ENTRIES_CUSTOMER_GROUPS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DBHandler", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + SURVEY_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PREDEFINED_MESSAGE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CUSTOMER_GROUPS_TABLE_NAME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createSurvey(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SURVEY_COLUMN_NAME, customer.getUserName());
        values.put(SURVEY_COLUMN_PHONE, customer.getPhoneNumber());
        values.put(SURVEY_COLUMN_EMAIL, customer.getEmail());
        values.put(SURVEY_COLUMN_GENDER, customer.getGender());
        values.put(SURVEY_COLUMN_PLACE, customer.getPlace());
        values.put(SURVEY_COLUMN_CREATED_DATE, customer.getCreatedDate());
        values.put(SURVEY_COLUMN_DATE_OF_BIRTH, customer.getDateOfBirth());
        values.put(SURVEY_COLUMN_CONTACT_GROUP, customer.getContactGroup());
        db.insert(SURVEY_TABLE_NAME, null, values);
        db.close();
    }

    public int getNumberOfSurveyEntries() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT  * FROM " + SURVEY_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void deleteAllSurveyEntries() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + SURVEY_TABLE_NAME);
    }

    public Cursor getFilteredList(String selection, String[] selectionArgs, String orderBy) {
        return getFilteredList(null, selection, selectionArgs, orderBy);
    }

    public Cursor getFilteredList(String[] columns, String selection, String[] selectionArgs, String orderBy) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(SURVEY_TABLE_NAME, columns, selection, selectionArgs, null, null, orderBy);
    }

    public Customer getSurvey(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(SURVEY_TABLE_NAME, null, " _id = ?", new String[]{String.valueOf(id)}, null, null, null, null);
        Log.d("DBHelper", "getSurvey with id = " + id + " returned a cursor with length = " + cursor.getCount());
        if (cursor != null) {
            cursor.moveToFirst();

            /** TODO: Finalize on when to set the date time formatting. If at the time of display,
             * modify the cursorAdapter accordingly **/
            long millis = cursor.getLong(cursor.getColumnIndexOrThrow(SURVEY_COLUMN_CREATED_DATE));
            Date addedOn = new Date(millis);
            DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            String formattedDateString = formatter.format(addedOn);

            String name = cursor.getString(cursor.getColumnIndexOrThrow(SURVEY_COLUMN_NAME));
            String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(SURVEY_COLUMN_PHONE));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(SURVEY_COLUMN_EMAIL));
            int gender = cursor.getInt(cursor.getColumnIndexOrThrow(SURVEY_COLUMN_GENDER));
            String place = cursor.getString(cursor.getColumnIndexOrThrow(SURVEY_COLUMN_PLACE));
            long createdDate = cursor.getLong(cursor.getColumnIndexOrThrow(SURVEY_COLUMN_CREATED_DATE));
            long dateOfbirth = cursor.getLong(cursor.getColumnIndexOrThrow(SURVEY_COLUMN_DATE_OF_BIRTH));
            int contactGroup = cursor.getInt(cursor.getColumnIndexOrThrow(SURVEY_COLUMN_CONTACT_GROUP));
            Customer customer = new Customer(name, phoneNumber, gender, createdDate, contactGroup);
            Log.d("DBHelper", "From DB: createdDate = " + createdDate + " dateOfbirth = " + dateOfbirth);
            customer.setEmail(email);
            customer.setPlace(place);
            customer.setDateOfBirth(dateOfbirth);
            cursor.close();
            return customer;
        }
        return null;
    }

    public void createPredefinedMessage(String message) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PREDEFINED_MESSAGE_COLUMN_MESSAGE, message);
        db.insert(PREDEFINED_MESSAGE_TABLE_NAME, null, values);
        db.close();
    }

    public int getNumberOfPredefinedMessages() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT  * FROM " + PREDEFINED_MESSAGE_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public void deleteAllPredefinedMessages() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + PREDEFINED_MESSAGE_TABLE_NAME);
    }


    public Cursor getAllPredefinedMessagesCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT  * FROM " + PREDEFINED_MESSAGE_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        return cursor;
    }

    public String getPredefinedMessage(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(PREDEFINED_MESSAGE_TABLE_NAME, null, " _id = ?", new String[]{String.valueOf(id)}, null, null, null, null);
        Log.d("DBHelper", "getPredefinedMessage with id = " + id + " returned a cursor with length = " + cursor.getCount());
        if (cursor != null) {
            cursor.moveToFirst();
            String message = cursor.getString(cursor.getColumnIndexOrThrow(PREDEFINED_MESSAGE_COLUMN_MESSAGE));
            Log.d("DBHelper", "From DB: message = " + message);
            cursor.close();
            return message;
        }
        return null;
    }

    public void createNewCustomerGroup(String customerGroupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CUSTOMER_GROUPS_COLUMN_NAME, customerGroupName);
        db.insert(CUSTOMER_GROUPS_TABLE_NAME, null, values);
        db.close();
    }

    public int getNumberOfCustomerGroups() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT  * FROM " + CUSTOMER_GROUPS_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public String[] getCustomerGroupsArray() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT  * FROM " + CUSTOMER_GROUPS_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        String[] customerGroups = new String[cursor.getCount()];
        int i = 0;
        try {
            while (cursor.moveToNext()) {
                customerGroups[i++] = cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_GROUPS_COLUMN_NAME));
            }
        } finally {
            cursor.close();
        }
        return customerGroups;
    }

    public String getCustomerGroup(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(CUSTOMER_GROUPS_TABLE_NAME, null, " _id = ?", new String[]{String.valueOf(id)}, null, null, null, null);
        Log.d("DBHelper", "getPredefinedMessage with id = " + id + " returned a cursor with length = " + cursor.getCount());
        cursor.moveToFirst();
        String customerGroupName = cursor.getString(cursor.getColumnIndexOrThrow(CUSTOMER_GROUPS_COLUMN_NAME));
        Log.d("DBHelper", "From DB: customerGroupName = " + customerGroupName);
        cursor.close();
        return customerGroupName;
    }
}
