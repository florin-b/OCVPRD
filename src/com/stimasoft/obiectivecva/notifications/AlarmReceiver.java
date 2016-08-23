package com.stimasoft.obiectivecva.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.stimasoft.obiectivecva.models.db_classes.User;
import com.stimasoft.obiectivecva.utils.Constants;
import com.stimasoft.obiectivecva.utils.SharedPrefHelper;

/**
 * Broadcast receiver that schedules service alarms on user Login / Reboot every 10 fuzzy minutes
 */
public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DBG_R", "Am primit alarma");
        // Set the alarm here.
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent serviceIntent = new Intent(context, ServiceSendNotification.class);

        SharedPrefHelper prefHelper = new SharedPrefHelper(context);
        // Set alarms only if user is logged in
        User user = prefHelper.getUserDetails();
        if(user != null){
        	Log.d("DBG_R", "Userul era logat");
	        if(user.getUserType() == User.TYPE_CVA) {
	        	Log.d("DBG_R", "Exista user logat. Intent-ul: " + intent.getAction());
	            if (intent.getAction().equals(Constants.NOTIFICATIONS)) {
	                // This happens only when the user logs in. Alarms are reset.
	
	                PendingIntent registeredPendingIntent = PendingIntent.getService(context,
	                        Constants.ALARM_REQUEST_CODE,
	                        serviceIntent,
	                        PendingIntent.FLAG_NO_CREATE);
	
	                if (registeredPendingIntent != null) {
	                    alarmManager.cancel(registeredPendingIntent);
	                    registeredPendingIntent.cancel();
	                }
	
	                PendingIntent pIntent = PendingIntent.getService(context, Constants.ALARM_REQUEST_CODE, serviceIntent, 0);
	
	                alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	                        SystemClock.elapsedRealtime() + 10 * 1000, 600 * 1000, pIntent);
	
	            } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
	                if (prefHelper.isLoggedIn()) {
	                    Log.d("DBG_R", "Pun alarma dupa restart");
	                    PendingIntent pIntent = PendingIntent.getService(context, Constants.ALARM_REQUEST_CODE, serviceIntent, 0);
	
	                    alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
	                            SystemClock.elapsedRealtime() + 10 * 1000, 600 * 1000, pIntent);
	                }
	            }
	        }
        }
    }
}
