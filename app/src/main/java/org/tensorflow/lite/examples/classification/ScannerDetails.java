package org.tensorflow.lite.examples.classification;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tensorflow.lite.examples.classification.data_class.CurrentPlantData;
import com.ar_tubo.app.Utils;


import java.util.ArrayList;
import java.util.List;
import com.ar_tubo.app.R;

public class ScannerDetails extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseUser mUser;

    ImageView img_back, img_plant_detected;

    FirebaseDatabase fbase;

    DatabaseReference dbref_plant_current_details;

    String firebase_uid = "";

    LinearLayout linear_home, linear_scan, linear_logout;

    TextView textview_humidity, textview_temperature, textview_soil_moisture, textview_disease_detected, textview_plant_detected_label, textview_plant_detected_sublabel;


    List<CurrentPlantData> list_plant_current_reading;

    ProgressBar progressBar_temp, progressBar_hum, progressBar_soil_moisture;

    String disease_detected = "";
    String object_detection_confidence_result = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_details);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#EDECE5"));
        }

        // Call the method to change navigation button color
        changeNavigationButtonColor("#F2F2ED"); // Change color to white

        img_back = (ImageView) findViewById(R.id.imageView_back);

        linear_home = (LinearLayout) findViewById(R.id.linearlayout_home_bottom_nav);
        linear_scan = (LinearLayout) findViewById(R.id.linearlayout_scan_bottom_nav);
        linear_logout = (LinearLayout) findViewById(R.id.linearlayout_logout_bottom_nav);

        img_plant_detected =  (ImageView) findViewById(R.id.imageView_plant_detected);
        textview_plant_detected_label = (TextView) findViewById(R.id.textView_plant_detected_label);
        textview_plant_detected_sublabel = (TextView) findViewById(R.id.textView_plant_detected_sublabel);

        textview_humidity = (TextView) findViewById(R.id.textView_humidity_status);
        textview_temperature = (TextView) findViewById(R.id.textView_temperature_status);
        textview_soil_moisture = (TextView) findViewById(R.id.textView_soil_moisture_status);
        textview_disease_detected = (TextView) findViewById(R.id.textView_disease_detected_label);



        progressBar_temp = (ProgressBar) findViewById(R.id.progressBar_temp);
        progressBar_hum = (ProgressBar) findViewById(R.id.progressBar_humidity);
        progressBar_soil_moisture = (ProgressBar) findViewById(R.id.progressBar_soilmoisture);


        Intent fromCamera = getIntent();
        disease_detected = fromCamera.getStringExtra("disease_detected");
        object_detection_confidence_result = fromCamera.getStringExtra("object_detection_confidence_result");

        if(disease_detected.equals("Healthy")){
            img_plant_detected.setImageResource(R.drawable.bgplantdetected2_tubo);
            textview_plant_detected_label.setText("Sugarcane");
            textview_plant_detected_sublabel.setText("Saccharum officinarum");
        }
        else if(disease_detected.toLowerCase().equals("no sugarcane found")) {
            textview_plant_detected_label.setText("No result");
            textview_plant_detected_sublabel.setText("Object detected is not a sugarcane");
            img_plant_detected.setImageResource(R.drawable.nosugarcanedetected1);
            textview_disease_detected.setText(disease_detected);
        }else{
            img_plant_detected.setImageResource(R.drawable.bgplantdetected2_tubo);
            textview_plant_detected_label.setText("Sugarcane");
            textview_plant_detected_sublabel.setText("Saccharum officinarum");
            textview_disease_detected.setText(disease_detected+" ("+object_detection_confidence_result+"%)");
        }


        FirebaseApp.initializeApp(this);



        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if(mUser!=null){
            firebase_uid = mUser.getUid();
        }else{
            //Intent ii = new Intent(getApplicationContext(), InitialSplashScreen.class);
            //startActivity(ii);
        }

        Utils util_tool = new Utils();


        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(getApplicationContext(),ClassifierActivity.class);
                startActivity(ii);
            }
        });
        linear_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(getApplicationContext(), InitialSplashScreen.class);
                startActivity(ii);
            }
        });

        linear_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(getApplicationContext(), ClassifierActivity.class);
                startActivity(ii);
            }
        });

        linear_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout_prompt(ScannerDetails.this);
            }
        });

        populate_current_plant_data(textview_temperature, textview_soil_moisture, textview_humidity);


    }

    private void logout_prompt(Context context){

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        final Dialog dialog_logout = new Dialog(context);
        dialog_logout .setContentView(R.layout.logout);

        Button btn_yes = (Button)  dialog_logout .findViewById(R.id.button_yes);
        Button btn_cancel = (Button)  dialog_logout .findViewById(R.id.button_cancel);

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAuth.signOut();
                Intent ii = new Intent(getApplicationContext(),Login.class);
                startActivity(ii);


            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_logout .dismiss();
            }
        });

        dialog_logout.show();

    } // logout_prompt

    public void populate_current_plant_data(TextView text_temperature, TextView text_soil_moisture, TextView text_humidity){



        list_plant_current_reading = new ArrayList<>();

        dbref_plant_current_details = FirebaseDatabase.getInstance().getReference("/current_reading");

        dbref_plant_current_details.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds: snapshot.getChildren()){

                    CurrentPlantData cpd = ds.getValue(CurrentPlantData.class);

                    String humidity = cpd.getHumidity();
                    String soil_moisture = cpd.getSoil_moisture();
                    String temperature = cpd.getTemperature();
                    String disease_detected = cpd.getDisease_detected();
                    String plant_detected = cpd.getPlant_detected();

                    list_plant_current_reading.add(new CurrentPlantData(humidity,soil_moisture,temperature,disease_detected,plant_detected));

                }

                for(CurrentPlantData pd: list_plant_current_reading){

                    String humidity_status = "";
                    String temp_status = "";
                    String soil_moisture_status = "";

                    String temp_value = pd.getTemperature();
                    String hum_value = pd.getHumidity();
                    String soil_ph_value = pd.getSoil_moisture();

                    String soil_moisture_out_of_bound = "0";

                    if(Double.parseDouble(temp_value) >= 20 && Double.parseDouble(temp_value) <= 30){
                        temp_status = "Normal";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            progressBar_temp.setProgressTintList(getColorStateList(R.color.progress_tint_normal));
                        }
                    }else{
                        temp_status = "Critical";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            progressBar_temp.setProgressTintList(getColorStateList(R.color.progress_tint_critical));
                        }
                    }

                    int temp_progress_value = 0;
                    temp_progress_value = Integer.parseInt(String.valueOf(Math.round(Double.parseDouble(temp_value))));

                    updateProgress(temp_progress_value,progressBar_temp);



                    if(Double.parseDouble(hum_value) >= 60 && Double.parseDouble(hum_value) <= 80){
                       humidity_status = "Normal";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            progressBar_hum.setProgressTintList(getColorStateList(R.color.progress_tint_normal));
                        }
                    }else{
                        humidity_status = "Critical";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            progressBar_hum.setProgressTintList(getColorStateList(R.color.progress_tint_critical));
                        }
                    }

                    int humidity_progress_value = 0;
                    humidity_progress_value = Integer.parseInt(String.valueOf(Math.round(Double.parseDouble(hum_value))));

                    updateProgress(humidity_progress_value,progressBar_hum);
                    //progressBar_hum.setProgress(humidity_progress_value);



                    if(Double.parseDouble(soil_ph_value) >= 700){
                        soil_moisture_status = "Dry";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            progressBar_soil_moisture.setProgressTintList(getColorStateList(R.color.progress_tint_critical));
                        }
                    }

                    if(Double.parseDouble(soil_ph_value) >= 300 && Double.parseDouble(soil_ph_value) <=700 ){
                        soil_moisture_status = "Moist";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            progressBar_soil_moisture.setProgressTintList(getColorStateList(R.color.progress_tint_normal));
                        }
                    }

                    if(Double.parseDouble(soil_ph_value) >= 1 && Double.parseDouble(soil_ph_value) <= 100 ){
                        soil_moisture_status = "Normal";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            progressBar_soil_moisture.setProgressTintList(getColorStateList(R.color.progress_tint_normal));
                        }
                    }

                    if(Double.parseDouble(soil_ph_value) >= 100 && Double.parseDouble(soil_ph_value) <=300 ){
                        soil_moisture_status = "Wet";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            progressBar_soil_moisture.setProgressTintList(getColorStateList(R.color.progress_tint_normal));
                        }
                    }



                    if(soil_ph_value.equals("0") && soil_ph_value.equals("1024")){
                        soil_moisture_status = "No contact with the ground";
                        soil_moisture_out_of_bound = "1";
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            progressBar_soil_moisture.setProgressTintList(getColorStateList(R.color.progress_tint_normal));
                        }
                    }

                    int soil_moisture_progress_value = 0;
                    soil_moisture_progress_value = Integer.parseInt(String.valueOf(Math.round(Double.parseDouble(soil_ph_value))));


                    // progressBar_soil_moisture.setProgress(soil_moisture_progress_value);

                    if(soil_moisture_out_of_bound.equals("1")){
                        updateProgress(0,progressBar_soil_moisture);
                        soil_moisture_out_of_bound = "0";
                    }else{
                        soil_moisture_progress_value = Math.round(soil_moisture_progress_value / 7);
                        updateProgress(soil_moisture_progress_value,progressBar_soil_moisture);
                    }


                    text_humidity.setText(""+hum_value +" - "+humidity_status);
                    text_temperature.setText( temp_value+" ℃ - "+temp_status);
                    text_soil_moisture.setText(soil_ph_value+"% - "+soil_moisture_status);
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    } // populate_current_plant_data

    private void updateProgress(int progress, ProgressBar progressBar) {
        ObjectAnimator.ofInt(progressBar, "progress", progress)
                .setDuration(500) // Set animation duration
                .start();
    }

    private void changeNavigationButtonColor(String color) {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(Color.parseColor(color)); // Replace with your color code
        }

    }
}