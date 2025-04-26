package org.tensorflow.lite.examples.classification;

import android.content.Intent;
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
import ar_tubo.Utils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CreateAccount extends AppCompatActivity {

    FirebaseDatabase db;
    DatabaseReference dbref, dbref_admin;

 
    private List<UserAdminClass> user_admin_list;

    FirebaseAuth mAuth;

    EditText et_username, et_arduino_id, et_email, et_pass, et_retry_pass;

    String username = "";

    TextView text_login;

    Button button_create_account;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_create_account);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        et_username = (EditText) findViewById(R.id.textview_companion_username);
        et_email =  (EditText) findViewById(R.id.textview_email);
        et_arduino_id = (EditText) findViewById(R.id.textview_arduino_id);
        et_pass =  (EditText) findViewById(R.id.desired_password);
        et_retry_pass =  (EditText) findViewById(R.id.retry_password);
        button_create_account = (Button) findViewById(R.id.buttonCreateAccount);
        text_login = (TextView) findViewById(R.id.textView_login);
        text_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(getApplicationContext(), Login.class);
                startActivity(ii);
            }
        });
        populate_user_admin_list();
        button_create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
                String username_field = et_username.getEditableText().toString();
                String email_address = et_email.getEditableText().toString();
                String arduino_id = et_arduino_id.getEditableText().toString();
                String password = et_pass.getEditableText().toString();
                String retry_password = et_retry_pass.getEditableText().toString();
                String error_messages = "";
                Utils util_tool = new Utils();
                int username_found = 0;
                int email_address_found = 0;
                if(user_admin_list.size() > 0){
                    for(UserAdminClass uac: user_admin_list){
                        if(username.equals(uac.getUsername())){
                            username_found += 1;
                        }
                        if(email_address.equals(uac.getEmail())){
                            email_address_found += 1;
                        }
                    }
                }
                if(username_found > 0) {
                    error_messages += "- Choose another Username \n";
                }
                if(email_address_found > 0) {
                    error_messages += "- Choose another E-mail Address\n";
                }
                if(username_field.trim().equals("")){
                    error_messages += "- Username must not be empty\n";
                }
                if(arduino_id.trim().equals("")){
                    error_messages += "- Arduino ID must not be empty\n";
                }
                if(email_address.trim().equals("")){
                    error_messages += "- Email Address must not be empty\n";
                }
                if(!email_address.trim().matches(emailPattern)){
                   error_messages += "- Email Address format is not valid\n";
                }
                if(!password.trim().equals(retry_password.trim())) {
                    error_messages += "- Password did not match\n";
                }
                if(password.trim().length() < 8 || password.trim().length() < 8){
                    error_messages += "- Password must be atleast 8 characters long\n";
                }
                if(error_messages.equals("")){
                    signup(email_address, arduino_id, username_field, password);
                    et_email.setText("");
                    et_arduino_id.setText("");
                    et_pass.setText("");
                    et_retry_pass.setText("");
                }else{
                    util_tool.alert_prompt(getApplicationContext(),error_messages);
                    Toast.makeText(getApplicationContext(),error_messages,Toast.LENGTH_LONG).show();
                }
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
                    String arduino_uid = uac.getArduino_uid();
                    String email = uac.getEmail();
                    user_admin_list.add(new UserAdminClass(""+firebase_uid+"",""+firebase_uid+"",""+username+"",""+email+"",""+arduino_uid+""));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void insert_account_details_to_firebase_db(String email, String firebase_uid, String username){
        dbref = FirebaseDatabase.getInstance().getReference("/user_admin");
        //String unique_user_id = dbref.push().getKey();
        UserAdminClass uac = new UserAdminClass(firebase_uid,firebase_uid,username,email);
        dbref.child(firebase_uid).setValue(uac);
    }


    private void signup(String email_address, String arduino_id, String username_string, String pw) {


        mAuth.createUserWithEmailAndPassword(email_address,pw)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    String firebaseUID = mAuth.getUid();
                                    insert_account_details_to_firebase_db(email_address, firebaseUID, username_string, arduino_id);
                                    Toast.makeText(getApplicationContext(),"Successfully created an account!\nWelcome to AR-Tubo", Toast.LENGTH_LONG).show();
                                    Intent ii = new Intent(getApplicationContext(), Login.class);
                                    startActivity(ii);


                                }

                            }
                        });





    } // signup
}