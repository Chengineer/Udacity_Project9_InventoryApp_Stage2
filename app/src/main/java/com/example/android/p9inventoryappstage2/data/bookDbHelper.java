package com.example.android.p9inventoryappstage2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.p9inventoryappstage2.data.bookContract.bookEntry;

/**
 * Database helper for books store app. Manages database creation and version management.
 */
public class bookDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = bookDbHelper.class.getSimpleName();
    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "bookstoredb";
    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link bookDbHelper}.
     *
     * @param context of the app
     */
    public bookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the books table
        String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + bookEntry.TABLE_NAME + " ("
                + bookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + bookEntry.COLUMN_BOOK_TITLE + " TEXT NOT NULL, "
                + bookEntry.COLUMN_BOOK_AUTHOR + " TEXT NOT NULL, "
                + bookEntry.COLUMN_BOOK_PRICE + " DOUBLE NOT NULL, "
                + bookEntry.COLUMN_BOOK_QUANTITY + " INTEGER NOT NULL, "
                + bookEntry.COLUMN_BOOK_SUPPLIER_NAME + " TEXT NOT NULL, "
                + bookEntry.COLUMN_BOOK_SUPPLIER_PHONE + "  TEXT NOT NULL);";
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}