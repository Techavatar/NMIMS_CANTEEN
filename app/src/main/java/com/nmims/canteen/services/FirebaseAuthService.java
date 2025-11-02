package com.nmims.canteen.services;

import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.nmims.canteen.models.User;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Firebase authentication operations service
 * Handles user authentication, profile management, and session management
 */
public class FirebaseAuthService {
    private static final String TAG = "FirebaseAuthService";
    private static FirebaseAuthService instance;

    private final FirebaseAuth mAuth;
    private final Executor executor;

    // Authentication result callback
    public interface AuthCallback {
        void onSuccess(AuthResult result);
        void onFailure(String error);
    }

    // User profile callback
    public interface UserProfileCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }

    // Password reset callback
    public interface PasswordResetCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    // Email verification callback
    public interface EmailVerificationCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    private FirebaseAuthService() {
        this.mAuth = FirebaseAuth.getInstance();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Get singleton instance
     */
    public static synchronized FirebaseAuthService getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthService();
        }
        return instance;
    }

    /**
     * Get current FirebaseAuth instance
     */
    public FirebaseAuth getAuth() {
        return mAuth;
    }

    // User Registration

    /**
     * Sign up new user with email and password
     */
    public void signUp(String email, String password, String name, AuthCallback callback) {
        // Validate input
        String validationError = validateSignUpInput(email, password, name);
        if (validationError != null) {
            if (callback != null) callback.onFailure(validationError);
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        AuthResult result = task.getResult();
                        FirebaseUser firebaseUser = result.getUser();

                        if (firebaseUser != null) {
                            // Update user profile
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(executor, profileTask -> {
                                        if (profileTask.isSuccessful()) {
                                            Log.d(TAG, "User profile updated.");

                                            // Create user document in Firestore
                                            createUserProfile(firebaseUser, name, email, new UserProfileCallback() {
                                                @Override
                                                public void onSuccess(User user) {
                                                    // Send email verification
                                                    sendEmailVerification(new EmailVerificationCallback() {
                                                        @Override
                                                        public void onSuccess(String message) {
                                                            Log.d(TAG, "Verification email sent");
                                                        }

                                                        @Override
                                                        public void onFailure(String error) {
                                                            Log.w(TAG, "Failed to send verification email: " + error);
                                                        }
                                                    });

                                                    if (callback != null) callback.onSuccess(result);
                                                }

                                                @Override
                                                public void onFailure(String error) {
                                                    Log.e(TAG, "Failed to create user profile: " + error);
                                                    if (callback != null) callback.onFailure("Account created but profile setup failed: " + error);
                                                }
                                            });
                                        } else {
                                            Log.e(TAG, "Failed to update user profile", profileTask.getException());
                                            if (callback != null) callback.onFailure("Failed to set up user profile");
                                        }
                                    });
                        } else {
                            if (callback != null) callback.onFailure("Failed to create user account");
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        if (callback != null) callback.onFailure(getAuthErrorMessage(task.getException()));
                    }
                });
    }

    /**
     * Sign up with additional user information
     */
    public void signUpWithDetails(String email, String password, String name, String phoneNumber,
                                 String dateOfBirth, String gender, AuthCallback callback) {
        signUp(email, password, name, new AuthCallback() {
            @Override
            public void onSuccess(AuthResult result) {
                FirebaseUser firebaseUser = result.getUser();
                if (firebaseUser != null) {
                    // Update user profile with additional details
                    updateUserProfile(firebaseUser.getUid(), phoneNumber, dateOfBirth, gender, new UserProfileCallback() {
                        @Override
                        public void onSuccess(User user) {
                            if (callback != null) callback.onSuccess(result);
                        }

                        @Override
                        public void onFailure(String error) {
                            if (callback != null) callback.onFailure("Account created but failed to save additional details: " + error);
                        }
                    });
                } else {
                    if (callback != null) callback.onFailure("Failed to create user account");
                }
            }

            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    // User Login

    /**
     * Sign in user with email and password
     */
    public void signIn(String email, String password, AuthCallback callback) {
        // Validate input
        String validationError = validateSignInInput(email, password);
        if (validationError != null) {
            if (callback != null) callback.onFailure(validationError);
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");

                        // Update last login in Firestore
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            updateLastLogin(firebaseUser.getUid());
                        }

                        if (callback != null) callback.onSuccess(task.getResult());
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        if (callback != null) callback.onFailure(getAuthErrorMessage(task.getException()));
                    }
                });
    }

    /**
     * Sign in with Google credentials
     */
    public void signInWithGoogle(String idToken, AuthCallback callback) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Check if user is new
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                // Create user profile for new Google user
                                createUserProfile(firebaseUser, firebaseUser.getDisplayName(),
                                        firebaseUser.getEmail(), new UserProfileCallback() {
                                    @Override
                                    public void onSuccess(User user) {
                                        if (callback != null) callback.onSuccess(task.getResult());
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        if (callback != null) callback.onFailure("Failed to create user profile: " + error);
                                    }
                                });
                            } else {
                                // Update last login for existing user
                                updateLastLogin(firebaseUser.getUid());
                                if (callback != null) callback.onSuccess(task.getResult());
                            }
                        } else {
                            if (callback != null) callback.onFailure("Failed to authenticate with Google");
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        if (callback != null) callback.onFailure(getAuthErrorMessage(task.getException()));
                    }
                });
    }

    /**
     * Sign in with phone number
     */
    public void signInWithPhone(String phoneNumber, String verificationCode, AuthCallback callback) {
        // This would implement phone authentication using Firebase Phone Auth
        // For now, return not implemented
        if (callback != null) callback.onFailure("Phone authentication not implemented yet");
    }

    // Password Management

    /**
     * Reset password
     */
    public void resetPassword(String email, PasswordResetCallback callback) {
        if (email == null || email.trim().isEmpty()) {
            if (callback != null) callback.onFailure("Email address is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (callback != null) callback.onFailure("Invalid email address");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Password reset email sent");
                        if (callback != null) callback.onSuccess("Password reset email sent to " + email);
                    } else {
                        Log.w(TAG, "Failed to send password reset email", task.getException());
                        if (callback != null) callback.onFailure(getAuthErrorMessage(task.getException()));
                    }
                });
    }

    /**
     * Change password
     */
    public void changePassword(String currentPassword, String newPassword, AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onFailure("No user is currently signed in");
            return;
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 6) {
            if (callback != null) callback.onFailure("New password must be at least 6 characters long");
            return;
        }

        // Re-authenticate user for security
        String email = user.getEmail();
        if (email != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(email, currentPassword);
            user.reauthenticate(credential)
                    .addOnCompleteListener(executor, reauthTask -> {
                        if (reauthTask.isSuccessful()) {
                            // Update password
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(executor, updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            Log.d(TAG, "Password updated successfully");
                                            if (callback != null) callback.onSuccess(reauthTask.getResult());
                                        } else {
                                            Log.w(TAG, "Failed to update password", updateTask.getException());
                                            if (callback != null) callback.onFailure(getAuthErrorMessage(updateTask.getException()));
                                        }
                                    });
                        } else {
                            Log.w(TAG, "Re-authentication failed", reauthTask.getException());
                            if (callback != null) callback.onFailure("Current password is incorrect");
                        }
                    });
        } else {
            if (callback != null) callback.onFailure("User email not found");
        }
    }

    // Email Verification

    /**
     * Send email verification
     */
    public void sendEmailVerification(EmailVerificationCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onFailure("No user is currently signed in");
            return;
        }

        if (user.isEmailVerified()) {
            if (callback != null) callback.onSuccess("Email is already verified");
            return;
        }

        user.sendEmailVerification()
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Verification email sent");
                        if (callback != null) callback.onSuccess("Verification email sent to " + user.getEmail());
                    } else {
                        Log.w(TAG, "Failed to send verification email", task.getException());
                        if (callback != null) callback.onFailure(getAuthErrorMessage(task.getException()));
                    }
                });
    }

    /**
     * Check if email is verified
     */
    public boolean isEmailVerified() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }

    // User Profile Management

    /**
     * Get current user profile
     */
    public void getCurrentUserProfile(UserProfileCallback callback) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            if (callback != null) callback.onFailure("No user is currently signed in");
            return;
        }

        FirebaseUtils.getUserDocument(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null) {
                            if (callback != null) callback.onSuccess(user);
                        } else {
                            if (callback != null) callback.onFailure("User profile not found");
                        }
                    } else {
                        Log.e(TAG, "Failed to get user profile", task.getException());
                        if (callback != null) callback.onFailure(getFirestoreErrorMessage(task.getException()));
                    }
                });
    }

    /**
     * Update user profile
     */
    public void updateUserProfile(String userId, String phoneNumber, String dateOfBirth, String gender, UserProfileCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        if (phoneNumber != null) updates.put("phoneNumber", phoneNumber);
        if (dateOfBirth != null) updates.put("dateOfBirth", dateOfBirth);
        if (gender != null) updates.put("gender", gender);
        updates.put("updatedAt", new java.util.Date());

        FirebaseUtils.getUserDocument(userId)
                .update(updates)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated successfully");
                        getCurrentUserProfile(callback); // Return updated profile
                    } else {
                        Log.e(TAG, "Failed to update user profile", task.getException());
                        if (callback != null) callback.onFailure(getFirestoreErrorMessage(task.getException()));
                    }
                });
    }

    /**
     * Update display name
     */
    public void updateDisplayName(String displayName, AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onFailure("No user is currently signed in");
            return;
        }

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Display name updated");

                        // Update in Firestore
                        FirebaseUtils.getUserDocument(user.getUid())
                                .update("name", displayName, "updatedAt", new java.util.Date())
                                .addOnCompleteListener(executor, firestoreTask -> {
                                    if (firestoreTask.isSuccessful()) {
                                        if (callback != null) callback.onSuccess(task.getResult());
                                    } else {
                                        if (callback != null) callback.onFailure("Display name updated but Firestore update failed");
                                    }
                                });
                    } else {
                        Log.w(TAG, "Failed to update display name", task.getException());
                        if (callback != null) callback.onFailure(getAuthErrorMessage(task.getException()));
                    }
                });
    }

    // Account Management

    /**
     * Sign out current user
     */
    public void signOut() {
        mAuth.signOut();
        Log.d(TAG, "User signed out");
    }

    /**
     * Delete user account
     */
    public void deleteAccount(AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onFailure("No user is currently signed in");
            return;
        }

        // Delete user data from Firestore first
        FirebaseUtils.getUserDocument(user.getUid())
                .delete()
                .addOnCompleteListener(executor, firestoreTask -> {
                    if (firestoreTask.isSuccessful()) {
                        // Delete Firebase Auth account
                        user.delete()
                                .addOnCompleteListener(executor, authTask -> {
                                    if (authTask.isSuccessful()) {
                                        Log.d(TAG, "User account deleted successfully");
                                        if (callback != null) callback.onSuccess(authTask.getResult());
                                    } else {
                                        Log.e(TAG, "Failed to delete user account", authTask.getException());
                                        if (callback != null) callback.onFailure(getAuthErrorMessage(authTask.getException()));
                                    }
                                });
                    } else {
                        Log.e(TAG, "Failed to delete user data from Firestore", firestoreTask.getException());
                        if (callback != null) callback.onFailure(getFirestoreErrorMessage(firestoreTask.getException()));
                    }
                });
    }

    // User Status Methods

    /**
     * Get current user
     */
    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        FirebaseUser user = mAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Check if user is signed in
     */
    public boolean isUserSignedIn() {
        return mAuth.getCurrentUser() != null;
    }

    /**
     * Check if current user is admin
     */
    public void isCurrentUserAdmin(UserProfileCallback callback) {
        getCurrentUserProfile(new UserProfileCallback() {
            @Override
            public void onSuccess(User user) {
                if (callback != null) callback.onSuccess(user);
            }

            @Override
            public void onFailure(String error) {
                if (callback != null) callback.onFailure(error);
            }
        });
    }

    // Private Helper Methods

    /**
     * Create user profile in Firestore
     */
    private void createUserProfile(FirebaseUser firebaseUser, String name, String email, UserProfileCallback callback) {
        User user = new User(firebaseUser.getUid(), email, name);
        user.setEmailVerified(firebaseUser.isEmailVerified());
        user.setCreatedAt(new java.util.Date());
        user.setUpdatedAt(new java.util.Date());

        FirebaseUtils.getUserDocument(firebaseUser.getUid())
                .set(user)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile created successfully");
                        if (callback != null) callback.onSuccess(user);
                    } else {
                        Log.e(TAG, "Failed to create user profile", task.getException());
                        if (callback != null) callback.onFailure(getFirestoreErrorMessage(task.getException()));
                    }
                });
    }

    /**
     * Update last login timestamp
     */
    private void updateLastLogin(String userId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("lastLogin", new java.util.Date());
        updates.put("updatedAt", new java.util.Date());

        FirebaseUtils.getUserDocument(userId)
                .update(updates)
                .addOnCompleteListener(executor, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Last login updated");
                    } else {
                        Log.w(TAG, "Failed to update last login", task.getException());
                    }
                });
    }

    /**
     * Validate sign up input
     */
    private String validateSignUpInput(String email, String password, String name) {
        if (email == null || email.trim().isEmpty()) {
            return "Email address is required";
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid email address";
        }

        if (password == null || password.length() < 6) {
            return "Password must be at least 6 characters long";
        }

        if (name == null || name.trim().isEmpty()) {
            return "Name is required";
        }

        if (name.length() > 50) {
            return "Name must be less than 50 characters";
        }

        return null; // No validation errors
    }

    /**
     * Validate sign in input
     */
    private String validateSignInInput(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            return "Email address is required";
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Invalid email address";
        }

        if (password == null || password.trim().isEmpty()) {
            return "Password is required";
        }

        return null; // No validation errors
    }

    /**
     * Get user-friendly authentication error message
     */
    private String getAuthErrorMessage(Exception exception) {
        if (exception == null) {
            return "Unknown authentication error";
        }

        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) exception;
            String errorCode = authException.getErrorCode();

            switch (errorCode) {
                case "ERROR_INVALID_CUSTOM_TOKEN":
                    return "The custom token format is incorrect";
                case "ERROR_CUSTOM_TOKEN_MISMATCH":
                    return "The custom token corresponds to a different audience";
                case "ERROR_INVALID_CREDENTIAL":
                    return "Invalid email or password";
                case "ERROR_INVALID_EMAIL":
                    return "Invalid email address";
                case "ERROR_WRONG_PASSWORD":
                    return "Incorrect password";
                case "ERROR_USER_MISMATCH":
                    return "The supplied credentials do not correspond to the previously signed in user";
                case "ERROR_REQUIRES_RECENT_LOGIN":
                    return "This operation is sensitive and requires recent authentication";
                case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                    return "An account already exists with the same email address but different sign-in credentials";
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "Email address is already in use by another account";
                case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                    return "This credential is already associated with a different user account";
                case "ERROR_USER_DISABLED":
                    return "The user account has been disabled by an administrator";
                case "ERROR_USER_TOKEN_EXPIRED":
                    return "The user's credential is no longer valid";
                case "ERROR_USER_NOT_FOUND":
                    return "No user found with this email address";
                case "ERROR_INVALID_USER_TOKEN":
                    return "The user's credential is no longer valid";
                case "ERROR_WEAK_PASSWORD":
                    return "The password is not strong enough";
                case "ERROR_OPERATION_NOT_ALLOWED":
                    return "This sign-in method is not allowed";
                default:
                    return "Authentication failed: " + authException.getMessage();
            }
        }

        return "Authentication error: " + exception.getMessage();
    }

    /**
     * Get user-friendly Firestore error message
     */
    private String getFirestoreErrorMessage(Exception exception) {
        if (exception == null) {
            return "Unknown database error";
        }

        if (exception instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException firestoreException = (FirebaseFirestoreException) exception;
            return "Database error: " + firestoreException.getMessage();
        }

        return "Database error: " + exception.getMessage();
    }
}