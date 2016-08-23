package com.stimasoft.obiectivecva;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

/**
 * Created by filip on 18/06/2015.
 */
public class GPSTracker extends Service implements LocationListener {

	private Context context;
	boolean isGPSEnabled = false;
	boolean isNetworkEnabled = false;
	boolean canGetLocation = false;

	Location location;

	double latitude;
	double longitude;

	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;

	protected LocationManager locationManager;

	public GPSTracker(Context context) {
		this.context = context;
		getLocation();
	}

	// Work in progress
	public Location getLocation2() {
		try {
			locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
			isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (isGPSEnabled) {
				this.canGetLocation = true;
				
//				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
//						MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}
			else if(isNetworkEnabled){
				this.canGetLocation = true;
				
//				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
//						MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}

	public Location getLocation() {
		try {
			locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
			isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {

			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				}

				if (isGPSEnabled) {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					if (locationManager != null) {
						location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}

				else if (locationManager != null) {
					location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null) {
						latitude = location.getLatitude();
						longitude = location.getLongitude();
					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return location;
	}

	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
	}

	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}
		return latitude;
	}

	public double getlongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}
		return longitude;
	}

	public boolean isCanGetLocation() {
		return this.canGetLocation;
	}

	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setTitle(R.string.gps_dialog_title);
		alertDialog.setMessage(R.string.gps_message_content);
		alertDialog.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
				context.startActivities(new Intent[] { intent });
				// context.startActivities(intent);
			}
		});

		alertDialog.setNegativeButton(R.string.btn_no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
		//stopUsingGPS(); work in progress
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
