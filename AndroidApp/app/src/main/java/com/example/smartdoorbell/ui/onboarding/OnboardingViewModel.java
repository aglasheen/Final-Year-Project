package com.example.smartdoorbell.ui.onboarding;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OnboardingViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public OnboardingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Onboarding fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
