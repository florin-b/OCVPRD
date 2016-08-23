package com.stimasoft.obiectivecva.notifications;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.stimasoft.obiectivecva.AddEditObjective;
import com.stimasoft.obiectivecva.Authenticator;
import com.stimasoft.obiectivecva.Objectives;
import com.stimasoft.obiectivecva.R;
import com.stimasoft.obiectivecva.models.db_classes.ObjectiveLite;
import com.stimasoft.obiectivecva.models.db_utilities.ObjectiveData;
import com.stimasoft.obiectivecva.utils.Constants;
import com.stimasoft.obiectivecva.utils.SharedPrefHelper;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Service that is fired once every 10 fuzzy minutes by the AlarmReceiver class.
 * On launch, the service checks for soon to expire objectives, launches a notification should it find
 * any and then shuts down.
 */
public class ServiceSendNotification extends IntentService {

    public ServiceSendNotification() {
        super("ServiceSendNotification");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("DBG", "Handle-ui service intent");
        ObjectiveData objectiveData = new ObjectiveData(this);

        SharedPrefHelper sharedPrefs = new SharedPrefHelper(this);

        Calendar dateLimit = Calendar.getInstance();
        dateLimit.add(Calendar.HOUR_OF_DAY, 8);
        ArrayList<ObjectiveLite> objectives = objectiveData.getExpiringObjectives(dateLimit,
                sharedPrefs.getUserDetails().getCode());

        String contentText;
        String smallContentText;

        Calendar calendar = Calendar.getInstance();
        NotificationCompat.Builder mBuilder;

        if (objectives.size() > 0 &&
                (calendar.get(Calendar.HOUR_OF_DAY) < 20 && calendar.get(Calendar.HOUR_OF_DAY) > 8)) {

            Intent addEditIntent;
            PendingIntent resultPendingIntent;

            if (objectives.size() > 1) {
                contentText = "Fazele care au nevoie de atenție sunt: \n";

                for (ObjectiveLite objective : objectives) {
                    contentText += objective.getName() + "\n";
                }

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

                Intent intentAuthenticator = new Intent(this, Authenticator.class);

                Intent intentObjectives = new Intent(this, Objectives.class);
                intentObjectives.putExtra(Constants.OBJECTIVES_MODE, Constants.OBJECTIVES_ONGOING);

                stackBuilder.addNextIntent(intentAuthenticator);
                stackBuilder.addNextIntent(intentObjectives);

                resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

                smallContentText = objectives.size() + " faze au nevoie de atenție!";

                mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_map_white_48dp)
                        .setContentTitle("Expira faze!")
                        .setContentText(smallContentText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));

            } else {
                Intent intentAuthenticator = new Intent(this, Authenticator.class);

                Intent intentObjectives = new Intent(this, Objectives.class);

                intentObjectives.putExtra(Constants.OBJECTIVES_MODE, Constants.OBJECTIVES_ONGOING);
                addEditIntent = new Intent(this, AddEditObjective.class);

                addEditIntent.putExtra(Constants.KEY_ID, objectives.get(0).getId());
                addEditIntent.putExtra(Constants.KEY_PURPOSE, Constants.VALUE_EDIT);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

                stackBuilder.addNextIntent(intentAuthenticator);
                stackBuilder.addNextIntent(intentObjectives);
                stackBuilder.addNextIntent(addEditIntent);

                resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_ONE_SHOT);

                smallContentText = "Urmatoarea faza are nevoie de atentie: " + objectives.get(0).getName();
                mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_map_white_48dp)
                        .setContentTitle("Expira o faza!")
                        .setContentText(smallContentText);
            }

            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setAutoCancel(true);
            mBuilder.setOngoing(true);

            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(uri);

            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            mNotifyMgr.notify(Constants.OBJECTIVE_EXPIRES_NOTIFICATION, mBuilder.build());
        }
    }
}
