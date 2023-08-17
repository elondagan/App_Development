package com.example.WatchMeAI;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class NotificationHandlerFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("tg", "what the fuck");

        View view = inflater.inflate(R.layout
                .fragment_notification_hnadlerragment, container, false);

        // TextViews
        TextView tv7 = view.findViewById(R.id.textView7);
        String dete = requireActivity().getIntent().getStringExtra("dete");
        tv7.setText(dete);

        Button btn_imOk = (Button) view.findViewById(R.id.btn_imOk);
        btn_imOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new ActionFragment()).commit();
            }
        });

        return view;
    }
}