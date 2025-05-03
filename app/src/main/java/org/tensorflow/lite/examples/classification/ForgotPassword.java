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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.asucare.app.R;

public class ForgotPassword extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseUser mUser;
    EditText email_et;
    TextView text_back_to_login;
    Button button_submit_email;

    String firebase_uid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if(mUser!=null){
            firebase_uid = mUser.getUid();
        }

        email_et =  (EditText) findViewById(R.id.email_forgot_password);
        button_submit_email = (Button) findViewById(R.id.buttonSendEmail);
        text_back_to_login = (TextView) findViewById(R.id.textView_back_to_login);

        button_submit_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset_password(email_et);
            }
        });

        text_back_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ii = new Intent(getApplicationContext(), Login.class);
                startActivity(ii);
            }
        });
    }

    private void reset_password(EditText email_address){



        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        String email = "";

        email = email_address.getEditableText().toString();

        String error_messages = "";

        if(email.trim().equals("")){
            error_messages += "- Email Address is Blank\n";
        }

        if(email.matches(emailPattern)){

        }else{
            error_messages += "- Entered E-mail Address format is not valid\n";
        }




        if(error_messages.trim().equals("")){

            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){


                        String error_message = "";
                        error_message = "Email for Changing Password Successfully Sent\nCheck your E-mail Account provided to finally change your password";

                        Toast.makeText(getApplicationContext(), error_message, Toast.LENGTH_LONG).show();


                        Intent ii = new Intent(getApplicationContext(),Login.class);
                        startActivity(ii);
                    }else{



                        String error_message = "";
                        error_message = "Error Sending Email for Changing Password";

                        Toast.makeText(getApplicationContext(), error_message, Toast.LENGTH_LONG).show();

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    String error_message = "";
                    error_message = "Error Sending Email for Changing Password";

                    Toast.makeText(getApplicationContext(), error_message, Toast.LENGTH_LONG).show();


                }
            });

        }else{
            Toast.makeText(getApplicationContext(), error_messages, Toast.LENGTH_LONG).show();
        }

    } // reset_password
}