package com.example.sdaassign4_2022;


import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Settings extends Fragment {


    public Settings() {
        // Required empty public constructor
    }

    private EditText userName;
    private EditText userEmail;
    private EditText userId;
    private TextView invalidEmail;
    private TextView invalidUsername;
    private TextView invalidId;
    public Button saveButton;
    public Button resetButton;

    private SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_settings, container, false);

        prefs = getActivity().getSharedPreferences("userdetails", MODE_PRIVATE);

        // get all input from view
        userName = root.findViewById(R.id.userName);
        userEmail = root.findViewById(R.id.email);
        userId = root.findViewById(R.id.borrowerID);
        // set them, if were not prev saved assign empty string
        userName.setText(prefs.getString(SharedPreferencesSettings.USER_NAME.name(), ""));
        userEmail.setText(prefs.getString(SharedPreferencesSettings.EMAIL.name(), ""));
        userId.setText(prefs.getString(SharedPreferencesSettings.USER_ID.name(), ""));

        // get all error text from view
        invalidUsername = root.findViewById(R.id.nameInvalid);
        invalidEmail = root.findViewById(R.id.EmailInvalid);
        invalidId = root.findViewById(R.id.idInvalid);
        // hide them
        invalidUsername.setVisibility(root.INVISIBLE);
        invalidEmail.setVisibility(root.INVISIBLE);
        invalidId.setVisibility(root.INVISIBLE);

        // set the button that will save changes
        saveButton = root.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveSettings(v));


        resetButton = root.findViewById(R.id.resetButton);
        resetButton.setOnClickListener(v -> resetSetting(v));

        return root;
    }

    /**
     * This method reset all data. The reset is done by simply assigning empty string to each input and applying
     * changes to Shared Preferences.
     * @param v
     */
    public void resetSetting(View v) {

        hideAllErrorMessages(v);
        SharedPreferences.Editor editor = prefs.edit();
        userName.setText("");
        userEmail.setText("");
        userId.setText("");

        editor.putString(SharedPreferencesSettings.USER_NAME.name(), "");
        editor.putString(SharedPreferencesSettings.EMAIL.name(), "");
        editor.putString(SharedPreferencesSettings.USER_ID.name(), "");

        editor.apply();
    }
        /**
         * This method will validate user input and, if valid, save the settings the user insert
         * In the three input fields
         * @param v
         */
    public void saveSettings (View v)
    {
        String name = userName.getText().toString();
        String email = userEmail.getText().toString();
        String id = userId.getText().toString();


        if (validateSetting(v, name, email, id)) {
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString(SharedPreferencesSettings.USER_NAME.name(), name);
            editor.putString(SharedPreferencesSettings.EMAIL.name(), email);
            editor.putString(SharedPreferencesSettings.USER_ID.name(), id);

            editor.apply();
        }
    }

    private boolean isValidInput (String input, String regex)
    {
        return input != null && !input.isEmpty() && input.matches(regex);
    }


    // private method used to validate the strings inserted by the user
    private boolean validateSetting (View v, String name, String email, String userId)
    {
        hideAllErrorMessages(v);

        boolean isValidSetting = true;

        // regex found here: https://stackoverflow.com/questions/30276258/how-to-ensure-a-string-has-no-digits
        if (!isValidInput(name, "\\D*")) {
            invalidUsername.setVisibility(v.VISIBLE);
            isValidSetting = false;
        }

        // regex found here: https://howtodoinjava.com/java/regex/java-regex-validate-email-address/
        if (!isValidInput(email, "^[\\w!#$%&amp;'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&amp;'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            invalidEmail.setVisibility(v.VISIBLE);
            isValidSetting = false;
        }
        // check not empty as digit rule is already enforced by the input
        if (!isValidInput(userId, "(.|\\s)*\\S(.|\\s)*")) {
            invalidId.setVisibility(v.VISIBLE);
            isValidSetting = false;
        }

        return isValidSetting;
    }

    private void hideAllErrorMessages (View v)
    {
        invalidUsername.setVisibility(v.INVISIBLE);
        invalidEmail.setVisibility(v.INVISIBLE);
        invalidId.setVisibility(v.INVISIBLE);
    }


}
