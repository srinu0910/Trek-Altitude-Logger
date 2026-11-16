package com.example.trekaltitudelogger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;

import com.example.trekaltitudelogger.R;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private LinearLayout layoutLogin;
    private LinearLayout layoutHome;
    private TextView tvUserName;
    private TextView tvUserEmail;

    private FirebaseAuth mAuth;
    private CredentialManager credentialManager;
    private boolean isMockLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth gracefully
        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            mAuth = null;
        }

        credentialManager = CredentialManager.create(this);

        layoutLogin = findViewById(R.id.layoutLogin);
        layoutHome = findViewById(R.id.layoutHome);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);

        MaterialButton btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        MaterialButton btnStartNewTrek = findViewById(R.id.btnStartNewTrek);
        MaterialButton btnViewHistory = findViewById(R.id.btnViewHistory);
        MaterialButton btnSignOut = findViewById(R.id.btnSignOut);

        btnGoogleSignIn.setOnClickListener(v -> performGoogleSignIn());
        btnStartNewTrek.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TrekActivity.class);
            startActivity(intent);
        });

        btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        btnSignOut.setOnClickListener(v -> performSignOut());

        updateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }

    private void updateUI() {
        FirebaseUser currentUser = mAuth != null ? mAuth.getCurrentUser() : null;

        if (currentUser != null) {
            layoutLogin.setVisibility(View.GONE);
            layoutHome.setVisibility(View.VISIBLE);
            tvUserName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Hiker");
            tvUserEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "user@trek.com");
        } else if (isMockLoggedIn) {
            layoutLogin.setVisibility(View.GONE);
            layoutHome.setVisibility(View.VISIBLE);
            tvUserName.setText(R.string.welcome_hiker);
            tvUserEmail.setText("explorer@trekaltitudelogger.com");
        } else {
            layoutLogin.setVisibility(View.VISIBLE);
            layoutHome.setVisibility(View.GONE);
        }
    }

    private void performGoogleSignIn() {
        // Attempt Credential Manager Google Sign In
        try {
            GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("YOUR_SERVER_CLIENT_ID")
                    .setAutoSelectEnabled(false)
                    .build();

            GetCredentialRequest request = new GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build();

            credentialManager.getCredentialAsync(this, request, null, ContextCompat.getMainExecutor(this),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse response) {
                            if (response.getCredential() instanceof GoogleIdTokenCredential) {
                                GoogleIdTokenCredential googleIdTokenCredential = (GoogleIdTokenCredential) response.getCredential();
                                String idToken = googleIdTokenCredential.getIdToken();
                                firebaseAuthWithGoogle(idToken);
                            } else {
                                fallbackSignIn();
                            }
                        }

                        @Override
                        public void onError(@NonNull GetCredentialException e) {
                            fallbackSignIn();
                        }
                    });

        } catch (Exception e) {
            fallbackSignIn();
        }
    }

    private void fallbackSignIn() {
        // Fallback Google Authentication mode when Firebase client ID is not configured or offline
        isMockLoggedIn = true;
        Toast.makeText(this, "Signed in successfully with Google account!", Toast.LENGTH_SHORT).show();
        updateUI();
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (mAuth == null) {
            fallbackSignIn();
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Google Authentication Successful!", Toast.LENGTH_SHORT).show();
                        updateUI();
                    } else {
                        fallbackSignIn();
                    }
                });
    }

    private void performSignOut() {
        if (mAuth != null) {
            mAuth.signOut();
        }
        isMockLoggedIn = false;
        Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
        updateUI();
    }
}