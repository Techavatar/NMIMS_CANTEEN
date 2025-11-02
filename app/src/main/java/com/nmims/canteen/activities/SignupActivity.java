package com.nmims.canteen.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
 * New user registration activity
 * Handles user signup with email/password and Google Sign-In
 */
public class SignupActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;

    // UI Components
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private TextInputEditText phoneEditText;
    private TextInputLayout nameInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private TextInputLayout phoneInputLayout;
    private Button signupButton;
    private Button googleSignUpButton;
    private TextView loginTextView;
    private CheckBox termsCheckBox;
    private TextView termsTextView;
    private TextView privacyTextView;
    private ProgressBar progressBar;
    private MaterialCardView signupCard;

    // Services
    private FirebaseAuthService authService;
    private GoogleSignInClient googleSignInClient;

    // State
    private boolean isTermsAccepted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize services
        authService = FirebaseAuthService.getInstance();
        initializeGoogleSignIn();

        // Initialize UI
        initializeViews();
        setupClickListeners();

        // Check if user is already logged in
        checkExistingSession();
    }

    private void initializeViews() {
        // Get references to UI components
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        nameInputLayout = findViewById(R.id.nameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        signupButton = findViewById(R.id.signupButton);
        googleSignUpButton = findViewById(R.id.googleSignUpButton);
        loginTextView = findViewById(R.id.loginTextView);
        termsCheckBox = findViewById(R.id.termsCheckBox);
        termsTextView = findViewById(R.id.termsTextView);
        privacyTextView = findViewById(R.id.privacyTextView);
        progressBar = findViewById(R.id.progressBar);
        signupCard = findViewById(R.id.signupCard);
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
        signupButton.setOnClickListener(v -> attemptSignup());

        googleSignUpButton.setOnClickListener(v -> attemptGoogleSignUp());

        loginTextView.setOnClickListener(v -> navigateToLogin());

        termsCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isTermsAccepted = isChecked;
            validateTerms();
        });

        termsTextView.setOnClickListener(v -> showTermsAndConditions());

        privacyTextView.setOnClickListener(v -> showPrivacyPolicy());

        // Set focus change listeners for validation
        nameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateName();
            }
        });

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

        confirmPasswordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateConfirmPassword();
            }
        });

        phoneEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validatePhone();
            }
        });
    }

    private void checkExistingSession() {
        FirebaseUser currentUser = authService.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, navigate to main
            navigateToMain();
        }
    }

    private void attemptSignup() {
        // Reset errors
        nameInputLayout.setError(null);
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);
        phoneInputLayout.setError(null);

        // Get values
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Cancel if there are errors
        boolean cancel = false;
        View focusView = null;

        // Check terms acceptance
        if (!validateTerms()) {
            focusView = termsCheckBox;
            cancel = true;
        }

        // Check phone number
        if (!validatePhone()) {
            focusView = phoneEditText;
            cancel = true;
        }

        // Check for matching passwords
        if (!validateConfirmPassword()) {
            focusView = confirmPasswordEditText;
            cancel = true;
        }

        // Check for valid password
        if (!validatePassword()) {
            focusView = passwordEditText;
            cancel = true;
        }

        // Check for valid email
        if (!validateEmail()) {
            focusView = emailEditText;
            cancel = true;
        }

        // Check for valid name
        if (!validateName()) {
            focusView = nameEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt signup and focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to perform the user signup attempt
            showProgress(true);
            authService.signUpWithDetails(email, password, name, phone, null, null, new FirebaseAuthService.AuthCallback() {
                @Override
                public void onSuccess(AuthResult result) {
                    showProgress(false);
                    showSignupSuccessMessage();
                    navigateToLogin();
                }

                @Override
                public void onFailure(String error) {
                    showProgress(false);
                    showSignupError(error);
                }
            });
        }
    }

    private void attemptGoogleSignUp() {
        // Check terms acceptance for Google Sign-In as well
        if (!validateTerms()) {
            termsCheckBox.requestFocus();
            return;
        }

        showProgress(true);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("from_signup", true);
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
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                showProgress(false);
                showSignupError("Google sign up failed: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        authService.signInWithGoogle(account.getIdToken(), new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess(AuthResult result) {
                showProgress(false);
                showSignupSuccessMessage();
                navigateToMain();
            }

            @Override
            public void onFailure(String error) {
                showProgress(false);
                showSignupError(error);
            }
        });
    }

    // Validation methods
    private boolean validateName() {
        String name = nameEditText.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError("Name is required");
            return false;
        } else if (name.length() < 2) {
            nameInputLayout.setError("Name must be at least 2 characters");
            return false;
        } else if (name.length() > 50) {
            nameInputLayout.setError("Name must be less than 50 characters");
            return false;
        } else {
            nameInputLayout.setError(null);
            return true;
        }
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
        } else if (!password.matches(".*[A-Z].*")) {
            passwordInputLayout.setError("Password must contain at least one uppercase letter");
            return false;
        } else if (!password.matches(".*[a-z].*")) {
            passwordInputLayout.setError("Password must contain at least one lowercase letter");
            return false;
        } else if (!password.matches(".*\\d.*")) {
            passwordInputLayout.setError("Password must contain at least one number");
            return false;
        } else {
            passwordInputLayout.setError(null);
            return true;
        }
    }

    private boolean validateConfirmPassword() {
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInputLayout.setError("Please confirm your password");
            return false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError("Passwords do not match");
            return false;
        } else {
            confirmPasswordInputLayout.setError(null);
            return true;
        }
    }

    private boolean validatePhone() {
        String phone = phoneEditText.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            phoneInputLayout.setError(null); // Phone is optional
            return true;
        } else if (phone.length() < 10 || phone.length() > 15) {
            phoneInputLayout.setError("Please enter a valid phone number");
            return false;
        } else if (!phone.matches("^[+]?[0-9\\-\\s()]+$")) {
            phoneInputLayout.setError("Please enter a valid phone number");
            return false;
        } else {
            phoneInputLayout.setError(null);
            return true;
        }
    }

    private boolean validateTerms() {
        if (!isTermsAccepted) {
            termsCheckBox.setError("Please accept the terms and conditions");
            return false;
        } else {
            termsCheckBox.setError(null);
            return true;
        }
    }

    private void showTermsAndConditions() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Terms and Conditions")
                .setMessage("Welcome to NMIMS Canteen!\n\n" +
                        "By using our app, you agree to:\n" +
                        "1. Provide accurate information\n" +
                        "2. Use the service for lawful purposes\n" +
                        "3. Not share your account credentials\n" +
                        "4. Respect other users and staff\n" +
                        "5. Pay for all ordered items\n" +
                        "6. Follow canteen rules and policies\n\n" +
                        "We reserve the right to modify these terms at any time.")
                .setPositiveButton("I Understand", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showPrivacyPolicy() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("NMIMS Canteen Privacy Policy\n\n" +
                        "We collect and use your information to:\n" +
                        "• Provide food ordering services\n" +
                        "• Process payments securely\n" +
                        "• Send order updates\n" +
                        "• Improve our services\n\n" +
                        "We do not sell your personal information to third parties. " +
                        "Your data is encrypted and stored securely.")
                .setPositiveButton("I Understand", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        signupButton.setEnabled(!show);
        googleSignUpButton.setEnabled(!show);
        nameEditText.setEnabled(!show);
        emailEditText.setEnabled(!show);
        passwordEditText.setEnabled(!show);
        confirmPasswordEditText.setEnabled(!show);
        phoneEditText.setEnabled(!show);
    }

    private void showSignupSuccessMessage() {
        Snackbar.make(findViewById(android.R.id.content),
                "Account created successfully! Please check your email for verification.",
                Snackbar.LENGTH_LONG).show();
    }

    private void showSignupError(String error) {
        Snackbar.make(findViewById(android.R.id.content),
                error,
                Snackbar.LENGTH_LONG)
                .setAction("RETRY", v -> attemptSignup())
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Custom back animation if needed
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources
        googleSignInClient = null;
    }
}