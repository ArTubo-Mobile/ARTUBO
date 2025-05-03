package com.asucare.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import com.asucare.app.classes.User;
import com.asucare.app.services.UserDataService;


public class AccountCreation extends AppCompatActivity {
    private UserDataService userDataService;

    private FirebaseAuth mAuth;

    private EditText username_field, email_field, pass1_field, pass2_field, arduino_id_field;
    private TextView text_login;
    private Button button_create_account;
    private Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_create_account);


        //initialize firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        userDataService = new UserDataService();
        utils = new Utils();

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        username_field = findViewById(R.id.textview_companion_username);
        email_field = findViewById(R.id.textview_email);
        arduino_id_field = findViewById(R.id.textview_arduino_id);
        pass1_field = findViewById(R.id.desired_password);
        pass2_field = findViewById(R.id.retry_password);
        button_create_account =  findViewById(R.id.buttonCreateAccount);
        text_login = findViewById(R.id.textView_login);
    }

    private void setupClickListeners() {
        text_login.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        button_create_account.setOnClickListener(v -> validateAndCreateAccount());
    }

    private void validateAndCreateAccount() {
        // Get form data
        String username = username_field.getText().toString().trim();
        String email = email_field.getText().toString().trim();
        String arduinoId = arduino_id_field.getText().toString().trim();
        String password = pass1_field.getText().toString();
        String retryPassword = pass2_field.getText().toString();

        // Validate inputs
        String validationError = validateInputs(username, email, arduinoId, password, retryPassword);

        if (!validationError.isEmpty()) {
            utils.alert_prompt(this, validationError);
            Toast.makeText(this, validationError, Toast.LENGTH_LONG).show();
            return;
        }

        // Show progress
        setFormEnabled(false);

        // Check if username is already taken
        checkUsernameAvailability(username, email, arduinoId, password);
    }

    private String validateInputs(String username, String email, String arduinoId,
                                  String password, String retryPassword) {
        StringBuilder errors = new StringBuilder();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if (username.isEmpty()) {
            errors.append("- Username must not be empty\n");
        }

        if (arduinoId.isEmpty()) {
            errors.append("- Arduino ID must not be empty\n");
        }

        if (email.isEmpty()) {
            errors.append("- Email Address must not be empty\n");
        } else if (!email.matches(emailPattern)) {
            errors.append("- Email Address format is not valid\n");
        }

        if (!password.equals(retryPassword)) {
            errors.append("- Password did not match\n");
        }

        if (password.length() < 8) {
            errors.append("- Password must be at least 8 characters long\n");
        }

        return errors.toString();
    }

    private void checkUsernameAvailability(String username, String email, String arduinoId, String password) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("/user");
        dbRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Username already exists
                            handleError("- Username is already taken");
                        } else {
                            // Username is available, proceed with account creation
                            registerNewUser(email, arduinoId, username, password);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        handleError("Database error: " + error.getMessage());
                    }
                });
    }

    // Better name for the method that creates a Firebase Auth account
    private void registerNewUser(String email, String arduinoId, String username, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Get the newly created user's UID
                        String newUserId = mAuth.getCurrentUser().getUid();
                        // Now that we have the UID, create the user profile
                        createUserProfile(newUserId, username, email, arduinoId);
                    } else {
                        // Handle specific auth errors
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            handleError("- Email address is already in use");
                        } else {
                            handleError("Account creation failed: " + task.getException().getMessage());
                        }
                    }
                });
    }

    // Better name for the method that saves user profile data
    private void createUserProfile(String userId, String username, String email, String arduinoId) {
        // Create user profile with the newly generated user ID
        User newUser = new User(userId, username, email, arduinoId);

        userDataService.saveUser(newUser, new UserDataService.DatabaseCallback() {
            @Override
            public void onSuccess() {
                // Reset the form
                clearForm();

                // Show success message
                Toast.makeText(AccountCreation.this,
                        "Successfully created an account!\nWelcome to AsuCare",
                        Toast.LENGTH_LONG).show();

                // Navigate to login
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                handleError("Error saving user data: " + errorMessage);
            }
        });
    }

    private void handleError(String errorMessage) {
        setFormEnabled(true);
        utils.alert_prompt(this, errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void setFormEnabled(boolean enabled) {
        username_field.setEnabled(enabled);
        email_field.setEnabled(enabled);
        arduino_id_field.setEnabled(enabled);
        pass1_field.setEnabled(enabled);
        pass2_field.setEnabled(enabled);
        button_create_account.setEnabled(enabled);
        text_login.setEnabled(enabled);
    }

    private void clearForm() {
        username_field.setText("");
        email_field.setText("");
        arduino_id_field.setText("");
        pass1_field.setText("");
        pass2_field.setText("");
        setFormEnabled(true);
    }
}
