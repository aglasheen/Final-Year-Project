package com.example.smartdoorbell;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.example.smartdoorbell.services.auth.AWSCognitoService;

// Activity to confirm account has been created using AWS Cognito
// User must enter OTP to confirm account before login
public class ConfirmAccountCreationActivity extends AppCompatActivity {

    private AWSCognitoService cognito;

    private EditText confirmCode;

    private Button confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_confirm_account_creation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String email = getIntent().getStringExtra("email");

        // Initialize edit texts and login button
        confirmCode = findViewById(R.id.confirmCode);
        confirmButton = findViewById(R.id.confirmButton);

        // Initialize Cognito helper
        cognito = new AWSCognitoService(getApplicationContext());

        // When click confirm button send the otp to AWS Cognito
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String code = confirmCode.getText().toString().trim();

                if (code.isEmpty()) {
                    confirmCode.setError("Code is required");
                    confirmCode.requestFocus();
                    return;
                }

                cognito.confirmUser(email, code, new GenericHandler() {
                    @Override
                    public void onSuccess() {
                        // Navigate to next activity
                        startActivity(new Intent(ConfirmAccountCreationActivity.this, LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        confirmCode.setError(exception.getMessage());
                        confirmCode.requestFocus();

                    }
                });
            }
        });
    }
}

