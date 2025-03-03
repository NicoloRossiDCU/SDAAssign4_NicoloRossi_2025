package com.example.sdaassign4_2022;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CheckOut extends AppCompatActivity {

    TextView mDisplaySummary;
    TextView mDisplayConfirm;
    TextView mAvailableMessage;
    Calendar mDateAndTime = Calendar.getInstance();
    SharedPreferences prefs;
    String bookTitle;
    String bookId;

    LocalDateTime mLastBorrow;
    boolean mIsAvailable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);
        findViewById(R.id.orderButton).setActivated(false);

        //get the preferences for user settings
        prefs = getApplicationContext().getSharedPreferences("userdetails", MODE_PRIVATE);

        //set the toolbar we have overridden
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDisplayConfirm = findViewById(R.id.confirm);
        mAvailableMessage = findViewById(R.id.availability);
        Bundle extras = getIntent().getExtras();
        // passing the extra to class fields just to make more readable the code
        if (extras != null) {
            bookTitle = extras.getString("title");
            bookId = extras.getString("id");
            mDisplayConfirm.setText(bookTitle);
            mIsAvailable = extras.getBoolean("Available");
            mLastBorrow = LocalDateTime.parse(extras.getString("lastBorrow"));
        }
        Log.e(TAG, "the book " + bookTitle + " is " + mIsAvailable);
        // ideally here handling the case extra are not complete (in reality app will just crash)

        //find the summary textview
        mDisplaySummary = findViewById(R.id.orderSummary);
        if(!mIsAvailable) {
            /*  if book not available show message and block buttons
                Ref: https://stackoverflow.com/questions/22463062/how-can-i-parse-format-dates-with-localdatetime-java-8
                Reference used for the formatting of localdatetime (Date class is deprecated)
            */
            DateTimeFormatter dTF = DateTimeFormatter.ofPattern("dd MMM");
            mAvailableMessage.setText(getString(R.string.not_available, mLastBorrow.format(DateTimeFormatter.ISO_LOCAL_DATE)));
            findViewById(R.id.date).setEnabled(false);
            findViewById(R.id.orderButton).setEnabled(false);
        }
        else {
            mAvailableMessage.setText(getString(R.string.available));
            findViewById(R.id.date).setEnabled(true);
            findViewById(R.id.orderButton).setEnabled(false);
        }
    }

    //source SDA_2019 android course examples ViewGroup demo
    public void onDateClicked(View v) {

        DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mDateAndTime.set(Calendar.YEAR, year);
                mDateAndTime.set(Calendar.MONTH, monthOfYear);
                mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                // check if data selected by user is valid, if so display summary
                if(isDateValid(mDateAndTime.get(Calendar.DAY_OF_YEAR), mDateAndTime.get(Calendar.YEAR))){
                    findViewById(R.id.orderButton).setEnabled(true);
                    updateSummaryMessage();

                }
                else {
                    findViewById(R.id.orderButton).setEnabled(false);
                    Toast.makeText(view.getContext(), getString(R.string.select_valid_date_message), Toast.LENGTH_SHORT).show();
                }
            }
        };



        new DatePickerDialog(CheckOut.this, mDateListener,
                mDateAndTime.get(Calendar.YEAR),
                mDateAndTime.get(Calendar.MONTH),
                mDateAndTime.get(Calendar.DAY_OF_MONTH)).show();
    }
    /**
     * This method will send the order to the db. This is done by creating a new document with data
     * of the order. It also update the book availability and last ordered.
     * However it does not update the book in the list since there is no listener for events added so
     * it will show the book is no available only next time the app is added.
     * In the three input fields
     * @param v
     */
    public void onSendOrderClicked(View v) {
        //  ref: https://firebase.google.com/docs/firestore/manage-data/add-data
        // reference used to understand how to save/update data to db
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // hash map used to save data. As alternative an object could be used here.
        Map<String, Object> order = new HashMap<>();
        com.google.firebase.Timestamp dateSelected = new Timestamp(mDateAndTime.getTime());

        String userId = prefs.getString(SharedPreferencesSettings.USER_ID.name(), "No User");
        order.put("BookId", bookId);
        order.put("UserId", userId);
        order.put("DateCollection", dateSelected);
        order.put("DateBooking", Timestamp.now());

        String newOrderId = GenerateOrderId(userId, bookId);

        // add order
        db.collection("orders").document(newOrderId)
                .set(order)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Order added successfully!");
                        findViewById(R.id.orderButton).setEnabled(false);
                        findViewById(R.id.date).setEnabled(false);
                        showSuccessMessage(mDateAndTime.getTime());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing order to db.", e);
                    }
                });

        // update book
        db.collection("drawable").document(bookId)
                .update("DateLastBorrow", dateSelected, "Available", false)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Book metadata updated.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing book metadata", e);
                    }
                });

    }

    // this method just generate a (almost) unique string to identify the order
    private String GenerateOrderId(String userId, String bookId){
        LocalDateTime bookingTime = LocalDateTime.now();

        ZonedDateTime zdt = ZonedDateTime.of(bookingTime, ZoneId.systemDefault());

        return"order-" + userId + "-" + bookId + "-"+ zdt.toInstant().toEpochMilli();


    }

    // this method update the summary message
    private void updateSummaryMessage() {
        //date time year
        CharSequence currentTime = DateUtils.formatDateTime(this, mDateAndTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
        CharSequence SelectedDate = DateUtils.formatDateTime(this, mDateAndTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_YEAR);
        String userName = prefs.getString(SharedPreferencesSettings.USER_NAME.name(), "Username");
        String userID = prefs.getString(SharedPreferencesSettings.USER_ID.name(), "UserId");

        mDisplaySummary.setText(getString(R.string.summary_string, bookTitle, SelectedDate, userName, userID, currentTime));
    }

    // update the summary to success message
    private void showSuccessMessage(Date bookDate) {
        mDisplaySummary.setText(getString(R.string.success_message, new SimpleDateFormat("dd", Locale.UK).format(bookDate),  new SimpleDateFormat("MMMM", Locale.UK).format(bookDate) ));
    }
    // check if date is valid i.e. not in the past and not further than 30 days in the future
    // this last limit is set because book availability will be set to unavailable
    // since a more complex booking calendar system is not set for the moment.
    private boolean isDateValid(int dayOfYear, int year) {
        LocalDateTime now = LocalDateTime.now();
        return now.getDayOfYear() <= dayOfYear && now.getYear() <= year && dayOfYear < now.getDayOfYear() + 30;
    }
}
