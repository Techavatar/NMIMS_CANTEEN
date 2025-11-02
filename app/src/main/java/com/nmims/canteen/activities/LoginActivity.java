package com.nmims.canteen.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.nmims.canteen.R;
import com.nmims.canteen.models.User;
import com.nmims.canteen.services.FirebaseAuthService;

/**
 * User authentication (login) activity
 * Handles user login with email/password and Google Sign-In
 */
public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private static final int SPLASH_DELAY = 2000; // 2 seconds

    // UI Components
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private Button loginButton;
    private Button googleSignInButton;
    private TextView forgotPasswordTextView;
    private TextView signupTextView;
    private TextView appTitleTextView;
    private TextView appSubtitleTextView;
    private ProgressBar progressBar;
    private MaterialCardView loginCard;

    // Services
    private FirebaseAuthService authService;
    private GoogleSignInClient googleSignInClient;

    // State
    private boolean isFromSignup = false;
    private boolean isSplashShowing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize services
        authService = FirebaseAuthService.getInstance();
        initializeGoogleSignIn();

        // Initialize UI
        initializeViews();
        setupClickListeners();

        // Handle intents
        handleIntent();

        // Check for existing session
        checkExistingSession();
    }

    private void initializeViews() {
        // Get references to UI components
        appTitleTextView = findViewById(R.id.appTitleTextView);
        appSubtitleTextView = findViewById(R.id.appSubtitleTextView);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        signupTextView = findViewById(R.id.signupTextView);
        progressBar = findViewById(R.id.progressBar);
        loginCard = findViewById(R.id.loginCard);
    }

    private void initializeGoogleSignIn() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());

        googleSignInButton.setOnClickListener(v -> attemptGoogleSignIn());

        forgotPasswordTextView.setOnClickListener(v -> handleForgotPassword());

        signupTextView.setOnClickListener(v -> navigateToSignup());

        // Set focus change listeners for validation
        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateEmail();
            }
        });

        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validatePassword();
            }
        });
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("from_signup", false)) {
            isFromSignup = true;
            showLoginMessage("Account created successfully! Please login to continue.");
        }
    }

    private void checkExistingSession() {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            // User is already logged in and verified
            navigateToMain();
        } else if (currentUser != null && !currentUser.isEmailVerified()) {
            // User is logged in but email not verified
            showLoginMessage("Please verify your email address first.");
            // Hide splash and show login form
            hideSplash();
        } else {
            // No existing session, show splash then login form
            showSplashThenLogin();
        }
    }

    private void showSplashThenLogin() {
        // Show splash screen for 2 seconds, then show login form
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            hideSplash();
        }, SPLASH_DELAY);
    }

    private void hideSplash() {
        isSplashShowing = false;
        appTitleTextView.setVisibility(View.VISIBLE);
        appSubtitleTextView.setVisibility(View.VISIBLE);
        loginCard.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    private void attemptLogin() {
        // Reset errors
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);

        // Get values
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Cancel if there are errors
        boolean cancel = false;
        View focusView = null;

        // Check for valid password
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Password is required");
            focusView = passwordEditText;
            cancel = true;
        } else if (password.length() < 6) {
            passwordInputLayout.setError("Password must be at least 6 characters");
            focusView = passwordEditText;
            cancel = true;
        }

        // Check for valid email
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Email is required");
            focusView = emailEditText;
            cancel = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Please enter a valid email address");
            focusView = emailEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to perform the user login attempt
            showProgress(true);
            authService.signIn(email, password, new FirebaseAuthService.AuthCallback() {
                @Override
                public void onSuccess(AuthResult result) {
                    showProgress(false);
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        if (user.isEmailVerified()) {
                            navigateToMain();
                        } else {
                            showLoginMessage("Please verify your email address before logging in.");
                            // Send verification email
                            authService.sendEmailVerification(new FirebaseAuthService.EmailVerificationCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(String error) {
                    showProgress(false);
                    showLoginError(error);
                }
            });
        }
    }

    private void attemptGoogleSignIn() {
        showProgress(true);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleForgotPassword() {
        String email = emailEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Please enter a valid email address");
            emailEditText.requestFocus();
            return;
        }

        authService.resetPassword(email, new FirebaseAuthService.PasswordResetCallback() {
            @Override
            public void onSuccess(String message) {
                showLoginMessage(message);
            }

            @Override
            public void onFailure(String error) {
                showLoginError(error);
            }
        });
    }

    private void navigateToSignup() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                showProgress(false);
                showLoginError("Google sign in failed: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        authService.signInWithGoogle(idToken, new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess(AuthResult result) {
                showProgress(false);
                navigateToMain();
            }

            @Override
            public void onFailure(String error) {
                showProgress(false);
                showLoginError(error);
            }
        });
    }

    private boolean validateEmail() {
        String email = emailEditText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError("Email is required");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Please enter a valid email address");
            return false;
        } else {
            emailInputLayout.setError(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String password = passwordEditText.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError("Password is required");
            return false;
        } else if (password.length() < 6) {
            passwordInputLayout.setError("Password must be at least 6 characters");
            return false;
        } else {
            passwordInputLayout.setError(null);
            return true;
        }
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        googleSignInButton.setEnabled(!show);
        emailEditText.setEnabled(!show);
        passwordEditText.setEnabled(!show);
    }

    private void showLoginMessage(String message) {
        if (isSplashShowing) {
            // Show message as toast during splash
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } else {
            // Show as snackbar during normal login
            Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showLoginError(String error) {
        if (isSplashShowing) {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        } else {
            Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG)
                    .setAction("RETRY", v -> attemptLogin())
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        if (isSplashShowing) {
            // Don't allow back button during splash
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources
        googleSignInClient = null;
    }
}