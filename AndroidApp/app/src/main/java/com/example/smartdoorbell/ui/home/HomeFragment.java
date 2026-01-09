package com.example.smartdoorbell.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.smartdoorbell.R;
import com.example.smartdoorbell.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        // Check if a device is already linked
        SharedPreferences prefs = requireContext().getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        boolean isDeviceLinked = prefs.getBoolean("device_linked", false);

        // Show/Hide onboarding button based on device status
        if (isDeviceLinked) {
            binding.onboardingButton.setVisibility(View.GONE);
            textView.setText("Your Doorbell is ready!");
        } else {
            binding.onboardingButton.setVisibility(View.VISIBLE);
            textView.setText("Click here to onboard your doorbell");
        }

        // Onboard the smart camera
        binding.onboardingButton.setOnClickListener(v -> {
            // Use Navigation Component instead of Intent
            Navigation.findNavController(v).navigate(R.id.navigation_onboarding);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
