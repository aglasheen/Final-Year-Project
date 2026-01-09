package com.example.smartdoorbell.ui.settings;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.example.smartdoorbell.services.auth.AWSCognitoService;

import java.util.Map;

public class SettingsViewModel extends AndroidViewModel {
    private final SharedPreferences prefs;
    private final MutableLiveData<String> userName = new MutableLiveData<>("Loading...");
    private final MutableLiveData<String> userEmail = new MutableLiveData<>("");

    private final MutableLiveData<Boolean> is2FAEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isBiometricEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isDarkMode = new MutableLiveData<>(false);

    private final MutableLiveData<Boolean> isChangePass = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> navigateToLogin = new MutableLiveData<>(false);
    private final AWSCognitoService cognitoService;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        prefs = application.getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        is2FAEnabled.setValue(prefs.getBoolean("2fa_enabled", false));
        isDarkMode.setValue(prefs.getBoolean("dark_mode", false));
        isBiometricEnabled.setValue(prefs.getBoolean("biometric_lock", false));
        isChangePass.setValue(prefs.getBoolean("change_pass", false));
        cognitoService = new AWSCognitoService(application.getApplicationContext());
        
        fetchUserDetails();
    }

    private void fetchUserDetails() {
        cognitoService.getUserDetails(new GetDetailsHandler() {
            @Override
            public void onSuccess(CognitoUserDetails userDetails) {
                Map<String, String> attributes = userDetails.getAttributes().getAttributes();
                String name = attributes.get("name");
                
                userName.postValue(name != null ? name : "User");
                userEmail.postValue(attributes.get("email"));
            }

            @Override
            public void onFailure(Exception exception) {
                userName.postValue("User");
                userEmail.postValue("Error fetching details");
            }
        });
    }

    public LiveData<String> getUserName() {
        return userName;
    }

    public LiveData<String> getUserEmail() {
        return userEmail;
    }

    public LiveData<Boolean> getIs2FAEnabled() {
        return is2FAEnabled;
    }

    public LiveData<Boolean> getIsBiometricEnabled() {
        return isBiometricEnabled;
    }

    public LiveData<Boolean> getIsChangePass() {
        return isChangePass;
    }


    public LiveData<Boolean> getIsDarkMode() {
        return isDarkMode;
    }


    public void set2FAEnabled(boolean enabled) {
        is2FAEnabled.setValue(enabled);
        prefs.edit().putBoolean("2fa_enabled", enabled).apply();
    }
    public void setBiometricEnabled(boolean enabled) {
        isBiometricEnabled.setValue(enabled);
        prefs.edit().putBoolean("biometric_lock", enabled).apply();
    }

    public void setDarkMode(boolean enabled) {
        isDarkMode.setValue(enabled);
        prefs.edit().putBoolean("dark_mode", enabled).apply();
    }

    public void setChangePass(boolean enabled) {
        isChangePass.setValue(enabled);
    }

    public LiveData<Boolean> getNavigateToLogin() {
        return navigateToLogin;
    }

    public void logout() {
        cognitoService.signOut();
        navigateToLogin.setValue(true);
    }
}
