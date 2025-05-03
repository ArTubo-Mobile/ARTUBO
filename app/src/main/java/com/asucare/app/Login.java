package com.asucare.app;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.asucare.app.classes.User;
import com.asucare.app.services.UserDataService;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private UserDataService userDataService;

    private EditText et_email, et_password;
    private Button button_login;
    private TextView text_create_account, text_forgot_password;

    private Utils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set status bar color
        configureStatusBar();

        setContentView(R.layout.activity_login);

        // Initialize Firebase and services
        initServices();

        // Initialize Utils
        utils = new Utils();

        // Initialize UI elements
        initViews();

        // Set up click listeners
        setupClickListeners();

        // Check if user is already logged in
        checkExistingLogin();
    }

    private void configureStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
    }

    private void initServices() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userDataService = new UserDataService();
    }

    private void initViews() {
        et_email = findViewById(R.id.email_login);
        et_password = findViewById(R.id.password_login);
        button_login = findViewById(R.id.buttonSignIn);
        text_create_account = findViewById(R.id.textView_create_account);
        text_forgot_password = findViewById(R.id.textView_forgot_password);
    }

    private void setupClickListeners() {
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndLogin();
            }
        });

        text_create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToCreateAccount();
            }
        });

        text_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToForgotPassword();
            }
        });
    }

    private void checkExistingLogin() {
        if (mUser != null) {
            String firebaseUid = mUser.getUid();
            fetchUserDataAndNavigate(firebaseUid);
        }
    }

    private void fetchUserDataAndNavigate(String firebaseUid) {
        userDataService.getUserByFirebaseUid(firebaseUid, new UserDataService.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                // User data loaded successfully, navigate to main screen
                navigateToDashboard();
            }

            @Override
            public void onUserNotFound() {
                // User exists in Auth but not in database
                Toast.makeText(getApplicationContext(), "User profile not found", Toast.LENGTH_LONG).show();
                mAuth.signOut(); // Sign out as data is incomplete
            }

            @Override
            public void onError(String errorMessage) {
                // Handle database error
                Toast.makeText(getApplicationContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void validateAndLogin() {
        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString();

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String validationError = validateInputs(email, password, emailPattern);

        if (validationError.isEmpty()) {
            setFormEnabled(false);
            performLogin(email, password);
        } else {
            utils.alert_prompt(this, validationError);
            Toast.makeText(getApplicationContext(), validationError, Toast.LENGTH_LONG).show();
        }
    }

    private String validateInputs(String email, String password, String emailPattern) {
        StringBuilder errors = new StringBuilder();

        if (email.isEmpty()) {
            errors.append("- Email Address is empty\n");
        } else if (!email.matches(emailPattern)) {
            errors.append("- Email Address format is not valid\n");
        }

        if (password.isEmpty()) {
            errors.append("- Password is empty\n");
        } else if (password.length() < 8) {
            errors.append("- Password must be 8 characters long\n");
        }

        return errors.toString();
    }

    private void performLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mUser = mAuth.getCurrentUser();

                    if (mUser != null) {
                        // Load user data before proceeding
                        fetchUserDataAndNavigate(mUser.getUid());
                        clearForm();
                        Toast.makeText(getApplicationContext(), "Welcome to AsuCare!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    handleError("Authentication Failed");
                }
            }
        });
    }

    private void handleError(String errorMessage) {
        setFormEnabled(true);
        utils.alert_prompt(this, errorMessage);
        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    private void setFormEnabled(boolean enabled) {
        et_email.setEnabled(enabled);
        et_password.setEnabled(enabled);
        button_login.setEnabled(enabled);
        text_create_account.setEnabled(enabled);
        text_forgot_password.setEnabled(enabled);
    }

    private void clearForm() {
        et_email.setText("");
        et_password.setText("");
        setFormEnabled(true);
    }

    private void navigateToCreateAccount() {
        Intent intent = new Intent(getApplicationContext(), AccountCreation.class);
        startActivity(intent);
    }


    private void navigateToForgotPassword() {
        Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
        startActivity(intent);
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(getApplicationContext(), Dashboard.class);
        startActivity(intent);
        finish();
    }
}