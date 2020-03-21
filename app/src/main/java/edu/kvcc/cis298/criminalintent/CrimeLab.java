package edu.kvcc.cis298.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;

import edu.kvcc.cis298.criminalintent.database.CrimeBaseHelper;
import edu.kvcc.cis298.criminalintent.database.CrimeCursorWrapper;
import edu.kvcc.cis298.criminalintent.database.CrimeDbSchema;
import edu.kvcc.cis298.criminalintent.database.CrimeDbSchema.CrimeTable;

public class CrimeLab {
    // This is a static variable so there can be only one.
    // It will hold the instance of this class.
    private static CrimeLab sCrimeLab;

    // Class level var for the context
    private Context mContext;
    // Var for the database helper
    private SQLiteDatabase mDatabase;

    // Boolean for whether or not we have already loaded that data
    private boolean mDataLoadedOnce;

    // Static get method which will allow us to call
    // CrimeLab.get(context) from anywhere in our program
    // to always get the same instance of our crimeLab.
    public static CrimeLab get(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    // Private constructor. Which means it is not possible
    // to create an instance from outside this class.
    // If you want an instance, you MUST use the static
    // get method above to get the instance.
    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new CrimeBaseHelper(mContext)
                .getWritableDatabase();
    }

    // Moved the default crimes into a separate method that can be
    // used to add some default crimes if desired.
    public void addDefaultCrimes() {
        for (int i = 0; i < 100; i++) {
            Crime crime = new Crime();
            crime.setTitle("Crime #" + i);
            crime.setSolved(i % 2 ==0);
            addCrime(crime);
        }
    }

    // Method to be able to add a new crime to the list.
    public void addCrime(Crime c) {
        // Get the content values for the crime
        ContentValues values = getContentValues(c);

        // Insert the record into the database
        mDatabase.insert(CrimeTable.NAME, null, values);
    }

    // Getter for Crime List
    public List<Crime> getCrimes() {
        // Create a list for the crimes
        List<Crime> crimes = new ArrayList<>();
        // Get the CursorWrapper with our data
        CrimeCursorWrapper cursor = queryCrimes(null, null);
        // Wrap the work of using the returned cursor in a try so we can ensure that
        // the cursor gets closed in the event that something goes wrong.
        try {
            // Move the cursor to the first record
            cursor.moveToFirst();
            // Start while loop to continue through the records
            while (!cursor.isAfterLast()) {
                // Use the getCrime method to convert the spot in the query result
                // into a crime instance and then add it to the list
                crimes.add(cursor.getCrime());
                // Move the cursor to the next record in the query result
                cursor.moveToNext();
            }
        } finally {
            // Make sure to always close the cursor.
            // If you don't, it could lead to not enough file pointers being available.
            cursor.close();
        }

        // Return the crimes that we got out
        return crimes;
    }

    // Getter for single crime.
    public Crime getCrime(UUID id) {
        // Use a CursorWrapper to get the query results
        CrimeCursorWrapper cursor = queryCrimes(
                CrimeTable.Cols.UUID + " = ?",
                new String[] { id.toString() }
        );
        // Still do in a try for safety
        try {
            // If we don't get a result, just return null
            if (cursor.getCount() == 0) {
                return null;
            }
            // Move to the first record in the query result
            cursor.moveToFirst();
            // Convert the query result into a crime object and return it.
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }

    // Method to update a Crime
    public void updateCrime(Crime crime) {
        String uuidString = crime.getId().toString();
        ContentValues values = getContentValues(crime);
         mDatabase.update(
                 CrimeTable.NAME,
                 values,
                 CrimeTable.Cols.UUID + " = ?",
                 new String[] { uuidString }
         );
    }

    // Method to query out crimes from the database
    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                CrimeTable.NAME,
                null, // All columns when using null
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );

        return new CrimeCursorWrapper(cursor);
    }

    // Getter to see if the list is empty
    public boolean isEmpty() {
        // Use the CrimeCursorWrapper to query the database for all entries
        CrimeCursorWrapper cursor = queryCrimes(null,null);
        // bool representing whether there are entries or not
        boolean isEmpty = true;

        try {
            isEmpty = (cursor.getCount() <= 0);
        } finally {
            cursor.close();
        }

        return isEmpty;
    }

    // Getter to see if the data has already been loaded once.
    public boolean isDataLoadedOnce() {
        return mDataLoadedOnce;
    }

    // Method to load the beverage list from a CSV file
    void loadCrimeList(InputStream inputStream) {
        // Define a scanner
        try (Scanner scanner  = new Scanner(inputStream)) {

            // While the scanner has another line to read
            while (scanner.hasNextLine()) {

                // Get the next link and split it into parts
                String line = scanner.nextLine();
                String parts[] = line.split(",");

                //Assign each part to a local var
                String id = parts[0];
                String title = parts[1];
                String dateString = parts[2];
                String solvedString = parts[3];

                UUID uuid = UUID.fromString(id);
                Date date = new SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                ).parse(dateString);
                boolean isSolved = (solvedString.equals("1"));

                // Add the Crime to the Crime list
                addCrime(
                        new Crime(
                                uuid,
                                title,
                                date,
                                isSolved
                        )
                );
            }

            // Date read in, so set the dataLoadedOnce flag to true.
            mDataLoadedOnce = true;

        } catch (Exception e) {
            Log.e("Read CSV", e.toString());
        }
    }


    private static ContentValues getContentValues(Crime crime)
    {
        ContentValues values = new ContentValues();
        values.put(CrimeTable.Cols.UUID, crime.getId().toString());
        values.put(CrimeTable.Cols.TITLE, crime.getTitle());
        values.put(CrimeTable.Cols.DATE, crime.getDate().getTime());
        values.put(CrimeTable.Cols.SOLVED, crime.isSolved() ? 1 : 0);

        return values;
    }

}
