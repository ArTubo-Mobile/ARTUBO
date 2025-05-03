package com.asucare.app;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.tensorflow.lite.examples.classification.AboutPage;
import org.tensorflow.lite.examples.classification.ClassifierActivity;
import org.tensorflow.lite.examples.classification.Login;
import org.tensorflow.lite.examples.classification.data_class.UserAdminClass;

import android.Manifest;
import android.content.pm.PackageManager;

import android.widget.Toast;
import androidx.core.app.ActivityCompat;



import java.util.ArrayList;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    Button button_start, button_about, button_exit, button_sensor_data;

    DatabaseReference dbref, dbref_admin;


    private List<UserAdminClass> user_admin_list;

    FirebaseAuth mAuth;
    FirebaseUser mUser;

    TextView text_welcome_note;

    String firebase_uid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Window window = getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.base_color));
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.base_color)); // Replace with your color code
        }

        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_dashboard);

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        button_start = (Button) findViewById(R.id.buttonStart);
        button_about = (Button) findViewById(R.id.buttonAbout);
        button_exit = (Button) findViewById(R.id.buttonExit);
        button_sensor_data = (Button) findViewById(R.id.button_sensor_data);

        if(mUser!=null){
            firebase_uid = mUser.getUid();
        }


        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for camera permission before opening camera screen
                if (ContextCompat.checkSelfPermission(Dashboard.this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // Request camera permission
                    ActivityCompat.requestPermissions(Dashboard.this,
                            new String[]{Manifest.permission.CAMERA}, 100);
                } else {
                    // Permission already granted, start scanner activity
                    openScannerActivity();
                }
            }
        });

        button_sensor_data.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ScannerDetails.class);
                startActivity(intent);
            }
        }));

        button_about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(getApplicationContext(), AboutPage.class);
                startActivity(ii);
            }
        });

        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logout_prompt(Dashboard.this);

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, start scanner activity
            openScannerActivity();
        } else {
            // Permission denied
            Toast.makeText(this, "Camera permission required for scanning",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void openScannerActivity() {
        Intent intent = new Intent(Dashboard.this, ScannerActivity.class);
        startActivity(intent);
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
                Intent ii = new Intent(getApplicationContext(), Login.class);
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

}