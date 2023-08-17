package com.example.WatchMeAI;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class NoConnectionFragment extends Fragment {

    private Thread backgroundThread;
    private String backTo;
    Button btn_home;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        keepNotify();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_no_connection, container, false);
        TextView tv_noConnectionWarning = view.findViewById(R.id.tv_noConnectionWarning);
        TextView tv_noConnectionExplain = view.findViewById(R.id.tv_noConnectionExplain);

        Bundle arguments = getArguments();

        backTo = arguments.getString("wasIn");


        if (arguments.getString("error_type") != null) {    // device is connected but not working properly
            tv_noConnectionWarning.setText(R.string.no_connection_Warning_2);
            tv_noConnectionExplain.setText(R.string.no_connection_explanation_2);
        }
        else{
            tv_noConnectionWarning.setText(R.string.no_connection_Warning_1);
            tv_noConnectionExplain.setText(R.string.no_connection_explanation_1);
        }

        // buttons

        Button btn_reconnect_fnc = (Button) view.findViewById(R.id.btn_reconnect_fnc);
        btn_reconnect_fnc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(backTo == null){
                    btn_home.performClick();
                    stopPermissionTrack();
                    return;
                }
                Bundle args = new Bundle();
                args.putString("requested_action", backTo);
                if(backTo.equals("Custom")){
                    args.putStringArray("trackOn", arguments.getStringArray("trackOn"));
                }
                ActionFragment af = new ActionFragment();
                af.setArguments(args);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, af, "tag").addToBackStack("tag").commit();
                stopPermissionTrack();
            }
        });

        Button btn_reconnect = (Button) view.findViewById(R.id.btn_newConnect_fnc);
        btn_reconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPermissionTrack();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new DevicesFragment(), "devices").commit();
//                stopPermissionTrack();
            }
        });

        btn_home = (Button) view.findViewById(R.id.btn_return_fnc);
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPermissionTrack();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new TerminalFragment(), "devices").commit();
//                stopPermissionTrack();
            }
        });

        return view;
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
    public void onStop(){
        super.onStop();

        stopPermissionTrack();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        stopPermissionTrack();
    }


    private void keepNotify(){
        AuxiliaryFunctions.pushNotification(requireContext(),
                "no connection", "No Connection!",
                "Device is not working! \nPlease reconnect",
                "NoConnectionFragment");
        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    AuxiliaryFunctions.pushNotification(requireContext(),
                            "no connection", "No Connection!",
                            "Device is not working! \nPlease reconnect",
                            "NoConnectionFragment");

                    try {
                        Thread.sleep(10000); // Delay between each iteration
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        backgroundThread.start();
    }

    private void stopPermissionTrack() {
        if (backgroundThread != null && backgroundThread.isAlive()) {
            Log.d("tg", "???????????????");
            backgroundThread.interrupt();
            backgroundThread = null;
        }
        else{
            Log.d("tg", "!!!!!!!!!!!!!");
        }
    }

}