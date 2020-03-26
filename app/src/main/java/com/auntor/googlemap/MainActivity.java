package com.auntor.googlemap;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isServiceOk()){
            init();
        }

    }
    public void init(){

        Button btn = findViewById(R.id.mapBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(),MapsActivity.class);
                startActivity(in);
            }
        });

    }

    public boolean isServiceOk(){

        Log.d(TAG,"isServiceaok");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if(available== ConnectionResult.SUCCESS){
            //Ok
            Log.d(TAG,"IsService :ok");
            return true;
        }else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
//Error but resolve
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,available,ERROR_DIALOG_REQUEST);
dialog.show();
        }else{
            Toast.makeText(this,"You Can't use Map",Toast.LENGTH_LONG).show();
        }

        return false;
    }
}
