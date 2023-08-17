package com.example.WatchMeAI;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class TerminalFragment extends Fragment { //  implements ServiceConnection, SerialListener

    // user info
    private String userName;


    /** Lifecycle **/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        userName = requireContext().getSharedPreferences("MyPrefs"
                , Context.MODE_PRIVATE).getString("userName", "");


        // check all required permissions are allowed
        if(!AuxiliaryFunctions.allAllowed(requireContext())){
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, new PermissionsFragment())
                    .commit();
            Toast.makeText(requireContext(), "Some permissions are missing, please allow all " +
                    "permissions", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    /**  UI  **/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_terminal, container, false);

        TextView welcomeView = view.findViewById(R.id.welcome_view);
        TextView textView = view.findViewById(R.id.tv_subtitle_ft);

        String[] first_name = userName.split(" ");
        welcomeView.setText("Good " + AuxiliaryFunctions.getTimeOfDay() + " " + first_name[0] + "!");
        textView.setText("How can I help you?");


        // sos Button
        Button btn_sos = (Button) view.findViewById(R.id.btn_callHelp_ft);
        btn_sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new PoliceFragment(), "devices").commit();
                onDestroy();
            }
        });


        // Info Buttons

        TextView h1 = view.findViewById(R.id.qm1_h);
        Button btn_qm1 = view.findViewById(R.id.qm1);
        btn_qm1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String current_text = h1.getText().toString();
                if(current_text.equals("")) h1.setText(R.string.watchMe_exp);
                else h1.setText("");
            }
        });

        TextView h2 = view.findViewById(R.id.qm2_h);
        Button btn_qm2 = view.findViewById(R.id.qm2);
        btn_qm2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String current_text = h2.getText().toString();
                if(current_text.equals("")) h2.setText(R.string.hike_exp);
                else h2.setText("");
            }
        });

        TextView h3 = view.findViewById(R.id.qm3_h);
        Button btn_qm3 = view.findViewById(R.id.qm3);
        btn_qm3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String current_text = h3.getText().toString();
                if(current_text.equals("")) h3.setText(R.string.walk_exp);
                else h3.setText("");
            }
        });

        TextView h4 = view.findViewById(R.id.qm4_h);
        Button btn_qm4 = view.findViewById(R.id.qm4);
        btn_qm4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String current_text = h4.getText().toString();
                if(current_text.equals("")) h4.setText(R.string.custom_exp);
                else h4.setText("");
            }
        });


        // Action Buttons

        Button btn_full = view.findViewById(R.id.btn_b4);
        btn_full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStop();
                Bundle args = new Bundle();
                args.putString("requested_action", "Full Watch");
                ActionFragment af = new ActionFragment();
                af.setArguments(args);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, af, "tag").addToBackStack("tag").commit();
            }
        });

        Button btn_track = view.findViewById(R.id.btn_b3);
        btn_track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStop();
                Bundle args = new Bundle();
                args.putString("requested_action", "Track");
                ActionFragment af = new ActionFragment();
                af.setArguments(args);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, af, "tag").addToBackStack("tag").commit();
            }
        });

        Button btn_relax = view.findViewById(R.id.btn_b1);
        btn_relax.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStop();
                Bundle args = new Bundle();
                args.putString("requested_action", "Relax Walk");
                ActionFragment af = new ActionFragment();
                af.setArguments(args);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, af, "tag").addToBackStack("tag").commit();
            }
        });

        Button btn_custom = view.findViewById(R.id.btn_b2);
        btn_custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onStop();
                Bundle args = new Bundle();
                args.putString("requested_action", "Custom");
                CustomActionFragment caf = new CustomActionFragment();
                caf.setArguments(args);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, caf, "tag").addToBackStack("tag").commit();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_terminal, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.op_about){
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, new aboutFragment()).commit();
            return true;
        }
        else if (id == R.id.op_Edit){
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, new RegisterFragment()).commit();
            return true;
        }
        else if (id == R.id.op_settings){
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, new SettingsFragment()).commit();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

}
