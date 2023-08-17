package com.example.WatchMeAI;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Calendar;

class AuxiliaryFunctions {

    // text handling

    static public String[] clean_str(String[] stringsArr){
        for (int i = 0; i < stringsArr.length; i++)  {
            stringsArr[i]=stringsArr[i].replaceAll(" ","");
        }
        stringsArr[0] = stringsArr[0].replaceAll("[^\\d\\-+\\.]", "");
        return stringsArr;
    }

    // text animation

    static public void animateTextView(TextView textView){
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(textView, "scaleX", 1f, 1.5f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(textView, "scaleY", 1f, 1.5f);

        scaleXAnimator.setDuration(1000);
        scaleYAnimator.setDuration(1000);

        scaleXAnimator.start();
        scaleYAnimator.start();
    }

    static public void infiniteAnimateTextView(TextView textView) {
        PropertyValuesHolder scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f);
        PropertyValuesHolder scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f);

        ValueAnimator valueAnimator = ValueAnimator.ofPropertyValuesHolder(scaleXHolder, scaleYHolder);
        valueAnimator.setDuration(1300); // Duration for each animation cycle
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE); // Repeat indefinitely
        valueAnimator.setRepeatMode(ValueAnimator.REVERSE); // Reverse animation on each cycle

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float scaleX = (float) animation.getAnimatedValue("scaleX");
                float scaleY = (float) animation.getAnimatedValue("scaleY");
                textView.setScaleX(scaleX);
                textView.setScaleY(scaleY);
            }
        });
        valueAnimator.start();
    }

    static public void animateImageView(ImageView imageView) {
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.5f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.5f);

        scaleXAnimator.setDuration(1000);
        scaleYAnimator.setDuration(1000);

        scaleXAnimator.start();
        scaleYAnimator.start();
    }



    // information

    static public String getTimeOfDay(){
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        String timeOfDay;

        if (hourOfDay >= 5 && hourOfDay < 12) {
            timeOfDay = "Morning";
        } else if (hourOfDay >= 12 && hourOfDay < 18) {
            timeOfDay = "Afternoon";
        } else if (hourOfDay >= 18 && hourOfDay < 21) {
            timeOfDay = "Evening";
        } else {
            timeOfDay = "Night";
        }
        return timeOfDay;
    }

    static boolean noKnownUser(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String userInput = sharedPreferences.getString("userInput", "");
        String userName = sharedPreferences.getString("userName", "");
        String userPhone = sharedPreferences.getString("userPhone", "");

        return(userInput.equals("") || userName.equals("") || userPhone.equals("") || userName.equals(" "));
    }

    // user interactions

    static void pushNotification(Context context, String type, String title, String additionText, String enterTo) {
        String channelId = type + "_1";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
        builder.setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(additionText) // baseMessage +
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("action_notification", type);
        intent.putExtra("enterTo", enterTo);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Add this line

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, "Channel Name", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(0, builder.build());
    }

    static void sendSMS(String phoneNumber, String phoneNumber2, String message){

        try{
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            if(!phoneNumber2.equals("")){
                smsManager.sendTextMessage(phoneNumber2, null, message, null, null);
            }
        }
        catch (Exception e){
            Log.d("tg", String.valueOf(e));
        }


    }

    static boolean NotificationPermissionEnabled(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat
                .from(context);
        return notificationManagerCompat.areNotificationsEnabled();
    }

    static boolean LocationPermissionEnabled(Context context) {
        int permissionStatus = ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    static boolean SmsPermissionEnabled(Context context) {
        int permissionStatus = ContextCompat.checkSelfPermission(context,
                Manifest.permission.SEND_SMS);
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    static boolean callsPermissionEnabled(Context context) {

        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED;
    }

    static boolean allAllowed(Context context){
        return (callsPermissionEnabled(context) && SmsPermissionEnabled(context) &&
                LocationPermissionEnabled(context) && NotificationPermissionEnabled(context));
    }



}


