package edu.kvcc.cis298.criminalintent.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import edu.kvcc.cis298.criminalintent.database.CrimeDbSchema.CrimeTable;

public class CrimeBaseHelper extends SQLiteOpenHelper {

    // Version number that can be incremented to indicate that the
    // app needs to run the database update script. We won't do this
    // but we still need a database version to even make SQLite work.
    private static final int VERSION = 1;
    // Name of the database file on the android system. This is different
    // than the name that we used over in the constants in CrimeDbSchema
    private static final String DATABASE_NAME = "crimeBase.db";

    // Constructor that just defers to what the parent would do.
    public CrimeBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    // This will be where we setup the database.
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + CrimeTable.NAME + "(" +
                        " _id integer primary key autoincrement, " +
                        CrimeTable.Cols.UUID + ", " +
                        CrimeTable.Cols.TITLE + ", " +
                        CrimeTable.Cols.DATE + ", " +
                        CrimeTable.Cols.SOLVED +
                        ")"
        );
    }

    // This is what will run if the version number changes and does not match.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
