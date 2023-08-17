package com.example.WatchMeAI;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class PermissionsFragment extends Fragment {

    Button btn_location_fp;
    Button btn_notification_fp;
    Button btn_sms_fp;
    Button btn_phone_fp;

    ImageView v1;
    ImageView v2;
    ImageView v3;
    ImageView v4;

    Button btn_continue_fp;

    private Thread backgroundThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permissions, container, false);

        v1 = view.findViewById(R.id.v1);
        v2 = view.findViewById(R.id.v2);
        v3 = view.findViewById(R.id.v3);
        v4 = view.findViewById(R.id.v4);
        v1.setVisibility(View.INVISIBLE);
        v2.setVisibility(View.INVISIBLE);
        v3.setVisibility(View.INVISIBLE);
        v4.setVisibility(View.INVISIBLE);

        // location permission
        TextView tv_location_fp = view.findViewById(R.id.tv_location_fp);
        tv_location_fp.setText(R.string.pf_location_explain);
        btn_location_fp = view.findViewById(R.id.btn_location_fp);
        btn_location_fp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(LocationPermissionEnabled()){
                    Toast.makeText(requireContext(), "location already enabled",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                requestLocationPermission();
            }
        });

        // notification permission
        TextView tv_notification_fp = view.findViewById(R.id.tv_notification_fp);
        tv_notification_fp.setText(R.string.pf_notification_explain);
        btn_notification_fp = view.findViewById(R.id.btn_notification_fp);
        btn_notification_fp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(NotificationPermissionEnabled()){
                    Toast.makeText(requireContext(), "notifications already enabled",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                requestNotificationPermission();
            }
        });

        // SMS permission
        TextView tv_sms_fp = view.findViewById(R.id.tv_sms_fp);
        tv_sms_fp.setText(R.string.pf_sms_explain);
        btn_sms_fp = view.findViewById(R.id.btn_sms_fp);
        btn_sms_fp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(SmsPermissionEnabled()){
                    Toast.makeText(requireContext(), "SMSs already enabled",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                requestSmsPermission();
            }
        });

        // calls permission
        TextView tv_pc_fp = view.findViewById(R.id.tv_pc_fp);
        tv_pc_fp.setText(R.string.pf_pc_explain);
        btn_phone_fp = view.findViewById(R.id.btn_phone_fp);
        btn_phone_fp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(callsPermissionEnabled()){
                    Toast.makeText(requireContext(), "calls already enabled",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                requestPhonePermission();
            }
        });

        // continue button

        btn_continue_fp = view.findViewById(R.id.btn_continue_fp);
        btn_continue_fp.setBackgroundResource(R.drawable.btn_round_nogood);
        btn_continue_fp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = requireContext()
                        .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        .getString("userName", "");
                String userPhone = requireContext()
                        .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        .getString("userPhone", "");


                // handle different missing permissions cases
                if(allAllowed()) {
                    stopPermissionTrack();
                    // user exists but canceled some permissions
                    if(!userName.equals("") && !userPhone.equals("")){
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment, new TerminalFragment(), "devices").commit();
                    }
                    // user don't exists yet, need to register
                    else{
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment, new RegisterFragment(), "devices").commit();
                    }
                }
                else
                {
                    // continue button
                    boolean locationEnabled = LocationPermissionEnabled();
                    boolean notificationEnabled = NotificationPermissionEnabled();
                    boolean smsEnabled = SmsPermissionEnabled();
                    boolean phoneEnabled = callsPermissionEnabled();


                    if(!locationEnabled && notificationEnabled && smsEnabled && phoneEnabled){
                        Toast.makeText(requireContext(), "Please allow location permission",
                                Toast.LENGTH_SHORT).show();
                    }
                    else if(locationEnabled && !notificationEnabled && smsEnabled && phoneEnabled){
                        Toast.makeText(requireContext(), "Please allow notifications",
                                Toast.LENGTH_SHORT).show();
                    }
                    else if(locationEnabled && notificationEnabled && !smsEnabled && phoneEnabled){
                        Toast.makeText(requireContext(), "Please allow SMS permission",
                                Toast.LENGTH_SHORT).show();
                    }
                    else if(locationEnabled && notificationEnabled && smsEnabled && !phoneEnabled){
                        Toast.makeText(requireContext(), "Please allow phone calls",
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(requireContext(), "Please allow all permissions",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        permissionTrack();

        return view;
    }

    // request permission functions

    private void requestLocationPermission(){
        ActivityCompat.requestPermissions((Activity) requireContext(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                1);
    }

    private void requestNotificationPermission() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
        startActivity(intent);
    }

    private void requestSmsPermission(){
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);
    }

    private void requestPhonePermission(){
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.CALL_PHONE}, 1);
    }

    // check permissions

    private boolean NotificationPermissionEnabled() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat
                .from(requireContext());
        return notificationManagerCompat.areNotificationsEnabled();
    }

    private boolean LocationPermissionEnabled() {
        int permissionStatus = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    private boolean SmsPermissionEnabled() {
        int permissionStatus = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS);
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    private boolean callsPermissionEnabled() {

        return ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean allAllowed(){
        return (callsPermissionEnabled() && SmsPermissionEnabled() &&
                LocationPermissionEnabled() && NotificationPermissionEnabled());
    }

    private void permissionTrack() {
        Handler handler = new Handler(Looper.getMainLooper());
        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (LocationPermissionEnabled()) {
                                v1.setVisibility(View.VISIBLE);
                            } else {
                                v1.setVisibility(View.INVISIBLE);
                            }
                            if (NotificationPermissionEnabled()) {
                                v2.setVisibility(View.VISIBLE);
                            } else {
                                v2.setVisibility(View.INVISIBLE);
                            }
                            if (SmsPermissionEnabled()) {
                                v3.setVisibility(View.VISIBLE);
                            } else {
                                v3.setVisibility(View.INVISIBLE);
                            }
                            if (callsPermissionEnabled()) {
                                v4.setVisibility(View.VISIBLE);
                            } else {
                                v4.setVisibility(View.INVISIBLE);
                            }
                            if (allAllowed()) {
                                btn_continue_fp.setBackgroundResource(R.drawable.round_button);
                            }
                        }
                    });

                    try {
                        Thread.sleep(1000); // Delay between each iteration
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
            backgroundThread.interrupt();
            backgroundThread = null;
        }
    }


}