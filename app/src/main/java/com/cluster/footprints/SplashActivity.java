package com.cluster.footprints;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.cluster.footprints.auth.SignInActivity;
import com.cluster.footprints.interfaces.JsonResponseCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 35526;
    private Handler handler;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUser();
            }
        }, 3000);
    }

    private void checkUser() {
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            // start tracking service
            liveTracking((res, e)-> {
                if(e == null){
                    // grant user access
                    Intent home = new Intent(SplashActivity.this, MainActivity.class);
                    finish();
                    startActivity(home);
                }else{
                    // notify user
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            // display login page
            Intent login = new Intent(SplashActivity.this, SignInActivity.class);
            finish();
            startActivity(login);
        }
    }


    private void liveTracking(JsonResponseCallback callback){
        //Check whether GPS tracking is enabled//
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Exception e1 = new Exception("GPS Provider is disabled. Kindly enable to use Footprints");
            callback.done(null, e1);
        }
        //Check whether this app has access to the location permission//
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        //If the location permission has been granted, then start the TrackerService//
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
            JSONObject res = new JSONObject();
            try {
                res.put("isStarted", true);
                callback.done(res, null);
            } catch (JSONException e) {
                e.printStackTrace();
                callback.done(null, e);
            }
        } else {
            //If the app doesn’t currently have access to the user’s location, then request access//
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
            Exception e2 = new Exception("Location Services permission has not been granted to Footprints");
            callback.done(null, e2);

        }
    }

    //Start the TrackerService//
    private void startTrackerService() {
        startService(new Intent(this, TrackingService.class));
        //Notify the user that tracking has been enabled//
        Toast.makeText(this, "GPS tracking enabled", Toast.LENGTH_SHORT).show();
        // finish();
    }
}
