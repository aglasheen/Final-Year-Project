package com.example.smartdoorbell.ui.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.example.smartdoorbell.databinding.FragmentChangePasswordBinding;
import com.example.smartdoorbell.services.auth.AWSCognitoService;

public class ChangePasswordFragment extends Fragment {

    private FragmentChangePasswordBinding binding;
    private AWSCognitoService cognitoService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        cognitoService = new AWSCognitoService(requireContext());

        binding.btnSubmitChangePassword.setOnClickListener(v -> handleChangePassword());

        return binding.getRoot();
    }

    private void handleChangePassword() {
        String currentUserPassword = binding.currentPassword.getText().toString().trim();
        String newUserPassword = binding.newPassword.getText().toString().trim();
        String confirmPassword = binding.confirmNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(currentUserPassword) || TextUtils.isEmpty(newUserPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newUserPassword.equals(confirmPassword)) {
            Toast.makeText(getContext(), "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSubmitChangePassword.setEnabled(false);

        cognitoService.changePassword(currentUserPassword, newUserPassword, new GenericHandler() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigateUp();
                    });
                }
            }

            @Override
            public void onFailure(Exception exception) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSubmitChangePassword.setEnabled(true);
                        Toast.makeText(getContext(), "Error: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
