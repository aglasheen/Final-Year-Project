package com.example.smartdoorbell;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.example.smartdoorbell.services.auth.AWSCognitoService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.concurrent.Executor;


/*
 * This class is the main activity of the application.
 * It manages the initial authentication check and biometric lock before showing the main UI.
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private boolean isCheckingAuth = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        splashScreen.setKeepOnScreenCondition(() -> isCheckingAuth);

        SharedPreferences prefs = getSharedPreferences("user_settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? 
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Authentication Check
        AWSCognitoService cognitoService = new AWSCognitoService(this);
        cognitoService.getUserSession(new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                runOnUiThread(() -> {
                    // Only check biometrics if we are not recreating the UI
                    if (savedInstanceState == null) {
                        checkBiometricLock();
                    } else {
                        isCheckingAuth = false;
                        setupApp();
                    }
                });
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation continuation, String userId) {
                runOnUiThread(() -> {
                    isCheckingAuth = false;
                    redirectToLogin();
                });
            }

            @Override
            public void onFailure(Exception exception) {
                runOnUiThread(() -> {
                    isCheckingAuth = false;
                    redirectToLogin();
                });
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {}
            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {}
        });
    }

    /**
     * Checks if biometric lock is enabled and either prompts or continues to app.
     */
    private void checkBiometricLock() {
        SharedPreferences prefs = getSharedPreferences("user_settings", MODE_PRIVATE);
        if (prefs.getBoolean("biometric_lock", false)) {
            showBiometricPrompt();
        } else {
            // No lock, proceed directly
            isCheckingAuth = false;
            setupApp();
        }
    }

    /**
     * Triggers the biometric scanner.
     */
    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        runOnUiThread(() -> {
                            isCheckingAuth = false;
                            setupApp();
                        });
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // Close app if cancelled or error occurs to maintain security
                        Toast.makeText(MainActivity.this, "Security Verification Required", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        // System handles retries automatically
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Lock")
                .setSubtitle("Confirm your identity to access the app")
                .setNegativeButtonText("Exit")
                .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Initializes the Main UI components. 
     */
    private void setupApp() {
        toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupWithNavController(bottomNavigation, navController);

        // Controller to hide or show the bottom nav menu based on current fragment
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();

            // Screens that should not show the bottom navigation menu
            boolean shouldHideNav = (id == R.id.navigation_settings || id == R.id.navigation_onboarding);

            if (shouldHideNav) {
                bottomNavigation.setVisibility(View.GONE);
            } else {
                bottomNavigation.setVisibility(View.VISIBLE);
            }
        });


        // Main navigation bar fragments
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
        ).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return (navController != null && navController.navigateUp()) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (navController != null && NavigationUI.onNavDestinationSelected(item, navController)) || super.onOptionsItemSelected(item);
    }
}
