package com.auntor.googlemap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.auntor.googlemap.models.PlaceInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener{
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    AutocompleteSessionToken autocompleteSessionToken;
    private PlaceInfo mPlace;
    private GoogleApiClient mGoogleApiClient;
    private PlaceAutocompleteAdapterNew mPlaceAutocompleteAdapter;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int permissionRequestCode = 1234;
    private static final float DEFAULT_ZOOM = 15f;
//    private static final LatLngBounds LAT_LNG_BOUNDS =
//            new LatLngBounds( new LatLng(-40, -168),
//            new LatLng(71, 136));


    //Widget
    private AutoCompleteTextView mSearchText;
    private ImageView mGPS;
    PlacesClient placesClient;


     Boolean mLocationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mSearchText =findViewById(R.id.input_search);
        mGPS =findViewById(R.id.ic_gps);
        getLocationPermission();

    }


    private void init(){
        Log.d(TAG, "init: initializing");

        if (!com.google.android.libraries.places.api.Places.isInitialized()) {
            com.google.android.libraries.places.api.Places.initialize(getApplicationContext(), "AIzaSyAdIZ9ot_DQcynrN20v05zUH1bNm4JbTyE");
        }



        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mSearchText.setOnItemClickListener(mautoCompleteClickListener);

        autocompleteSessionToken=AutocompleteSessionToken.newInstance();
        placesClient = com.google.android.libraries.places.api.Places.createClient(getApplicationContext());

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapterNew(this,placesClient,autocompleteSessionToken);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);


        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });
        mGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });
        hideSoftKeyboard();
    }

    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            moveCamera(new LatLng(address.getLatitude(),address.getLongitude()),DEFAULT_ZOOM,address.getAddressLine(0));

        }

    }

private void getDeviceLocation(){

        mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        try{

            if(mLocationPermissionGranted){
Task location = mFusedLocationProviderClient.getLastLocation();
location.addOnCompleteListener(new OnCompleteListener() {
    @Override
    public void onComplete(@NonNull Task task) {
        if(task.isSuccessful()){
             Log.d(TAG,"Location found");
            Location currentLocation = (Location) task.getResult();
            moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()),DEFAULT_ZOOM,"My Location");
            

        }else{
            Log.d(TAG,"Location is null");
            Toast.makeText(MapsActivity.this, "Unable to get Current Location", Toast.LENGTH_SHORT).show();
        }
    }
});
            }
        }catch (SecurityException e){
            Log.e(TAG,"SecurityException"+e.getMessage());
        }
}


private void moveCamera(LatLng latLng,float zoom,String title){
    Log.d(TAG, "moveCamera: Lat"+latLng.latitude+"long:"+latLng.longitude);
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

   if(!title.equals("My Location")){
       MarkerOptions options = new MarkerOptions().position(latLng).title(title);
       mMap.addMarker(options);
   }
   hideSoftKeyboard();


}

    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    private void getLocationPermission(){
        String[] permissions = {FINE_LOCATION,COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                  mLocationPermissionGranted=true;
                  initMap();
            }else{
                ActivityCompat.requestPermissions(this,permissions,permissionRequestCode);
            }
        }else{
            ActivityCompat.requestPermissions(this,permissions,permissionRequestCode);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted=false;
        switch (requestCode){
            case permissionRequestCode:{
                if(grantResults.length>0){
                    for(int i=0;i<grantResults.length;i++){
                        if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted=false;
                            return;
                        }
                        mLocationPermissionGranted=true;
                        //init our map
                        initMap();
                    }
                }
            }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(getApplicationContext(), "Map Is Ready", Toast.LENGTH_SHORT).show();
        mMap=googleMap;
        if(mLocationPermissionGranted){
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();
        }
    }


    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }




    private AdapterView.OnItemClickListener mautoCompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyboard();
            final AutocompletePrediction item = (AutocompletePrediction) mPlaceAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient,placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = (Place) places.get(0);

            try{
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: name: " + place.getName());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: address: " + place.getAddress());
//                mPlace.setAttributions(place.getAttributions().toString());
//                Log.d(TAG, "onResult: attributions: " + place.getAttributions());
                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: id:" + place.getId());
                mPlace.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: latlng: " + place.getLatLng());
//                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: rating: " + place.getRating());
                mPlace.setPhoneNumber(place.getPhoneNumber().toString());
                Log.d(TAG, "onResult: phone number: " + place.getPhoneNumber());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: website uri: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());
            }catch (NullPointerException e){
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage() );
            }

            moveCamera(new LatLng(place.getViewport().getCenter().latitude,
                    place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace.getName());

            places.release();
        }
    };


}
