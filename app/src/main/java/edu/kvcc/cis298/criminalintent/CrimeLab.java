package edu.kvcc.cis298.criminalintent;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.UUID;

public class CrimeLab {
    // This is a static variable so there can be only one.
    // It will hold the instance of this class.
    private static CrimeLab sCrimeLab;

    // List for the collection of crimes.
    private List<Crime> mCrimes;

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
        mCrimes = new ArrayList<>();
    }

    // Moved the default crimes into a separate method that can be
    // used to add some default crimes if desired.
    public void addDefaultCrimes() {
        for (int i = 0; i < 100; i++) {
            Crime crime = new Crime();
            crime.setTitle("Crime #" + i);
            crime.setSolved(i % 2 ==0);
            mCrimes.add(crime);
        }
    }

    // Method to be able to add a new crime to the list.
    public void addCrime(Crime c) {
        mCrimes.add(c);
    }

    // Getter for Crime List
    public List<Crime> getCrimes() {
        return mCrimes;
    }

    // Getter for single crime.
    public Crime getCrime(UUID id) {
        for (Crime crime : mCrimes) {
            if (crime.getId().equals(id)) {
                return crime;
            }
        }
        return null;
    }

    // Getter to see if the list is empty
    public boolean isEmpty() {
        return mCrimes.isEmpty();
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
                mCrimes.add(
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


}
