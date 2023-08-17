package com.example.WatchMeAI;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


public class MainActivity extends AppCompatActivity implements FragmentManager
        .OnBackStackChangedListener, ActionFragment.NotificationCallback{

    /* life circle */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // set background
        findViewById(R.id.imageView).setBackgroundResource(R.drawable.bg2);
//        String tod = AuxiliaryFunctions.getTimeOfDay();
//        switch (tod) {
//            case "Morning":
//                findViewById(R.id.imageView).setBackgroundResource(R.drawable.t_1);
//                break;
//            case "Afternoon":
//                findViewById(R.id.imageView).setBackgroundResource(R.drawable.t_2);
//                break;
//            case "Evening":
//                findViewById(R.id.imageView).setBackgroundResource(R.drawable.t_3);
//                break;
//            case "Night":
//                findViewById(R.id.imageView).setBackgroundResource(R.drawable.t_4);
//                break;
//        }

        // set tool bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        // handle notification intents
        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("action_notification")){
            onNewIntent(intent);
            return;
        }

        // UI
        TextView appView = findViewById(R.id.appView);
        ImageView imageView = findViewById(R.id.imageView2);
        AuxiliaryFunctions.animateTextView(appView);
        AuxiliaryFunctions.animateImageView(imageView);

//        // handle restart case
//        if (savedInstanceState != null) {
//            appView.setText("");
//            imageView.setVisibility(View.INVISIBLE);
//        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                appView.setText("");
                imageView.setVisibility(View.INVISIBLE);
                Boolean firstTime = (Boolean) isFirstTimeOpen();
                // if first launch
                if(firstTime || AuxiliaryFunctions.noKnownUser(MainActivity.this)){
                    aboutFragment aboutFragment = new aboutFragment();
                    Bundle args = new Bundle();
                    args.putBoolean("firstTime", firstTime);
                    aboutFragment.setArguments(args);
                    if (savedInstanceState == null)
                        getSupportFragmentManager().beginTransaction().add(R.id.fragment,
                                aboutFragment).commit();
                    else
                        onBackStackChanged();
                } // else, welcome message and start app
                else{
                    getSupportFragmentManager().beginTransaction().add(R.id.fragment,
                            new TerminalFragment(), "devices").commit();
                }
            }
        }, 2500);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if the intent contains data from the notification
        if (intent.hasExtra("action_notification")) {

            NotificationHandlerFragment fragment = (NotificationHandlerFragment)
                    getSupportFragmentManager().findFragmentByTag("tag");
            if(fragment != null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, fragment).commit();
            }
            else{
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new NotificationHandlerFragment()).commit();
            }
        }
    }

    /* Functionality */

    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager()
                .getBackStackEntryCount()>0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    /* Auxiliary Functions */

    private boolean isFirstTimeOpen() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs"
                , Context.MODE_PRIVATE);

        boolean isFirstTime = sharedPreferences.getBoolean("isFirstTime", true);
        if (isFirstTime) {
            // Set the flag to false to indicate that the app has been opened before
            sharedPreferences.edit().putBoolean("isFirstTime", false).apply();
        }
        return isFirstTime;
    }

    @Override
    public void onNotificationDataReceived(String data) {
        Log.d("tg", "main - onNotificationDataReceived");
        ActionFragment fragment = (ActionFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment);
        if (fragment != null) {
            fragment.handleNotificationData(data);
        }
    }

}
