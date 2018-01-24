package com.example.jahidul.colourdetector;

import android.support.v4.app.SupportActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kosalgeek.genasync12.AsyncResponse;
import com.kosalgeek.genasync12.PostResponseAsyncTask;

import java.util.HashMap;

public class EditColor extends AppCompatActivity implements View.OnClickListener {
    final String LOG =this.getClass().getName();
    TextView tvColorCode;
   // TextView tvName;
    EditText tvColorName;
    Button btnSubmit;
    String colorCode;
    String time;
    int ttime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_color);
        tvColorCode=(TextView)findViewById(R.id.textViewColorCode);
       // tvName=(TextView)findViewById(R.id.textViewName);
        tvColorName=(EditText) findViewById(R.id.editTextColorName);
        btnSubmit=(Button)findViewById(R.id.buttonRename);
        btnSubmit.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();

        colorCode = bundle.getString("colorCode");
       // time=bundle.getString("Time");
         //ttime=Integer.parseInt(time);
        tvColorCode.setText(colorCode);



    }

    @Override
    public void onClick(View v) {
        HashMap postData =new HashMap();
        String colorName= tvColorName.getText().toString();
        if(!(TextUtils.isEmpty(colorCode) || TextUtils.isEmpty(colorName))){

            postData.put("colorCode", colorCode);
            postData.put("colorName",colorName);



            PostResponseAsyncTask taskWrite = new PostResponseAsyncTask(EditColor.this, postData, new AsyncResponse() {
                @Override
                public void processFinish(String s) {

                    Log.d(LOG, s);
                    if (s.contains("success")) {
                        Toast.makeText(EditColor.this, "Update Successful", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(EditColor.this, "Check Your Information", Toast.LENGTH_LONG).show();
                    }

                }
            });
            taskWrite.execute("http://jahidul.netau.net/addColorName.php");
        }
        else {
            Toast.makeText(getBaseContext(), "Edit failed ", Toast.LENGTH_LONG).show();

        }




    }


}
