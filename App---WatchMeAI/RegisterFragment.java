package com.example.WatchMeAI;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class RegisterFragment extends Fragment {

    private String nameInput;
    private String phoneInput;
    private String phoneInput2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        Button btn_set = (Button) view.findViewById(R.id.btn_set_fr);
        btn_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new DevicesFragment(), "devices").commit();
            }
        });


        // user information
        TextView header = view.findViewById(R.id.tv_header_fr);
        EditText name = view.findViewById(R.id.et_name_input_fr);
        name.setHintTextColor(getResources().getColor(R.color.black));
        EditText phone = view.findViewById(R.id.et_phone_input_fr);
        phone.setHintTextColor(getResources().getColor(R.color.black));
        EditText phone2 = view.findViewById(R.id.et_phone_input_fr2);
        phone2.setHintTextColor(getResources().getColor(R.color.black));

        // no known user, need to register to use the app
        if (AuxiliaryFunctions.noKnownUser(requireContext())){

            Button btn_remove = (Button) view.findViewById(R.id.btn_remove_fr);
            btn_remove.setVisibility(View.INVISIBLE);

            header.setText("Register");

            // user input
            name.setHint("Enter Your Name");
            name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        name.setHint(""); // Clear the hint when the EditText gains focus
                    } else {
                        if (name.getText().length() == 0) {
                            name.setHint("Enter Your Name"); // Restore the hint if no text is entered and EditText loses focus
                            name.setTextColor(getResources().getColor(R.color.black));
                        }
                    }
                }
            });

            phone.setHint("Emergency Phone Number");
            phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        phone.setHint(""); // Clear the hint when the EditText gains focus
                    } else {
                        if (phone.getText().length() == 0) {
                            phone.setHint("Emergency Phone Number"); // Restore the hint if no text is entered and EditText loses focus
                        }
                    }
                }
            });

            phone2.setHint("2nd Emergency Phone Number (Optional)");
            phone2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        phone2.setHint(""); // Clear the hint when the EditText gains focus
                    } else {
                        if (phone2.getText().length() == 0) {
                            phone2.setHint("2nd Emergency Phone Number (Optional)"); //
                        }
                    }
                }
            });

        }

        else{
            header.setText("Personal Information:");

            Button btn_remove = (Button) view.findViewById(R.id.btn_remove_fr);
            btn_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    String userPhone2 = requireContext().getSharedPreferences("MyPrefs",
//                            Context.MODE_PRIVATE).getString("userPhone2", "");
                    SharedPreferences sharedPreferences = requireContext()
                            .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userPhone2", "").apply();
                    phone2.setHint("2nd Emergency Phone Number (Optional)");
                    btn_remove.setVisibility(View.INVISIBLE);
                }
            });

            // user input
            String userName = requireContext().getSharedPreferences("MyPrefs",
                    Context.MODE_PRIVATE).getString("userName", "");
            String userPhone = requireContext().getSharedPreferences("MyPrefs",
                    Context.MODE_PRIVATE).getString("userPhone", "");
            String userPhone2 = requireContext().getSharedPreferences("MyPrefs",
                    Context.MODE_PRIVATE).getString("userPhone2", "");

            name.setHint(userName);

            name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        name.setHint(""); // Clear the hint when the EditText gains focus
                    } else {
                        if (name.getText().length() == 0) {
                            // Restore hint if no text is entered and EditText loses focus
                            name.setHint(userName);
                        }
                    }
                }
            });

            phone.setHint(userPhone);
            phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        phone.setHint(""); // Clear the hint when the EditText gains focus
                    } else {
                        if (phone.getText().length() == 0) {
                            // Restore the hint if no text is entered and EditText loses focus
                            phone.setHint(userPhone);
                        }
                    }
                }
            });


            if(userPhone2.equals("")){
                phone2.setHint("2nd Emergency Phone Number (Optional)");
                btn_remove.setVisibility(View.INVISIBLE);
            }
            else{
                phone2.setHint(userPhone2);
            }
            phone2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        phone2.setHint(""); // Clear the hint when the EditText gains focus
                    } else {
                        if (phone2.getText().length() == 0) {
                            // Restore the hint if no text is entered and EditText loses focus
                            if(userPhone2.equals("")){
                                phone2.setHint("2nd Emergency Phone Number (Optional)");
                            }
                            else{
                                phone2.setHint(userPhone2);
                            }

                        }
                    }
                }
            });
        }


        Button btn_done = (Button) view.findViewById(R.id.btn_done_fr);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(AuxiliaryFunctions.noKnownUser(requireContext())){
                    // get user input
                    nameInput = name.getText().toString();
                    phoneInput = phone.getText().toString();
                    phoneInput2 = phone2.getText().toString();

                    // check if input is valid
                    boolean enteredDevice = !requireContext().getSharedPreferences("MyPrefs",
                            Context.MODE_PRIVATE).getString("userInput", "").equals("");
                    boolean enteredPhone = validPhoneNumber(phoneInput);
                    boolean enteredName = !nameInput.equals("");

                    boolean more2 = (!enteredPhone && !enteredName) ||
                                    (!enteredPhone && !enteredDevice) ||
                                    (!enteredName && !enteredDevice);

                    if(more2){
                        Toast.makeText(getContext(), "please fill information",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(!enteredDevice){
                        Toast.makeText(getContext(), "please connect a device",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(!enteredName){
                        Toast.makeText(getContext(), "please enter your name",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(!validPhoneNumber(phoneInput)){
                        Toast.makeText(getContext(), "please enter a valid phone number",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if(!validPhoneNumber(phoneInput2) && !phoneInput2.equals("")){
                        Toast.makeText(getContext(), "please enter a valid 2nd phone number",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // save inputs to future use
                    SharedPreferences sharedPreferences = requireContext()
                            .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if(!nameInput.equals("")){
                        nameInput = Character.toUpperCase(nameInput.charAt(0)) +
                                nameInput.substring(1);
                        editor.putString("userName", nameInput);
                    }
                    if(!phoneInput.equals("")){
                        editor.putString("userPhone", phoneInput);
                    }
                    if(!phoneInput2.equals("")){
                        editor.putString("userPhone2", phoneInput2);
                    }
                    editor.apply();
                }

                else{
                    // get user input
                    nameInput = name.getText().toString();
                    phoneInput = phone.getText().toString();
                    phoneInput2 = phone2.getText().toString();

                    // save inputs to future use
                    SharedPreferences sharedPreferences = requireContext()
                            .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if(!nameInput.equals("")){
                        nameInput = Character.toUpperCase(nameInput.charAt(0)) +
                                nameInput.substring(1);
                        editor.putString("userName", nameInput);
                    }
                    if(!phoneInput.equals("")){
                        if(validPhoneNumber(phoneInput)){
                            editor.putString("userPhone", phoneInput);
                        }
                        else{
                            Toast.makeText(getContext(), "please enter a valid phone number",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                    if(!phoneInput2.equals("")){
                        if(validPhoneNumber(phoneInput2)){
                            editor.putString("userPhone2", phoneInput2);
                        }
                        else{
                            Toast.makeText(getContext(), "please enter a valid second phone number",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }
                    editor.apply();
                }

                // finish registration
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new TerminalFragment(), "devices").commit();

            }
        });

        // set default user information
        SharedPreferences sharedPreferences = requireContext()
                .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userRegularHeartRate", "90");
        editor.putString("Full Watch Timer", "60");
        editor.putString("Track Timer", "60");
        editor.putString("Relax Walk Timer", "30");
        editor.putString("Custom Timer", "60");
        editor.putString("sms_or_call", "Send SMS");
        editor.apply();

        return view;
    }

    /* Auxiliary Function */

    private boolean validPhoneNumber(String phoneNumber){

        if (phoneNumber.length() != 10){
            return false;
        }
        boolean valid = phoneNumber.startsWith("050") || phoneNumber.startsWith("054") ||
                phoneNumber.startsWith("052") || phoneNumber.startsWith("053")
                                                        || phoneNumber.startsWith("058");
        return valid;
    }

}
