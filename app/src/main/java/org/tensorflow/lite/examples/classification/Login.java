package org.tensorflow.lite.examples.classification;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import org.tensorflow.lite.examples.classification.data_class.UserAdminClass;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Login extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;


    FirebaseDatabase db;
    DatabaseReference dbref, dbref_admin;

  
    private List<UserAdminClass> user_admin_list;

    String firebase_uid = "";


    String user_role_name = "";
    Button button_login;
    TextView text_create_account, text_forgot_password;

    EditText et_email, et_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        et_email = (EditText) findViewById(R.id.email_login);
        et_password = (EditText) findViewById(R.id.password_login);

        button_login = (Button) findViewById(R.id.buttonSignIn);

        text_create_account = (TextView) findViewById(R.id.textView_create_account);
        text_forgot_password = (TextView) findViewById(R.id.textView_forgot_password);




        mAuth = FirebaseAuth.getInstance();

        mUser = mAuth.getCurrentUser();


        populate_user_admin_list();



        if(mUser!=null){
            firebase_uid = mUser.getUid();
            Intent ii = new Intent(getApplicationContext(), InitialSplashScreen.class);
            startActivity(ii);

        }



        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = "";
                String password = "";

                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                email = et_email.getEditableText().toString();
                password = et_password.getEditableText().toString();

                String error_messages = "";

                //Intent ii = new Intent(getApplicationContext(), InitialSplashScreen.class);
                //startActivity(ii);

                if(email.trim().equals("")){
                    error_messages += "- Email Address is empty\n";
                }

                if(email.matches(emailPattern)){

                }else{
                    error_messages += "- Email Address format is not valid\n";
                }

                if(password.trim().equals("")){
                    error_messages += "- Password is empty\n";
                }

                if(password.trim().length() < 8){
                    error_messages += "- Password must be 8 characters long\n";
                }

                if(error_messages.trim().equals("")){


                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                mUser = mAuth.getCurrentUser();

                                if(mUser != null){
                                    et_email.setText("");
                                    et_password.setText("");
                                    Toast.makeText(getApplicationContext(),"Welcome to AR-Tubo!",Toast.LENGTH_LONG).show();
                                    Intent ii = new Intent(getApplicationContext(), InitialSplashScreen.class);
                                    startActivity(ii);
                                }


                            }else{
                                Toast.makeText(getApplicationContext(),"Authentication Failed",Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }else{

                    Toast.makeText(getApplicationContext(),error_messages,Toast.LENGTH_LONG).show();

                }


            }
        }); // button_login

        text_create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(getApplicationContext(), CreateAccount.class);
                startActivity(ii);
            }
        });

        text_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(getApplicationContext(), ForgotPassword.class);
                startActivity(ii);
            }
        });


    }

   


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
                    String phone = uac.getPhone();
                    String email = uac.getEmail();



                    user_admin_list.add(new UserAdminClass(""+firebase_uid+"",""+firebase_uid+"",""+username+"",""+email+""));

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });


    } // populate_user_admin_list()

}