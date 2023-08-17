package com.example.WatchMeAI;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class CustomActionFragment extends Fragment {

    String running_walking = "no";
    String pulse = "no";
    String fall = "no";
    String const_gps = "no";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_custom_action, container, false);


        Switch swc_run_walk = view.findViewById(R.id.swc_run_walk);
        swc_run_walk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    running_walking = "yes";
                }
                else{
                    running_walking = "no";
                }
            }
        });

        Switch swc_pulse = view.findViewById(R.id.swc_pulse);
        swc_pulse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    pulse = "yes";
                }
                else{
                    pulse = "no";
                }
            }
        });

        Switch swc_fall = view.findViewById(R.id.swc_fall);
        swc_fall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    fall = "yes";
                }
                else{
                    fall = "no";
                }

            }
        });

        Switch swc_gps = view.findViewById(R.id.swc_gps);
        swc_gps.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    const_gps = "yes";
                }
                else{
                    const_gps = "no";
                }
            }
        });

        EditText timer = view.findViewById(R.id.et_timer);
        timer.setHintTextColor(getResources().getColor(R.color.black));
        timer.setHint("Choose Count Down time for Help");
        timer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    timer.setHint(""); // Clear the hint when the EditText gains focus
                } else {
                    if (timer.getText().length() == 0) {
                        timer.setHint("Choose Count Down for Help"); // Restore the hint if no text is entered and EditText loses focus
                        timer.setTextColor(getResources().getColor(R.color.black));
                    }
                }
            }
        });

        Button btn_start = (Button) view.findViewById(R.id.btn_start_ca);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // make sure that at least one tracking options choose
                if(running_walking.equals("no") && pulse.equals("no") &&
                        fall.equals("no") && const_gps.equals("no")){
                    Toast.makeText(requireContext(), "you must choose at least " +
                            "one tracking option", Toast.LENGTH_SHORT).show();
                    return;
                }

                // if user chose time, it would be used. otherwise default time will be used
                if(!timer.getText().toString().equals("")){
                    SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("Custom", timer.getText().toString());
                    editor.apply();
                }

                String[] trackings = {running_walking, pulse, fall, const_gps};
                Bundle args = new Bundle();
                args.putStringArray("trackOn", trackings);
                args.putString("requested_action", "Custom");
                ActionFragment af = new ActionFragment();
                af.setArguments(args);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, af, "tag").addToBackStack("tag").commit();
            }
        });

        return view;
    }
}