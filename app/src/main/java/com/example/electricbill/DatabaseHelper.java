package com.example.electricbill;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ElectricBillDB";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_BILLS = "bills";
    public static final String COL_ID = "id";
    public static final String COL_MONTH = "month";
    public static final String COL_UNITS = "units";
    public static final String COL_REBATE = "rebate_percent";
    public static final String COL_TOTAL = "total_charges";
    public static final String COL_FINAL = "final_cost";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_BILLS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_MONTH + " TEXT NOT NULL, " +
                    COL_UNITS + " REAL NOT NULL, " +
                    COL_REBATE + " REAL NOT NULL, " +
                    COL_TOTAL + " REAL NOT NULL, " +
                    COL_FINAL + " REAL NOT NULL" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }

    // INSERT
    public long insertBill(String month, double units, double rebate,
                           double totalCharges, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MONTH, month);
        values.put(COL_UNITS, units);
        values.put(COL_REBATE, rebate);
        values.put(COL_TOTAL, totalCharges);
        values.put(COL_FINAL, finalCost);
        long result = db.insert(TABLE_BILLS, null, values);
        db.close();
        return result;
    }

    // GET ALL
    public List<BillRecord> getAllBills() {
        List<BillRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BILLS + " ORDER BY id DESC", null);
        if (cursor.moveToFirst()) {
            do {
                BillRecord record = new BillRecord(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MONTH)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_UNITS)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_REBATE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_FINAL))
                );
                list.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    // GET ONE
    public BillRecord getBillById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_BILLS + " WHERE " + COL_ID + "=?",
                new String[]{String.valueOf(id)}
        );
        BillRecord record = null;
        if (cursor.moveToFirst()) {
            record = new BillRecord(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_MONTH)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_UNITS)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_REBATE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TOTAL)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_FINAL))
            );
        }
        cursor.close();
        db.close();
        return record;
    }

    // UPDATE
    public int updateBill(int id, String month, double units, double rebate,
                          double totalCharges, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MONTH, month);
        values.put(COL_UNITS, units);
        values.put(COL_REBATE, rebate);
        values.put(COL_TOTAL, totalCharges);
        values.put(COL_FINAL, finalCost);
        int rows = db.update(TABLE_BILLS, values, COL_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }

    // DELETE
    public void deleteBill(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BILLS, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }
}