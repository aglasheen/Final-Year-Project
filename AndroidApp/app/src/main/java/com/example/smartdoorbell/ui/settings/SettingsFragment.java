package com.example.smartdoorbell.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.smartdoorbell.LoginActivity;
import com.example.smartdoorbell.databinding.FragmentSettingsBinding;

import java.util.concurrent.Executor;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel settingsViewModel;
    private Executor executor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        executor = ContextCompat.getMainExecutor(requireContext());
        
        setupObservers();
        setupListeners();

        binding.appVersion.setText("Version: 1.0.0");

        return binding.getRoot();
    }

    private void setupObservers() {
        settingsViewModel.getUserName().observe(getViewLifecycleOwner(), name -> {
            binding.userName.setText(name);
        });

        settingsViewModel.getUserEmail().observe(getViewLifecycleOwner(), email -> {
            binding.userEmail.setText(email);
        });

        settingsViewModel.getIs2FAEnabled().observe(getViewLifecycleOwner(), isEnabled -> {
            if (binding.switch2fa.isChecked() != isEnabled) {
                binding.switch2fa.setChecked(isEnabled);
            }
        });

        settingsViewModel.getIsBiometricEnabled().observe(getViewLifecycleOwner(), isEnabled -> {
            if (binding.switchBiometric.isChecked() != isEnabled) {
                binding.switchBiometric.setChecked(isEnabled);
            }
        });

        settingsViewModel.getIsDarkMode().observe(getViewLifecycleOwner(), isDarkMode -> {
            if (binding.switchDarkMode.isChecked() != isDarkMode) {
                binding.switchDarkMode.setChecked(isDarkMode);
            }
        });

        settingsViewModel.getNavigateToLogin().observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (shouldNavigate) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    private void setupListeners() {
        binding.switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !settingsViewModel.getIsBiometricEnabled().getValue()) {
                checkAndEnableBiometrics();
            } else if (!isChecked && settingsViewModel.getIsBiometricEnabled().getValue()) {
                settingsViewModel.setBiometricEnabled(false);
            }
        });

        binding.switchDarkMode.setOnClickListener(v -> {
            boolean isChecked = binding.switchDarkMode.isChecked();
            settingsViewModel.setDarkMode(isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked ? 
                    AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        binding.btnLogout.setOnClickListener(v -> {
            settingsViewModel.logout();
            Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();
        });
    }

    private void checkAndEnableBiometrics() {
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                showBiometricPromptToEnable();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(getContext(), "No biometric hardware found", Toast.LENGTH_SHORT).show();
                binding.switchBiometric.setChecked(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(getContext(), "Biometric hardware unavailable", Toast.LENGTH_SHORT).show();
                binding.switchBiometric.setChecked(false);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(getContext(), "No biometrics enrolled on device", Toast.LENGTH_SHORT).show();
                binding.switchBiometric.setChecked(false);
                break;
        }
    }

    private void showBiometricPromptToEnable() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verify Identity")
                .setSubtitle("Confirm biometrics to enable app lock")
                .setNegativeButtonText("Cancel")
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        settingsViewModel.setBiometricEnabled(true);
                        Toast.makeText(getContext(), "Biometric lock enabled", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        binding.switchBiometric.setChecked(false);
                        Toast.makeText(getContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        // This is called for unrecognized prints, don't toggle off yet
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
