package com.example.stockapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "StockPriceDB";
    public static final String TABLE_STOCK_PRICES = "stock_prices";
    public static final String KEY_ID = "id";
    public static final String KEY_SYMBOL = "symbol";
    public static final String KEY_PRICE = "price";
    public static final String KEY_EXCHANGE = "exchange";
    public static final String KEY_COMPANY = "company";

    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_STOCK_PRICES_TABLE = "CREATE TABLE " + TABLE_STOCK_PRICES + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_SYMBOL + " TEXT,"
                + KEY_PRICE + " REAL,"
                + KEY_EXCHANGE + " TEXT,"
                + KEY_COMPANY + " TEXT" + ")";
        db.execSQL(CREATE_STOCK_PRICES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCK_PRICES);
        onCreate(db);
    }

    public void addStockPrice(String symbol, double price, String exchange, String company) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SYMBOL, symbol);
        values.put(KEY_PRICE, price);
        values.put(KEY_EXCHANGE, exchange);
        values.put(KEY_COMPANY, company);

        db.insert(TABLE_STOCK_PRICES, null, values);
        db.close();
    }
}
