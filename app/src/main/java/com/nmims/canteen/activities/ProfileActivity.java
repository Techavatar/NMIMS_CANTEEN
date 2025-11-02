package com.nmims.canteen.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.nmims.canteen.R;
import com.nmims.canteen.models.User;
import com.nmims.canteen.services.FirebaseAuthService;
import com.nmims.canteen.services.FirestoreService;

/**
 * User Profile Management Activity
 * Handles viewing and editing user profile information
 */
public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextInputEditText nameEditText;
    private TextInputEditText phoneEditText;
    private Button saveButton;
    private Button editProfileButton;

    // Services
    private FirebaseAuthService authService;
    private FirestoreService firestoreService;

    // Data
    private User currentUser;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize services
        authService = FirebaseAuthService.getInstance();
        firestoreService = FirestoreService.getInstance();

        // Initialize UI
        initializeViews();
        setupToolbar();
        loadUserProfile();
        setupClickListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        saveButton = findViewById(R.id.saveButton);
        editProfileButton = findViewById(R.id.editProfileButton);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void loadUserProfile() {
        if (authService.getCurrentUser() != null) {
            authService.getCurrentUserProfile(new FirebaseAuthService.UserProfileCallback() {
                @Override
                public void onSuccess(User user) {
                    currentUser = user;
                    updateUI();
                }

                @Override
                public void onFailure(String error) {
                    showError("Failed to load profile: " + error);
                    finish();
                }
            });
        }
    }

    private void updateUI() {
        if (currentUser != null) {
            nameTextView.setText(currentUser.getName());
            emailTextView.setText(currentUser.getEmail());
            nameEditText.setText(currentUser.getName());
            phoneEditText.setText(currentUser.getPhone());
        }
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> toggleEditMode());
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        updateEditModeUI();
    }

    private void updateEditModeUI() {
        if (isEditMode) {
            nameEditText.setVisibility(android.view.View.VISIBLE);
            phoneEditText.setVisibility(android.view.View.VISIBLE);
            nameTextView.setVisibility(android.view.View.GONE);
            saveButton.setVisibility(android.view.View.VISIBLE);
            editProfileButton.setVisibility(android.view.View.GONE);
        } else {
            nameEditText.setVisibility(android.view.View.GONE);
            phoneEditText.setVisibility(android.view.View.GONE);
            nameTextView.setVisibility(android.view.View.VISIBLE);
            saveButton.setVisibility(android.view.View.GONE);
            editProfileButton.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void saveProfile() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (name.isEmpty()) {
            nameEditText.setError("Name cannot be empty");
            return;
        }

        if (currentUser != null) {
            currentUser.setName(name);
            currentUser.setPhone(phone);

            firestoreService.updateUserProfile(currentUser, new FirestoreService.DatabaseCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    toggleEditMode();
                    updateUI();
                }

                @Override
                public void onFailure(String error) {
                    showError("Failed to update profile: " + error);
                }
            });
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_settings) {
            // Navigate to settings
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isEditMode) {
            toggleEditMode();
        } else {
            super.onBackPressed();
        }
    }
}