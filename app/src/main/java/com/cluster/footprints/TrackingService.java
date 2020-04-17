package com.cluster.footprints;

import com.cluster.footprints.utils.AppPrefs;
import com.cluster.footprints.utils.Globals;
import com.cluster.footprints.utils.UserLocation;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.Manifest;
import android.location.Location;
import android.app.Notification;
import android.content.pm.PackageManager;
import android.app.PendingIntent;
import android.app.Service;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrackingService extends Service {
    private static final String TAG = TrackingService.class.getSimpleName();
    private  AppPrefs appPrefs;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appPrefs = new AppPrefs(getApplicationContext());
        buildNotification();
        loginToFirebase();

    }

    //Create the persistent notification//
    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);

        // Create the persistent notification//
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tracking_enabled_notif))
                //Make this notification ongoing so it can’t be dismissed by the user//
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_my_location_black_24dp);
        startForeground(1, builder.build());
    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Unregister the BroadcastReceiver when the notification is tapped//
            unregisterReceiver(stopReceiver);
            //Stop the Service//
            stopSelf();
        }
    };

    private void loginToFirebase() {
        requestLocationUpdates(FirebaseAuth.getInstance().getCurrentUser());
    }

    //Initiate the request to track the device's location//
    private void requestLocationUpdates(FirebaseUser user) {
        LocationRequest request = new LocationRequest();
        //Specify how often your app should request the device’s location//
        request.setInterval(30000);
        //Get the most accurate location data available//
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        final String path = Globals.location;
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        //If the app currently has access to the location permission...//
        if (permission == PackageManager.PERMISSION_GRANTED) {
            //...then request location updates//
            client.requestLocationUpdates(request, new LocationCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
                    String currentTime = new SimpleDateFormat("HHmmss", Locale.getDefault()).format(new Date());

                    //Get a reference to the database, so your app can perform read and write operations//
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(path).child(user.getUid()).child(currentDate).child(currentTime);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        // check if the distance between the current location
                        // is substantial compare to the previously logged location
                        Log.d("Distance => Saving loc", location.toString());
                        UserLocation userLocation = new UserLocation();
                        userLocation.setmAltitude(location.getAltitude());
                        userLocation.setmBearing(location.getBearing());
                        userLocation.setmElapsedRealtimeNanos(location.getElapsedRealtimeNanos());
                        userLocation.setmLatitude(location.getLatitude());
                        userLocation.setmLongitude(location.getLongitude());
                        userLocation.setmProvider(location.getProvider());
                        userLocation.setmSpeed(location.getSpeed());
                        userLocation.setmTime(location.getTime());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            userLocation.setmElapsedRealtimeUncertaintyNanos(location.getElapsedRealtimeUncertaintyNanos());
                        }
                        if(distanceIsSubstantial(location)) {
                            //Save the location data to the database//
                            ref.setValue(userLocation);
                            // save the most recent location in shared preference
                            appPrefs.setFloatValue(Globals.LAST_LAT, Float.parseFloat(location.getLatitude()+""));
                            appPrefs.setFloatValue(Globals.LAST_LON, Float.parseFloat(location.getLongitude()+""));
                        }
                        // create a record of the user's live location
                        DatabaseReference liveRef = FirebaseDatabase.getInstance().getReference().child(Globals.liveLocation).child(user.getUid());
                        liveRef.setValue(userLocation);
                    }
                }
            }, null);
        }
    }

    private boolean distanceIsSubstantial(Location location) {
        Log.d("Distance", "Checking distance");
        if(appPrefs.getFloatValue(Globals.LAST_LAT) > 0 && appPrefs.getFloatValue(Globals.LAST_LON) > 0) {
            float[] distanceMeters = new float[1];
            Location.distanceBetween(location.getLatitude(), location.getLongitude(), appPrefs.getFloatValue(Globals.LAST_LAT), appPrefs.getFloatValue(Globals.LAST_LON), distanceMeters);
            Log.d("Distance ", distanceMeters[0]+"");
            if(distanceMeters[0] >= Globals.recordableDistance){ // 1km = 1000
                return true;
            }
        }else{
            return true;
        }
        return false;
    }
}
