package com.corpus.survey.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.corpus.survey.Survey;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by devadas.vijayan on 5/30/16.
 */
public class SurveySQLiteHelper extends SQLiteOpenHelper {

    private static final int database_VERSION = 4;
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


    public SurveySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, database_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DBHandler", "Creating DB with command \n" + SQL_CREATE_ENTRIES_SURVEY);
        db.execSQL(SQL_CREATE_ENTRIES_SURVEY);
        db.execSQL(SQL_CREATE_ENTRIES_PREDEFINED_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DBHandler", "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + SURVEY_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + PREDEFINED_MESSAGE_TABLE_NAME);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void createSurvey(Survey survey) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SURVEY_COLUMN_NAME, survey.getUserName());
        values.put(SURVEY_COLUMN_PHONE, survey.getPhoneNumber());
        values.put(SURVEY_COLUMN_EMAIL, survey.getEmail());
        values.put(SURVEY_COLUMN_GENDER, survey.getGender());
        values.put(SURVEY_COLUMN_PLACE, survey.getPlace());
        values.put(SURVEY_COLUMN_CREATED_DATE, survey.getCreatedDate());
        values.put(SURVEY_COLUMN_DATE_OF_BIRTH, survey.getDateOfBirth());
        values.put(SURVEY_COLUMN_CONTACT_GROUP, survey.getContactGroup());
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

    public Survey getSurvey(int id) {
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
            Survey survey = new Survey(name, phoneNumber, gender, createdDate, contactGroup);
            Log.d("DBHelper", "From DB: createdDate = " + createdDate + " dateOfbirth = " + dateOfbirth);
            survey.setEmail(email);
            survey.setPlace(place);
            survey.setDateOfBirth(dateOfbirth);
            cursor.close();
            return survey;
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
}
