package com.example.stockapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbAdapter {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    public DbAdapter(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addStockPrice(String symbol, double price, String exchange, String company) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_SYMBOL, symbol);
        values.put(DatabaseHelper.KEY_PRICE, price);
        values.put(DatabaseHelper.KEY_EXCHANGE, exchange);
        values.put(DatabaseHelper.KEY_COMPANY, company);

        return database.insert(DatabaseHelper.TABLE_STOCK_PRICES, null, values);
    }

    public String getData() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {DatabaseHelper.KEY_ID, DatabaseHelper.KEY_SYMBOL, DatabaseHelper.KEY_PRICE, DatabaseHelper.KEY_EXCHANGE, DatabaseHelper.KEY_COMPANY};
        Cursor cursor = db.query(DatabaseHelper.TABLE_STOCK_PRICES, columns, null, null, null, null, null);
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID));
            String symbol = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_SYMBOL));
            double price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PRICE));
            String exchange = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_EXCHANGE));
            String company = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_COMPANY));
            buffer.append("ID: " + id + ", Symbol: " + symbol + ", Price: " + price + ", Exchange: " + exchange + ", Company: " + company + "\n");
        }
        cursor.close();
        return buffer.toString();
    }

}
