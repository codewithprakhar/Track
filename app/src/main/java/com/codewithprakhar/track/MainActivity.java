package com.codewithprakhar.track;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void getcurrentlocation(View view) {
        startActivity(new Intent(this,MapsActivity.class));
    }

    public void btnRetriveLocation(View view) {
        startActivity(new Intent(getApplicationContext(),RetriveMapsActivity.class));
    }

}
