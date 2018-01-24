package com.example.jahidul.colourdetector;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnFindColour;
    private Camera mCamera;
    private Camera.Parameters parameters;
    Button btnOn,btnOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnFindColour=(Button)findViewById(R.id.buttonFindColour);
        btnFindColour.setOnClickListener(this);
        btnOn=(Button)findViewById(R.id.flashON);
        btnOff=(Button)findViewById(R.id.flashOFF);

        mCamera= Camera.open();
        btnOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parameters=mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                mCamera.startPreview();

            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                mCamera.stopPreview();
                mCamera.release();


            }
        });


    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(MainActivity.this,FindColour.class);
        startActivity(intent);

    }
}
