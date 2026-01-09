package com.example.smartdoorbell;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult;
import com.example.smartdoorbell.services.auth.AWSCognitoService;

public class RegisterActivity extends AppCompatActivity {

    private AWSCognitoService cognito;

    private EditText registerUsername;
    private EditText registerEmail;
    private EditText registerPassword;
    private EditText registerConfirmPassword;
    private Button registerButton;

    private Button goToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cognito = new AWSCognitoService(getApplicationContext());

        // Initialise fields and button
        registerUsername = findViewById(R.id.registerUsername);
        registerEmail = findViewById(R.id.registerEmail);
        registerPassword = findViewById(R.id.registerPassword);
        registerConfirmPassword = findViewById(R.id.registerConfirmPassword);
        registerButton = findViewById(R.id.registerButton);
        goToLoginButton = findViewById(R.id.gotToLoginButton);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });


        // Go to Register Page
        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to next activity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });


    }

    private void register() {

        String username = registerUsername.getText().toString().trim();
        String email = registerEmail.getText().toString().trim();
        String password = registerPassword.getText().toString();
        String confirmPassword = registerConfirmPassword.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Register user with Cognito
        cognito.signUp(username, email, password, new SignUpHandler() {
            public void onSuccess(
                    CognitoUser user,
                    boolean userConfirmed,
                    CognitoUserCodeDeliveryDetails details) {

                if (userConfirmed) {
                    // User is already confirmed, go to login
                    goToLogin();
                } else {
                    // Email verification required
                    Intent intent = new Intent(RegisterActivity.this, ConfirmAccountCreationActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                }
            }

            @Override
            public void onSuccess(CognitoUser user, SignUpResult signUpResult) {
                Intent intent = new Intent(RegisterActivity.this, ConfirmAccountCreationActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(RegisterActivity.this,
                        exception.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }



}
