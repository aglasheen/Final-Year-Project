package com.example.smartdoorbell.services.auth;

import android.content.Context;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Regions;
import com.example.smartdoorbell.BuildConfig;


/**
 * Service class to handle AWS Cognito authentication operations using Android SDK
 */
public class AWSCognitoService {

    private final CognitoUserPool userPool;
    private final Context context;

    private static final String USER_POOL_ID = BuildConfig.COGNITO_USER_POOL_ID;
    private static final String CLIENT_ID = BuildConfig.COGNITO_CLIENT_ID;
    private static final String CLIENT_SECRET = null;
    private static final Regions REGION = Regions.US_EAST_1;

    public interface SignInCallback {
        void onSuccess(CognitoUserSession session);
        void onFailure(Exception exception);
    }

    public AWSCognitoService(Context context) {
        this.context = context;
        this.userPool = new CognitoUserPool(context, USER_POOL_ID, CLIENT_ID, CLIENT_SECRET, REGION);
    }

    /**
     * Registers a new user account
     *
     */
    public void signUp(String name, String email, String password, SignUpHandler callback) {
        final CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        userAttributes.addAttribute("email", email);
        userAttributes.addAttribute("name", name);


        userPool.signUpInBackground(email, password, userAttributes, null, callback);
    }

    public void confirmUser(String email, String confirmationCode, GenericHandler callback) {
        CognitoUser cognitoUser = userPool.getUser(email);
        cognitoUser.confirmSignUpInBackground(confirmationCode, false, callback);
    }

    public void signIn(String email, String password, final SignInCallback callback) {
        CognitoUser cognitoUser = userPool.getUser(email);

        AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                callback.onSuccess(userSession);
            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation continuation, String userId) {
                AuthenticationDetails authDetails = new AuthenticationDetails(userId, password, null);
                continuation.setAuthenticationDetails(authDetails);
                continuation.continueTask();
            }

            @Override
            public void onFailure(Exception exception) {
                callback.onFailure(exception);
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) {
                continuation.continueTask();
            }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) {
                 continuation.continueTask();
            }
        };
        cognitoUser.getSessionInBackground(authenticationHandler);
    }

    public void getUserSession(AuthenticationHandler handler) {
        CognitoUser currentUser = getCurrentUser();
        if (currentUser != null) {
            currentUser.getSessionInBackground(handler);
        } else {
            handler.onFailure(new Exception("No cached user found"));
        }
    }

    /**
     * Fetches details for the currently logged in user
     */
    public void getUserDetails(GetDetailsHandler handler) {
        CognitoUser currentUser = getCurrentUser();
        if (currentUser != null) {
            currentUser.getDetailsInBackground(handler);
        } else {
            handler.onFailure(new Exception("No current user"));
        }
    }

    public void signOut() {
        CognitoUser currentUser = getCurrentUser();
        if (currentUser != null) {
            currentUser.signOut();
        }
    }

    /**
     * Changes the password for the currently logged in user
     */
    public void changePassword(String oldUserPassword, String newUserPassword, GenericHandler handler) {

        CognitoUser currentUser = getCurrentUser();
        if (currentUser != null) {
            currentUser.changePasswordInBackground(oldUserPassword, newUserPassword, handler);
        } else {
            handler.onFailure(new Exception("No current user"));
        }
    }



    public CognitoUser getCurrentUser() {
        return userPool.getCurrentUser();
    }
}
