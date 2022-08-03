package com.humdet.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlohaDb extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "aloha";

    public AlohaDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    SQLiteDatabase db = this.getReadableDatabase();
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_COUNTRY_TABLE = "CREATE TABLE country( id INTEGER, name TEXT) " ;
        String CREATE_REGION_TABLE = "CREATE TABLE region(id INTEGER, country_id INTEGER, name TEXT) " ;
        String CREATE_CITY_TABLE = "CREATE TABLE city(id INTEGER,region_id INTEGER,name TEXT) " ;
        db.execSQL(CREATE_COUNTRY_TABLE);
        db.execSQL(CREATE_REGION_TABLE);
        db.execSQL(CREATE_CITY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    void addCountry(Country country) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", country.getId());
        values.put("name", country.getName());
        db.insert("country", null, values);
        db.close(); // Closing database connection
    }
    void addRegion(Region region) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", region.getId());
        values.put("country_id", region.getCountry_id());
        values.put("name", region.getName());
        db.insert("region", null, values);
        db.close(); // Closing database connection
    }
    void addCity(City city) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", city.getId());
        values.put("region_id", city.getRegion_id());
        values.put("name", city.getName());
        db.insert("region", null, values);
        db.close(); // Closing database connection
    }


    Country getCountry(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("country", new String[] { "id","name" }, "id" + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Country country = new Country();
        country.setId(cursor.getInt(1));
        country.setName(cursor.getString(2));
        // return contact
        return country;
    }

}































