package com.example.WatchMeAI;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class PoliceFragment extends Fragment {

    CountDownTimer countDownTimer;
    TextView tv_timer_fp;
    TextView tv_callPolice_fp;
    MediaPlayer mediaPlayer;
    Button btn_ok;
    private String[] trackOn;
    private String backTo;

    ArrayList<Pair<Float, Float>> locationHistory = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaPlayer = MediaPlayer.create(requireActivity(), R.raw.siren_);

        Bundle arguments = getArguments();
        try{
            backTo = arguments.getString("wasIn");
            trackOn = arguments.getStringArray("trackOn");
        }
        catch (Exception e){
            backTo = null;
            trackOn = null;
        }


//        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                // Restart the sound when playback completes
//                mediaPlayer.start();
//            }
//        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_police, container, false);

        tv_timer_fp = view.findViewById(R.id.tv_timer_fp);
        tv_callPolice_fp = view.findViewById(R.id.tv_callPolice_fp);


        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                long seconds = l / 1000;
                tv_timer_fp.setText("In " + String.valueOf(seconds));
            }

            @Override
            public void onFinish() {
                btn_ok.setVisibility(View.INVISIBLE);
                mediaPlayer.start();

                tv_callPolice_fp.setText("POLICE ON THE WAY");
                tv_timer_fp.setText(R.string.calm);
                tv_timer_fp.setTextColor(getResources().getColor(R.color.black));
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + "0543399418"));
                startActivity(callIntent);

                onDestroy();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new TerminalFragment(), "devices").commit();

            }
        };

        btn_ok = (Button) view.findViewById(R.id.btn_cancelHelp_fp);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDownTimer.cancel();

                if(backTo == null){
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment, new TerminalFragment(), "devices").commit();
                }
                else{
                    Bundle args = new Bundle();
                    args.putString("requested_action", backTo);
                    if(backTo.equals("Custom")){
                        args.putStringArray("trackOn", trackOn);
                    }
                    ActionFragment af = new ActionFragment();
                    af.setArguments(args);
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment, af, "tag").addToBackStack("tag").commit();
                }

                Toast.makeText(requireContext(), "Police Canceled", Toast.LENGTH_LONG).show();
                onDestroy();
            }
        });

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        countDownTimer.start();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        onDestroy();
    }
}