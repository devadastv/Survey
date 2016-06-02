package com.corpus.survey.com.corpus.survey.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.corpus.survey.Survey;

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

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + SURVEY_TABLE_NAME + " (" +
                    SURVEY_COLUMN_ID + " INTEGER PRIMARY KEY," +
                    SURVEY_COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
                    SURVEY_COLUMN_PHONE + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + SURVEY_TABLE_NAME;


    public SurveySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, database_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
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
        db.insert(SURVEY_TABLE_NAME, null, values);
        db.close();
    }

    public int getNumberOfSurveyEntries()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT  * FROM " + SURVEY_TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        return cursor.getCount();
    }

    public void deleteAllSurveyEntries()
    {
        SQLiteDatabase db = this.getWritableDatabase();
//        db.execSQL("delete from "+ SURVEY_TABLE_NAME);
        db.execSQL(SQL_DELETE_ENTRIES);
    }

    public Cursor getAllSurveyList(){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(SURVEY_TABLE_NAME, null, null, null, null, null, null);
    }
}
