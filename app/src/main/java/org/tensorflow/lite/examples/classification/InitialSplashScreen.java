package org.tensorflow.lite.examples.classification;

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

import org.tensorflow.lite.examples.classification.data_class.UserAdminClass;

import java.util.ArrayList;
import java.util.List;
import com.ar_tubo.app.R;

public class InitialSplashScreen extends AppCompatActivity {

    Button button_start, button_about, button_exit;

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
//        text_welcome_note = (TextView) findViewById(R.id.textView_welcome_note);

        if(mUser!=null){
            //Intent ii = new Intent(getApplicationContext(), Scanner.class);
            //startActivity(ii);
            firebase_uid = mUser.getUid();
//            populate_user_admin_list();

        }





        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(getApplicationContext(),ClassifierActivity.class);
                startActivity(ii);
            }
        });

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

                logout_prompt(InitialSplashScreen.this);

            }
        });
    }

//    private void welcomeNote(){
//
//        if(firebase_uid.equals("")){
//
//        }else{
//
//            for(UserAdminClass uac: user_admin_list){
//
//                if(firebase_uid.equals(uac.getFirebase_uid())){
//                    text_welcome_note.setText("Welcome "+uac.getUsername()+"!");
//                }
//
//            }
//
//        }
//
//
//
//    } // welcomeNote


    private void populate_user_admin_list(){

        user_admin_list = new ArrayList<>();

        dbref_admin = FirebaseDatabase.getInstance().getReference("/user_admin");

        dbref_admin.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot ds: snapshot.getChildren()){

                    UserAdminClass uac = ds.getValue(UserAdminClass.class);

                    String user_admin_id = uac.getUser_admin_id();
                    String firebase_uid = uac.getFirebase_uid();

                    String username = uac.getUsername();
//                    String phone = uac.getPhone();
                    String email = uac.getEmail();


//                    user_admin_list.add(new UserAdminClass(""+firebase_uid+"",""+firebase_uid+"",""+username+"",""+email+""));

                }

//                welcomeNote();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });


    } // populate_user_admin_list()

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

}