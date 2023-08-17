package com.example.WatchMeAI;

import android.app.Activity;
import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;

import pl.droidsonroids.gif.GifImageView;

public class ActionFragment extends NotificationHandlerFragment implements ServiceConnection,
        SerialListener {

    // Serial Connection
    private enum Connected { False, Pending, True }
    private Connected connected = Connected.False;
    private SerialService service;  // communication with serial device (e.g. USB)
    private boolean initialStart = true;
    private String newline = TextUtil.newline_crlf;

    private Thread backgroundThread;
    private int delay = 0;
    private int noConnection = 0;

    // User Information (1-3 must provide, 4-5 has default values)
    String deviceAddress;
    String userName;
    String userPhone;
    String userPhone2;
    String userRegularHeartRate;
    String userTimer;
    String sms_or_call;

    // UI
    TextView tv_timer;
    TextView tv_ask;
    GifImageView gifImageView;
    TextView tv_connecting;
    Button btn_help;
    Button btn_imOk;
    Button btn_cancelHelp;
    Button btn_finish;

    // danger detection
    CountDownTimer countDownTimer;
    private boolean detectedDanger = false;
    private String actionType;
    private String[] trackOn;

    // values history
    ArrayList<Float> accX = new ArrayList<>(Collections.nCopies(100, 0.0f));
    ArrayList<Float> accY = new ArrayList<>(Collections.nCopies(100, 0.0f));
    ArrayList<Float> accZ = new ArrayList<>(Collections.nCopies(100, 0.0f));
    ArrayList<Float> pulse = new ArrayList<>(Collections.nCopies(100, 100.0f));
    ArrayList<String> locations = new ArrayList<>(Collections.nCopies(3, ""));


    int location_sampleTime_counter = 0;
    int missing_location_counter = 0;
    boolean notified_noGPS = false;
    int r_counter = 0;

    int pulse_counter = 0;
    int pulse_threat_counter = 0;


    //_____________________________________________________________________________________________

    /** Lifecycle **/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // receive user chosen tracking activity
        Bundle args = getArguments();
        actionType = args.getString("requested_action");
        if(actionType.equals("Custom")){
            trackOn = args.getStringArray("trackOn");
        }

        // retrieve user information
        deviceAddress = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .getString("userInput", "");
        userName = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .getString("userName", "");
        userPhone = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .getString("userPhone", "");
        userPhone2 = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .getString("userPhone2", "");
        userRegularHeartRate = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .getString("userRegularHeartRate", "");
        userTimer = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .getString(actionType + " Timer", "");
        sms_or_call = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                .getString("sms_or_call", "");
    }

    @Override
    public void onDestroy() {
        if (connected != Connected.False)
            disconnect();
        getActivity().stopService(new Intent(getActivity(), SerialService.class));

        // Stop the background thread
        if (backgroundThread != null) {
            backgroundThread.interrupt();
            backgroundThread = null;
        }

        // restore default custom timer
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Custom Timer", "60");
        editor.apply();

        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            // prevents service destroy on unbind from recreated activity caused by orientation change
            getActivity().startService(new Intent(getActivity(), SerialService.class));
    }

    @Override
    public void onStop() {
        super.onStop();
//        if(backgroundThread != null){
//            backgroundThread.interrupt();
//        }
    }

    @SuppressWarnings("deprecation") // onAttach(context) was added with API 23.
    // onAttach(activity) works for all API versions
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        trackWorkingConnection();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
        if(initialStart && isResumed()) {
            initialStart = false;
            getActivity().runOnUiThread(this::connect);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    /**  UI  **/

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_action, container, false);

        // TextViews
        TextView tv_header = view.findViewById(R.id.tv_header);
        TextView tv_custom = view.findViewById(R.id.tv_custom_header);
        if(!actionType.equals("Custom")){
            if(actionType.equals("Full Watch")){
                tv_header.setText("Watch Me");
            }
            else if(actionType.equals("Track")){
                tv_header.setText("Hike");
            }
            else if(actionType.equals("Relax Walk")){
                tv_header.setText("Walk");
            }
        }
        else{
            tv_header.setText("Custom Action");
            String a = "";
            String b = "";
            String c = "";
            String d = "";
            if(trackOn[0].equals("yes")){
                a += ", Pace";
            }
            if(trackOn[1].equals("yes")){
                b += ", Pulse";
            }
            if(trackOn[2].equals("yes")){
                c += ", Fall";
            }
            if(trackOn[3].equals("yes")){
                d += ", Live location";
            }
            String str = "keeping track of" + a + b + c + d;

            int firstCommaIndex = str.indexOf(",");
            int lastCommaIndex = str.lastIndexOf(",");

            if(firstCommaIndex != lastCommaIndex){
                str = str.substring(0, firstCommaIndex) + str.substring(firstCommaIndex + 1, lastCommaIndex) + " and" + str.substring(lastCommaIndex + 1);
            }
            else{
                str = str.replace(",", "");
            }



            tv_custom.setText(str);
        }

        gifImageView = view.findViewById(R.id.gif_image_view);
        tv_ask = view.findViewById(R.id.tv_ask);
        tv_ask.setText("Watching");
        tv_timer = view.findViewById(R.id.tv_timer);

        //// connecting view, hide 'watching'
        tv_connecting = view.findViewById(R.id.tv_connecting);
        tv_connecting.setText("Connecting . . .");
        gifImageView.setVisibility(View.INVISIBLE);
        tv_ask.setVisibility(View.INVISIBLE);

        // Buttons
        Button btn_callHelp = (Button) view.findViewById(R.id.btn_callHelp_fa);
        btn_callHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDestroy();

                Bundle bundle = new Bundle();
                bundle.putString("wasIn", actionType);
                if(actionType.equals("Custom")){
                    bundle.putStringArray("trackOn", trackOn);
                }
                if(backgroundThread != null && backgroundThread.isAlive()){
                    backgroundThread.interrupt();
                }
                PoliceFragment policeFragment = new PoliceFragment();
                policeFragment.setArguments(bundle);
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, policeFragment,"con").addToBackStack("con")
                        .commitAllowingStateLoss();


//                requireActivity().getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragment, new PoliceFragment(), "devices").commit();
            }
        });

        btn_help = (Button) view.findViewById(R.id.btn_help);
        btn_help.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(backgroundThread != null && backgroundThread.isAlive()){
                    backgroundThread.interrupt();
                }

                btn_cancelHelp.setVisibility(View.VISIBLE);
                callHelp();
                try{
                    countDownTimer.cancel();
                }
                catch (Exception ignored){}
                if(backgroundThread != null && backgroundThread.isAlive()){
                    backgroundThread.interrupt();
                }
                tv_timer.setVisibility(View.INVISIBLE);
                detectedDanger = false;
//                btn_imOk.performClick(); // hide timer
//                detectedDanger = true; // undo part of imOk button virtual click
                tv_ask.setText("Called help! Hang on!");
                makeSound("called_help");
            }
        });

        btn_cancelHelp = (Button) view.findViewById(R.id.btn_cancelHelp);
        btn_cancelHelp.setVisibility(View.INVISIBLE);
        btn_cancelHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(requireContext(), "canceled help", Toast.LENGTH_SHORT).show();
                tv_ask.setText("Watching");
                btn_cancelHelp.setVisibility(View.INVISIBLE);
                btn_imOk.performClick();
                AuxiliaryFunctions.sendSMS(userPhone, userPhone2, "all good");
                pulse = new ArrayList<>(Collections.nCopies(100, 100.0f));
            }
        });

        btn_imOk = (Button) view.findViewById(R.id.btn_imOk);
        btn_imOk.setVisibility(View.GONE);
        btn_imOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pulse = new ArrayList<>(Collections.nCopies(100, 100.0f));
                detectedDanger = false;
                countDownTimer.cancel();
                tv_timer.setText("");
                tv_ask.setText("Watching");
                btn_imOk.setVisibility(View.GONE);
                try{
                    backgroundThread.start();
                }
                catch (Exception ignored){}
            }
        });

        btn_finish = (Button) view.findViewById(R.id.btn_finish_full);
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(backgroundThread != null){
                    backgroundThread.interrupt();
                }
                btn_imOk.performClick();
                onDestroy();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new TerminalFragment(), "devices").commit();
            }
        });

        countDownTimer = new CountDownTimer((int) Integer.parseInt(userTimer) * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tv_timer.setText(String.valueOf(seconds));

                float timeLeft =Float.parseFloat((String)tv_timer.getText());
                if(timeLeft % 10 == 0){
                    String helpIn = "Calling help in " + String.format("%.0f", timeLeft) + " seconds!";
                    AuxiliaryFunctions.pushNotification(requireContext(), "full", "is everything ok?", helpIn, "ActionFragment");
                }
            }
            @Override
            public void onFinish() {
                btn_help.performClick();
            }
        };

        return view;
    }

    /* Serial */

    private void connect() {

        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            status("connecting...");
            connected = Connected.Pending;
            SerialSocket socket = new SerialSocket(getActivity().getApplicationContext(), device);
            service.connect(socket);
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    private void disconnect() {
        connected = Connected.False;
        service.disconnect();
    }

    private void receive(byte[] message) {

        // working connection counter tracker
        noConnection = 0;

        // handling input
        String msg = new String(message);
        if (newline.equals(TextUtil.newline_crlf) && msg.length() > 0) {
            // don't show CR as ^M if directly before LF
            String msg_to_save = msg;
            msg_to_save = msg.replace(TextUtil.newline_crlf, TextUtil.emptyString);
            // check message length
            if (msg_to_save.length() > 1) {
                // fix input
                String[] parts = msg_to_save.split(",");
                parts = AuxiliaryFunctions.clean_str(parts);

                // fix arduino fuck ups
                int dotIndex = parts[3].lastIndexOf(".");
                if(dotIndex != -1){
                    parts[3] = parts[3].substring(0, dotIndex);
                }

                try{
                    fillStacks(parts);
                    if(!detectedDanger){
                        detectThreats(actionType);
                    }
                }
                catch (Exception ignored){}
            }
        }
    }

    private void status(String str) {
        SpannableStringBuilder spn = new SpannableStringBuilder(str + '\n');
        spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorStatusText)), 0, spn.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /**  SerialListener **/

    @Override
    public void onSerialConnect() {
        tv_connecting.setText("");
        gifImageView.setVisibility(View.VISIBLE);
        tv_ask.setVisibility(View.VISIBLE);
        status("connected");
        connected = Connected.True;
    }

    @Override
    public void onSerialConnectError(Exception e) {
        onDestroy();
        Bundle bundle = new Bundle();
        bundle.putString("wasIn", actionType);
        if(actionType.equals("Custom")){
            bundle.putStringArray("trackOn", trackOn);
        }
        if(backgroundThread != null && backgroundThread.isAlive()){
            backgroundThread.interrupt();
        }
        NoConnectionFragment noConnectionFragment = new NoConnectionFragment();
        noConnectionFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, noConnectionFragment,"con").addToBackStack("con")
                .commitAllowingStateLoss();
        status("connection failed: " + e.getMessage());
        disconnect();
    }

    @Override
    public void onSerialRead(byte[] data) {
        try {
            receive(data);}
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSerialIoError(Exception e) {
        if(detectedDanger){
            callHelp();
        }
        onDestroy();

        Bundle bundle = new Bundle();
        bundle.putString("wasIn", actionType);
        if(actionType.equals("Custom")){
            bundle.putStringArray("trackOn", trackOn);
        }
        if(backgroundThread != null && backgroundThread.isAlive()){
            backgroundThread.interrupt();
        }
        NoConnectionFragment noConnectionFragment = new NoConnectionFragment();
        noConnectionFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, noConnectionFragment,"con").addToBackStack("con")
                .commitAllowingStateLoss();

        status("connection lost: " + e.getMessage());
        disconnect();
    }


    /** Auxiliary Functions **/

    private void trackWorkingConnection(){
        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {

                    // time for first launch
                    if(delay<3){
                        delay += 1;
                        continue;
                    }
                    // check for working connection (allow 10 straight missing 'receive' entries)
                    if(noConnection < 10){
                        noConnection+=1;
                    }
                    // alert user that device not working properly
                    else{
                        // call help immediately in case danger detected
                        if(detectedDanger){
                            callHelp();
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString("error_type", "connected_not_working");
                        bundle.putString("wasIn", actionType);
                        if(actionType.equals("Custom")){
                            bundle.putStringArray("trackOn", trackOn);
                        }
                        if(backgroundThread != null && backgroundThread.isAlive()){
                            backgroundThread.interrupt();
                        }
                        NoConnectionFragment noConnectionFragment = new NoConnectionFragment();
                        noConnectionFragment.setArguments(bundle);
                        onDestroy();
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment, noConnectionFragment, "con").addToBackStack("con")
                                .commitAllowingStateLoss();
                    }

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

    public void fillStacks(String[] values){
        accX.add(Float.parseFloat(values[0]));
        accX.remove(0);

        accY.add(Float.parseFloat(values[1]));
        accY.remove(0);

        accZ.add(Float.parseFloat(values[2]));
        accZ.remove(0);

        // keep only 1 out of every 12 signals
        if(pulse_counter == 12){
            pulse.add(Float.parseFloat(values[3]));
            pulse.remove(0);
            pulse_counter = 1;
        }
        else{
            pulse_counter += 1;
        }
    }

    public void handleNotificationData(String data) {
        // Handle the data here based on your requirements
    }

    public interface NotificationCallback {
        void onNotificationDataReceived(String data);
    }

    private void makeSound(String fileName) {
        MediaPlayer mediaPlayer = MediaPlayer.create(requireActivity(), getResourceIdFromFileName(fileName));
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mediaPlayer.start();
    }

    private int getResourceIdFromFileName(String fileName) {
        return getResources().getIdentifier(fileName, "raw", requireActivity().getPackageName());
    }

    private double norm(float x, float y, float z){
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }


    /**  Threat Detection **/

    public void detectThreats(String type){

        switch (type){
            case "Full Watch":  //watchMe option
                detectedDanger = watchMeDetection();
                break;
            case "Relax Walk": // walk option
                detectedDanger = walkDetection();
                break;
            case "Track":  // hike option
                detectedDanger = hikeDetection();
                break;
            case "Custom":
                detectedDanger = customDetection();
                break;
        }
        if(detectedDanger){
            btn_imOk.setVisibility(View.VISIBLE);
            detectedDanger = true;
            countDownTimer.start();
            tv_ask.setText("is everything ok?");

            AuxiliaryFunctions.pushNotification(requireContext(), type,
                    "Hey!", "is everything ok?", "ActionFragment");
        }
    }

    public boolean watchMeDetection(){

        boolean fall = fallDetection();
        boolean pulse = pulseDetection();

        return (fall || pulse);
    }

    public boolean walkDetection(){

        boolean walkRun = runWalkDetection();
        boolean pulse = pulseDetection();

        return (walkRun|| pulse);
    }

    public boolean hikeDetection(){

        boolean fall = fallDetection();
        gpsDetection();

        return fall;
    }

    public boolean customDetection(){

        boolean runWalk = false;
        boolean fall = false;
        boolean pulse = false;

        if(trackOn[0].equals("yes")){
            runWalk = runWalkDetection();
        }
        if(trackOn[1].equals("yes")){
            pulse = pulseDetection();
        }
        if(trackOn[2].equals("yes")){
            fall = fallDetection();
        }
        if(trackOn[3].equals("yes")){
            gpsDetection();
        }
        return (runWalk || fall || pulse);
    }

    // Actions Algorithms

    public boolean fallDetection(){

//        Log.d("tg", String.valueOf(norm(accX.get(99), accY.get(99), accZ.get(99))));
        for(int j=0; j<3; j++){

            boolean all = true;
            double cur_norm = norm(accX.get(89+j), accY.get(89+j), accZ.get(89+j));
            double prev_norm;
            if(cur_norm>20){
                for(int i=79; i<89; i++){
                    prev_norm = norm(accX.get(i), accY.get(i), accZ.get(i));
                    all = all & cur_norm > (prev_norm+0.001) * 1.5;
                }
                for(int i=92; i<99; i++){
                    prev_norm = norm(accX.get(i), accY.get(i), accZ.get(i));
                    all = all & cur_norm > (prev_norm+0.001) * 1.5;
                }

                if(all){
                    return true;
                }
            }


        }

        return false;

    }

    public boolean runWalkDetection(){

        if(r_counter<100){
            r_counter += 1;
            return false;
        }

        if(norm(accX.get(99), accY.get(99), accZ.get(99)) < 15){
            return false;
        }

        int runCount = 0;


        double[] vec = new double[28];
        for(int i=79, j=0; i<100; i++){
            vec[j] = (double) accX.get(i);
            vec[j+1] = (double) accY.get(i);
            vec[j+2] = (double) accZ.get(i);
            vec[j+3] = norm(accX.get(i), accY.get(i), accZ.get(i));
            j += 4;

            if(i==85 || i==92){
                j=0;
                double[] out = layer1(vec);
                out = layer2(out);
                double prob = layer3(out);
                if(prob > 0.55){
                    runCount += 1;
                }
            }
        }

        if(runCount > 1){
            return true;
        }



//        if(prob > 0.65){
//            Log.d("tg", "running");
//            return true;
//
//        }
//        else {
//            Log.d("tg", "walking");
//
//        }

        return false;
    }

    public boolean pulseDetection(){

        double pulse_mean = 0;
        for(int i=0; i<100; i++){
            pulse_mean += pulse.get(i);
        }

        if(pulse_mean < 20 || pulse_mean > 130){
            pulse_threat_counter += 1;
        }
        else{
            pulse_threat_counter = 0;
        }

        if(pulse_threat_counter > 100){
            pulse_threat_counter = 0;
            return false;


        }
        return false;

    }

    public void gpsDetection(){

        Log.d("tg", "GPS");
        try{
            LocationHelper locationHelper;
            locationHelper = new LocationHelper(requireContext());
            String current_location = locationHelper.startLocationUpdates();
            Log.d("tg", current_location);
        }
        catch (Exception e){
            Log.d("tg", "exep");
            if(!notified_noGPS){
                if(!locations.get(2).equals("")){
                    String[] splitStrings = locations.get(2).split("@");
                    String coordinates = splitStrings[1].trim().substring(0, 5) +
                            ',' + splitStrings[2].trim().substring(0, 5);
                    String text = "NO GPS! Your last known location: " +
                            "https://www.google.com/maps/place/" + coordinates;
                    AuxiliaryFunctions.pushNotification(requireContext(), "Track", "No GPS!",
                            text, "ActionFragment");
                }
                else{
                    AuxiliaryFunctions.pushNotification(requireContext(), "Track", "No GPS!",
                            "", "ActionFragment");
                }
                notified_noGPS = true;
            }
        }


        location_sampleTime_counter += 1;
        if(location_sampleTime_counter == 50){
            location_sampleTime_counter = 0;
            LocationHelper locationHelper;
            locationHelper = new LocationHelper(requireContext());
            String current_location = locationHelper.startLocationUpdates();
//            Log.d("tg", current_location);
            if(current_location.equals("")){  // no available location

                missing_location_counter += 1;
                if(missing_location_counter == 3){
                    missing_location_counter = 0;

                    // notify once when there is no GPS
                    if(!notified_noGPS){
                        if(!locations.get(2).equals("")){
                            String[] splitStrings = locations.get(2).split("@");
                            String coordinates = splitStrings[1].trim().substring(0, 5) +
                                    ',' + splitStrings[2].trim().substring(0, 5);
                            String text = "NO GPS! Your last known location: " +
                                    "https://www.google.com/maps/place/" + coordinates;
                            AuxiliaryFunctions.pushNotification(requireContext(), "Track", "No GPS!",
                                    text, "ActionFragment");
                        }
                        else{
                            AuxiliaryFunctions.pushNotification(requireContext(), "Track", "No GPS!",
                                    "", "ActionFragment");
                        }
                        notified_noGPS = true;
                    }
                }
            }
            else{
                locations.add(current_location);
                locations.remove(0);
                notified_noGPS = false;
            }
        }
    }

    // runWalkDetection NN Auxiliary functions

    public static double[] vecmat(double[] vector, double[][] matrix) {
        int vectorLength = vector.length;
        int matrixRows = matrix.length;
        int matrixCols = matrix[0].length;

        if (vectorLength != matrixCols) {
            throw new IllegalArgumentException("Vector length must match matrix column count");
        }

        double[] result = new double[matrixRows];

        for (int i = 0; i < matrixRows; i++) {
            for (int j = 0; j < vectorLength; j++) {
                result[i] += vector[j] * matrix[i][j];
            }
        }

        return result;
    }

    public static double[] add(double[] vector1, double[] vector2) {
        double[] result = new double[vector1.length];
        for (int i = 0; i < vector1.length; i++) {
            result[i] = vector1[i] + vector2[i];
        }
        return result;
    }

    public static double dot(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vector lengths do not match");
        }

        double dotProduct = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
        }

        return dotProduct;
    }

    public static double[] relu(double[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = Math.max(0, vector[i]);
        }
        return result;
    }

    public static double[] leakyRelu(double[] vector) {
        double[] result = new double[vector.length];
        for (int i = 0; i < vector.length; i++) {
            result[i] = (vector[i] >= 0) ? vector[i] : 0.01 * vector[i];
        }
        return result;
    }

    public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    public static double[] layer1(double[] vector){
        double[][] mat = {{     0.0604353,      0.0059299,     -0.2285128,      0.0387725,
                0.1836419,     -0.0440568,     -0.0987561,      0.1902702,
                0.2755782,     -0.1080983,     -0.0447831,      0.2472759,
                0.3047139,     -0.0258612,     -0.2987202,      0.1815877,
                0.1944320,     -0.2662191,     -0.1482775,      0.0448390,
                -0.0074251,     -0.0211261,     -0.1505054,     -0.1415648,
                0.4772506,     -0.2851114,      0.0358519,      0.2340990},
                {    -0.4513517,     -0.3837094,     -0.4312678,      0.1421755,
                        -0.0818676,     -0.1450455,     -0.3768971,      0.1202434,
                        -0.0862693,     -0.0627197,     -0.0254138,     -0.0434969,
                        -0.1030149,      0.1308757,     -0.1767980,      0.0826210,
                        -0.5066044,     -0.2542349,     -0.4269018,     -0.0200893,
                        -0.2877821,      0.0829171,     -0.6340328,      0.0277386,
                        -0.2068399,      0.0483540,     -0.3296415,      0.1082633},
                {     0.0883147,     -0.0784522,     -0.1301166,      0.1139077,
                        -0.0953075,      0.3111036,      0.1683336,      0.0957963,
                        -0.0790028,      0.0359851,      0.1536499,     -0.0413851,
                        0.0111401,     -0.1557326,     -0.2443148,      0.0312905,
                        0.0819428,     -0.2410861,     -0.4388602,      0.0085577,
                        0.2013311,      0.2069132,      0.0670170,      0.0404415,
                        0.0976739,     -0.1070593,      0.2157104,     -0.0891236},
                {     0.0348135,      0.5008529,     -0.0484056,     -0.1639322,
                        -0.0654484,      0.2200101,     -0.0634660,     -0.0357477,
                        0.0683705,      0.0686241,      0.0173424,     -0.0003976,
                        -0.0626458,      0.3582009,     -0.0265432,     -0.0297411,
                        0.0332722,      0.5490323,      0.0052389,     -0.0959983,
                        0.1172713,      0.0816815,      0.1616089,     -0.1591561,
                        0.0586410,     -0.0391896,     -0.0243280,      0.0645338},
                {     0.0340359,     -0.0147315,     -0.3327816,      0.0427577,
                        -0.2335636,     -0.2633535,     -0.2562028,      0.2756721,
                        -0.1288373,     -0.0622041,      0.1153958,      0.2661319,
                        -0.3211359,      0.0349330,     -0.0622071,      0.0909217,
                        -0.5679019,     -0.0784982,     -0.0562706,      0.2070660,
                        -0.2864886,     -0.1413536,      0.0202813,      0.0392833,
                        0.1318735,     -0.0794827,     -0.1196363,     -0.2260138},
                {    -0.0074157,      0.1374191,      0.1418138,      0.2537668,
                        -0.1041104,     -0.1077165,      0.0705909,      0.0426161,
                        0.0530264,     -0.0299531,      0.2894367,      0.0188303,
                        0.0377289,      0.1512159,      0.2254299,      0.0827316,
                        0.2389669,     -0.0032568,      0.0410470,     -0.1924724,
                        0.1735418,     -0.0497032,      0.1712231,     -0.1547857,
                        0.1632637,      0.1279712,      0.3593383,     -0.1718953},
                {    -0.2463489,     -0.2973438,      0.1833932,      0.1553872,
                        -0.0354933,     -0.1737680,      0.1510188,     -0.0275000,
                        0.1812207,      0.2872450,      0.0085039,      0.0398572,
                        0.2589927,     -0.0990480,      0.0589424,      0.0395463,
                        0.1200369,     -0.3251160,     -0.0680969,     -0.1880574,
                        -0.1723553,     -0.0043892,     -0.0580213,     -0.1366497,
                        -0.1477505,      0.4896609,      0.1055436,      0.0489737},
                {     0.1412654,      0.4795248,      0.3366266,     -0.1005918,
                        0.0664875,      0.1471184,     -0.1367335,     -0.1261604,
                        -0.0642410,     -0.2648016,     -0.3139067,     -0.0273199,
                        0.0826978,     -0.1455742,      0.0947414,      0.0551033,
                        0.1839725,     -0.0145933,      0.0289803,     -0.1284765,
                        -0.1301989,      0.0986009,      0.0343954,      0.1605405,
                        -0.1816466,     -0.0743582,      0.1083642,      0.2781141},
                {     0.0692504,     -0.0690090,      0.2208543,      0.1509521,
                        -0.0845060,     -0.3655368,     -0.0464818,     -0.1124770,
                        -0.0191946,      0.0510047,      0.1111046,     -0.0302606,
                        0.0950157,      0.6709308,      0.2801914,     -0.4334974,
                        0.0201730,      0.0045109,     -0.1834660,     -0.0536182,
                        0.1399116,     -0.1744444,     -0.2164810,      0.0378368,
                        0.0258983,     -0.0793702,     -0.0184750,     -0.0325036},
                {     0.3783134,     -0.0271229,     -0.0573712,     -0.0677182,
                        0.5525904,     -0.0976070,      0.0290918,      0.0304146,
                        0.4480636,     -0.1894649,     -0.0661505,      0.0029621,
                        0.3884443,      0.0081880,     -0.0844383,     -0.0763481,
                        0.3380696,     -0.0283363,     -0.0765783,     -0.0600035,
                        0.3908685,     -0.1207079,     -0.0893111,     -0.1340557,
                        0.2881128,     -0.0986956,     -0.1474613,     -0.1117392},
                {    -0.0403930,      0.2228880,     -0.1850442,      0.1125675,
                        0.1981038,      0.0420071,      0.0868664,     -0.0128957,
                        0.3849331,     -0.1973994,      0.0868875,     -0.2430239,
                        -0.0715846,      0.1156734,     -0.3073666,     -0.0185654,
                        -0.1695070,      0.0627884,      0.0126587,      0.2076577,
                        -0.1239191,     -0.2106754,      0.1586882,      0.0348612,
                        0.0780491,      0.0262665,      0.0403885,     -0.0541363},
                {     0.0197534,      0.0606505,     -0.0160577,     -0.0128047,
                        0.0432148,     -0.1247149,     -0.0997319,      0.1421670,
                        -0.0174610,      0.0844526,     -0.0160329,      0.1470871,
                        -0.0148113,      0.2051286,      0.1507500,      0.1433525,
                        0.1165458,     -0.1841868,      0.1610879,      0.0912770,
                        0.2287868,     -0.2804816,     -0.4361395,     -0.2919559,
                        -0.0324173,      0.1918225,     -0.1168336,     -0.1987995},
                {    -0.2000564,      0.0101850,     -0.0307133,      0.1308442,
                        -0.2122416,     -0.0630320,      0.1445999,     -0.0778465,
                        0.0081884,     -0.1391772,     -0.0214894,     -0.1036833,
                        0.1420398,      0.1563750,     -0.2103892,      0.2725812,
                        0.3062702,      0.5047150,      0.1008700,      0.0955992,
                        0.2249610,      0.0983774,      0.2440080,     -0.2333362,
                        0.0201710,     -0.2274517,     -0.1541707,      0.0067168},
                {    -0.0261604,     -0.2544605,      0.0730666,      0.1897581,
                        0.1004623,      0.0630483,     -0.1590293,      0.0784464,
                        0.3271852,     -0.1555382,      0.0704437,      0.0848945,
                        0.2927313,     -0.3164077,      0.1658053,     -0.2064647,
                        -0.5353040,      0.1975002,     -0.2615868,     -0.1775758,
                        -0.2364792,      0.3008668,     -0.1438662,     -0.0359557,
                        -0.0237669,     -0.1817673,     -0.2801400,     -0.0276627},
                {     0.0471661,      0.1078748,     -0.1386792,     -0.0932025,
                        0.0152915,     -0.1231389,     -0.2958184,      0.1788418,
                        0.1315676,     -0.0049791,      0.1113826,      0.2038763,
                        -0.0188995,      0.0959088,      0.1645344,     -0.0598439,
                        -0.1341210,      0.1780083,     -0.0317721,     -0.0573733,
                        -0.0010475,     -0.0208850,     -0.1698349,      0.0534578,
                        0.1108166,     -0.0798065,      0.0200401,      0.1570906},
                {     0.2774906,     -0.3334804,     -0.1562631,     -0.3463078,
                        0.4152234,     -0.0468715,      0.1608890,      0.0153797,
                        -0.0291778,      0.1267979,     -0.1694280,      0.0604921,
                        -0.2119445,      0.0084136,     -0.0432698,      0.0424273,
                        -0.0418458,     -0.0217076,      0.2138584,      0.0312109,
                        -0.0982424,     -0.0232426,      0.1174438,      0.0975126,
                        0.0896706,      0.0052848,     -0.3776868,      0.0269989}}

                ;

        double[] out = vecmat(vector, mat);
        double[] bias = {1.0999961,  0.3543088, -0.5454383,  0.6766526,  0.8088120,  0.1813376,
                -0.1263999, -0.2359277, -0.0212954,  0.8412574, -1.0094624, -1.2413485,
                -0.3854460, -0.4574386, -1.5370051, -0.7619852};
        out = add(out, bias);
        return relu(out);
    }

    public static double[] layer2(double[] vector){
        double[][] mat = {{-0.0682427,  0.2350752, -0.0224881, -0.4302990, -0.0993900,  0.1027374,
                0.0502057, -0.2963941,  0.4383874, -0.2553620, -0.0184516, -0.2042270,
                0.1505892,  0.0035655,  0.0703204,  0.2645463},
                { 0.3463580,  0.1936742, -0.3238005, -0.0328391, -0.0793591,  0.1657667,
                        0.0132946, -0.0711008,  0.1755712, -0.0708221,  0.2186199, -0.2764714,
                        -0.2312069,  0.4195673, -0.2582533, -0.0200930},
                { 0.3707823, -0.0709999,  0.2383518, -0.1380792, -0.3442066, -0.2227293,
                        0.3435367,  0.1760676,  0.4648896, -0.6966237,  0.2853220,  0.3250047,
                        0.2412719,  0.4916617,  0.1101845,  0.3237804},
                {-0.1684619, -0.6871871, -0.2690644, -0.2537631, -0.4155053,  0.0434441,
                        0.3226951, -0.2082927,  0.1379596,  0.3440458,  0.2285670,  0.3843603,
                        -0.1557427, -0.1062790, -0.0470128, -0.0058275},
                {-0.1544513, -0.1140997,  0.4374229, -0.4323693,  0.0735237, -0.0545869,
                        0.4278233,  0.4963441,  0.2332312, -0.1710786,  0.5052412,  0.4759608,
                        0.2692796,  0.4432887,  0.1681595,  0.4673581},
                { 0.0181650, -0.5925455,  0.2047388, -0.2969497,  0.4375208,  0.0862168,
                        0.3627538,  0.2933644,  0.3521441,  0.2081973, -0.0961936,  0.0647071,
                        0.4884225,  0.2741335,  0.2023327,  0.0212697},
                { 0.2049424,  0.0681562, -0.2454323,  0.4148648,  0.0340762,  0.1725700,
                        -0.0723926, -0.0896207, -0.5130637,  0.2932262, -0.4618080, -0.6767524,
                        -0.1866467,  0.3401199, -0.6087148, -0.2207565},
                { 0.1167770, -0.3921254, -0.1704994,  0.1488474, -0.2196181,  0.2867340,
                        0.1564836,  0.0956001,  0.1802378,  0.1051338, -0.3841262, -0.3882023,
                        0.1984871,  0.1938699, -0.4186369, -0.4856442}}
                ;
        double[] out = vecmat(vector, mat);
        double[] bias = {-1.6104500,  1.2618737, -0.3669770,  0.1768908, -0.5814009, -0.0745478,
                0.7270499,  0.3505812};
        out = add(out, bias);
        return leakyRelu(out);
    }

    public static double layer3(double[] vector){
        double[] vec = { 0.7404440, -0.3074177,  1.0595958, -0.7426177,  0.6785147,  0.6782292,
                -0.6680332, -0.6083406};
        double out = dot(vector, vec);
        out = out - 1.0693458;
        return sigmoid(out);
    }

    // ________________________________________________________

    public void callHelp(){
        LocationHelper locationHelper;
        String currentLocation;
        String locationLink;
        String at;
        String message;

        try{
            try{
                countDownTimer.cancel();
            } catch (Exception ignored){}

            Log.d("tg", "current .........");

            locationHelper = new LocationHelper(requireContext());
            currentLocation = locationHelper.startLocationUpdates();

            String[] splitStrings = currentLocation.split("@");

            at = splitStrings[0].trim();

            String coordinates = splitStrings[1].trim().substring(0, 5) +
                    ',' + splitStrings[2].trim().substring(0, 5);

            locationLink = "https://www.google.com/maps/place/" + coordinates;
            message = userName + " needs help! he's at " + at + "\n" +
                    "here is a link for his precise location:  -" + locationLink;
        }
        catch (Exception e){
            message = userName + " needs help!";
        }

        Log.d("tg", message);

        if(sms_or_call.equals("Send SMS")){
            AuxiliaryFunctions.sendSMS(userPhone,userPhone2, message);
        }
        else if(sms_or_call.equals("Call")){
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + userPhone));
            startActivity(callIntent);
        }
        else{
            AuxiliaryFunctions.sendSMS(userPhone,userPhone2, message);
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + userPhone));
            startActivity(callIntent);
        }

        // announce help
        MediaPlayer mediaPlayer = MediaPlayer.create(requireActivity(), R.raw.called_help);
        mediaPlayer.start();
    }

}
























