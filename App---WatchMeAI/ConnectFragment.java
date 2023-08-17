package com.example.WatchMeAI;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;


import java.util.Set;

public class ConnectFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        /** need to handle no blutooth **/
        if (bluetoothAdapter == null) {
        }
        /** need to handle no permission **/
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED) {
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        SharedPreferences sharedPreferences = requireContext()
                .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userInput = sharedPreferences.getString("userInput", "");


        for (BluetoothDevice device : pairedDevices) {
            if (device.getAddress().equals(userInput)) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new TerminalFragment(), "devices").commit();
            }
        }

    }
}
