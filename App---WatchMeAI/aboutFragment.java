package com.example.WatchMeAI;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class aboutFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_about, container, false);
        Bundle args = getArguments();

        TextView tv = view.findViewById(R.id.tv_explanation_fabout);
        String explanation =
                "Welcome to WatchMeAI! \n\n" +
                        "WatchMeAI is a next generation smart device that keeps you safe whether walking home, " +
                        "out on a hike, or even while you are at home. \n\n" +

                        "I track your GPS connectivity, pulse, change in walking pace and if you " +
                        "fall – if something happens to you I will message your emergency contact " +
                        "with your location for help.\n" +
                        "I have 3 preset activity profiles based on different monitoring tasks, " +
                        "or you can customize your own with any combination of the above. \n" +
                        "    -    'Watch Me' monitors your pulse and detects falls, and is designed for use at " +
                        "home.\n" +
                        "    -    'Hike' monitors your GPS connection and detects sudden stops, and is designed for any outdoor activity.\n" +
                        "    -    'Walk' monitors your pulse and pace and detects sudden stops. It is designed for situations when you are walking somewhere and feel unsafe.\n" +
                        "The SOS button gives you a direct link to call the police. \n\n" +
                        "To get started, make sure that you have your WatchMeAI smart device paired via Bluetooth " +
                        "to your phone. \n\n" +
                        "No need to fear, I've got your back!\n";
        tv.setText(explanation);

        try{
            final Boolean isFirstTime;
            Boolean isFirstTime1;
            try{
                isFirstTime1 =  args.getBoolean("firstTime");
            } catch (Exception e){
                isFirstTime1 = false;
            }
            isFirstTime = isFirstTime1;
            boolean noKnownUser = AuxiliaryFunctions.noKnownUser(requireContext());

            Button btn_continue = (Button) view.findViewById(R.id.btn_continue);
            if(isFirstTime || noKnownUser){
                btn_continue.setText("get started");
            }
            else{
                btn_continue.setText("back");
            }
            btn_continue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isFirstTime || AuxiliaryFunctions.noKnownUser(requireContext())) {
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment, new PermissionsFragment(), "devices")
                                .commit();
                    } else {
                        requireActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment, new TerminalFragment(), "devices")
                                .commit();
                    }
                }
            });
        } catch (Exception ignored){}

        return view;
    }
}