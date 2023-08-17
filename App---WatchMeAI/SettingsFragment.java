package com.example.WatchMeAI;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class SettingsFragment extends Fragment {

    String sms_call;
    String prev_sms_call;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prev_sms_call = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .getString("sms_or_call", "");

        Log.d("tg", prev_sms_call);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // retrieve current timers
        String Full_Watch_Timer = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .getString("Full Watch Timer", "");
        String Track_Timer = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .getString("Track Timer", "");
        String Relax_Walk_Timer = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .getString("Relax Walk Timer", "");


        // Full Watch input
        EditText fw = view.findViewById(R.id.et_1_st);
        fw.setHintTextColor(getResources().getColor(R.color.black));
        fw.setHint("Current: " + Full_Watch_Timer);
        fw.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    fw.setHint("Current: " + Full_Watch_Timer);
                } else {
                    if (fw.getText().length() == 0) {
                        fw.setHint("Current: " + Full_Watch_Timer);
                        fw.setTextColor(getResources().getColor(R.color.black));
                    }
                }
            }
        });

        // Relax Walk input
        EditText rw = view.findViewById(R.id.et_2_st);
        rw.setHintTextColor(getResources().getColor(R.color.black));
        rw.setHint("Current: " + Relax_Walk_Timer);
        rw.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    rw.setHint("Current: " + Relax_Walk_Timer);
                } else {
                    if (rw.getText().length() == 0) {
                        rw.setHint("Current: " + Relax_Walk_Timer);
                        rw.setTextColor(getResources().getColor(R.color.black));
                    }
                }
            }
        });

        // Track input
        EditText tr = view.findViewById(R.id.et_3_st);
        tr.setHintTextColor(getResources().getColor(R.color.black));
        tr.setHint("Current: " + Track_Timer);
        tr.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    tr.setHint("Current: " + Track_Timer);
                } else {
                    if (tr.getText().length() == 0) {
                        tr.setHint("Current: " + Track_Timer);
                        tr.setTextColor(getResources().getColor(R.color.black));
                    }
                }
            }
        });

        // spinner - call or text
        Spinner spinner = view.findViewById(R.id.sp_sms_or_call_fs);
        String[] spinnerValues;

        if(prev_sms_call.equals("Send SMS")){
            spinnerValues = new String[]{"Send SMS", "Call", "Call & Send SMS"};
        }
        else if(prev_sms_call.equals("Call")){
            spinnerValues = new String[]{"Call", "Send SMS", "Call & Send SMS"};
        }
        else{
            spinnerValues = new String[]{"Call & Send SMS", "Call", "Send SMS"};
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, spinnerValues);


        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                sms_call= adapter.getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                sms_call = prev_sms_call;
            }
        });



        Button btn_done = (Button) view.findViewById(R.id.btn_done_st);
        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if(!fw.getText().toString().equals("")){
                    editor.putString("Full Watch Timer", fw.getText().toString()).apply();
                }
                if(!rw.getText().toString().equals("")){
                    editor.putString("Relax Walk Timer", rw.getText().toString()).apply();
                }
                if(!tr.getText().toString().equals("")){
                    editor.putString("Track Timer", tr.getText().toString()).apply();
                }
                editor.putString("sms_or_call", sms_call).apply();
//                if(!sms_call.equals("")){
//                    editor.putString("sms_or_call", sms_call).apply();
//                }


                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new TerminalFragment()).commit();
            }
        });

        return view;

    }
}