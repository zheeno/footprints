package com.cluster.footprints;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cluster.footprints.utils.AppPrefs;
import com.cluster.footprints.utils.Globals;
import com.cluster.footprints.utils.Hotspot;
import com.cluster.footprints.utils.HotspotInfo;
import com.cluster.footprints.utils.HotspotsAdapter;
import com.cluster.footprints.utils.UserLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";
    private DatabaseReference mDatabase;

    private GoogleMap mapApi;
    private SupportMapFragment mapFragment;
    private ArrayList<UserLocation> markersArray = new ArrayList<UserLocation>();
    private FirebaseUser user;
    private ImageButton locBtn, filterBtn;
    private boolean hasMovedCamera;
    private AppPrefs appPrefs;
    private ArrayList<HotspotInfo> monLocations = new ArrayList<HotspotInfo>();

    private RecyclerView recyclerView;
    private HotspotsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appPrefs = new AppPrefs(this);
        recyclerView = findViewById(R.id.recycler_view);

        mAdapter = new HotspotsAdapter(monLocations);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        hasMovedCamera = false;
        locBtn = findViewById(R.id.tracking_loc_btn);
        locBtn.setOnClickListener(v-> {
            // set map marker to last visited location
            float last_lat = appPrefs.getFloatValue(Globals.LAST_LAT);
            float last_lon = appPrefs.getFloatValue(Globals.LAST_LON);
            if(last_lat > 0 && last_lon > 0) {
                LatLng loc = new LatLng(last_lat, last_lon);
                mapApi.moveCamera(CameraUpdateFactory.newLatLng(loc));
                mapApi.animateCamera( CameraUpdateFactory.zoomTo( 15.0f ) );
            }
//            mockHotspots(); // remove later
        });

        filterBtn = findViewById(R.id.tracking_filter_btn);
        filterBtn.setOnClickListener(v-> {
            final AlertDialog dialogBuilder = new AlertDialog.Builder(this).create();
            LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            View dialogView = inflater.inflate( R.layout.date_selector_layout, null );
            DatePicker simpleDatePicker = dialogView.findViewById(R.id.simpleDatePicker);
//            simpleDatePicker.setMaxDate();
            Button dateSelConBtn = dialogView.findViewById(R.id.dateSelConBtn);
            dateSelConBtn.setOnClickListener(v1 -> {
                // get the values for day of month , month and year from a date picker
                String day = ""+simpleDatePicker.getDayOfMonth();
                String month = "" + (simpleDatePicker.getMonth() + 1);
                String year = "" + simpleDatePicker.getYear();
                String path = year+""+(month.length() == 1 ? "0"+month : month)+
                        ""+(day.length() == 1 ? "0"+day : day);
                // get foot prints for the selected date
                markersArray.clear();
                mapApi.clear();
                monLocations.clear();
                mAdapter.notifyDataSetChanged();
                getFootprint(path);
                dialogBuilder.dismiss();
            });
            Button dateSelCancelBtn = dialogView.findViewById(R.id.dateSelCancelBtn);
            dateSelCancelBtn.setOnClickListener(v1 -> dialogBuilder.dismiss());

            dialogBuilder.setCancelable(true);
            dialogBuilder.setView(dialogView);
            dialogBuilder.show();
        });
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // map fragment initialization
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapApi);
        mapFragment.getMapAsync(this);

        String currentDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        getFootprint(currentDate);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapApi = googleMap;
        mapApi.getUiSettings().setAllGesturesEnabled(true);
        mapApi.animateCamera( CameraUpdateFactory.zoomTo( 15.0f ) );
        mapApi.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        mapApi.setOnMarkerClickListener(marker -> { // listen for clicks on markers
            marker.showInfoWindow();
            // search markersArray for the selected entry
            for (UserLocation entry: markersArray){
                if(entry.getmLatitude() == marker.getPosition().latitude && entry.getmLongitude() == marker.getPosition().longitude){
                    // get all related data about the selected location
                    checkRadar(entry);
                    break;
                }
            }
            return false;
        });
        plotJourney();
    }


    private void plotJourney() {
        Log.d("Checking users' journey", markersArray.toString());
        monLocations.clear();
        for(int i = 0 ; i < markersArray.size() ; i++) {
            if (mapApi != null) {
                checkRadar(markersArray.get(i));
                String title = new Date(markersArray.get(i).getmTime()).toString() + "";
                String snippet =  "Lat: "+String.format("%.6f", markersArray.get(i).getmLatitude())+", Lon: "+String.format("%.6f", markersArray.get(i).getmLongitude());
                createMarker(markersArray.get(i).getmLatitude(), markersArray.get(i).getmLongitude(), title, snippet, proximitySafe(markersArray.get(i)) ? R.drawable.location_pole : R.drawable.location_medical);
            }
        }
        // move the view to the most recent location
        if(markersArray.size() > 0) {
            Log.d(TAG+" Markers=>", markersArray.size()+"");
            UserLocation here = markersArray.get(markersArray.size() -1);
            LatLng loc = new LatLng(here.getmLatitude(), here.getmLongitude());
            if(!hasMovedCamera) {
                mapApi.moveCamera(CameraUpdateFactory.newLatLng(loc));
                hasMovedCamera = true;
            }
        }
    }

    private boolean proximitySafe(UserLocation userLocation) {
        Log.d("RADAR PROXIMITY", "Listed: "+monLocations.size());
        for(HotspotInfo info : monLocations) {
            float[] distanceMeters = new float[1];
            Location.distanceBetween(userLocation.getmLatitude(), userLocation.getmLongitude(),
                    info.getLocation().getmLatitude(), info.getLocation().getmLongitude(), distanceMeters);
            if(distanceMeters[0] <= Globals.unsafeDistance){
                Log.d("RADAR PROXIMITY", "Unsafe Distance: "+distanceMeters[0]);
                return false;
            }
            Log.d("RADAR PROXIMITY", "Safe Distance: "+distanceMeters[0]);
        }
        Log.d("RADAR PROXIMITY", "Very Safe Distance");
        return true;
    }

    protected Marker createMarker(double latitude, double longitude, String title, String snippet, int iconResID) {
        return mapApi.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title)
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.fromResource(iconResID)));
    }

    private void getFootprint(String path){
        Log.d("FOOTPRINT - PATH", path);
        // My top posts by number of stars
        String myUserId = user.getUid();
        Query myFootprints = mDatabase.child(Globals.location).child(myUserId).child(path).orderByKey();
        myFootprints.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("Children Response", dataSnapshot.toString());
                UserLocation footprint = dataSnapshot.getValue(UserLocation.class);
                markersArray.add(footprint);
                plotJourney();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                handleLocationUpdate(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                handleLocationUpdate(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                handleLocationUpdate(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
            // TODO: implement the ChildEventListener methods as documented above
            // ...
        });
    }



    // TODO: THIS SECTION HANDLES ALL LOGIN INVOLVED IN FINDING VIRAL HOT SPOTS
    // TODO: UNDER THE FOOTPRINTS RADAR

    private void checkRadar(UserLocation location) {
//        monLocations.clear();
        Date date = new  Date(location.getmTime());
        // get key from the selected location using the time (milliseconds)
        String targetKey = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date);
        Query hotspots = mDatabase.child(Globals.hotspot).child(targetKey);
        hotspots.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                for(DataSnapshot hotspot: dataSnapshot.getChildren()){
                    HotspotInfo thisHotspot = hotspot.getValue(HotspotInfo.class);
                    if(thisHotspot != null) {
                        Log.d("RADAR - hotspots", thisHotspot.getTitle());
                        // loop through all hotspot locations
                        UserLocation selectedLocation = thisHotspot.getLocation();
                        if(selectedLocation != null) {
                            float[] distanceMeters = new float[1];
                            assert selectedLocation != null;
                            Location.distanceBetween(location.getmLatitude(), location.getmLongitude(),
                                    selectedLocation.getmLatitude(), selectedLocation.getmLongitude(), distanceMeters);
                            Log.d("RADAR - Distance ", distanceMeters[0] + "");
                            // if distance is between 200 meters, then this location is to be
                            // placed under the location monitor
                            if (distanceMeters[0] <= Globals.unsafeDistance) { // 1km = 1000
                                // verify that only one record of the hostpot is in the array
                                if(!arrayListHas(monLocations, thisHotspot)) {
                                    monLocations.add(thisHotspot);
                                    displayHotspots();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("RADAR ERROR", databaseError.getMessage());
            }
        });



//        Query locations = mDatabase.child(Globals.location).child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
//        locations.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
//                // now, we have the various user log entries for the selected date
//                for(DataSnapshot thisUserLog: dataSnapshot.getChildren()) {
//                    for(DataSnapshot usersLogs: thisUserLog.getChildren()) {
//                        if (usersLogs.getKey().equals(targetKey)) {
//                            // get all log entries for the selected date
//                            for (DataSnapshot trip: usersLogs.getChildren()){
//                                UserLocation selectedLocation = trip.getValue(UserLocation.class);
//                                float[] distanceMeters = new float[1];
//                                assert selectedLocation != null;
//                                Location.distanceBetween(location.getmLatitude(), location.getmLongitude(), selectedLocation.getmLatitude(), selectedLocation.getmLongitude(), distanceMeters);
//                                Log.d("RADAR - Distance ", distanceMeters[0]+"");
//                                // if distance is between 200 meters, then this location is to be
//                                // placed under the location monitor
//                                if(distanceMeters[0] <= 200){ // 1km = 1000
//                                    monLocations.add(selectedLocation);
//                                }
//                            }
//                        }
//                    }
//                }
//                Log.d("RADAR - loc. matches", ""+monLocations.size());
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                // Failed to read value
//                Log.w(TAG, "Failed to read value.", error.toException());
//            }
//        });

    }

    private boolean arrayListHas(ArrayList<HotspotInfo> monLocations, HotspotInfo thisHotspot) {
        for(HotspotInfo info : monLocations){
            if(info.getLocation().getmTime() == thisHotspot.getLocation().getmTime()){
                return true;
            }
        }
        return false;
    }

    private void displayHotspots() {
        mAdapter.notifyDataSetChanged();
    }


    // TODO: Create dummy data for mocking potential hotspots
    private void mockHotspots(){
        String key = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        // location of the event
        UserLocation location = new UserLocation();
        location.setmAltitude(45.30144500732422);
        location.setmBearing(2);
        location.setmElapsedRealtimeNanos(3951669);
        location.setmLatitude(6.6000383);
        location.setmLongitude(3.42205);
        location.setmProvider("Mocked");
        location.setmSpeed(3);
        location.setmTime(1585994109);
        // info about individual hospots for the day
        HotspotInfo info = new HotspotInfo();
        info.setContaminated(true);
        info.setDate("2020-04-04");
        info.setTime("08:45:32");
        info.setDescription("A mild case was observed at Access bank branch");
        info.setThreatLevel(2);
        info.setTitle("Case with minimal threat level");
        info.setLocation(location);
        // populate the array of hotspots
        ArrayList<HotspotInfo> hotspostsDetails = new ArrayList<>();
        hotspostsDetails.add(info);
        // create the hotspot object
        Hotspot hotspot = new Hotspot(
                "Test Hotspot 1",
                "Testing out what it feels like",
                "",
                "",
                hotspostsDetails
        );
        // save to the database
        DatabaseReference ref = mDatabase.child(Globals.hotspot).child(key);
        ref.setValue(hotspot);
    }
}
